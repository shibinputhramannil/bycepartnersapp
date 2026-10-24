package com.example.ui.owner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.MembershipPlanItem
import com.example.data.services.GymOwnerRepository
import com.example.ui.components.*
import com.example.ui.theme.*

/**
 * Read-only list of BYCE passes that give entry at this gym.
 * BYCE sets pass tiers and pricing; partners can't create or edit them.
 */
@Composable
fun OwnerPlansScreen(
    modifier: Modifier = Modifier
) {
    val passes = remember { GymOwnerRepository.getAcceptedPasses() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "BYCE PASSES",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextSubtle,
                letterSpacing = 1.sp
            )
            Text(
                text = "Accepted Passes",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Members with these BYCE passes can check in at your gym. Pass tiers and pricing are managed by BYCE.",
                fontSize = 12.sp,
                color = ByceCoolGray
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        if (passes.isEmpty()) {
            item {
                OwnerEmptyState(
                    title = "No Accepted Passes",
                    description = "No BYCE passes currently give entry at this gym."
                )
            }
        } else {
            items(passes, key = { it.id }) { pass ->
                AcceptedPassCard(pass = pass)
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun AcceptedPassCard(pass: MembershipPlanItem) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = pass.tierLevel,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NeonGreen,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = pass.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            Spacer(modifier = Modifier.height(12.dp))

            PassDetailRow(icon = Icons.Default.FitnessCenter, label = "Access", value = pass.accessScope)
            Spacer(modifier = Modifier.height(6.dp))
            PassDetailRow(icon = Icons.Default.Groups, label = "Group classes", value = pass.groupClassesQuota)
        }
    }
}

@Composable
private fun PassDetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = StatusActiveGreen, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "$label: ", fontSize = 12.sp, color = TextMuted)
        Text(text = value, fontSize = 12.sp, color = TextWhite)
    }
}
