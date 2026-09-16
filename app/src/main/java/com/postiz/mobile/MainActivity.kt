package com.postiz.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.postiz.mobile.ui.navigation.PostizNavGraph
import com.postiz.mobile.ui.theme.PostizMobileTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PostizMobileTheme {
                PostizNavGraph()
            }
        }
    }
}
