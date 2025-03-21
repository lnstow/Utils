package com.github.lnstow.utils.no

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

object Test {
    @Composable
    fun Test() {
        Text("test", Modifier.padding(150.dp))
        Text("te676st", Modifier.padding(150.dp))
    }
}

@Preview
@Composable
fun TestPreview() {
    MaterialTheme {
        Test.Test()
    }
}