package com.example.clicker21

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compose.AppTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
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
    var boxSize by remember  { mutableStateOf(IntSize.Zero) }
    var isPressed by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(true) }

    val scale by animateFloatAsState(
        targetValue = if(isPressed) 0.9f else 1f,
        animationSpec = tween(delayMillis = 10)
    )
    LaunchedEffect(Unit) {
        while (true){
            delay(1000L)
            viewModel.clicks += viewModel.clicksPerSecond
            if(viewModel.clicksPerSecond > BigDecimal(0) ){
                val point = getRandomParticleInCircle(boxPosition.x + boxSize.width/2, boxPosition.y + boxSize.height/2 , boxSize.height/2f)
                particles.add(point)
            }
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Добро пожаловать обратно!") },
            text = { Text("Вы заработали очков за время отсутствия.") },
            confirmButton = {
                Button(onClick = { showDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
    AppTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
            )
            {
                Text("Натапано: ${viewModel.clicks.formatNumber()}",
                    fontSize = 30.sp,
                    modifier = Modifier.align(Alignment.TopCenter))

                Box(Modifier
                    .size(300.dp)

                    .clip(CircleShape)
                    .align(Alignment.Center)
                    //.background(Color.Blue)
                    .onGloballyPositioned {
                        boxPosition = Offset(it.positionInParent().x, it.positionInParent().y)
                        boxSize = it.size
                    }
                    .pointerInput(Unit){
                        coroutineScope {
                            while (true){
                                awaitPointerEventScope {
                                    val down = awaitFirstDown()
                                    position = down.position
                                    viewModel.clicks++
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
                BottomSheet(viewModel)
            }
        }
    }

    MyScreenWithLifecycle { viewModel.saveData() }
}

@Composable
fun MyScreenWithLifecycle(onExit: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
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

    if (isSheetOpen){
        ModalBottomSheet(
            onDismissRequest = { isSheetOpen = false },
            sheetState = rememberModalBottomSheetState(),
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            Column (Modifier.fillMaxSize().navigationBarsPadding()) {
                UpgradeView(viewModel)
            }
        }
    }


    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter){
        FloatingActionButton(onClick = {isSheetOpen = true}, modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp)) {
            Icon(Icons.Filled.Menu,"")
        }
    }
}


@Composable
fun UpgradeView(viewModel: GameViewModel){
    var invalidate by remember { mutableStateOf(false) }
    Column (modifier = Modifier.padding(10.dp)) {
        Text("Улучшения:")
        Spacer(Modifier.height(10.dp))
        invalidate.let{
            viewModel.upgrades.forEach{
                UpgradeButton(it.title) {
                    it.upgrade()
                    when(it){
                        is AutoClickerUpgrade -> viewModel.clicksPerSecond = it.clicksPerSecond
                        is ClickMultiplierUpgrade -> viewModel.multiplier = it.multiplier
                        is OfflineEarningsUpgrade -> viewModel.offlineCap = it.offlineCap
                    }
                    invalidate = !invalidate
                }
            }
        }

    }
}
@Composable
fun UpgradeButton(text: String, icon: ImageVector = Icons.Default.KeyboardArrowUp, onClick: () -> Unit){
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ){
        Icon(icon, contentDescription = text)
        Text(text)
    }
}



@Preview(showSystemUi = true)
@Composable
fun ClickerGamePreview() {
    ClickerGame()
}