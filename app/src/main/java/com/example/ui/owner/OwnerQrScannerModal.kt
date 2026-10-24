package com.example.ui.owner

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.models.ScannedQrResult
import com.example.data.models.ScannedQrType
import com.example.data.services.GymOwnerRepository
import com.example.ui.components.OwnerModalDialog
import com.example.ui.theme.*
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

/**
 * Universal Camera QR & Barcode Scanner for BYCE Gym Dashboard
 * Auto-opens camera and scans 1D/2D barcodes to match member passes and bookings.
 */
@Composable
fun OwnerQrScannerModal(
    onDismiss: () -> Unit,
    onScannedSuccessfully: (ScannedQrResult) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val clipboardManager = LocalClipboardManager.current

    // Permission state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Camera permission required for QR scanner", Toast.LENGTH_SHORT).show()
        }
    }

    // Auto request camera permission on open
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Camera control states
    var isTorchOn by remember { mutableStateOf(false) }
    var useFrontCamera by remember { mutableStateOf(false) }
    var cameraControlRef by remember { mutableStateOf<CameraControl?>(null) }
    var isScanningActive by remember { mutableStateOf(true) }

    // Scanned result sheet state
    var scannedResult by remember { mutableStateOf<ScannedQrResult?>(null) }
    var checkInSuccessMessage by remember { mutableStateOf<String?>(null) }
    var showManualInputDialog by remember { mutableStateOf(false) }
    var manualInputText by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (hasCameraPermission) {
                // ----------------------------------------------------
                // 1. LIVE CAMERAX PREVIEW & ML KIT BARCODE ANALYZER
                // ----------------------------------------------------
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        val cameraExecutor = Executors.newSingleThreadExecutor()

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }

                            val barcodeOptions = BarcodeScannerOptions.Builder()
                                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                                .build()
                            val barcodeScanner = BarcodeScanning.getClient(barcodeOptions)

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                if (!isScanningActive || scannedResult != null) {
                                    imageProxy.close()
                                    return@setAnalyzer
                                }

                                @OptIn(ExperimentalGetImage::class)
                                val mediaImage = imageProxy.image
                                if (mediaImage != null) {
                                    val image = InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees
                                    )
                                    barcodeScanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            if (barcodes.isNotEmpty() && isScanningActive && scannedResult == null) {
                                                val barcode = barcodes.first()
                                                val raw = barcode.rawValue ?: barcode.displayValue ?: ""
                                                if (raw.isNotBlank()) {
                                                    val formatName = getFormatName(barcode.format)
                                                    val res = GymOwnerRepository.processScannedQrPayload(raw, formatName)
                                                    scannedResult = res
                                                    isScanningActive = false
                                                }
                                            }
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                } else {
                                    imageProxy.close()
                                }
                            }

                            val cameraSelector = if (useFrontCamera) {
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            } else {
                                CameraSelector.DEFAULT_BACK_CAMERA
                            }

                            try {
                                cameraProvider.unbindAll()
                                val camera = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalysis
                                )
                                cameraControlRef = camera.cameraControl
                            } catch (exc: Exception) {
                                exc.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // ----------------------------------------------------
                // 2. CAMERA OVERLAY & SCANNING LASER HUD
                // ----------------------------------------------------
                ScannerViewfinderOverlay(
                    isScanning = isScanningActive && scannedResult == null
                )
            } else {
                // Permission Denied / Request Prompt State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F172A))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color(0x2210B981))
                                .border(1.5.dp, Color(0xFF10B981), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Camera Access Required",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Enable camera permission to scan member QR passes, slot bookings, and payments instantly in real time.",
                            fontSize = 13.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusActiveGreen),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Grant Camera Permission", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ----------------------------------------------------
            // 3. TOP BAR CONTROLS
            // ----------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 36.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Close button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0x77000000))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Scanner",
                            tint = TextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Scanner Title Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x88000000))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(StatusActiveGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Universal QR & Barcode Scanner",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }
                    }

                    // Top Action Controls (Torch & Flip)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Flashlight / Torch Button
                        IconButton(
                            onClick = {
                                isTorchOn = !isTorchOn
                                cameraControlRef?.enableTorch(isTorchOn)
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isTorchOn) StatusActiveGreen else Color(0x77000000))
                        ) {
                            Icon(
                                imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Toggle Torch",
                                tint = if (isTorchOn) Color.Black else TextWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Flip Camera Button
                        IconButton(
                            onClick = {
                                useFrontCamera = !useFrontCamera
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0x77000000))
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlipCameraAndroid,
                                contentDescription = "Flip Camera",
                                tint = TextWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Supported Formats Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "⚡ Real-time Scan: Member QRs · Booking Passes · 1D/2D Barcodes",
                        fontSize = 10.sp,
                        color = Color(0xCCFFFFFF),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // ----------------------------------------------------
            // 4. BOTTOM FLOATING CONTROLS
            // ----------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
            ) {
                // Manual Code Input Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xDD1E293B))
                        .clickable { showManualInputDialog = true }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Keyboard, contentDescription = null, tint = TextWhite, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Enter / Paste Code Manually",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite
                        )
                    }
                }
            }
        }
    }

    // ----------------------------------------------------
    // 5. SCANNED RESULT DETAIL MODAL (CLEAN SOLID DIALOG)
    // ----------------------------------------------------
    if (scannedResult != null) {
        val result = scannedResult!!
        OwnerModalDialog(
            onDismissRequest = {
                scannedResult = null
                checkInSuccessMessage = null
                isScanningActive = true
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (result.type) {
                                ScannedQrType.MEMBER_CHECKIN -> Icons.Default.Badge
                                ScannedQrType.BOOKING_PASS -> Icons.Default.EventAvailable
                                ScannedQrType.GYM_FACILITY -> Icons.Default.FitnessCenter
                                ScannedQrType.UNIVERSAL_CODE -> Icons.Default.QrCode
                            },
                            contentDescription = null,
                            tint = StatusActiveGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SCAN RESULT DETAILS",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    IconButton(
                        onClick = {
                            scannedResult = null
                            checkInSuccessMessage = null
                            isScanningActive = true
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextWhite, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Success Toast/Banner after confirming check-in
                if (checkInSuccessMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x2210B981))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = checkInSuccessMessage!!,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Type Badge & Format Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (result.type) {
                                    ScannedQrType.MEMBER_CHECKIN -> Color(0x2510B981)
                                    ScannedQrType.BOOKING_PASS -> Color(0x25F59E0B)
                                    else -> Color(0x2564748B)
                                }
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = result.badgeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (result.type) {
                                ScannedQrType.MEMBER_CHECKIN -> StatusActiveGreen
                                ScannedQrType.BOOKING_PASS -> StatusPendingAmber
                                else -> TextWhite
                            }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x20FFFFFF))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = result.formatName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = ByceCoolGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title & Subtitle Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E242B))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = result.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = result.subtitle,
                            fontSize = 13.sp,
                            color = TextMuted
                        )

                        if (result.shift != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = StatusActiveGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${result.shift} · Session Verified",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = StatusActiveGreen
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Raw Code Payload Display
                Column {
                    Text(
                        text = "DECODED PAYLOAD STRING",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextSubtle,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0D1117))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = result.rawPayload,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(result.rawPayload))
                                    Toast.makeText(context, "Copied payload to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextWhite, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Primary Action: Confirm Check-In / Action
                when (result.type) {
                    ScannedQrType.MEMBER_CHECKIN, ScannedQrType.BOOKING_PASS -> {
                        Button(
                            onClick = {
                                val attendanceRecord = GymOwnerRepository.confirmQrCheckIn(result)
                                if (attendanceRecord != null) {
                                    checkInSuccessMessage = "✓ ${attendanceRecord.customerName} successfully checked in at ${attendanceRecord.checkInTime}!"
                                    onScannedSuccessfully(result)
                                    Toast.makeText(context, "Check-in Confirmed!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "This code doesn't match a member or booking", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusActiveGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Confirm Check-In & Log Visit",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                    else -> {
                        if (!result.isRecognizedGymEntity) {
                            Text(
                                text = "This code doesn't match any member or booking, so it can't be used for check-in.",
                                fontSize = 12.sp,
                                color = TextMuted,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        if (result.rawPayload.startsWith("http", ignoreCase = true)) {
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result.rawPayload))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Open Web URL Link",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scan Next / Resume Scanning Button
                OutlinedButton(
                    onClick = {
                        scannedResult = null
                        checkInSuccessMessage = null
                        isScanningActive = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = TextWhite, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "📸 Scan Another Code", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    // ----------------------------------------------------
    // 6. MANUAL CODE INPUT MODAL
    // ----------------------------------------------------
    if (showManualInputDialog) {
        OwnerModalDialog(
            onDismissRequest = { showManualInputDialog = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Keyboard, contentDescription = null, tint = StatusActiveGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MANUAL CODE ENTRY",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    IconButton(
                        onClick = { showManualInputDialog = false },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextWhite, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Type or paste a member ID, name, email, phone number, or booking pass code to verify.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = manualInputText,
                    onValueChange = { manualInputText = it },
                    placeholder = { Text("e.g. Rahul Menon, c1, or member email...", fontSize = 12.sp, color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = StatusActiveGreen,
                        unfocusedBorderColor = GlassBorderLight,
                        focusedContainerColor = Color(0x20FFFFFF),
                        unfocusedContainerColor = Color(0x10FFFFFF)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (manualInputText.isNotBlank()) {
                            val res = GymOwnerRepository.processScannedQrPayload(manualInputText, "Manual Entry")
                            scannedResult = res
                            showManualInputDialog = false
                            isScanningActive = false
                        }
                    },
                    enabled = manualInputText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusActiveGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Verify Code", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Animated Viewfinder Overlay with Neon Brackets and Sweeping Laser Beam
 */
@Composable
private fun ScannerViewfinderOverlay(
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scannerLaser")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "laserBeam"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val boxSize = width * 0.72f
        val left = (width - boxSize) / 2f
        val top = (height - boxSize) / 2.3f
        val right = left + boxSize
        val bottom = top + boxSize
        val cornerLength = 40.dp.toPx()
        val cornerStroke = 4.dp.toPx()
        val cornerRadius = 16.dp.toPx()

        // 1. Semi-transparent dark background mask
        drawRect(
            color = Color(0x99000000),
            size = Size(width, height)
        )

        // 2. Clear out the transparent square hole in the middle
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(left, top),
            size = Size(boxSize, boxSize),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            blendMode = BlendMode.Clear
        )

        // 3. Draw Green / Emerald Target Reticle HUD Corners
        val bracketBrush = Brush.sweepGradient(
            colors = listOf(Color(0xFF34D399), Color(0xFF10B981), Color(0xFF059669), Color(0xFF34D399))
        )

        // Top-Left Corner
        drawLine(bracketBrush, Offset(left, top + cornerRadius), Offset(left, top + cornerLength), cornerStroke)
        drawLine(bracketBrush, Offset(left + cornerRadius, top), Offset(left + cornerLength, top), cornerStroke)
        drawArc(
            brush = bracketBrush,
            startAngle = 180f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(left, top),
            size = Size(cornerRadius * 2, cornerRadius * 2),
            style = Stroke(cornerStroke)
        )

        // Top-Right Corner
        drawLine(bracketBrush, Offset(right, top + cornerRadius), Offset(right, top + cornerLength), cornerStroke)
        drawLine(bracketBrush, Offset(right - cornerRadius, top), Offset(right - cornerLength, top), cornerStroke)
        drawArc(
            brush = bracketBrush,
            startAngle = 270f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(right - cornerRadius * 2, top),
            size = Size(cornerRadius * 2, cornerRadius * 2),
            style = Stroke(cornerStroke)
        )

        // Bottom-Left Corner
        drawLine(bracketBrush, Offset(left, bottom - cornerRadius), Offset(left, bottom - cornerLength), cornerStroke)
        drawLine(bracketBrush, Offset(left + cornerRadius, bottom), Offset(left + cornerLength, bottom), cornerStroke)
        drawArc(
            brush = bracketBrush,
            startAngle = 90f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(left, bottom - cornerRadius * 2),
            size = Size(cornerRadius * 2, cornerRadius * 2),
            style = Stroke(cornerStroke)
        )

        // Bottom-Right Corner
        drawLine(bracketBrush, Offset(right, bottom - cornerRadius), Offset(right, bottom - cornerLength), cornerStroke)
        drawLine(bracketBrush, Offset(right - cornerRadius, bottom), Offset(right - cornerLength, bottom), cornerStroke)
        drawArc(
            brush = bracketBrush,
            startAngle = 0f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(right - cornerRadius * 2, bottom - cornerRadius * 2),
            size = Size(cornerRadius * 2, cornerRadius * 2),
            style = Stroke(cornerStroke)
        )

        // 4. Draw Animated Sweeping Laser Line
        if (isScanning) {
            val laserY = top + (boxSize * laserProgress)
            val laserBrush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0x0010B981),
                    Color(0xAA34D399),
                    Color(0xFF10B981),
                    Color(0xFF34D399),
                    Color(0x0010B981)
                ),
                startX = left,
                endX = right
            )

            // Laser glow shadow
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x0010B981), Color(0x3310B981), Color(0x0010B981)),
                    startY = laserY - 20.dp.toPx(),
                    endY = laserY + 20.dp.toPx()
                ),
                topLeft = Offset(left + 4.dp.toPx(), laserY - 15.dp.toPx()),
                size = Size(boxSize - 8.dp.toPx(), 30.dp.toPx())
            )

            // Sharp center beam line
            drawLine(
                brush = laserBrush,
                start = Offset(left + 8.dp.toPx(), laserY),
                end = Offset(right - 8.dp.toPx(), laserY),
                strokeWidth = 3.dp.toPx()
            )
        }
    }
}

/**
 * Format Code to Display Name
 */
private fun getFormatName(format: Int): String {
    return when (format) {
        Barcode.FORMAT_QR_CODE -> "QR Code"
        Barcode.FORMAT_AZTEC -> "Aztec"
        Barcode.FORMAT_DATA_MATRIX -> "Data Matrix"
        Barcode.FORMAT_PDF417 -> "PDF417"
        Barcode.FORMAT_CODE_128 -> "Code 128"
        Barcode.FORMAT_CODE_39 -> "Code 39"
        Barcode.FORMAT_CODE_93 -> "Code 93"
        Barcode.FORMAT_CODABAR -> "Codabar"
        Barcode.FORMAT_EAN_13 -> "EAN-13"
        Barcode.FORMAT_EAN_8 -> "EAN-8"
        Barcode.FORMAT_ITF -> "ITF Barcode"
        Barcode.FORMAT_UPC_A -> "UPC-A"
        Barcode.FORMAT_UPC_E -> "UPC-E"
        else -> "Universal Barcode"
    }
}
