package pl.dakil.healthyshopping.ui.scanner

import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import pl.dakil.healthyshopping.R
import pl.dakil.healthyshopping.ui.designsystem.decorativeMotion
import pl.dakil.healthyshopping.ui.designsystem.decorativeSpec
import pl.dakil.healthyshopping.ui.designsystem.icon.AppIcon
import pl.dakil.healthyshopping.ui.designsystem.icon.AppSymbols
import pl.dakil.healthyshopping.ui.designsystem.rememberHaptics
import pl.dakil.healthyshopping.ui.theme.Spacing
import java.util.concurrent.Executors

/** How long the frame's "got it" snap plays before the product opens (decorative only). */
private const val DETECTED_HOLD_MS = 260L

@Composable
fun ScannerScreen(
    onBarcodeDetected: (String) -> Unit,
    onBackClicked: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptics = rememberHaptics()
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val onDetected by rememberUpdatedState(onBarcodeDetected)

    var camera by remember { mutableStateOf<Camera?>(null) }
    var torchOn by remember { mutableStateOf(false) }
    var detected by remember { mutableStateOf<String?>(null) }
    val decorative = decorativeMotion

    LaunchedEffect(detected) {
        val code = detected ?: return@LaunchedEffect
        haptics.confirm()
        if (decorative) delay(DETECTED_HOLD_MS)
        onDetected(code)
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val mainExecutor = ContextCompat.getMainExecutor(ctx)
                val analysisExecutor = Executors.newSingleThreadExecutor()
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(analysisExecutor, BarcodeAnalyzer { barcode ->
                                // Back on the main thread to update the UI and navigate.
                                mainExecutor.execute { if (detected == null) detected = barcode }
                            })
                        }
                    try {
                        cameraProvider.unbindAll()
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalyzer,
                        )
                    } catch (exc: Exception) {
                        Log.e("ScannerScreen", "Camera binding failed", exc)
                    }
                }, mainExecutor)
                previewView
            },
            modifier = Modifier.fillMaxSize(),
        )

        ScanFrame(detected = detected != null)

        // Top: back
        FilledIconButton(
            onClick = onBackClicked,
            shapes = IconButtonDefaults.shapes(),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color.Black.copy(alpha = 0.45f),
                contentColor = Color.White,
            ),
            modifier = Modifier.statusBarsPadding().padding(Spacing.l),
        ) {
            AppIcon(AppSymbols.ArrowBack, contentDescription = stringResource(R.string.action_back))
        }

        // Bottom: hint and torch
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.l),
        ) {
            Surface(shape = CircleShape, color = Color.Black.copy(alpha = 0.55f), contentColor = Color.White) {
                Text(
                    stringResource(R.string.scanner_hint),
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = Spacing.l, vertical = Spacing.s),
                )
            }
            if (camera?.cameraInfo?.hasFlashUnit() == true) {
                Row {
                    FilledTonalIconToggleButton(
                        checked = torchOn,
                        onCheckedChange = { on ->
                            haptics.toggle(on)
                            torchOn = on
                            camera?.cameraControl?.enableTorch(on)
                        },
                        shapes = IconButtonDefaults.toggleableShapes(),
                        modifier = Modifier.size(IconButtonDefaults.mediumContainerSize()),
                    ) {
                        AppIcon(
                            if (torchOn) AppSymbols.FlashlightOn else AppSymbols.FlashlightOff,
                            contentDescription = stringResource(if (torchOn) R.string.scanner_torch_off else R.string.scanner_torch_on),
                            filled = torchOn,
                        )
                    }
                }
            }
        }
    }
}

/**
 * The dimmed viewfinder with a barcode-shaped window. Its corner brackets draw themselves in when the
 * scanner opens, and snap inward in the primary color once a code is read (both decorative).
 */
@Composable
private fun ScanFrame(detected: Boolean) {
    val decorative = decorativeMotion
    val draw = remember { Animatable(if (decorative) 0f else 1f) }
    val drawSpec = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
    LaunchedEffect(Unit) { draw.animateTo(1f, drawSpec) }

    val snap by animateFloatAsState(
        targetValue = if (detected) 1f else 0f,
        animationSpec = decorativeSpec(MaterialTheme.motionScheme.fastSpatialSpec()),
        label = "detectedSnap",
    )
    val bracketColor by animateColorAsState(
        if (detected) MaterialTheme.colorScheme.primary else Color.White,
        MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "bracketColor",
    )

    Canvas(Modifier.fillMaxSize()) {
        val rectWidth = size.width * 0.72f
        val rectHeight = rectWidth * 0.62f
        val inset = 10.dp.toPx() * snap
        val left = (size.width - rectWidth) / 2f + inset
        val top = (size.height - rectHeight) / 2f + inset
        val right = left + rectWidth - inset * 2
        val bottom = top + rectHeight - inset * 2
        val radius = 28.dp.toPx()

        val scrim = Path().apply {
            addRect(Rect(0f, 0f, size.width, size.height))
            addRoundRect(RoundRect(Rect(left, top, right, bottom), CornerRadius(radius, radius)))
            fillType = PathFillType.EvenOdd
        }
        drawPath(scrim, Color.Black.copy(alpha = 0.55f))

        // Four rounded L-shaped brackets, each arm growing out of its corner.
        val arm = 36.dp.toPx() * draw.value.coerceIn(0f, 1.2f)
        val stroke = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
        fun bracket(cx: Float, cy: Float, dx: Float, dy: Float) {
            val path = Path().apply {
                moveTo(cx, cy + dy * (radius + arm))
                lineTo(cx, cy + dy * radius)
                quadraticTo(cx, cy, cx + dx * radius, cy)
                lineTo(cx + dx * (radius + arm), cy)
            }
            drawPath(path, bracketColor, style = stroke)
        }
        bracket(left, top, 1f, 1f)
        bracket(right, top, -1f, 1f)
        bracket(left, bottom, 1f, -1f)
        bracket(right, bottom, -1f, -1f)
    }
}
