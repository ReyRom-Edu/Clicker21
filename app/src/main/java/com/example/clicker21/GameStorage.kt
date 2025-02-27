package com.example.clicker21

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("game_prefs")

class GameStorage(private val context: Context) {
    private val SCORE_KEY = intPreferencesKey("score")
    private val AUTOCLICK_KEY = intPreferencesKey("autoclick")

    val score: Flow<Int> = context.dataStore.data.map { prefs->
        prefs[SCORE_KEY] ?: 0
    }

    val autoclicks: Flow<Int> = context.dataStore.data.map { prefs->
        prefs[AUTOCLICK_KEY] ?: 0
    }

    suspend fun saveScore(newScore:Int, newAutoclicks:Int){
        context.dataStore.edit { pefs ->
            pefs[SCORE_KEY] = newScore
            pefs[AUTOCLICK_KEY] = newAutoclicks
        }
    }
}