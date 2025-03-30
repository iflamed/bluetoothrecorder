package com.caitun.bluetoothrecorder

import android.content.Intent
import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.caitun.bluetoothrecorder.observer.MirrorObserver
import com.caitun.bluetoothrecorder.ui.theme.BluetoothRecorderTheme

class MainActivity : ComponentActivity() {
    private var name = mutableStateOf("Android")
    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                100 -> {
                    name.value = msg.obj as String
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val cr = contentResolver
        val mirrorObserver: ContentObserver = MirrorObserver(this, handler)
        cr.registerContentObserver(MirrorObserver.contentUri, true, mirrorObserver)

        setContent {
            BluetoothRecorderTheme {
                Scaffold(modifier = Modifier
                    .fillMaxSize()
                    .fillMaxHeight()) { innerPadding ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .fillMaxHeight()
                        ) {
                        Greeting(
                            name = name.value,
                            modifier = Modifier.padding(innerPadding)
                        )
                        BluetoothLink(onClick = {
                            val it = Intent(
                                this@MainActivity,
                                AudioRecordActivity::class.java
                            )
                            startActivity(it)
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun BluetoothLink(onClick: () -> Unit) {
    Button(onClick = { onClick() }) {
        Text("Bluetooth Debug")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BluetoothRecorderTheme {
        Greeting("Android")
    }
}