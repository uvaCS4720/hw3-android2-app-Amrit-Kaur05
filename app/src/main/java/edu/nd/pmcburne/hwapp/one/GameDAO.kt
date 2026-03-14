package edu.nd.pmcburne.hwapp.one

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GameDAO { // accessing data about games with data accessing object

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGames(games: List<GameEntity>)

    @Query("SELECT * FROM games WHERE date = :date AND isMens = :isMens")
    suspend fun getGames(date: String, isMens: Boolean): List<GameEntity>
}