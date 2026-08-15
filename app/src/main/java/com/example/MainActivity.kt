package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.ui.navigation.SafeHerMainApp
import com.example.ui.theme.HighDensityCanvas
import com.example.ui.theme.SafeHerTheme
import com.example.ui.viewmodel.SafetyViewModel

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: SafetyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[SafetyViewModel::class.java]

        setContent {
            SafeHerTheme(darkTheme = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = HighDensityCanvas
                ) {
                    SafeHerMainApp(viewModel = viewModel)
                }
            }
        }
    }
}
