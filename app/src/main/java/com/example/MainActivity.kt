package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.data.local.ShortlyDatabase
import com.example.data.repository.ShortlyRepository
import com.example.domain.LocalizationManager
import com.example.ui.ShortlyApp
import com.example.ui.theme.ShortlyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = ShortlyDatabase.getInstance(applicationContext)
        val repository = ShortlyRepository(applicationContext, database.shortlyDao())
        val localizationManager = LocalizationManager.getInstance(applicationContext)

        setContent {
            ShortlyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    ShortlyApp(
                        repository = repository,
                        localizationManager = localizationManager
                    )
                }
            }
        }
    }
}
