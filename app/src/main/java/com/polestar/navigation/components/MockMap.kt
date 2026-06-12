package com.polestar.navigation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.polestar.navigation.data.FuelStation
import com.polestar.navigation.data.NavigationHUDState
import com.polestar.navigation.data.Restaurant
import com.polestar.navigation.theme.KineticGold
import com.polestar.navigation.theme.Obsidian
import com.polestar.navigation.theme.TextPrimary
import com.polestar.navigation.theme.TextSecondary

@Composable
fun MockMap(
    fuelStations: List<FuelStation>,
    restaurants: List<Restaurant>,
    navHUDState: NavigationHUDState,
    onPinClick: (String, String) -> Unit, // id, type ("fuel" or "restaurant")
    modifier: Modifier = Modifier
) {
    // Pulse animation for user location
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    Box(modifier = modifier.fillMaxSize().background(Obsidian)) {
        Canvas(modifier = Modifier.fillMaxSize().clickable { /* map click stub */ }) {
            val width = size.width
            val height = size.height
            val center = Offset(width / 2, height / 2)

            // 1. Draw Streets Grid (Nordic Dark Style Vector Lines)
            val streetColor = Color(0xFF1E1E1E)
            val streetStroke = 4f
            
            // Grid lines
            for (i in -4..4) {
                // Vertical roads
                drawLine(
                    color = streetColor,
                    start = Offset(center.x + i * (width / 8f), 0f),
                    end = Offset(center.x + i * (width / 8f) + 100f, height),
                    strokeWidth = streetStroke
                )
                // Horizontal roads
                drawLine(
                    color = streetColor,
                    start = Offset(0f, center.y + i * (height / 6f)),
                    end = Offset(width, center.y + i * (height / 6f) - 50f),
                    strokeWidth = streetStroke
                )
            }

            // Diagonal ring road
            drawCircle(
                color = streetColor,
                radius = width / 3.5f,
                center = center,
                style = Stroke(width = 6f)
            )

            // 2. Draw Navigation Route Overlay (if navigation is active)
            if (navHUDState.isActive) {
                // Connect center (start) to a destination point based on navigation progress
                // Let's assume the active destination is located somewhere in the top right
                val routeStart = center
                val routeMid = Offset(center.x + width / 4f, center.y - height / 6f)
                val routeEnd = Offset(center.x + width / 3f, center.y - height / 2.5f)

                val path = Path().apply {
                    moveTo(routeStart.x, routeStart.y)
                    lineTo(routeMid.x, routeMid.y)
                    lineTo(routeEnd.x, routeEnd.y)
                }

                // Neon route line background glow
                drawPath(
                    path = path,
                    color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                    style = Stroke(width = 16f)
                )

                // Neon active route path
                drawPath(
                    path = path,
                    color = Color(0xFF00E5FF),
                    style = Stroke(width = 6f)
                )

                // Draw destination target pin
                drawCircle(
                    color = Color(0xFF00E5FF),
                    radius = 12f,
                    center = routeEnd
                )
                drawCircle(
                    color = Obsidian,
                    radius = 6f,
                    center = routeEnd
                )
            }

            // 3. Draw Stations Pins
            fuelStations.forEach { station ->
                val pinX = center.x + station.lngOffset * (width / 2.2f)
                val pinY = center.y - station.latOffset * (height / 2.2f)
                val pinPos = Offset(pinX, pinY)

                // Draw card backdrop
                drawCircle(
                    color = Color.Black.copy(alpha = 0.6f),
                    radius = 24f,
                    center = pinPos
                )
                
                // Draw color pin representing charging or gas regular
                val color = if (station.isElectric) KineticGold else Color(0xFF00E5FF)
                drawCircle(
                    color = color,
                    radius = 16f,
                    center = pinPos,
                    style = Stroke(width = 4f)
                )
                drawCircle(
                    color = color,
                    radius = 8f,
                    center = pinPos
                )
            }

            // 4. Draw Restaurant Pins
            restaurants.forEach { rest ->
                val pinX = center.x + rest.lngOffset * (width / 2.2f)
                val pinY = center.y - rest.latOffset * (height / 2.2f)
                val pinPos = Offset(pinX, pinY)

                drawCircle(
                    color = Color.Black.copy(alpha = 0.6f),
                    radius = 24f,
                    center = pinPos
                )
                
                drawCircle(
                    color = Color(0xFFFFB4AB), // Peach color for restaurants
                    radius = 16f,
                    center = pinPos,
                    style = Stroke(width = 4f)
                )
                drawCircle(
                    color = Color(0xFFFFB4AB),
                    radius = 6f,
                    center = pinPos
                )
            }

            // 5. Draw User Location Indicator (Car Position)
            // It sits near center. If navigation is active, it moves along the path
            val userPos = if (navHUDState.isActive) {
                // Interpolate along route path
                val prog = navHUDState.progress
                if (prog < 0.5f) {
                    val p = prog / 0.5f
                    Offset(
                        center.x + (p * (width / 4f)),
                        center.y - (p * (height / 6f))
                    )
                } else {
                    val p = (prog - 0.5f) / 0.5f
                    val midX = center.x + width / 4f
                    val midY = center.y - height / 6f
                    val endX = center.x + width / 3f
                    val endY = center.y - height / 2.5f
                    Offset(
                        midX + (p * (endX - midX)),
                        midY + (p * (endY - midY))
                    )
                }
            } else {
                center
            }

            // Location pulsing halo
            drawCircle(
                color = KineticGold.copy(alpha = pulseAlpha),
                radius = pulseRadius,
                center = userPos
            )

            // Outer white ring
            drawCircle(
                color = Color.White,
                radius = 12f,
                center = userPos
            )

            // Inner pointer triangle representing car heading
            val arrowPath = Path().apply {
                moveTo(userPos.x, userPos.y - 8f)
                lineTo(userPos.x - 6f, userPos.y + 6f)
                lineTo(userPos.x + 6f, userPos.y + 6f)
                close()
            }
            rotate(degrees = if (navHUDState.isActive) 45f else 0f, pivot = userPos) {
                drawPath(
                    path = arrowPath,
                    color = KineticGold
                )
            }
        }
    }
}
