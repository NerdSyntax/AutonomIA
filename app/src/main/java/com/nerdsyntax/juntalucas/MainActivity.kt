package com.nerdsyntax.juntalucas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.nerdsyntax.juntalucas.ui.navigation.AppNavigation
import com.nerdsyntax.juntalucas.ui.theme.JuntaLucasTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContent {

            JuntaLucasTheme {

                AppNavigation()
            }
        }
    }
}