package com.example.clicker21

import android.app.Application
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.magnifier
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.example.compose.AppTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal

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
fun ClickerGame(viewModel: GameViewModel = viewModel()) {
    val particles = remember { mutableStateListOf<Particle>() }
    var position by remember { mutableStateOf(Offset.Zero) }
    var boxPosition by remember  { mutableStateOf(Offset.Zero) }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if(isPressed) 0.9f else 1f,
        animationSpec = tween(delayMillis = 10)
    )

    var showDialog by remember { mutableStateOf(false) }
    var offlineEarnings by remember { mutableStateOf(BigDecimal(0)) }

    LaunchedEffect(Unit) {
        offlineEarnings = viewModel.calculateOfflineEarnings().await()
        if (offlineEarnings > BigDecimal(0)){
            showDialog = true
        }
        viewModel.clicks += offlineEarnings
    }

    if (showDialog){
        AlertDialog(
            onDismissRequest = {showDialog= false},
            title = { Text("С возвращением!") },
            text = { Text("Последователи заработали ${offlineEarnings.formatNumber()} безумия, пока вы отсутствовали") },
            confirmButton = {
                Button(onClick = {showDialog = false}) {
                    Text("Ок")
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        while (true){
            delay(1000L)
            viewModel.clicks += viewModel.clicksPerSecond
            if (viewModel.clicksPerSecond > BigDecimal(0)){
                val point = getRandomParticleInCircle(
                    boxPosition.x + boxSize.width/2,
                    boxPosition.y + boxSize.height/2,
                    boxSize.width/2f)
                particles.add(point)
            }
        }
    }


    AppTheme(darkTheme = viewModel.isDarkTheme) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
            )
            {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(MaterialTheme.colorScheme.primary)

                ){
                    Text("Сила культа: ${viewModel.clicks.formatNumber()}",
                        fontSize = 30.sp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.align(Alignment.Center))
                }


                Box(Modifier
                    .size(300.dp)

                    .clip(CircleShape)
                    .align(Alignment.Center)
                    //.background(Color.Blue)
                    .onGloballyPositioned {
                        boxPosition = Offset(it.positionInParent().x, it.positionInParent().y)
                        boxSize = it.size
                    }
                    .pointerInput(Unit) {
                        coroutineScope {
                            while (true) {
                                awaitPointerEventScope {
                                    val down = awaitFirstDown()
                                    position = down.position
                                    viewModel.clicks += viewModel.multiplier
                                    isPressed = true
                                    repeat(5) {
                                        particles.add(
                                            Particle(
                                                position.x + boxPosition.x,
                                                position.y + boxPosition.y
                                            )
                                        )
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
                        contentDescription = "Background",
                        contentScale = ContentScale.Crop
                    )
                    Image(
                        painter = painterResource(id = R.drawable.cthulhu),
                        modifier = Modifier
                            .fillMaxSize(0.7f)
                            .align(Alignment.Center)
                            .graphicsLayer(scaleX = scale, scaleY = scale),
                        contentDescription = "Cthulhu",
                        contentScale = ContentScale.Crop
                    )
                }

                ParticleAnimation(particles)
                BottomSheet(viewModel)
            }
        }
    }
    ApplicationLifecycleObserver { viewModel.saveData() }
}

@Composable
fun ApplicationLifecycleObserver(onExit: ()->Unit){
    val  lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner)
    {
        val observer = object : DefaultLifecycleObserver
        {
            override fun onStop(owner: LifecycleOwner) {
                onExit()
            }
            override fun onDestroy(owner: LifecycleOwner) {
                onExit()
            }
            override fun onPause(owner: LifecycleOwner) {
                onExit()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(viewModel: GameViewModel){
    var isSheetOpen by remember { mutableStateOf(false) }

    val tabs = listOf("Улучшения", "Магазин","Настройки")
    var selectedTabIndex by remember { mutableStateOf(0) }

    if (isSheetOpen){
        ModalBottomSheet(
            onDismissRequest = { isSheetOpen = false },
            sheetState = rememberModalBottomSheetState(),
            shape = RectangleShape,
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            Column (
                Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()) {
                TabRow(
                    selectedTabIndex = selectedTabIndex
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = {selectedTabIndex = index}
                        ) {
                            Text(title)
                        }
                    }

                }

                when(selectedTabIndex){
                    0 -> UpgradeView(viewModel)
                    1 -> ShopView()
                    2 -> SettingsView(viewModel)
                }
            }
        }
    }


    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter){
        Button(
            onClick = {isSheetOpen = true},
            shape = RectangleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)) {
            Text("Меню")
        }
    }
}

@Composable
fun SettingsView(viewModel: GameViewModel) {
    var volume by remember { mutableStateOf(0f) }
    Column {
        Row (verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)){
            Text("Звук")
            Spacer(Modifier.width(15.dp))
            Slider(value = volume, onValueChange = {volume = it}, modifier = Modifier.fillMaxWidth())
        }
        Row (verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)){
            Text("Темная тема")
            Spacer(Modifier.width(15.dp))
            Switch(checked = viewModel.isDarkTheme, onCheckedChange = {viewModel.isDarkTheme = !viewModel.isDarkTheme})
        }
    }
}

@Composable
fun ShopView() {

}


@Composable
fun UpgradeView(viewModel: GameViewModel){
    var invalidate by remember { mutableStateOf(false) }
    Column (modifier = Modifier.padding(10.dp)) {
        invalidate.let {
            viewModel.upgrades.forEach{
                UpgradeButton(it.title, it.description, it.cost.formatNumber()){
                    viewModel.upgrade(it)
                    invalidate = !invalidate
                }
            }
        }
    }
}
@Composable
fun UpgradeButton(title: String, description: String, cost: String, icon: ImageVector = Icons.Default.KeyboardArrowUp, onClick: () -> Unit){
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ){

        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth().align(Alignment.CenterStart)
            ) {
                Icon(icon, contentDescription = title)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(title)
                    Text(description)
                }

            }
            Text(cost, Modifier.align(Alignment.CenterEnd))
        }
    }
}



@Preview(showSystemUi = true)
@Composable
fun ClickerGamePreview() {
    ClickerGame()
}