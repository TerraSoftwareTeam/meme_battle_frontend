package com.dev.memebattle.feature.home.impl.presentation.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import com.dev.memebattle.core.localization.Res
import com.dev.memebattle.core.localization.play
import com.dev.memebattle.core.localization.store
import com.dev.memebattle.feature.home.impl.presentation.component.HomeComponent
import com.dev.memebattle.feature.home.impl.presentation.store.HomeStore

@Composable
fun HomeView(component: HomeComponent) {
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E1035),
            Color(0xFF0F081D),
            Color(0xFF08040F)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "MemeBattle",
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFFD700),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "The Ultimate Card Game",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFB0A2C7),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 64.dp)
            )

            Button(
                onClick = { component.onIntent(HomeStore.Intent.OnPlayClicked) },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6C47FF),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp).padding(end = 8.dp)
                    )
                    Text(
                        text = stringResource(Res.string.play),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = { component.onIntent(HomeStore.Intent.OnStoreClicked) },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFB0A2C7)
                ),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = Color(0xFFB0A2C7).copy(alpha = 0.5f)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp).padding(end = 8.dp)
                    )
                    Text(
                        text = stringResource(Res.string.store),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

