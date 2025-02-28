package com.example.clicker21

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

private val Context.dataStore by preferencesDataStore("game_prefs")

class GameStorage(private val context: Context) {
    private val SCORE_KEY = intPreferencesKey("score")
    private val UPGRADES_KEY = stringPreferencesKey("upgrades")
    private val LAST_EXIT_TIME_KEY = stringPreferencesKey("last_exit_time")

    suspend fun getScore() : Int{
        return context.dataStore.data.map { prefs->
            prefs[SCORE_KEY] ?: 0
        }.first()
    }

    suspend fun saveScore(clicks:Int){
        context.dataStore.edit { pefs ->
            pefs[SCORE_KEY] = clicks
        }
    }

    private val upgradeModule = SerializersModule {
        polymorphic(Upgrade::class){
            subclass(ClickMultiplierUpgrade::class, ClickMultiplierUpgrade.serializer())
            subclass(AutoclickUpgrade::class, AutoclickUpgrade.serializer())
            subclass(OfflineEarningsUpgrade::class, OfflineEarningsUpgrade.serializer())
        }
    }

    private val json = Json {
        serializersModule = upgradeModule
        classDiscriminator = "type"
    }

    suspend fun getUpgrages() : List<Upgrade>{
        return context.dataStore.data.map<Preferences, List<Upgrade>> { prefs->
            val data = prefs[UPGRADES_KEY] ?: "[]"
            json.decodeFromString(data)
        }.first()
    }

    suspend fun saveUpgrages(upgrades: List<Upgrade>){
        val data = json.encodeToString(upgrades)
        context.dataStore.edit { pefs ->
            pefs[UPGRADES_KEY] = data
        }
    }


    suspend fun getExitTime() : Long{
        return context.dataStore.data.map { prefs->
            prefs[LAST_EXIT_TIME_KEY]?.toLongOrNull() ?: 0L
        }.first()
    }

    suspend fun saveExitTime(){
        val currentTime = System.currentTimeMillis()
        context.dataStore.edit { pefs ->
            pefs[LAST_EXIT_TIME_KEY] = currentTime.toString()
        }
    }
}