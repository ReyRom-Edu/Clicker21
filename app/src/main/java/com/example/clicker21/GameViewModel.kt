package com.example.clicker21

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.serialization.Contextual
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.math.BigDecimal
import kotlin.math.pow

class GameViewModel(app: Application): AndroidViewModel(app) {
    var isDarkTheme by mutableStateOf(true)

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
                upgrades.add(ClickMultiplierUpgrade(
                    0,
                    BigDecimal(100),
                    1.5,
                    "Безумие",
                    BigDecimal(1))
                )
                upgrades.add(AutoclickUpgrade(
                    0,
                    BigDecimal(100),
                    1.5,
                    "Последователи культа",
                    BigDecimal(0))
               )
                upgrades.add(OfflineEarningsUpgrade(
                    0,
                    BigDecimal(100),
                    1.5,
                    "Храм Древних",
                    BigDecimal(0))
                )
            }

            upgrades.map {
                when(it){
                    is ClickMultiplierUpgrade -> {
                        multiplier = it.multiplier
                        it.descriptionBuilder = { u -> "Множитель клика ур.${u.level} - ${u.multiplier.formatNumber(2)}" }
                    }
                    is AutoclickUpgrade ->{
                        clicksPerSecond = it.clicksPerSecond
                        it.descriptionBuilder =  { u -> "Автоклик ур.${u.level} - ${u.clicksPerSecond.formatNumber()}" }
                    }
                    is OfflineEarningsUpgrade -> {
                        offlineCap = it.offlineCap
                        it.descriptionBuilder = { u -> "Офлайн доход ур.${u.level} - ${u.offlineCap.formatNumber()}" }
                    }
                }
            }
        }
    }
    var clicks by mutableStateOf(BigDecimal(0))

    var clicksPerSecond by mutableStateOf(BigDecimal(0))

    var multiplier by mutableStateOf(BigDecimal(0.0))

    var offlineCap by mutableStateOf(BigDecimal(0))

    val upgrades = mutableStateListOf<Upgrade>()

    suspend fun calculateOfflineEarnings(): Deferred<BigDecimal>{
        return viewModelScope.async {
            val currentTime = System.currentTimeMillis()
            val exitTime = storage.getExitTime()

            val deltaSec = (currentTime - exitTime)/1000

            var earnings = BigDecimal(0)
            if (offlineCap > BigDecimal(0)){
                earnings = (BigDecimal(deltaSec)*clicksPerSecond)
            }

            if(earnings > offlineCap) offlineCap else earnings
        }
    }

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
    abstract var cost: BigDecimal
    abstract val growthFactor: Double
    abstract val title: String
    abstract val description : String

    open fun upgrade(){
        level++
        cost = calculateNextCost()
    }
    fun calculateNextCost(): BigDecimal{
        return cost * BigDecimal(growthFactor.pow(level))
    }
}

@Serializable
@SerialName("ClickMultiplierUpgrade")
class ClickMultiplierUpgrade(
    override var level:Int,
    @Serializable(BigDecimalSerializer::class)
    override var cost: BigDecimal,
    override val growthFactor: Double,
    override val title: String,
    @Serializable(BigDecimalSerializer::class)
    var multiplier: BigDecimal
) : Upgrade(){
    @Transient
    var descriptionBuilder: (ClickMultiplierUpgrade) -> String = {u->""}

    override val description : String
        get() = descriptionBuilder(this)

    override fun upgrade() {
        super.upgrade()
        multiplier *= BigDecimal(1.2)
    }
}

@Serializable
@SerialName("AutoclickUpgrade")
class AutoclickUpgrade(
    override var level:Int,
    @Serializable(BigDecimalSerializer::class)
    override var cost: BigDecimal,
    override val growthFactor: Double,
    override val title: String,
    @Serializable(BigDecimalSerializer::class)
    var clicksPerSecond: BigDecimal
) : Upgrade(){
    @Transient
    var descriptionBuilder: (AutoclickUpgrade) -> String = {u->""}

    override val description : String
        get() = descriptionBuilder(this)

    override fun upgrade() {
        super.upgrade()
        clicksPerSecond = clicksPerSecond * BigDecimal(1.05) + BigDecimal(1)
    }
}

@Serializable
@SerialName("OfflineEarningsUpgrade")
class OfflineEarningsUpgrade(
    override var level:Int,
    @Serializable(BigDecimalSerializer::class)
    override var cost: BigDecimal,
    override val growthFactor: Double,
    override val title: String,
    @Serializable(BigDecimalSerializer::class)
    var offlineCap: BigDecimal
) : Upgrade(){
    @Transient
    var descriptionBuilder: (OfflineEarningsUpgrade) -> String = {u->""}

    override val description : String
        get() = descriptionBuilder(this)

    override fun upgrade() {
        super.upgrade()
        offlineCap = offlineCap * BigDecimal(1.2) + BigDecimal(10)
    }
}