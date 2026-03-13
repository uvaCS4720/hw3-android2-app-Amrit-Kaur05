package edu.nd.pmcburne.hwapp.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.benchmark.traceprocessor.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.nd.pmcburne.hwapp.one.ui.theme.HWStarterRepoTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HWStarterRepoTheme {
                BasketballTrackerScreen()
                //                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
////                    )
//                Scaffold(
//                    topBar = {
//                        TopAppBar(
//                            title = {
//                                Text(
//                                    text = "Basketball Tracker",
//                                    fontSize = 25.sp
//                                )
//                            }
//                        )
//                    },
//                    bottomBar = {
//                        Text(
//                            text = "",
//                            fontSize = 25.sp
//                        )
//                    }
//                ) {
//                        innerPadding ->
//                    LazyColumn(
////                        modifier = modifier
////                            .fillMaxSize()
////                            .padding(innerPadding)
////                            .padding(horizontal = 16.dp),
//                        verticalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
////                        Text(
////                            text = "Basketball Stats",
////                            fontSize = 25.sp
////                        )
//                    }
//                }
//                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasketballTrackerScreen() {
    // state variables
    var selectedDate by remember { mutableStateOf("2025-03-13") }
    var isMens by remember { mutableStateOf(true) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Placeholder stats list — replace with real data later
    val statsList = listOf(
        "Notre Dame 78 - Duke 74",
        "Kentucky 85 - Kansas 80",
        "UConn 91 - Villanova 67",
        "Gonzaga 88 - Arizona 77",
        "Michigan 72 - Ohio State 69",
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text (
                        text = "Basketball Tracker",
                        fontSize = 22.sp
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // create date picker and men and women options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment  = Alignment.CenterVertically
            ) {
                // create date picker
                OutlinedButton(onClick = { showDatePicker = true }) {
                    Text(text = selectedDate)
                }

                // create option of men or women
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Women")
                    Switch(
                        checked = isMens,
                        onCheckedChange = { isMens = it },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Text(text = "Men")
                }
            }

            // create a refresh button
            Button(
                onClick = { /* TODO: fetch from API */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Refresh")
            }

            HorizontalDivider()

            // creating a list of stats found
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(statsList) { stat ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = stat,
                            modifier = Modifier.padding(16.dp),
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // if user clicks data picker, show options
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest  = { showDatePicker = false },
                confirmButton  = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formatted = java.text.SimpleDateFormat(
                                "yyyy-MM-dd", java.util.Locale.getDefault()
                            ).format(java.util.Date(millis))
                            selectedDate = formatted
                        }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton  = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
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

@Preview(showBackground = true)
@Composable
fun BasketballTrackerPreview() {
    HWStarterRepoTheme {
        BasketballTrackerScreen()
    }
}