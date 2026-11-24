package com.openpos.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.openpos.app.ui.navigation.OpenPosNavGraph
import com.openpos.app.ui.theme.OpenPosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpenPosTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    OpenPosNavGraph()
                }
            }
        }
    }
}
