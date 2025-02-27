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
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
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
    var clicks by rememberSaveable { mutableStateOf(0) }
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

                    .clip(CircleShape)
                    .align(Alignment.Center)
                    //.background(Color.Blue)
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

                                    if (up != null){
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
                        contentDescription = "Background",
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
            BottomSheetWithTabs()
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
                        color = android.graphics.Color.argb(particle.alpha, 0.38f,0.96f, 0.86f)
                        textSize = 80f
                        alpha = (particle.alpha * 255).toInt()
                        typeface = ResourcesCompat.getFont(context, R.font.daedra)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetWithTabs() {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var isSheetOpen by remember { mutableStateOf(false) }

    // Вкладки
    val tabs = listOf("Улучшения", "Магазин", "Настройки")
    var selectedTabIndex by remember { mutableStateOf(0) }

    if (isSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isSheetOpen = false },
            sheetState = sheetState,
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(), // Заполняем весь экран
            containerColor = Color.DarkGray, // Цвет фона панели
            windowInsets = WindowInsets(0.dp),
            shape = RectangleShape // Убираем скругление
        ) {
            Column(modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()) {
                // Вкладки
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Black, // Цвет вкладок
                    contentColor = Color.White // Цвет текста
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }

                // Содержимое вкладок
                when (selectedTabIndex) {
                    0 -> UpgradesTabContent()
                    1 -> ShopTabContent()
                    2 -> SettingsTabContent()
                }
            }
        }
    }

    // Кнопка для открытия меню
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Button(onClick = { isSheetOpen = true }) {
            Text("Открыть меню")
        }
    }
}

// Вкладка "Улучшения"
@Composable
fun UpgradesTabContent() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Меню улучшений", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(10.dp))

        UpgradeButton("Скорость x2", Icons.Default.AddCircle) { }
        UpgradeButton("Доход +50%", Icons.Default.Call) { }
        UpgradeButton("Автоклик", Icons.Default.AccountBox) { }
    }
}

// Вкладка "Магазин"
@Composable
fun ShopTabContent() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Магазин", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(10.dp))

        Button(onClick = { }) {
            Text("Купить сундук")
        }
        Spacer(Modifier.height(8.dp))

        Button(onClick = { }) {
            Text("Купить бонус")
        }
    }
}

// Вкладка "Настройки"
@Composable
fun SettingsTabContent() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Настройки", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Звук", color = Color.White)
            Spacer(Modifier.width(8.dp))
            Switch(checked = true, onCheckedChange = {})
        }

        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Уведомления", color = Color.White)
            Spacer(Modifier.width(8.dp))
            Switch(checked = false, onCheckedChange = {})
        }
    }
}

// Кнопка для улучшений
@Composable
fun UpgradeButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}

data class Particle(var x:Float, var y:Float,
                    var alpha: Float = 1f,
                    var rotation: Float = Random.nextFloat() * 360,
                    val letter: String = ('A'..'Z').random().toString()){

    private val angle = Random.nextFloat() * 2 * PI.toFloat()
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




@Preview(showSystemUi = true)
@Composable
fun ClickerGamePreview() {
    ClickerGame()
}