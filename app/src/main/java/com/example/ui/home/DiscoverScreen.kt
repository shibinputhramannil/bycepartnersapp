package com.example.ui.home

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch
import com.example.R
import com.example.ui.components.FloatingBlurBalls
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassTextField
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassSurfaceMedium
import com.example.ui.theme.NeonGreen

/**
 * Screen for Discover Nearby Gyms.
 * Unified card design across default feed and search mode.
 */
@Composable
fun DiscoverScreen(
    onGymSelect: (GymLocation) -> Unit = {},
    onBackClick: () -> Unit = {},
    onTabSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var sortBy by remember { mutableStateOf("Distance: Nearest") }
    var isSearchActive by remember { mutableStateOf(false) }

    val filters = listOf("All", "Free weights", "Sauna", "Classes", "Open 24/7")

    val isSearching = isSearchActive || searchQuery.isNotBlank()

    Crossfade(targetState = isSearching, label = "discover_view_crossfade") { searching ->
        if (searching) {
            // Map + Draggable Nearby Gyms View (Shown while searching)
            DiscoverMapSearchView(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it },
                sortBy = sortBy,
                onSortBySelected = { sortBy = it },
                filters = filters,
                onBackClick = {
                    searchQuery = ""
                    isSearchActive = false
                },
                onGymSelect = onGymSelect,
                onTabSelected = onTabSelected,
                modifier = modifier
            )
        } else {
            // Standard Discover UI (Default View)
            DiscoverDefaultView(
                searchQuery = searchQuery,
                onSearchQueryChange = {
                    searchQuery = it
                    if (it.isNotBlank()) isSearchActive = true
                },
                onSearchFocus = { isSearchActive = true },
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it },
                sortBy = sortBy,
                onSortBySelected = { sortBy = it },
                filters = filters,
                onBackClick = onBackClick,
                onGymSelect = onGymSelect,
                onTabSelected = onTabSelected,
                modifier = modifier
            )
        }
    }
}

/**
 * Standard Discover Feed View
 */
@Composable
private fun DiscoverDefaultView(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchFocus: () -> Unit,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    sortBy: String,
    onSortBySelected: (String) -> Unit,
    filters: List<String>,
    onBackClick: () -> Unit,
    onGymSelect: (GymLocation) -> Unit,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val isScrolling = listState.isScrollInProgress
    var isSortDropdownExpanded by remember { mutableStateOf(false) }

    val filteredGyms = remember(searchQuery, selectedFilter, sortBy) {
        val baseList = SAMPLE_GYMS.filter { gym ->
            val matchesQuery = searchQuery.isBlank() ||
                    gym.name.contains(searchQuery, ignoreCase = true) ||
                    gym.cityArea.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "All" -> true
                "Free weights" -> gym.tags.contains("Free weights") || gym.tags.contains("Heavy lifting")
                "Sauna" -> gym.tags.contains("Sauna")
                "Classes" -> gym.tags.contains("Classes")
                "Open 24/7" -> gym.statusText.contains("24/7")
                else -> true
            }
            matchesQuery && matchesFilter
        }

        when (sortBy) {
            "Distance: Nearest" -> baseList.sortedBy { gym: GymLocation ->
                gym.distance.replace("km", "").replace("mi", "").trim().toDoubleOrNull() ?: 0.0
            }
            "Highest Rated" -> baseList.sortedByDescending { gym: GymLocation -> gym.rating }
            "Name: A to Z" -> baseList.sortedBy { gym: GymLocation -> gym.name }
            else -> baseList
        }
    }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavy)
    ) {
        // Cult.fit Aurora Animated Background Orbs
        FloatingBlurBalls()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = statusBarPadding + 10.dp, bottom = navBarPadding + 90.dp)
                .padding(horizontal = 18.dp)
        ) {
            // Header Row: Circular Dark Back Button + Discover Gyms Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GlassSurfaceMedium)
                        .border(1.dp, GlassBorderLight, CircleShape)
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFFFFFFF),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Discover Gyms",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFFFFF)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar (Capsule shape with #2C2B2C background)
            LiquidGlassTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = "Search by gym name or area...",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF9E9D9E),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onSearchFocus() }
                    )
                },
                testTag = "discover_search_input"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Top Sort Dropdown + Count Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dropdown Sort Selector Button
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color(0xFF2D2C2D))
                            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(50.dp))
                            .clickable { isSortDropdownExpanded = true }
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sortBy,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFFFFFF)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dropdown",
                            tint = Color(0xFFB0B0B0),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Dropdown Menu
                    DropdownMenu(
                        expanded = isSortDropdownExpanded,
                        onDismissRequest = { isSortDropdownExpanded = false },
                        modifier = Modifier
                            .background(Color(0xFF262526))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp))
                    ) {
                        listOf(
                            "Distance: Nearest",
                            "Highest Rated",
                            "Name: A to Z"
                        ).forEach { option ->
                            val isChosen = (option == sortBy)
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option,
                                        color = if (isChosen) Color(0xFFFFFFFF) else Color(0xFFCCCCCC),
                                        fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                },
                                onClick = {
                                    onSortBySelected(option)
                                    isSortDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Count text
                Text(
                    text = "${filteredGyms.size} nearby",
                    fontSize = 12.sp,
                    color = Color(0xFF9E9D9E)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filters) { filter ->
                    val isSelected = filter == selectedFilter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(if (isSelected) Color(0xFFFFFFFF) else Color(0xFF2D2C2D))
                            .clickable { onFilterSelected(filter) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Color(0xFF121212) else Color(0xFFFFFFFF)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Gym Cards List
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filteredGyms, key = { it.id }) { gym ->
                    DiscoverGymCard(
                        gym = gym,
                        isSelected = false,
                        onClick = { onGymSelect(gym) }
                    )
                }
            }
        }

        // Bottom Nav Bar with scroll border fade
        GlassBottomBar(
            selectedTab = "Discover",
            onTabSelected = onTabSelected,
            isScrolling = isScrolling,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * Unified Gym Card Component used across both Search & Default views
 */
@Composable
private fun DiscoverGymCard(
    gym: GymLocation,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gym Image Thumbnail
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(id = gym.imageRes),
                    contentDescription = gym.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = gym.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFFFFF)
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF9E9D9E),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${gym.distance} · ${gym.cityArea}",
                        fontSize = 12.sp,
                        color = Color(0xFF9E9D9E)
                    )
                }

                Spacer(modifier = Modifier.height(7.dp))

                Text(
                    text = gym.tags.joinToString(" · "),
                    fontSize = 12.sp,
                    color = Color(0xFF8E8D8E)
                )
            }
        }
    }
}

