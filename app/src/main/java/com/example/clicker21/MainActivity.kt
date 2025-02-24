package com.example.clicker21

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Typeface
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import com.example.compose.AppTheme
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
        targetValue = if(isPressed) 0.9f else 1f,
        animationSpec = tween(delayMillis = 10)
    )

    AppTheme {
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
                    .size(300.dp)
                    .align(Alignment.Center)
                    .clip(shape = CircleShape)
                    .background(Color.Transparent)
                    .onGloballyPositioned {
                        boxPosition = Offset(it.positionInParent().x, it.positionInParent().y)
                    }
                    .pointerInput(Unit){
                        coroutineScope {
                            while (true){
                                awaitPointerEventScope {
                                    val down = awaitFirstDown()
                                    position = down.position
                                    clicks++
                                    isPressed = true
                                    repeat(5){
                                        particles.add(Particle(position.x + boxPosition.x
                                            ,position.y + boxPosition.y))
                                    }

                                    down.consume()
                                    val up = waitForUpOrCancellation()

                                    if (up != null) {
                                        isPressed = false
                                    }
                                }
                            }
                        }
                    }
                ){
                    Image(
                        painter = painterResource(id = R.drawable.cthulhu_star),
                        modifier = Modifier.fillMaxSize(),
                        contentDescription = "Background Image",
                        contentScale = ContentScale.Crop
                    )
                    Image(
                        painter = painterResource(id = R.drawable.cthulhu),
                        modifier = Modifier.fillMaxSize(0.7f)
                            .align(Alignment.Center)
                            .graphicsLayer(scaleX = scale, scaleY = scale),
                        contentDescription = "Cthulhu",
                        contentScale = ContentScale.Crop
                    )
                }

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
    val context = LocalContext.current
    Canvas(modifier = Modifier.fillMaxSize()) {
        indalidate.let {
            particles.forEach{ particle ->
                drawIntoCanvas { canvas ->
                    //drawCircle(Color.Red.copy(alpha = particle.alpha),
                    //    radius = 8f,
                    //    center = Offset(particle.x, particle.y))

                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(particle.alpha, 0.38f,0.96f,0.86f)
                        textSize = 80f // Размер шрифта
                        alpha = (particle.alpha * 255).toInt()
                        typeface = ResourcesCompat.getFont(context, R.font.daedra)
                    }
                    canvas.nativeCanvas.drawText(
                        particle.letter, // Буква частицы
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
                    val letter: String =  ('A'..'Z').random().toString()){
    private val angle = Random.nextFloat() * 2 * PI.toFloat()
    private val speed = Random.nextFloat() * 4 + 1
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




@Preview(showSystemUi = true)
@Composable
fun ClickerGamePreview() {
    ClickerGame()
}