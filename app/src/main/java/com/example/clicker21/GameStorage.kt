package com.example.clicker21

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import java.lang.reflect.Type

private val Context.dataStore by preferencesDataStore("game_prefs")

class GameStorage(private val context: Context) {

    private val SCORE_KEY = intPreferencesKey("score")
    private val LAST_EXIT_TIME_KEY = stringPreferencesKey("last_exit_time")
    private val UPGRADES_KEY = stringPreferencesKey("upgrades")

    suspend fun saveScore(clicks:Int){
        context.dataStore.edit { pefs ->
            pefs[SCORE_KEY] = clicks
        }
    }
    suspend fun getScore(): Int {
        return context.dataStore.data.map { prefs ->
            prefs[SCORE_KEY] ?: 0
        }.first()
    }

    private val upgradeModule = SerializersModule {
        polymorphic(Upgrade::class) {
            subclass(ClickMultiplierUpgrade::class, ClickMultiplierUpgrade.serializer())
            subclass(AutoClickerUpgrade::class, AutoClickerUpgrade.serializer())
            subclass(OfflineEarningsUpgrade::class, OfflineEarningsUpgrade.serializer())
        }
    }

    private val json = Json {
        serializersModule = upgradeModule
        classDiscriminator = "type" // Указывает, какое поле отвечает за определение типа
    }

    suspend fun saveUpgrades(upgrades: List<Upgrade>) {
        val data = json.encodeToString(upgrades)
        context.dataStore.edit { prefs ->
            prefs[UPGRADES_KEY] = data
        }
    }

    suspend fun getUpgrades(): List<Upgrade> {
        return context.dataStore.data.map<Preferences, List<Upgrade>> { prefs ->
            val data = prefs[UPGRADES_KEY] ?: "[]"
            json.decodeFromString(data)
        }.first()
    }

    suspend fun saveExitTime() {
        val currentTime = System.currentTimeMillis()
        context.dataStore.edit { prefs ->
            prefs[LAST_EXIT_TIME_KEY] = currentTime.toString()
        }
    }

    suspend fun getExitTime(): Long {
        return context.dataStore.data.map { prefs ->
            prefs[LAST_EXIT_TIME_KEY]?.toLongOrNull() ?: 0L
        }.first()
    }
}
