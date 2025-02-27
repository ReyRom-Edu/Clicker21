package com.example.clicker21

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class GameViewModel(app: Application): AndroidViewModel(app) {
    val storage: GameStorage = GameStorage(app)
    init{
        viewModelScope.launch{
            storage.score.collect(){
                clicks = it
            }
            storage.autoclicks.collect(){
                clicksPerSecond = it
            }
        }

    }
    var clicks by mutableStateOf(0)

    var clicksPerSecond by mutableStateOf(0)

    fun upgradeAutoclick(){
        clicksPerSecond++
    }

    fun saveData(){
        viewModelScope.launch {
            storage.saveScore(clicks, clicksPerSecond)
        }
    }
}