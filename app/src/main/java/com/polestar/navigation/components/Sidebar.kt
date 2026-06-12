package com.polestar.navigation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.polestar.navigation.data.Screen
import com.polestar.navigation.theme.GraphiteCard
import com.polestar.navigation.theme.KineticGold
import com.polestar.navigation.theme.TextPrimary
import com.polestar.navigation.theme.TextSecondary

@Composable
fun Sidebar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(120.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Logo
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "P",
                color = KineticGold,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "POLESTAR",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Nav Buttons
        Column(
            modifier = Modifier.weight(1f).padding(vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SidebarButton(
                icon = Icons.Default.Home,
                label = "Home",
                isActive = currentScreen == Screen.Dashboard,
                onClick = { onNavigate(Screen.Dashboard) }
            )

            SidebarButton(
                icon = Icons.Default.Explore,
                label = "Nav",
                isActive = currentScreen == Screen.NavigationView || currentScreen == Screen.FuelFinder || currentScreen == Screen.RestaurantDiscovery,
                onClick = { onNavigate(Screen.NavigationView) }
            )

            SidebarButton(
                icon = Icons.Default.MusicNote,
                label = "Media",
                isActive = false,
                onClick = { /* Media toggle stub */ }
            )

            SidebarButton(
                icon = Icons.Default.Thermostat,
                label = "Climate",
                isActive = false,
                onClick = { /* Climate toggle stub */ }
            )
        }

        // Profile / Footer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Profile image mock circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(GraphiteCard)
                    .clickable { /* Profile stub */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = TextSecondary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = "User 01",
                color = TextSecondary,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun SidebarButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp) // Minimum 64dp touch target size for automotive safety
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) KineticGold else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) MaterialTheme.colorScheme.onPrimary else TextSecondary,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = label,
                color = if (isActive) MaterialTheme.colorScheme.onPrimary else TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
