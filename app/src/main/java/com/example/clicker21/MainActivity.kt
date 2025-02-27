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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.runBlocking

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

    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if(isPressed) 0.9f else 1f,
        animationSpec = tween(delayMillis = 10)
    )

    LaunchedEffect(Unit) {
        while (true){
            delay(1000L)
            viewModel.clicks += viewModel.clicksPerSecond
        }
    }


    AppTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
            )
            {
                Text("Натапано: ${viewModel.clicks}",
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
    DisposableEffect(Unit) {
        onDispose {
            runBlocking {
                viewModel.saveData()
            }
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
        Button(onClick = {isSheetOpen = true}) {
            Text("Меню")
        }
    }
}


@Composable
fun UpgradeView(viewModel: GameViewModel){
    Column (modifier = Modifier.padding(10.dp)) {
        Text("Улучшения:")
        Spacer(Modifier.height(10.dp))
        UpgradeButton("Автоклик ур.${viewModel.clicksPerSecond}",
            Icons.Default.Refresh) {viewModel.upgradeAutoclick()}
    }
}
@Composable
fun UpgradeButton(text: String, icon: ImageVector, onClick: () -> Unit){
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