/**
 * Search Mode: Interactive Full-Screen Dark Map + Draggable Bottom Sheet
 */
@Composable
private fun DiscoverMapSearchView(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    sortBy: String,
    onSortBySelected: (String) -> Unit,
    filters: List<String>,
    onBackClick: () -> Unit,
    onGymSelect: (GymLocation) -> Unit,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCity by remember { mutableStateOf("All Kerala") }
    val cities = listOf("All Kerala", "Kozhikode", "Kannur", "Kochi", "Trivandrum")

    var selectedGym by remember { mutableStateOf<GymLocation?>(SAMPLE_GYMS.firstOrNull()) }
    val listState = rememberLazyListState()
    val isScrolling = listState.isScrollInProgress
    var isSortDropdownExpanded by remember { mutableStateOf(false) }

    val density = LocalDensity.current

    val filteredGyms = remember(searchQuery, selectedFilter, sortBy, selectedCity) {
        val baseList = SAMPLE_GYMS.filter { gym ->
            val matchesQuery = searchQuery.isBlank() ||
                    gym.name.contains(searchQuery, ignoreCase = true) ||
                    gym.cityArea.contains(searchQuery, ignoreCase = true) ||
                    gym.address.contains(searchQuery, ignoreCase = true) ||
                    gym.tags.any { it.contains(searchQuery, ignoreCase = true) }

            val matchesCity = if (selectedCity == "All Kerala") true else {
                gym.city.equals(selectedCity, ignoreCase = true) || gym.cityArea.contains(selectedCity, ignoreCase = true)
            }

            val matchesFilter = when (selectedFilter) {
                "All" -> true
                "Free weights" -> gym.tags.contains("Free weights") || gym.tags.contains("Heavy lifting")
                "Sauna" -> gym.tags.contains("Sauna")
                "Classes" -> gym.tags.contains("Classes")
                "Open 24/7" -> gym.statusText.contains("24/7")
                else -> true
            }
            matchesQuery && matchesCity && matchesFilter
        }

        when (sortBy) {
            "Distance: Nearest" -> baseList.sortedBy { gym: GymLocation ->
                gym.distance.replace("km", "").replace("mi", "").trim().toDoubleOrNull() ?: 0.0
            }
            "Highest Rated" -> baseList.sortedByDescending { gym: GymLocation -> gym.rating }
            "Name: A to Z" -> baseList.sortedBy { gym: GymLocation -> gym.name }
            else -> baseList
        }
    }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF191819))
    ) {
        val screenHeightPx = constraints.maxHeight.toFloat()
        val minSheetHeightPx = with(density) { (130.dp + navBarPadding).toPx() }
        val halfSheetHeightPx = screenHeightPx * 0.48f
        val fullSheetHeightPx = screenHeightPx * 0.84f

        var targetSheetHeightPx by remember { mutableFloatStateOf(halfSheetHeightPx) }
        var isDragging by remember { mutableStateOf(false) }

        val animatedSheetHeightPx by animateFloatAsState(
            targetValue = targetSheetHeightPx,
            animationSpec = spring(dampingRatio = 0.85f, stiffness = 500f),
            label = "sheetHeightAnimation"
        )

        val currentSheetHeightPx = if (isDragging) targetSheetHeightPx else animatedSheetHeightPx
        val currentSheetHeightDp = with(density) { currentSheetHeightPx.toDp() }

        var recenterTrigger by remember { mutableIntStateOf(0) }
        var zoomInTrigger by remember { mutableIntStateOf(0) }
        var zoomOutTrigger by remember { mutableIntStateOf(0) }

        // 1. FULL BACKGROUND: Interactive Original Style Dark Map Engine
        Box(modifier = Modifier.fillMaxSize()) {
            DarkLeafletMapView(
                gyms = SAMPLE_GYMS,
                selectedGym = selectedGym,
                selectedCity = selectedCity,
                recenterTrigger = recenterTrigger,
                zoomInTrigger = zoomInTrigger,
                zoomOutTrigger = zoomOutTrigger,
                onGymClicked = { gymId ->
                    val clicked = SAMPLE_GYMS.find { it.id == gymId }
                    if (clicked != null) {
                        selectedGym = clicked
                    }
                }
            )

            // Zoom & Recenter Controls (floats above the bottom sheet)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp)
                    .offset(y = -currentSheetHeightDp - 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Zoom In FAB
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xD9262526))
                        .border(1.dp, Color(0x33FFFFFF), CircleShape)
                        .clickable { zoomInTrigger++ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = Color(0xFFFFFFFF),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Zoom Out FAB
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xD9262526))
                        .border(1.dp, Color(0x33FFFFFF), CircleShape)
                        .clickable { zoomOutTrigger++ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        tint = Color(0xFFFFFFFF),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Recenter FAB
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xD9262526))
                        .border(1.dp, Color(0x33FFFFFF), CircleShape)
                        .clickable { recenterTrigger++ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Recenter Map",
                        tint = Color(0xFFFFFFFF),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 2. FLOATING TOP OVERLAY: Back Button + Live Search Bar + City Pills + Filter Pills
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = statusBarPadding + 10.dp, start = 16.dp, end = 16.dp)
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button (Exits search mode and returns to default Discover UI)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A292A))
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Exit Search",
                        tint = Color(0xFFFFFFFF),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Search Bar with Clear button
                Box(modifier = Modifier.weight(1f)) {
                    LiquidGlassTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = "Search Kerala BYCE gyms...",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF9E9D9E),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Color(0xFF9E9D9E),
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { onSearchQueryChange("") }
                                )
                            }
                        },
                        testTag = "map_search_input"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // City Navigation Pills (Kozhikode, Kannur, Kochi, Trivandrum)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(cities) { city ->
                    val isSelected = city == selectedCity
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(if (isSelected) Color(0xFF6B9330) else Color(0x352D2C2D))
                            .border(1.dp, if (isSelected) Color(0xFF8DBB45) else Color(0x25FFFFFF), RoundedCornerShape(50.dp))
                            .clickable {
                                selectedCity = city
                                val match = SAMPLE_GYMS.firstOrNull { it.city.equals(city, ignoreCase = true) }
                                if (match != null) {
                                    selectedGym = match
                                }
                            }
                            .padding(horizontal = 13.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (isSelected) Color(0xFFFFFFFF) else Color(0xFFB0AFB0),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = city,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFFFFFFFF) else Color(0xFFD0CFD0)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filters) { filter ->
                    val isSelected = filter == selectedFilter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(if (isSelected) Color(0xFFFFFFFF) else Color(0xFF2D2C2D))
                            .clickable { onFilterSelected(filter) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = filter,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Color(0xFF121212) else Color(0xFFFFFFFF)
                        )
                    }
                }
            }
        }

        // 3. DRAGGABLE UBER-STYLE SLIDING GLASS SHEET
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(currentSheetHeightDp)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color(0xFF1E1D1E))
                .border(
                    width = 1.dp,
                    color = Color(0x22FFFFFF),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = navBarPadding + 76.dp)
            ) {
                // Drag Handle & Header Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragStart = { isDragging = true },
                                onDragEnd = {
                                    isDragging = false
                                    val distMin = kotlin.math.abs(targetSheetHeightPx - minSheetHeightPx)
                                    val distHalf = kotlin.math.abs(targetSheetHeightPx - halfSheetHeightPx)
                                    val distFull = kotlin.math.abs(targetSheetHeightPx - fullSheetHeightPx)

                                    targetSheetHeightPx = when {
                                        distMin < distHalf && distMin < distFull -> minSheetHeightPx
                                        distFull < distHalf -> fullSheetHeightPx
                                        else -> halfSheetHeightPx
                                    }
                                },
                                onDragCancel = { isDragging = false },
                                onVerticalDrag = { change, dragAmount ->
                                    change.consume()
                                    targetSheetHeightPx = (targetSheetHeightPx - dragAmount)
                                        .coerceIn(minSheetHeightPx, fullSheetHeightPx)
                                }
                            )
                        }
                        .padding(top = 10.dp, bottom = 8.dp, start = 20.dp, end = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Drag Handle Bar
                    Box(
                        modifier = Modifier
                            .size(width = 42.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF555455))
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Nearby Gyms",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFFFFF)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF333233))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${filteredGyms.size} found",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE2E8F0)
                                )
                            }
                        }

                        // Top Sort Dropdown in Sheet Header
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF2B2A2B))
                                    .clickable { isSortDropdownExpanded = true }
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = sortBy.substringBefore(":").ifEmpty { sortBy },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFB0B0B0)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Sort Dropdown",
                                    tint = Color(0xFF9E9D9E),
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = isSortDropdownExpanded,
                                onDismissRequest = { isSortDropdownExpanded = false },
                                modifier = Modifier
                                    .background(Color(0xFF262526))
                                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                            ) {
                                listOf("Distance: Nearest", "Highest Rated", "Name: A to Z").forEach { option ->
                                    val isChosen = (option == sortBy)
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option,
                                                color = if (isChosen) Color(0xFFFFFFFF) else Color(0xFFCCCCCC),
                                                fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp
                                            )
                                        },
                                        onClick = {
                                            onSortBySelected(option)
                                            isSortDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Scrollable Nearby Gym List (Uses the same clean unified DiscoverGymCard)
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredGyms, key = { it.id }) { gym ->
                        val isSelected = gym.id == selectedGym?.id
                        DiscoverGymCard(
                            gym = gym,
                            isSelected = isSelected,
                            onClick = {
                                if (selectedGym?.id == gym.id) {
                                    onGymSelect(gym)
                                } else {
                                    selectedGym = gym
                                }
                            }
                        )
                    }
                }
            }
        }

        // 4. BOTTOM NAVIGATION BAR
        GlassBottomBar(
            selectedTab = "Discover",
            onTabSelected = onTabSelected,
            isScrolling = isScrolling,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * Interactive Original-Style Dark Vector Map for Kerala.
 * Supports smooth 60fps pan/drag in any direction and pinch-to-zoom.
 * Renders Kerala Arabian Sea coast, backwaters, NH 66 corridor, city districts (Kozhikode, Kannur, Kochi, Trivandrum),
 * pulsing user GPS radar dot, dashed route line, and interactive BYCE Kettlebell Logo pins.
 */
@Composable
private fun DarkLeafletMapView(
    gyms: List<GymLocation>,
    selectedGym: GymLocation?,
    selectedCity: String = "All Kerala",
    recenterTrigger: Int = 0,
    zoomInTrigger: Int = 0,
    zoomOutTrigger: Int = 0,
    onGymClicked: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Virtual origin for user location (Kozhikode Center)
    val userVirtualX = 615f
    val userVirtualY = 680f

    // Animated pan offsets & zoom
    val animPanX = remember { Animatable(0f) }
    val animPanY = remember { Animatable(0f) }
    val animZoom = remember { Animatable(1f) }

    // Map gym IDs to virtual coordinates across Kerala
    val gymCoordinates = remember(gyms) {
        gyms.associate { gym ->
            val offset = when (gym.id) {
                "2" -> Offset(480f, 430f)   // Kannur (Payyambalam)
                "1" -> Offset(615f, 680f)   // Kozhikode (Mavoor Rd)
                "5" -> Offset(550f, 740f)   // Kozhikode (Beach Rd)
                "3" -> Offset(770f, 1180f)  // Kochi (Marine Drive)
                "4" -> Offset(980f, 1750f)  // Trivandrum (Kowdiar)
                else -> {
                    val y = 1900f - ((gym.latitude.toFloat() - 8.2f) / 4.0f) * 1600f
                    val x = 300f + ((gym.longitude.toFloat() - 74.8f) / 2.8f) * 900f
                    Offset(x.coerceIn(200f, 1400f), y.coerceIn(200f, 1900f))
                }
            }
            gym.id to offset
        }
    }

    // City focal targets
    val cityFocalPoints = remember {
        mapOf(
            "All Kerala" to Triple(userVirtualX - 700f, userVirtualY - 1050f, 0.72f),
            "Kannur" to Triple(userVirtualX - 480f, userVirtualY - 430f, 1.35f),
            "Kozhikode" to Triple(0f, 0f, 1.35f),
            "Kochi" to Triple(userVirtualX - 770f, userVirtualY - 1180f, 1.35f),
            "Trivandrum" to Triple(userVirtualX - 980f, userVirtualY - 1750f, 1.35f)
        )
    }

    // Auto pan/zoom when city filter is selected
    LaunchedEffect(selectedCity) {
        val target = cityFocalPoints[selectedCity]
        if (target != null) {
            launch { animPanX.animateTo(target.first, tween(550, easing = FastOutSlowInEasing)) }
            launch { animPanY.animateTo(target.second, tween(550, easing = FastOutSlowInEasing)) }
            launch { animZoom.animateTo(target.third, tween(550, easing = FastOutSlowInEasing)) }
        }
    }

    // Recenter animation when button is clicked
    LaunchedEffect(recenterTrigger) {
        if (recenterTrigger > 0) {
            launch { animPanX.animateTo(0f, tween(450, easing = FastOutSlowInEasing)) }
            launch { animPanY.animateTo(0f, tween(450, easing = FastOutSlowInEasing)) }
            launch { animZoom.animateTo(1f, tween(450, easing = FastOutSlowInEasing)) }
        }
    }

    // Zoom In FAB
    LaunchedEffect(zoomInTrigger) {
        if (zoomInTrigger > 0) {
            val targetZoom = (animZoom.value * 1.35f).coerceIn(0.55f, 2.8f)
            animZoom.animateTo(targetZoom, tween(300, easing = FastOutSlowInEasing))
        }
    }

    // Zoom Out FAB
    LaunchedEffect(zoomOutTrigger) {
        if (zoomOutTrigger > 0) {
            val targetZoom = (animZoom.value * 0.74f).coerceIn(0.55f, 2.8f)
            animZoom.animateTo(targetZoom, tween(300, easing = FastOutSlowInEasing))
        }
    }

    // Auto-center on selected gym smoothly
    LaunchedEffect(selectedGym?.id) {
        selectedGym?.id?.let { gymId ->
            val coord = gymCoordinates[gymId]
            if (coord != null) {
                val targetPanX = (userVirtualX - coord.x) * animZoom.value
                val targetPanY = (userVirtualY - coord.y) * animZoom.value + 40f
                launch { animPanX.animateTo(targetPanX, tween(500, easing = FastOutSlowInEasing)) }
                launch { animPanY.animateTo(targetPanY, tween(500, easing = FastOutSlowInEasing)) }
            }
        }
    }

    // Radar pulse animation for user location
    val infiniteTransition = rememberInfiniteTransition(label = "map_radar_pulse")
    val radarPulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 46f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarRadius"
    )
    val radarPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAlpha"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121214))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    coroutineScope.launch {
                        val newZoom = (animZoom.value * zoom).coerceIn(0.55f, 2.8f)
                        animZoom.snapTo(newZoom)
                        animPanX.snapTo(animPanX.value + pan.x)
                        animPanY.snapTo(animPanY.value + pan.y)
                    }
                }
            }
    ) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()
        val viewCenterX = screenWidth / 2f
        val viewCenterY = screenHeight * 0.36f // Map viewport center above half bottom sheet

        // 1. HARDWARE-ACCELERATED KERALA VECTOR MAP CANVAS
        Canvas(modifier = Modifier.fillMaxSize()) {
            withTransform({
                translate(viewCenterX + animPanX.value, viewCenterY + animPanY.value)
                scale(animZoom.value, animZoom.value, Offset.Zero)
                translate(-userVirtualX, -userVirtualY)
            }) {
                // Background land surface
                drawRect(color = Color(0xFF131215), size = Size(1800f, 2200f))

                // ----------------------------------------------------
                // 1. ARABIAN SEA / KERALA WEST COASTLINE
                // ----------------------------------------------------
                val seaPath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(380f, 0f)
                    cubicTo(370f, 250f, 440f, 400f, 460f, 500f) // Kannur coast
                    cubicTo(480f, 600f, 520f, 700f, 540f, 820f) // Kozhikode coast
                    cubicTo(560f, 950f, 640f, 1080f, 720f, 1200f) // Kochi coast
                    cubicTo(780f, 1350f, 860f, 1550f, 940f, 1750f) // Alleppey -> Kollam -> Trivandrum
                    cubicTo(970f, 1850f, 1020f, 2000f, 1050f, 2200f)
                    lineTo(0f, 2200f)
                    close()
                }
                drawPath(seaPath, color = Color(0xFF0C1622)) // Arabian sea deep water
                drawPath(seaPath, color = Color(0xFF162D42), style = Stroke(width = 3.5f)) // Coastline surf

                // ----------------------------------------------------
                // 2. VEMBANAD BACKWATERS & RIVERS (Kochi / Alleppey)
                // ----------------------------------------------------
                val backwaterPath = Path().apply {
                    moveTo(740f, 1140f)
                    cubicTo(790f, 1170f, 820f, 1240f, 800f, 1320f)
                    cubicTo(780f, 1380f, 820f, 1450f, 850f, 1500f)
                    cubicTo(820f, 1500f, 780f, 1400f, 760f, 1300f)
                    cubicTo(740f, 1220f, 710f, 1180f, 740f, 1140f)
                    close()
                }
                drawPath(backwaterPath, color = Color(0xFF0E1A26))
                drawPath(backwaterPath, color = Color(0xFF1B3850), style = Stroke(width = 2f))

                // Kozhikode River (Chaliyar River channel)
                val chaliyarPath = Path().apply {
                    moveTo(1400f, 780f)
                    cubicTo(1150f, 770f, 900f, 810f, 750f, 800f)
                    cubicTo(680f, 790f, 600f, 830f, 530f, 820f)
                }
                drawPath(chaliyarPath, color = Color(0xFF0E1A26), style = Stroke(width = 12f, cap = StrokeCap.Round))
                drawPath(chaliyarPath, color = Color(0xFF1B3850), style = Stroke(width = 2f))

                // ----------------------------------------------------
                // 3. WESTERN GHATS & NATURE RESERVES (Green Belts)
                // ----------------------------------------------------
                // Wayanad / Aralam Reserve (East of Kannur/Calicut)
                val wayanadPark = Path().apply {
                    moveTo(760f, 320f)
                    cubicTo(920f, 300f, 1050f, 380f, 1020f, 500f)
                    cubicTo(960f, 580f, 840f, 560f, 770f, 460f)
                    close()
                }
                drawPath(wayanadPark, color = Color(0xFF112217))
                drawPath(wayanadPark, color = Color(0xFF1B3624), style = Stroke(width = 1.5f))

                // Munnar / High Ranges (East of Kochi)
                val munnarPark = Path().apply {
                    moveTo(960f, 1050f)
                    cubicTo(1140f, 1020f, 1240f, 1120f, 1190f, 1250f)
                    cubicTo(1110f, 1320f, 970f, 1260f, 940f, 1160f)
                    close()
                }
                drawPath(munnarPark, color = Color(0xFF112217))
                drawPath(munnarPark, color = Color(0xFF1B3624), style = Stroke(width = 1.5f))

                // Ponmudi / Agasthyakoodam Reserve (East of Trivandrum)
                val ponmudiPark = Path().apply {
                    moveTo(1100f, 1680f)
                    cubicTo(1250f, 1650f, 1340f, 1740f, 1290f, 1850f)
                    cubicTo(1210f, 1910f, 1110f, 1860f, 1080f, 1770f)
                    close()
                }
                drawPath(ponmudiPark, color = Color(0xFF112217))
                drawPath(ponmudiPark, color = Color(0xFF1B3624), style = Stroke(width = 1.5f))

                // ----------------------------------------------------
                // 4. KERALA CITY DISTRICT BLOCKS
                // ----------------------------------------------------
                val blockColor = Color(0xFF18171A)
                val blockBorder = Color(0xFF222125)

                val cityBlocks = listOf(
                    // Kannur Zone (Payyambalam / Fort / Stadium)
                    Triple(440f, 380f, Size(100f, 65f)),
                    Triple(560f, 380f, Size(110f, 65f)),
                    Triple(440f, 460f, Size(95f, 75f)),
                    Triple(550f, 460f, Size(120f, 75f)),

                    // Kozhikode Zone (Mavoor Rd / Beach Rd / Focus Mall / Cyberpark)
                    Triple(520f, 640f, Size(120f, 70f)),
                    Triple(660f, 640f, Size(130f, 70f)),
                    Triple(520f, 730f, Size(115f, 80f)),
                    Triple(650f, 730f, Size(140f, 80f)),
                    Triple(810f, 680f, Size(130f, 85f)),

                    // Kochi Zone (Marine Drive / MG Road / Kakkanad Infopark)
                    Triple(670f, 1120f, Size(130f, 80f)),
                    Triple(820f, 1120f, Size(140f, 80f)),
                    Triple(670f, 1220f, Size(125f, 90f)),
                    Triple(810f, 1220f, Size(150f, 90f)),
                    Triple(980f, 1160f, Size(140f, 100f)),

                    // Trivandrum Zone (Kowdiar / MG Road / Technopark / Kowdiar Palace)
                    Triple(880f, 1680f, Size(130f, 85f)),
                    Triple(1030f, 1680f, Size(140f, 85f)),
                    Triple(880f, 1780f, Size(125f, 95f)),
                    Triple(1020f, 1780f, Size(150f, 95f))
                )

                for ((bx, by, bsize) in cityBlocks) {
                    drawRoundRect(
                        color = blockColor,
                        topLeft = Offset(bx, by),
                        size = bsize,
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    drawRoundRect(
                        color = blockBorder,
                        topLeft = Offset(bx, by),
                        size = bsize,
                        cornerRadius = CornerRadius(6f, 6f),
                        style = Stroke(width = 1f)
                    )
                }

                // ----------------------------------------------------
                // 5. SECONDARY ARTERIAL ROADS & CITY GRIDS
                // ----------------------------------------------------
                val streetColor = Color(0xFF1E1D21)
                val avenueColor = Color(0xFF27262B)

                // Kannur local streets
                listOf(360f, 450f, 540f).forEach { yPos ->
                    drawLine(color = streetColor, start = Offset(400f, yPos), end = Offset(720f, yPos), strokeWidth = 3f)
                }
                // Kozhikode Mavoor Road & Beach Road avenues
                drawLine(color = avenueColor, start = Offset(480f, 690f), end = Offset(980f, 690f), strokeWidth = 7f, cap = StrokeCap.Round)
                drawLine(color = avenueColor, start = Offset(620f, 600f), end = Offset(620f, 860f), strokeWidth = 6f, cap = StrokeCap.Round)

                // Kochi Marine Drive & MG Road avenues
                drawLine(color = avenueColor, start = Offset(630f, 1190f), end = Offset(1180f, 1190f), strokeWidth = 7f, cap = StrokeCap.Round)
                drawLine(color = avenueColor, start = Offset(780f, 1080f), end = Offset(780f, 1340f), strokeWidth = 6f, cap = StrokeCap.Round)

                // Trivandrum Kowdiar Main Avenue & MG Road
                drawLine(color = avenueColor, start = Offset(830f, 1740f), end = Offset(1250f, 1740f), strokeWidth = 7f, cap = StrokeCap.Round)
                drawLine(color = avenueColor, start = Offset(980f, 1640f), end = Offset(980f, 1900f), strokeWidth = 6f, cap = StrokeCap.Round)

                // ----------------------------------------------------
                // 6. MAJOR NH 66 EXPRESSWAY CORRIDOR (KERALA HIGHWAY)
                // ----------------------------------------------------
                val nh66Path = Path().apply {
                    moveTo(470f, 150f)
                    cubicTo(490f, 380f, 580f, 650f, 620f, 750f) // Kannur to Calicut
                    cubicTo(670f, 900f, 750f, 1100f, 780f, 1250f) // Calicut to Kochi
                    cubicTo(820f, 1400f, 900f, 1650f, 990f, 1850f) // Kochi to Trivandrum
                    lineTo(1040f, 2150f)
                }
                // Highway bed
                drawPath(
                    nh66Path,
                    color = Color(0xFF323038),
                    style = Stroke(width = 13f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                // Highway center divider
                drawPath(
                    nh66Path,
                    color = Color(0xFF1E1D22),
                    style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // ----------------------------------------------------
                // 7. DISTRICT, CITY & HIGHWAY LABELS (Typography)
                // ----------------------------------------------------
                drawContext.canvas.nativeCanvas.apply {
                    val regionPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(80, 100, 160, 220)
                        textSize = 28f
                        isAntiAlias = true
                        letterSpacing = 0.35f
                        typeface = android.graphics.Typeface.create(
                            android.graphics.Typeface.DEFAULT,
                            android.graphics.Typeface.BOLD
                        )
                    }

                    val cityPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(170, 235, 235, 240)
                        textSize = 23f
                        isAntiAlias = true
                        letterSpacing = 0.22f
                        typeface = android.graphics.Typeface.create(
                            android.graphics.Typeface.DEFAULT,
                            android.graphics.Typeface.BOLD
                        )
                    }

                    val streetPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(100, 150, 148, 155)
                        textSize = 15f
                        isAntiAlias = true
                        letterSpacing = 0.12f
                        typeface = android.graphics.Typeface.create(
                            android.graphics.Typeface.DEFAULT,
                            android.graphics.Typeface.NORMAL
                        )
                    }

                    drawText("ARABIAN SEA", 120f, 750f, regionPaint)
                    drawText("ARABIAN SEA", 200f, 1400f, regionPaint)

                    drawText("KANNUR", 460f, 350f, cityPaint)
                    drawText("PAYYAMBALAM", 450f, 440f, streetPaint)

                    drawText("KOZHIKODE", 600f, 615f, cityPaint)
                    drawText("MAVOOR ROAD", 680f, 680f, streetPaint)
                    drawText("BEACH ROAD", 490f, 780f, streetPaint)

                    drawText("KOCHI", 760f, 1090f, cityPaint)
                    drawText("MARINE DRIVE", 740f, 1175f, streetPaint)
                    drawText("MG ROAD", 880f, 1260f, streetPaint)

                    drawText("TRIVANDRUM", 940f, 1640f, cityPaint)
                    drawText("KOWDIAR AVE", 960f, 1725f, streetPaint)

                    drawText("NH 66 HIGHWAY", 570f, 920f, streetPaint)
                    drawText("NH 66 HIGHWAY", 830f, 1470f, streetPaint)
                }

                // ----------------------------------------------------
                // 8. ACTIVE NAVIGATION ROUTE TO SELECTED GYM
                // ----------------------------------------------------
                selectedGym?.id?.let { gymId ->
                    val gymCoord = gymCoordinates[gymId]
                    if (gymCoord != null) {
                        val routePath = Path().apply {
                            moveTo(userVirtualX, userVirtualY)
                            val midX = userVirtualX
                            val midY = gymCoord.y
                            lineTo(midX, midY)
                            lineTo(gymCoord.x, gymCoord.y)
                        }

                        // Glow behind route
                        drawPath(
                            routePath,
                            color = NeonGreen.copy(alpha = 0.25f),
                            style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )

                        // Dashed neon route
                        drawPath(
                            routePath,
                            color = NeonGreen,
                            style = Stroke(
                                width = 3.5f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }

                // ----------------------------------------------------
                // 9. USER LOCATION PULSING GPS RADAR
                // ----------------------------------------------------
                drawCircle(
                    color = Color(0xFFFFFFFF).copy(alpha = radarPulseAlpha),
                    radius = radarPulseRadius,
                    center = Offset(userVirtualX, userVirtualY)
                )
                drawCircle(
                    color = Color(0x33FFFFFF),
                    radius = 18f,
                    center = Offset(userVirtualX, userVirtualY)
                )
                drawCircle(
                    color = Color(0xFF191819),
                    radius = 9f,
                    center = Offset(userVirtualX, userVirtualY)
                )
                drawCircle(
                    color = Color(0xFFFFFFFF),
                    radius = 6.5f,
                    center = Offset(userVirtualX, userVirtualY)
                )
            }
        }

        // ----------------------------------------------------
        // 10. INTERACTIVE BYCE KETTLEBELL GYM PIN BADGES
        // ----------------------------------------------------
        gyms.forEach { gym ->
            val coord = gymCoordinates[gym.id] ?: Offset(615f, 680f)
            val isSelected = (gym.id == selectedGym?.id)

            // Convert virtual coordinates to screen space
            val pinScreenX = viewCenterX + (coord.x - userVirtualX) * animZoom.value + animPanX.value
            val pinScreenY = viewCenterY + (coord.y - userVirtualY) * animZoom.value + animPanY.value

            val pinXDp = with(density) { pinScreenX.toDp() }
            val pinYDp = with(density) { pinScreenY.toDp() }

            Box(
                modifier = Modifier
                    .offset(x = pinXDp - 70.dp, y = pinYDp - 68.dp)
                    .width(140.dp)
                    .clickable { onGymClicked(gym.id) },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onGymClicked(gym.id) }
                ) {
                    // Floating Badge Pill with BYCE branding & distance
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(
                                if (isSelected) Color(0xFF1C1C1F) else Color(0xEB1E1D1F)
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) NeonGreen else Color(0x33FFFFFF),
                                shape = RoundedCornerShape(50.dp)
                            )
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Live Green status dot
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(NeonGreen)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = gym.name.replace(" (BYCE Elite)", "").take(14),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = Color(0xFFFFFFFF),
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Kettlebell Pin Logo Icon Box
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 40.dp else 34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF181719))
                            .border(
                                width = if (isSelected) 2.dp else 1.5.dp,
                                color = if (isSelected) NeonGreen else Color(0x80FFFFFF),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.byce_kettlebell_pin),
                            contentDescription = "BYCE Logo Pin",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(if (isSelected) 36.dp else 30.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            }
        }
    }
}


