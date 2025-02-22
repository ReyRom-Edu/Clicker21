package com.example.clicker21

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clicker21.ui.theme.Clicker21Theme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClickerGame()
        }
    }
}

@Composable
fun ClickerGame() {

    var clicks by remember { mutableStateOf(0) }
    val particles = remember { mutableStateListOf<Particle>() }
    var position by remember { mutableStateOf(Offset.Zero) }
    var boxPosition by remember  { mutableStateOf(Offset.Zero) }

    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if(isPressed) 1.1f else 1f,
        animationSpec = tween(delayMillis = 500)
    )

    Clicker21Theme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
            )
            {
                Text("Натапано: $clicks",
                    fontSize = 30.sp,
                    modifier = Modifier.align(Alignment.TopCenter))

                Box(Modifier
                    .size(100.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .align(Alignment.Center)
                    .background(Color.Blue)
                    .onGloballyPositioned {
                        boxPosition = Offset(it.positionInParent().x, it.positionInParent().y)
                    }
                    .pointerInput(Unit){
                        coroutineScope {
                            while (true){

                                awaitPointerEventScope {
                                    position = awaitFirstDown().position
                                    clicks++
                                    repeat(10){
                                        particles.add(Particle(position.x + boxPosition.x
                                            ,position.y + boxPosition.y))
                                    }
                                }
                            }
                        }
                    }
                )

                ParticleAnimation(particles)
            }
        }
    }
}

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

    Canvas(modifier = Modifier.fillMaxSize()) {
        indalidate.let {
            particles.forEach{ particle ->
                drawIntoCanvas {
                    drawCircle(Color.Red.copy(alpha = particle.alpha),
                        radius = 8f,
                        center = Offset(particle.x, particle.y))
                }
            }
        }
    }
}


data class Particle(var x:Float, var y:Float,
                    var alpha: Float = 1f,
                    var rotation: Float = Random.nextFloat() * 360){
    private val angle = Random.nextFloat() * 2 * PI.toFloat()
    private val speed = Random.nextFloat() * 5 + 2
    private val speedX = speed * cos(angle)
    private val speedY = speed * sin(angle)
    private var lifeTime = 1f

    fun update() : Boolean{
        x += speedX
        y += speedY
        alpha -= 0.05f
        rotation += 5
        lifeTime -= 0.05f
        return lifeTime > 0
    }
}




@Preview(showSystemUi = true)
@Composable
fun ClickerGamePreview() {
    ClickerGame()
}