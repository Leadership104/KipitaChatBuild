package com.kipita

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.kipita.presentation.main.KipitaApp
import com.kipita.presentation.theme.KipitaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppEntryPoint() }
    }
}

@Composable
private fun AppEntryPoint() {
    KipitaTheme {
        KipitaApp()
    }
}
