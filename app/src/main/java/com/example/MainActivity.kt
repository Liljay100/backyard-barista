package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.BaristaMainApp
import com.example.ui.theme.BackyardBaristaTheme
import com.example.ui.viewmodel.BaristaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BackyardBaristaTheme {
                val viewModel: BaristaViewModel = viewModel()
                Surface(modifier = Modifier.fillMaxSize()) {
                    BaristaMainApp(viewModel = viewModel)
                }
            }
        }
    }
}
