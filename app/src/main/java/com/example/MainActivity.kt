package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.db.AppDatabase
import com.example.data.repository.NavRepository
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.NavViewModel
import com.example.data.remote.RemoteConfigRepository
import com.example.ui.viewmodel.NavViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = NavRepository(database.itemRecordDao(), database.uploadedResourceDao(), database.cloneAppDao())
        val remoteConfigRepository = RemoteConfigRepository(applicationContext)
        val factory = NavViewModelFactory(repository, remoteConfigRepository)
        val viewModel = ViewModelProvider(this, factory)[NavViewModel::class.java]

        setContent {
            val uiState = viewModel.uiState.collectAsState()
            MyApplicationTheme(
                themePreset = uiState.value.currentTheme,
                uiverseState = uiState.value.activeUiverseState
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun Greeting(name: String, modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier) {
    androidx.compose.material3.Text(text = "Hello $name!", modifier = modifier)
}

