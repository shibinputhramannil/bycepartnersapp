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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
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
    var selectedGym by remember { mutableStateOf<GymLocation?>(SAMPLE_GYMS.firstOrNull()) }
    val listState = rememberLazyListState()
    val isScrolling = listState.isScrollInProgress
    var isSortDropdownExpanded by remember { mutableStateOf(false) }

    val density = LocalDensity.current

    val filteredGyms = remember(searchQuery, selectedFilter, sortBy) {
        val baseList = SAMPLE_GYMS.filter { gym ->
            val matchesQuery = searchQuery.isBlank() ||
                    gym.name.contains(searchQuery, ignoreCase = true) ||
                    gym.cityArea.contains(searchQuery, ignoreCase = true) ||
                    gym.tags.any { it.contains(searchQuery, ignoreCase = true) }
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

        // 1. FULL BACKGROUND: Interactive Original Style Dark Map Engine
        Box(modifier = Modifier.fillMaxSize()) {
            DarkLeafletMapView(
                gyms = SAMPLE_GYMS,
                selectedGym = selectedGym,
                recenterTrigger = recenterTrigger,
                onGymClicked = { gymId ->
                    val clicked = SAMPLE_GYMS.find { it.id == gymId }
                    if (clicked != null) {
                        selectedGym = clicked
                    }
                }
            )

            // Recenter FAB (floats above the bottom sheet)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp)
                    .offset(y = -currentSheetHeightDp - 14.dp)
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

        // 2. FLOATING TOP OVERLAY: Back Button + Live Search Bar + Filter Pills
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
                        placeholder = "Search gyms, amenities, areas...",
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

            Spacer(modifier = Modifier.height(10.dp))

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
 * Interactive Original-Style Dark Vector Map.
 * Supports smooth 60fps pan/drag in any direction and pinch-to-zoom.
 * Renders realistic waterways, parks, city blocks, expressways, street grid, district labels,
 * pulsing user GPS radar dot, dashed route line, and interactive gym pin badges.
 */
@Composable
private fun DarkLeafletMapView(
    gyms: List<GymLocation>,
    selectedGym: GymLocation?,
    recenterTrigger: Int,
    onGymClicked: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Virtual origin for user location
    val userVirtualX = 800f
    val userVirtualY = 800f

    // Animated pan offsets & zoom
    val animPanX = remember { Animatable(0f) }
    val animPanY = remember { Animatable(0f) }
    val animZoom = remember { Animatable(1f) }

    // Map gym IDs to virtual coordinates
    val gymCoordinates = remember(gyms) {
        mapOf(
            "g1" to Offset(660f, 620f),  // Pulse Fitness (North-West)
            "g2" to Offset(980f, 660f),  // Iron Vault Gym (North-East)
            "g3" to Offset(860f, 1020f)  // Apex Performance (South)
        )
    }

    // Recenter animation when button is clicked
    LaunchedEffect(recenterTrigger) {
        if (recenterTrigger > 0) {
            launch { animPanX.animateTo(0f, tween(450, easing = FastOutSlowInEasing)) }
            launch { animPanY.animateTo(0f, tween(450, easing = FastOutSlowInEasing)) }
            launch { animZoom.animateTo(1f, tween(450, easing = FastOutSlowInEasing)) }
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
        targetValue = 44f,
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
                        val newZoom = (animZoom.value * zoom).coerceIn(0.65f, 2.5f)
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

        // 1. HARDWARE-ACCELERATED VECTOR MAP CANVAS
        Canvas(modifier = Modifier.fillMaxSize()) {
            withTransform({
                translate(viewCenterX + animPanX.value, viewCenterY + animPanY.value)
                scale(animZoom.value, animZoom.value, Offset.Zero)
                translate(-userVirtualX, -userVirtualY)
            }) {
                // Background surface
                drawRect(color = Color(0xFF131215), size = Size(1600f, 1600f))

                // ----------------------------------------------------
                // 1. NATURAL WATERWAY / BAY
                // ----------------------------------------------------
                val waterPath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(1600f, 0f)
                    lineTo(1600f, 320f)
                    cubicTo(1400f, 340f, 1250f, 260f, 1100f, 310f)
                    cubicTo(950f, 360f, 850f, 290f, 700f, 330f)
                    cubicTo(550f, 370f, 400f, 280f, 250f, 350f)
                    cubicTo(120f, 410f, 50f, 380f, 0f, 420f)
                    close()
                }
                drawPath(waterPath, color = Color(0xFF0F1822))
                drawPath(
                    waterPath,
                    color = Color(0xFF1B2B3C),
                    style = Stroke(width = 3f)
                )

                // Secondary River Channel
                val riverPath = Path().apply {
                    moveTo(1600f, 820f)
                    cubicTo(1350f, 800f, 1200f, 920f, 1050f, 900f)
                    cubicTo(900f, 880f, 820f, 960f, 700f, 980f)
                    cubicTo(580f, 1000f, 450f, 940f, 300f, 970f)
                    cubicTo(150f, 1000f, 80f, 1080f, 0f, 1100f)
                    lineTo(0f, 1135f)
                    cubicTo(80f, 1115f, 150f, 1035f, 300f, 1005f)
                    cubicTo(450f, 975f, 580f, 1035f, 700f, 1015f)
                    cubicTo(820f, 995f, 900f, 915f, 1050f, 935f)
                    cubicTo(1200f, 955f, 1350f, 835f, 1600f, 855f)
                    close()
                }
                drawPath(riverPath, color = Color(0xFF0E1620))
                drawPath(riverPath, color = Color(0xFF162534), style = Stroke(width = 2f))

                // ----------------------------------------------------
                // 2. PARKS & NATURE RESERVES
                // ----------------------------------------------------
                val marinaPark = Path().apply {
                    moveTo(180f, 440f)
                    cubicTo(260f, 430f, 380f, 450f, 440f, 490f)
                    cubicTo(420f, 580f, 260f, 590f, 160f, 560f)
                    close()
                }
                drawPath(marinaPark, color = Color(0xFF132218))
                drawPath(marinaPark, color = Color(0xFF1E3626), style = Stroke(width = 1.5f))

                val missionPark = Path().apply {
                    moveTo(720f, 740f)
                    cubicTo(800f, 730f, 880f, 760f, 890f, 820f)
                    cubicTo(860f, 870f, 760f, 880f, 710f, 840f)
                    close()
                }
                drawPath(missionPark, color = Color(0xFF132218))
                drawPath(missionPark, color = Color(0xFF1E3626), style = Stroke(width = 1.5f))

                val southPark = Path().apply {
                    moveTo(1020f, 1120f)
                    cubicTo(1150f, 1100f, 1260f, 1150f, 1240f, 1260f)
                    cubicTo(1160f, 1300f, 1000f, 1260f, 980f, 1180f)
                    close()
                }
                drawPath(southPark, color = Color(0xFF132218))
                drawPath(southPark, color = Color(0xFF1E3626), style = Stroke(width = 1.5f))

                // ----------------------------------------------------
                // 3. CITY BLOCKS & NEIGHBORHOOD ARCHITECTURE
                // ----------------------------------------------------
                val blockColor = Color(0xFF18171A)
                val blockBorder = Color(0xFF222125)

                val cityBlocks = listOf(
                    // NW District
                    Triple(220f, 620f, Size(120f, 80f)),
                    Triple(360f, 620f, Size(110f, 80f)),
                    Triple(220f, 720f, Size(120f, 90f)),
                    Triple(360f, 720f, Size(110f, 90f)),
                    Triple(490f, 620f, Size(130f, 80f)),
                    Triple(490f, 720f, Size(130f, 90f)),

                    // Downtown Central
                    Triple(640f, 480f, Size(130f, 100f)),
                    Triple(790f, 480f, Size(140f, 100f)),
                    Triple(950f, 480f, Size(130f, 100f)),
                    Triple(1100f, 480f, Size(120f, 100f)),

                    Triple(640f, 600f, Size(130f, 110f)),
                    Triple(790f, 600f, Size(140f, 110f)),
                    Triple(950f, 600f, Size(130f, 110f)),
                    Triple(1100f, 600f, Size(120f, 110f)),

                    // Financial / SOMA
                    Triple(920f, 740f, Size(160f, 110f)),
                    Triple(1100f, 740f, Size(130f, 110f)),
                    Triple(1250f, 740f, Size(140f, 110f)),

                    // Mission / Potrero
                    Triple(520f, 840f, Size(150f, 90f)),
                    Triple(520f, 950f, Size(150f, 110f)),
                    Triple(700f, 900f, Size(130f, 100f)),
                    Triple(850f, 900f, Size(120f, 100f)),

                    // South District
                    Triple(680f, 1060f, Size(140f, 100f)),
                    Triple(840f, 1060f, Size(140f, 100f)),
                    Triple(1000f, 1060f, Size(130f, 100f)),
                    Triple(680f, 1180f, Size(140f, 110f)),
                    Triple(840f, 1180f, Size(140f, 110f))
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
                // 4. SECONDARY STREETS & AVENUES
                // ----------------------------------------------------
                val streetColor = Color(0xFF1E1D21)
                val avenueColor = Color(0xFF262529)

                // Horizontal streets
                listOf(460f, 590f, 720f, 830f, 870f, 1020f, 1040f, 1170f, 1310f).forEach { yPos ->
                    drawLine(
                        color = streetColor,
                        start = Offset(100f, yPos),
                        end = Offset(1500f, yPos),
                        strokeWidth = 3.5f
                    )
                }

                // Vertical streets
                listOf(200f, 350f, 480f, 630f, 780f, 935f, 1090f, 1240f, 1400f).forEach { xPos ->
                    drawLine(
                        color = streetColor,
                        start = Offset(xPos, 380f),
                        end = Offset(xPos, 1400f),
                        strokeWidth = 3.5f
                    )
                }

                // Main Avenues (wider)
                drawLine(
                    color = avenueColor,
                    start = Offset(100f, 725f),
                    end = Offset(1500f, 725f),
                    strokeWidth = 7f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = avenueColor,
                    start = Offset(780f, 350f),
                    end = Offset(780f, 1400f),
                    strokeWidth = 7f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = avenueColor,
                    start = Offset(1090f, 350f),
                    end = Offset(1090f, 1400f),
                    strokeWidth = 7f,
                    cap = StrokeCap.Round
                )

                // Diagonal Boulevard (Broadway / Market St Style)
                drawLine(
                    color = Color(0xFF2C2B30),
                    start = Offset(180f, 1100f),
                    end = Offset(1350f, 480f),
                    strokeWidth = 9f,
                    cap = StrokeCap.Round
                )

                // ----------------------------------------------------
                // 5. MAJOR EXPRESSWAY / HIGHWAY (HWY 101)
                // ----------------------------------------------------
                val hwyPath = Path().apply {
                    moveTo(1450f, 340f)
                    cubicTo(1380f, 550f, 1280f, 850f, 1200f, 1050f)
                    cubicTo(1150f, 1200f, 1050f, 1350f, 980f, 1500f)
                }
                // Highway bed
                drawPath(
                    hwyPath,
                    color = Color(0xFF323038),
                    style = Stroke(width = 14f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                // Highway center divider
                drawPath(
                    hwyPath,
                    color = Color(0xFF1E1D22),
                    style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // ----------------------------------------------------
                // 6. DISTRICT & STREET LABELS (Typography)
                // ----------------------------------------------------
                drawContext.canvas.nativeCanvas.apply {
                    val districtPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(130, 170, 168, 175)
                        textSize = 24f
                        isAntiAlias = true
                        letterSpacing = 0.22f
                        typeface = android.graphics.Typeface.create(
                            android.graphics.Typeface.DEFAULT,
                            android.graphics.Typeface.BOLD
                        )
                    }

                    val streetPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(90, 140, 138, 145)
                        textSize = 17f
                        isAntiAlias = true
                        letterSpacing = 0.12f
                        typeface = android.graphics.Typeface.create(
                            android.graphics.Typeface.DEFAULT,
                            android.graphics.Typeface.NORMAL
                        )
                    }

                    drawText("MARINA DISTRICT", 230f, 520f, districtPaint)
                    drawText("DOWNTOWN", 710f, 545f, districtPaint)
                    drawText("FINANCIAL DIST", 970f, 545f, districtPaint)
                    drawText("SOMA", 840f, 790f, districtPaint)
                    drawText("MISSION BAY", 620f, 980f, districtPaint)
                    drawText("POTRERO HILL", 910f, 1220f, districtPaint)

                    drawText("MARKET ST", 680f, 800f, streetPaint)
                    drawText("BRANNAN ST", 950f, 715f, streetPaint)
                    drawText("HWY 101", 1250f, 920f, streetPaint)
                    drawText("4TH AVE", 790f, 430f, streetPaint)
                }

                // ----------------------------------------------------
                // 7. ACTIVE NAVIGATION / WALKING ROUTE LINE
                // ----------------------------------------------------
                selectedGym?.id?.let { gymId ->
                    val gymCoord = gymCoordinates[gymId]
                    if (gymCoord != null) {
                        val routePath = Path().apply {
                            moveTo(userVirtualX, userVirtualY)
                            // Cornered realistic street path
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
                // 8. USER LOCATION PULSING RADAR PIN
                // ----------------------------------------------------
                // Expanding Radar Wave
                drawCircle(
                    color = Color(0xFFFFFFFF).copy(alpha = radarPulseAlpha),
                    radius = radarPulseRadius,
                    center = Offset(userVirtualX, userVirtualY)
                )

                // Outer aura ring
                drawCircle(
                    color = Color(0x33FFFFFF),
                    radius = 18f,
                    center = Offset(userVirtualX, userVirtualY)
                )

                // Dark border ring
                drawCircle(
                    color = Color(0xFF191819),
                    radius = 9f,
                    center = Offset(userVirtualX, userVirtualY)
                )

                // Pure White center GPS core
                drawCircle(
                    color = Color(0xFFFFFFFF),
                    radius = 6.5f,
                    center = Offset(userVirtualX, userVirtualY)
                )
            }
        }

        // ----------------------------------------------------
        // 9. INTERACTIVE COMPOSABLE GYM PINS & BADGES
        // ----------------------------------------------------
        gyms.forEach { gym ->
            val coord = gymCoordinates[gym.id] ?: Offset(800f, 800f)
            val isSelected = (gym.id == selectedGym?.id)

            // Convert virtual coordinates to screen space
            val pinScreenX = viewCenterX + (coord.x - userVirtualX) * animZoom.value + animPanX.value
            val pinScreenY = viewCenterY + (coord.y - userVirtualY) * animZoom.value + animPanY.value

            val pinXDp = with(density) { pinScreenX.toDp() }
            val pinYDp = with(density) { pinScreenY.toDp() }

            Box(
                modifier = Modifier
                    .offset(x = pinXDp - 65.dp, y = pinYDp - 60.dp)
                    .width(130.dp)
                    .clickable { onGymClicked(gym.id) },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onGymClicked(gym.id) }
                ) {
                    // Floating Badge Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(
                                if (isSelected) Color(0xFF222123) else Color(0xEB1E1D1F)
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) Color(0x80FFFFFF) else Color(0x33FFFFFF),
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
                                text = gym.name.take(13),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = Color(0xFFFFFFFF),
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Marker Pin Circle
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 36.dp else 30.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color(0xFF2C2A2D) else Color(0xFF222123)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.5.dp,
                                color = if (isSelected) Color(0xFFFFFFFF) else Color(0xB3FFFFFF),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🏋️",
                            fontSize = if (isSelected) 16.sp else 13.sp
                        )
                    }
                }
            }
        }
    }
}


