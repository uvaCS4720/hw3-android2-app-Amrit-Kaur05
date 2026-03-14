package edu.nd.pmcburne.hwapp.one

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "games")
data class GameEntity( // creating a class for each game entity, holding key game information
    @PrimaryKey val gameId: String,
    val awayTeam: String,
    val homeTeam: String,
    val awayScore: String,
    val homeScore: String,
    val gameState: String,
    val startTime: String,
    val currentPeriod: String,
    val contestClock: String,
    val awayWinner: Boolean,
    val homeWinner: Boolean,
    val finalMessage: String,
    val date: String,
    val isMens: Boolean
)