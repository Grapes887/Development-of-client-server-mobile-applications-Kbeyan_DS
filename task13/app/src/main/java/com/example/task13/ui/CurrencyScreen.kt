package com.example.task13.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.task13.viewmodel.CurrencyViewModel
import com.example.task13.viewmodel.RateDirection
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CurrencyScreen(
    viewModel: CurrencyViewModel = viewModel()
) {
    val rate by viewModel.usdRate.collectAsState()
    val direction by viewModel.rateChangeDirection.collectAsState()

    var pulseAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(rate) {
        pulseAnimation = true
        delay(300)
        pulseAnimation = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "USD -> RUB",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedContent(
                    targetState = rate,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) with
                                fadeOut(animationSpec = tween(300))
                    },
                    label = "rate_animation"
                ) { currentRate ->
                    Box(
                        modifier = Modifier
                            .scale(if (pulseAnimation) 1.1f else 1f)
                            .animateContentSize()
                    ) {
                        Text(
                            text = "%.2f".format(currentRate),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (direction) {
                                RateDirection.UP -> Color.Green
                                RateDirection.DOWN -> Color.Red
                                RateDirection.STABLE -> MaterialTheme.colorScheme.onSurface
                            },
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Индикатор направления
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    when (direction) {
                        RateDirection.UP -> {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Up",
                                tint = Color.Green,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "Растет",
                                color = Color.Green,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        RateDirection.DOWN -> {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Down",
                                tint = Color.Red,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "Падает",
                                color = Color.Red,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        RateDirection.STABLE -> {
                            Icon(
                                imageVector = Icons.Default.HorizontalRule,
                                contentDescription = "Stable",
                                tint = Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "Стабильно",
                                color = Color.Gray,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.refreshRate() },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Обновить сейчас",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Автообновление каждые 5 секунд",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}