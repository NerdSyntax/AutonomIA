package com.nerdsyntax.juntalucas.feature.auth.ui.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    val darkBlue = Color(0xFF0F2A4A)
    val highlightPurple = Color(0xFF8B5CF6)


    LaunchedEffect(Unit) {
        delay(2500)
        onTimeout()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBlue)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))


        Box(
            modifier = Modifier
                .size(120.dp)
                .border(1.dp, Color(0xFF1E3A8A), CircleShape)
                .padding(16.dp)
                .border(2.dp, Color(0xFF2563EB).copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0xFF1E3A8A).copy(alpha = 0.5f), shape = MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = highlightPurple,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))


        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 40.sp)) {
                    append("Autonom")
                }
                withStyle(style = SpanStyle(color = highlightPurple, fontWeight = FontWeight.ExtraBold, fontSize = 40.sp)) {
                    append("IA")
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))


        Text(
            text = "Convierte los datos de tu negocio\nen decisiones",
            color = Color.LightGray,
            fontSize = 16.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))


        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(8.dp).background(highlightPurple, CircleShape))
            Box(modifier = Modifier.size(8.dp).background(highlightPurple.copy(alpha = 0.5f), CircleShape))
            Box(modifier = Modifier.size(8.dp).background(highlightPurple.copy(alpha = 0.5f), CircleShape))
        }

        Spacer(modifier = Modifier.height(16.dp))

        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(4.dp),
            color = highlightPurple,
            trackColor = Color(0xFF1E3A8A)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Versión 1.0.0",
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}