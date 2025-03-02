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
import java.math.BigDecimal
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
                upgrades.add(AutoClickerUpgrade(0, BigDecimal(100),1.7, BigDecimal(0)))
                upgrades.add(ClickMultiplierUpgrade(0,BigDecimal(100),1.5, BigDecimal(1)) { u -> "Множитель кликов ур.$u.level - x${u.multiplier.formatNumber()}" })
                upgrades.add(OfflineEarningsUpgrade(0,BigDecimal(100),1.2, BigDecimal(0)))
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

    var clicks by mutableStateOf(BigDecimal(0))

    var multiplier by mutableStateOf(BigDecimal(0))

    var clicksPerSecond by mutableStateOf(BigDecimal(0))

    var offlineCap by mutableStateOf(BigDecimal(0))

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
    abstract  var cost: BigDecimal
    abstract  val growthFactor: Double
    abstract  val title: String
    open fun upgrade(){
        level++
        cost = calculateNextCost()
    }
    fun calculateNextCost(): BigDecimal {
        return (cost * BigDecimal(growthFactor.pow(level)))
    }
}

@Serializable
@SerialName("ClickMultiplierUpgrade")
class ClickMultiplierUpgrade(
    override var level: Int,
    @Serializable(with = BigDecimalSerializer::class)
    override var cost: BigDecimal,
    override val growthFactor: Double,
    @Serializable(with = BigDecimalSerializer::class)
    var multiplier: BigDecimal,  // Увеличивает очки за клик
    @SerialName("-")
    val description: (ClickMultiplierUpgrade) -> String
) : Upgrade() {
    override val title: String
        get() = description(this)

    override fun upgrade(){
        super.upgrade()
        multiplier *= BigDecimal(1.2)
    }
}

@Serializable
@SerialName("AutoClickerUpgrade")
class AutoClickerUpgrade(
    override var level: Int,
    @Serializable(with = BigDecimalSerializer::class)
    override var cost: BigDecimal,
    override val growthFactor: Double,
    @Serializable(with = BigDecimalSerializer::class)
    var clicksPerSecond: BigDecimal // Добавляет авто-клики
) : Upgrade(){
    override val title: String
        get() = "Автоклик ур.$level - ${clicksPerSecond.formatNumber()} к/с"

    override fun upgrade() {
        super.upgrade()
        clicksPerSecond = clicksPerSecond * BigDecimal(1.05) + BigDecimal(1)
    }
}

@Serializable
@SerialName("OfflineEarningsUpgrade")
class OfflineEarningsUpgrade(
    override var level: Int,
    @Serializable(with = BigDecimalSerializer::class)
    override var cost: BigDecimal,
    override val growthFactor: Double,
    @Serializable(with = BigDecimalSerializer::class)
    var offlineCap: BigDecimal // Улучшает доход в оффлайне
) : Upgrade(){
    override val title: String
        get() = "Лимит оффлайн дохода ур.$level - максимум ${offlineCap.formatNumber()}"

    override fun upgrade() {
        super.upgrade()
        offlineCap = offlineCap * BigDecimal(1.2) + BigDecimal(10)
    }
}


