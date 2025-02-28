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
            val data = storage.getUpgrages()
            if (data.any()){
                upgrades.addAll(data)
            }
            else{
                upgrades.add(ClickMultiplierUpgrade(0,100,1.5,"Безумие", 1.0))
                upgrades.add(AutoclickUpgrade(0,100,1.5,"Последователи культа", 0))
                upgrades.add(OfflineEarningsUpgrade(0,100,1.5,"Храм Древних", 0))
            }

            upgrades.map {
                when(it){
                    is ClickMultiplierUpgrade -> multiplier = it.multiplier
                    is AutoclickUpgrade -> clicksPerSecond = it.clicksPerSecond
                    is OfflineEarningsUpgrade -> offlineCap = it.offlineCap
                }
            }
        }
    }
    var clicks by mutableStateOf(0)

    var clicksPerSecond by mutableStateOf(0)

    var multiplier by mutableStateOf(0.0)

    var offlineCap by mutableStateOf(0)

    val upgrades = mutableStateListOf<Upgrade>()


    fun upgrade(item: Upgrade){
        item.upgrade()
        when(item){
            is ClickMultiplierUpgrade -> multiplier = item.multiplier
            is AutoclickUpgrade -> clicksPerSecond = item.clicksPerSecond
            is OfflineEarningsUpgrade -> offlineCap = item.offlineCap
        }
    }

    fun saveData(){
        viewModelScope.launch {
            storage.saveScore(clicks)
            storage.saveUpgrages(upgrades)
            storage.saveExitTime()
        }
    }
}

@Serializable
@Polymorphic
sealed class Upgrade{
    abstract var level: Int
    abstract var cost: Int
    abstract val growthFactor: Double
    abstract val title: String
    abstract val description : String

    open fun upgrade(){
        level++
        cost = calculateNextCost()
    }
    fun calculateNextCost(): Int{
        return (cost * (growthFactor.pow(level))).toInt()
    }
}

@Serializable
@SerialName("ClickMultiplierUpgrade")
class ClickMultiplierUpgrade(
    override var level:Int,
    override var cost: Int,
    override val growthFactor: Double,
    override val title: String,
    var multiplier: Double
) : Upgrade(){
    override val description : String
        get() = "Множитель кликов ур.%d - x%.2f".format(level, multiplier)

    override fun upgrade() {
        super.upgrade()
        multiplier *= 1.2
    }
}

@Serializable
@SerialName("AutoclickUpgrade")
class AutoclickUpgrade(
    override var level:Int,
    override var cost: Int,
    override val growthFactor: Double,
    override val title: String,
    var clicksPerSecond: Int
) : Upgrade(){
    override val description : String
        get() = "Автоклик ур.$level - $clicksPerSecond к/с"

    override fun upgrade() {
        super.upgrade()
        clicksPerSecond = (clicksPerSecond * 1.05).toInt() + 1
    }
}

@Serializable
@SerialName("OfflineEarningsUpgrade")
class OfflineEarningsUpgrade(
    override var level:Int,
    override var cost: Int,
    override val growthFactor: Double,
    override val title: String,
    var offlineCap: Int
) : Upgrade(){
    override val description : String
        get() = "Лимит офлайн дохода ур.$level - максимум $offlineCap"

    override fun upgrade() {
        super.upgrade()
        offlineCap = (offlineCap * 1.2).toInt() + 10
    }
}