package edu.nd.pmcburne.hwapp.one

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.nd.pmcburne.hwapp.one.ui.theme.HWStarterRepoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HWStarterRepoTheme {
                BasketballTrackerScreen()
            }
        }
    }
}

// checking if online connection available
@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
fun isOnline(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val caps = connectivityManager.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

// creating a function to parse through JSON information to make a list of GameInfo items
fun parseInfo(json: String, date: String, isMens: Boolean): List<GameEntity> {
    // creating variable to work with game info
    val root = JSONObject(json)
    val gamesArray = root.getJSONArray("games")
    val result = mutableListOf<GameEntity>()

    for (i in 0 until gamesArray.length()) {
        // get JSONObject for each game
        val game = gamesArray.getJSONObject(i).getJSONObject("game")

        // making variables to work with away and home teams and getting their names
        val away = game.getJSONObject("away")
        val home = game.getJSONObject("home")
        val awayNames = away.getJSONObject("names")
        val homeNames = home.getJSONObject("names")

        // creating a game entity for each game to store information
        result.add(
            GameEntity(
                gameId = game.getString("gameID"),
                awayTeam = awayNames.getString("short"),
                homeTeam = homeNames.getString("short"),
                awayScore = away.getString("score"),
                homeScore = home.getString("score"),
                gameState = game.getString("gameState"),
                startTime = game.getString("startTime"),
                currentPeriod = game.getString("currentPeriod"),
                contestClock = game.getString("contestClock"),
                awayWinner = away.getBoolean("winner"),
                homeWinner = home.getBoolean("winner"),
                finalMessage  = game.getString("finalMessage"),
                date = date,
                isMens = isMens
            )
        )
    }
    return result
}

// making urls for men and women
const val men_url   = "https://ncaa-api.henrygd.me/scoreboard/basketball-men/d1"
const val women_url = "https://ncaa-api.henrygd.me/scoreboard/basketball-women/d1"


// getting new api information and update
@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
suspend fun fetchAndSync(
    context: Context,
    isMens: Boolean,
    date: String
): List<GameEntity> {
    val dao = AppDatabase.getInstance(context).gameDao()
    return if (isOnline(context)) {
        withContext(Dispatchers.IO) {
            val url = if (isMens) men_url else women_url
            val json = URL(url).readText()
            val freshGames = parseInfo(json, date, isMens)
            dao.upsertGames(freshGames)
            freshGames
        }
    } else {
        withContext(Dispatchers.IO) {
            dao.getGames(date, isMens)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasketballTrackerScreen() {
    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf("2025-03-13") }
    var isMens by remember { mutableStateOf(true) }
    var showDatePicker by remember { mutableStateOf(false) }
    var games by remember { mutableStateOf<List<GameEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isOffline by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }

    // updating information from api
    LaunchedEffect(selectedDate, isMens, refreshTrigger) @androidx.annotation.RequiresPermission(
        android.Manifest.permission.ACCESS_NETWORK_STATE
    ) {
        isLoading = true
        errorMessage = null
        isOffline = !isOnline(context)
        try {
            games = fetchAndSync(context, isMens, selectedDate)
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
            try {
                val dao = AppDatabase.getInstance(context).gameDao()
                games = withContext(Dispatchers.IO) {
                    dao.getGames(selectedDate, isMens)
                }
            } catch (_: Exception) {
            }
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
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
                verticalAlignment = Alignment.CenterVertically
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
                onClick = { refreshTrigger++ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Refresh")
            }

            HorizontalDivider()

            // making a loading screen and checking for errors
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = errorMessage ?: "")
                    }
                }

                games.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No games found for this date.")
                    }
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(games) { game ->
                            GameCard(game = game, isMens = isMens)
                        }
                    }
                }
            }

            // if user clicks data picker, show options
            if (showDatePicker) {
                val datePickerState = rememberDatePickerState()
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
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
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
        }
    }
}


    // making cards to show a list with each game info
    @Composable
    fun GameCard(game: GameEntity, isMens: Boolean) {
        val periodLabel = when (game.gameState) {
            // updating if the game is done (final) or live
            "final" -> if (game.finalMessage.isNotBlank()) game.finalMessage else "Final"
            "live" -> {
                val clock = if (game.contestClock.isNotBlank()) game.contestClock else "--"
                val period = game.currentPeriod.ifBlank {
                    if (isMens) "1st Half" else "1st Qtr"
                }
                "$period  •  $clock"
            }

            else -> ""
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            // updating live, upcoming, or done (final) information
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = when (game.gameState) {
                        "pre" -> "Upcoming"
                        "live" -> "In Progress"
                        "final" -> "Final"
                        else -> game.gameState
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (game.gameState) {
                        "live" -> MaterialTheme.colorScheme.primary
                        "final" -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Away: ${game.awayTeam}",
                            fontWeight = if (game.awayWinner) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 16.sp
                        )
                        // making a winner tag if the team won
                        if (game.gameState == "final" && game.awayWinner) {
                            Text(
                                text = "WINNER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (game.gameState != "pre") {
                        Text(
                            text = game.awayScore,
                            fontWeight = if (game.awayWinner) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 16.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Home: ${game.homeTeam}",
                            fontWeight = if (game.homeWinner) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 16.sp
                        )
                        // making a winner tag if the team won
                        if (game.gameState == "final" && game.homeWinner) {
                            Text(
                                text = "WINNER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (game.gameState != "pre") {
                        Text(
                            text = game.homeScore,
                            fontWeight = if (game.homeWinner) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = when (game.gameState) {
                        "pre" -> "Starts at ${game.startTime}"
                        "final" -> periodLabel
                        "live" -> periodLabel
                        else -> game.startTime
                    },
                    fontSize = 13.sp,
                    color = when (game.gameState) {
                        "live" -> MaterialTheme.colorScheme.primary
                        "final" -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }


@Preview(showBackground = true)
@Composable
fun BasketballTrackerPreview() {
    HWStarterRepoTheme {
        BasketballTrackerScreen()
    }
}
