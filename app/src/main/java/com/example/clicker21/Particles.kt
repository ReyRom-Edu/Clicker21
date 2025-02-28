package com.example.clicker21

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

@Composable
fun ParticleAnimation(particles: MutableList<Particle>){
    var indalidate by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true){
            delay(16L)
            particles.removeAll{ !it.update() }
            indalidate = !indalidate
        }
    }
    val context = LocalContext.current
    Canvas(modifier = Modifier.fillMaxSize()) {
        indalidate.let {
            particles.forEach{ particle ->
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(particle.alpha, 0.38f,0.96f, 0.86f)
                        textSize = 80f
                        typeface = ResourcesCompat.getFont(context, R.font.daedra)
                        setShadowLayer(10f,5f,5f,
                            android.graphics.Color.argb(particle.alpha, 0f,0f, 0f))
                    }
                    canvas.nativeCanvas.drawText(
                        particle.letter,
                        particle.x, particle.y,
                        paint
                    )
                }
            }
        }
    }
}


data class Particle(var x:Float, var y:Float,
                    var alpha: Float = 1f,
                    var rotation: Float = Random.nextFloat() * 360,
                    val angle: Float = Random.nextFloat() * 2 * PI.toFloat(),
                    val letter: String = ('A'..'Z').random().toString()){

    private val speed = Random.nextFloat() * 5 + 2
    private val speedX = speed * cos(angle)
    private val speedY = speed * sin(angle)
    private var lifeTime = 1f

    fun update() : Boolean{
        x += speedX
        y += speedY
        alpha -= 0.02f
        rotation += 5
        lifeTime -= 0.02f
        return lifeTime > 0
    }
}


fun getRandomParticleInCircle(centerX: Float, centerY: Float, radius: Float): Particle{
    val theta = Random.nextFloat() * 2 * PI.toFloat()
    val r = sqrt(Random.nextFloat()) * radius

    val x = centerX + r * cos(theta)
    val y = centerY + r * sin(theta)

    return Particle(x,y, angle = theta)
}