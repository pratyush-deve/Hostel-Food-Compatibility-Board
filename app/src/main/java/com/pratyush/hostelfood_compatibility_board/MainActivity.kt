package com.pratyush.hostelfood_compatibility_board

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pratyush.hostelfood_compatibility_board.ui.screens.BoardScreen
import com.pratyush.hostelfood_compatibility_board.ui.theme.HostelFoodCompatibilityBoardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HostelFoodCompatibilityBoardTheme {
                BoardScreen()
            }
        }
    }
}