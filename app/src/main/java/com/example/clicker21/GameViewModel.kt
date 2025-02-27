package com.example.clicker21

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.pow

class GameViewModel(app: Application): AndroidViewModel(app) {
    val storage: GameStorage = GameStorage(app)
    init{
        viewModelScope.launch{
            clicks = storage.getScore()
        }
        viewModelScope.launch {
            val data = storage.getUpgrades()
            if (data.any()){
                upgrades.addAll(data)
            }
            else{
                upgrades.add(AutoClickerUpgrade(0,100,1.7, 0))
                upgrades.add(ClickMultiplierUpgrade(0,100,1.5, 1.0))
                upgrades.add(OfflineEarningsUpgrade(0,100,1.2, 0))
            }

            upgrades.map {
                when(it){
                    is AutoClickerUpgrade -> clicksPerSecond = it.clicksPerSecond
                    is ClickMultiplierUpgrade -> multiplier = it.multiplier
                    is OfflineEarningsUpgrade -> offlineCap = it.offlineCap
                }
            }
        }
    }

    val upgrades = mutableStateListOf<Upgrade>()

    var clicks by mutableStateOf(0)

    var multiplier by mutableStateOf(0.0)

    var clicksPerSecond by mutableStateOf(0)

    var offlineCap by mutableStateOf(0)

    fun saveData(){
        viewModelScope.launch {
            storage.saveScore(clicks)
            storage.saveUpgrades(upgrades)
            storage.saveExitTime()
        }
    }


}

@Serializable
@Polymorphic
sealed class Upgrade{
    abstract  var level: Int
    abstract  var cost: Int
    abstract  val growthFactor: Double
    abstract  val title: String
    open fun upgrade(){
        level++
        cost = calculateNextCost()
    }
    fun calculateNextCost(): Int {
        return (cost * (growthFactor.pow(level))).toInt()
    }
}

@Serializable
@SerialName("ClickMultiplierUpgrade")
class ClickMultiplierUpgrade(
    override var level: Int,
    override var cost: Int,
    override val growthFactor: Double,
    var multiplier: Double,  // Увеличивает очки за клик
) : Upgrade() {
    override val title: String
        get() = "Множитель кликов ур.$level - x$multiplier"

    override fun upgrade(){
        super.upgrade()
        multiplier *= 1.2
    }
}

@Serializable
@SerialName("AutoClickerUpgrade")
class AutoClickerUpgrade(
    override var level: Int,
    override var cost: Int,
    override val growthFactor: Double,
    var clicksPerSecond: Int // Добавляет авто-клики
) : Upgrade(){
    override val title: String
        get() = "Автоклик ур.$level - $clicksPerSecond к/с"

    override fun upgrade() {
        super.upgrade()
        clicksPerSecond = (clicksPerSecond * 1.05).toInt() + 1
    }
}

@Serializable
@SerialName("OfflineEarningsUpgrade")
class OfflineEarningsUpgrade(
    override var level: Int,
    override var cost: Int,
    override val growthFactor: Double,
    var offlineCap: Int // Улучшает доход в оффлайне
) : Upgrade(){
    override val title: String
        get() = "Лимит оффлайн дохода ур.$level - максимум $offlineCap"

    override fun upgrade() {
        super.upgrade()
        offlineCap = (offlineCap * 1.2).toInt() + 10
    }
}


