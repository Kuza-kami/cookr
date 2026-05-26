package com.example.presentation.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.CookrThemeSelection
import com.example.presentation.CookrViewModel
import com.example.presentation.ThemeMode
import com.example.presentation.core.components.NeoCard
import com.example.presentation.core.components.NeoButton
import com.example.presentation.core.components.NeoTextField
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

@Composable
fun ProfileScreen(
    viewModel: CookrViewModel,
    userName: String,
    onNameChange: (String) -> Unit,
    dailyCalTarget: Int,
    onTargetChange: (Int) -> Unit
) {
    val context = LocalContext.current
    val recipes by viewModel.allRecipes.collectAsState()
    val savedCount = recipes.filter { it.isSaved }.size

    var isFoodManagementExpanded by remember { mutableStateOf(false) }
    var isAppearanceExpanded by remember { mutableStateOf(false) }
    var isLanguageExpanded by remember { mutableStateOf(false) }
    var isAccountExpanded by remember { mutableStateOf(false) }
    var isAboutExpanded by remember { mutableStateOf(false) }
    var isPermissionsExpanded by remember { mutableStateOf(false) }

    var hasCameraPermission by remember {
        mutableStateOf(
            try {
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            } catch (e: Exception) {
                false
            }
        )
    }
    var hasNotificationPermission by remember {
        mutableStateOf(
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                } else {
                    true
                }
            } catch (e: Exception) {
                true
            }
        )
    }
    var hasStoragePermission by remember {
        mutableStateOf(
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
                } else {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                }
            } catch (e: Exception) {
                false
            }
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        try {
            hasCameraPermission = isGranted
            val msg = if (isGranted) "Camera permission is fully active! 📸" else "Camera access was denied!"
            Toast.makeText(context.applicationContext, msg, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        try {
            hasNotificationPermission = isGranted
            val msg = if (isGranted) "Smart cooking alerts are preheated! 🔔" else "Notifications are turned off!"
            Toast.makeText(context.applicationContext, msg, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val storageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        try {
            hasStoragePermission = isGranted
            val msg = if (isGranted) "Gallery linked! Access active. 📂" else "Storage linked was cancelled!"
            Toast.makeText(context.applicationContext, msg, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF9F6F0), // Beautiful soft cozy cream background
                        Color(0xFFE8E4D9)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            WeeklyProgressTopWidget(userName = userName)

            Spacer(modifier = Modifier.height(24.dp))

            // THEME-ALIGNED BENTO STATS (Side-by-side)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Recipes Count
                NeoCard(
                    modifier = Modifier.weight(1f),
                    containerColor = Color(0xFFE1CEFC), // Lavender
                    shadowColor = Color(0xFF313131),
                    shadowOffset = 4.dp,
                    borderWidth = 2.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.RestaurantMenu, contentDescription = null, tint = Color(0xFF313131), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("14", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF313131))
                        Text("Dishes", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF313131).copy(alpha = 0.8f))
                    }
                }

                // Card 2: Saved Favorites count
                NeoCard(
                    modifier = Modifier.weight(1f),
                    containerColor = Color(0xFFF9D3CD), // Rose
                    shadowColor = Color(0xFF313131),
                    shadowOffset = 4.dp,
                    borderWidth = 2.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(savedCount.toString(), fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF313131))
                        Text("Saved", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF313131).copy(alpha = 0.8f))
                    }
                }

                // Card 3: Streak Counter
                NeoCard(
                    modifier = Modifier.weight(1f),
                    containerColor = Color(0xFFFEE6A5), // Gold Warm
                    shadowColor = Color(0xFF313131),
                    shadowOffset = 4.dp,
                    borderWidth = 2.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("3", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF313131))
                        Text("Streaks", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF313131).copy(alpha = 0.8f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // -- KITCHEN FIRE STOVE SETTINGS (DAILY CALORIE TARGET SLIDER) --
            Text(
                text = "🪵 Hearth Temperature Mode",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF313131),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            var sliderValue by remember(dailyCalTarget) { mutableStateOf(dailyCalTarget.toFloat().coerceIn(1200f, 3500f)) }
            
            val fireColor = when {
                sliderValue <= 1600f -> Color(0xFF8DAB7F) // Gentle moss ember green
                sliderValue <= 2400f -> Color(0xFFFF9800) // Warming cozy hearth orange
                else -> Color(0xFFD32F2F) // Blazing roaring Calcifer red
            }
            
            val fireStatusText = when {
                sliderValue <= 1600f -> "🍃 Whispering Ember"
                sliderValue <= 2400f -> "🔥 Cozy Hearth Stew"
                else -> "⚡ Calcifer Roaring Blaze"
            }
            
            val fireDescription = when {
                sliderValue <= 1600f -> "Gentle low fire mode for light or peaceful low-calorie snacking."
                sliderValue <= 2400f -> "Optimal, comforting, warming warmth for daily standard recipes."
                else -> "Roaring hot wood oven fire! Fueling heavy tasks and active high energy!"
            }

            NeoCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.White,
                shadowColor = Color(0xFF313131),
                shadowOffset = 4.dp,
                borderWidth = 2.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.5.dp, Color(0xFF313131), RoundedCornerShape(8.dp))
                                    .background(fireColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocalFireDepartment,
                                    contentDescription = "Kitchen Fire Intensity",
                                    tint = fireColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Daily Calorie Fire",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF313131)
                                )
                                Text(
                                    text = "Calibrate stoves to regulate ingredients",
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(fireColor.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                                .border(1.dp, fireColor, RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${sliderValue.toInt()} kcal",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF313131)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Slider(
                        value = sliderValue,
                        onValueChange = { 
                            sliderValue = it
                            onTargetChange(it.toInt())
                        },
                        valueRange = 1200f..3500f,
                        colors = SliderDefaults.colors(
                            thumbColor = fireColor,
                            activeTrackColor = fireColor,
                            inactiveTrackColor = fireColor.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF9F6F0), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFF313131).copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🍳",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Column {
                            Text(
                                text = fireStatusText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = fireColor
                            )
                            Text(
                                text = fireDescription,
                                fontSize = 10.sp,
                                color = Color(0xFF313131).copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // CATEGORIES GROUPS - DESIGN IMPROVEMENTS
            Text(
                text = "Preferences Panel",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF313131),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 0. System & Device Permissions Block
            SettingGroupNeoCard(
                icon = Icons.Outlined.CheckCircle,
                title = "Device Permissions & System",
                isExpanded = isPermissionsExpanded,
                onClick = { isPermissionsExpanded = !isPermissionsExpanded },
                headerColor = Color(0xFFCDE89C)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text(
                        text = "To unlock full digital-kitchen experiences, activate real device integrations below:",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    PermissionItemRow(
                        title = "Smart Barcode & Photo Camera",
                        description = "Log recipe dishes with live photos/scanner",
                        icon = Icons.Outlined.CameraAlt,
                        isGranted = hasCameraPermission,
                        onRequest = {
                            try {
                                cameraLauncher.launch(Manifest.permission.CAMERA)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Failed to launch Camera request!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    PermissionItemRow(
                        title = "Smart Cooking Notifications",
                        description = "Alert when timers complete or new meals plan",
                        icon = Icons.Outlined.Notifications,
                        isGranted = hasNotificationPermission,
                        onRequest = {
                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    Toast.makeText(context, "System automatically manages alerts for this device OS!", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Failed to launch Notification request!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    PermissionItemRow(
                        title = "Photo Library & Storage",
                        description = "Add cozy culinary illustrations from gallery",
                        icon = Icons.Outlined.Folder,
                        isGranted = hasStoragePermission,
                        onRequest = {
                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    storageLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                                } else {
                                    storageLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Failed to launch Storage request!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Language Preferences Block
            SettingGroupNeoCard(
                icon = Icons.Outlined.Language,
                title = "Language Preferences",
                isExpanded = isLanguageExpanded,
                onClick = { isLanguageExpanded = !isLanguageExpanded },
                headerColor = Color(0xFFE1CEFC) // Soft elegant purple header accent
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    var currentLang by remember { mutableStateOf("English (US)") }
                    listOf("English (US)", "Spanish", "French", "German").forEach { lang ->
                        val selected = currentLang == lang
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) Color(0xFFE1CEFC).copy(alpha = 0.25f) else Color.Transparent)
                                .clickable { currentLang = lang }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = lang,
                                fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold,
                                color = Color(0xFF313131),
                                fontSize = 13.sp
                            )
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, Color(0xFF313131), CircleShape)
                                    .background(if (selected) Color(0xFFE1CEFC) else Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selected) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF313131))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Food Management Block
            SettingGroupNeoCard(
                icon = Icons.Outlined.RestaurantMenu,
                title = "Food Management & Diets",
                isExpanded = isFoodManagementExpanded,
                onClick = { isFoodManagementExpanded = !isFoodManagementExpanded },
                headerColor = Color(0xFFF9D3CD) // Soft elegant rose header accent
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    var isVegan by remember { mutableStateOf(false) }
                    var isGlutenFree by remember { mutableStateOf(false) }

                    // Vegan toggle option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isVegan = !isVegan }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Spa, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Strict Vegan Dishes Only", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF313131))
                        }
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.5.dp, Color(0xFF313131), RoundedCornerShape(6.dp))
                                .background(if (isVegan) Color(0xFFF9D3CD) else Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isVegan) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = Color(0xFF313131), modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Gluten Free toggle option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isGlutenFree = !isGlutenFree }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Grass, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gluten-Free Filtering", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF313131))
                        }
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.5.dp, Color(0xFF313131), RoundedCornerShape(6.dp))
                                .background(if (isGlutenFree) Color(0xFFF9D3CD) else Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isGlutenFree) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = Color(0xFF313131), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Theme / Appearance Block
            SettingGroupNeoCard(
                icon = Icons.Outlined.Palette,
                title = "Appearance & Theme",
                isExpanded = isAppearanceExpanded,
                onClick = { isAppearanceExpanded = !isAppearanceExpanded },
                headerColor = Color(0xFFFEE6A5) // Soft elegant gold header accent
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text(
                        text = "Customize system colors & contrast toggling:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val themeMode by viewModel.themeMode.collectAsState()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeMode.values().forEach { mode ->
                            val active = themeMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        width = 1.5.dp,
                                        color = Color(0xFF313131),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .background(
                                        if (active) Color(0xFFFEE6A5) else Color.White
                                    )
                                    .clickable { viewModel.themeMode.value = mode }
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF313131)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Account Block
            SettingGroupNeoCard(
                icon = Icons.Outlined.ManageAccounts,
                title = "Account controls",
                isExpanded = isAccountExpanded,
                onClick = { isAccountExpanded = !isAccountExpanded },
                headerColor = Color(0xFFD4EAE4) // Soft mint accent
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NeoButton(
                        onClick = { Toast.makeText(context, "Cloud sync successful! Backup generated. ☁️", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Color(0xFFD4EAE4),
                        shape = RoundedCornerShape(10.dp),
                        shadowOffset = 3.dp,
                        borderWidth = 1.5.dp
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = Color(0xFF313131), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Offline Cooking Backup", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    NeoButton(
                        onClick = { Toast.makeText(context, "Log Out cancelled.", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Color(0xFFF9D3CD),
                        shape = RoundedCornerShape(10.dp),
                        shadowOffset = 3.dp,
                        borderWidth = 1.5.dp
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.Logout, contentDescription = null, tint = Color(0xFF313131), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign Out Chef License", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. About Developer Block
            SettingGroupNeoCard(
                icon = Icons.Outlined.Info,
                title = "About Cookr & License",
                isExpanded = isAboutExpanded,
                onClick = { isAboutExpanded = !isAboutExpanded },
                headerColor = Color(0xFFF5EEFF) // Light lavender
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    // Logo or Title
                    Text("Cookr - Ghibli Cozy Kitchen", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFF313131))
                    Text("Version 2.0 (Studio Premium Edition)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Designed with organic Ghibli-themed backgrounds & sturdy Neo-Brutalist elements to elevate food logging from a chore into a work of cozy art.",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF313131).copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    NeoButton(
                        onClick = { Toast.makeText(context, "Thank you immensely for supporting our cozy team! 🌾❤️", Toast.LENGTH_LONG).show() },
                        modifier = Modifier.fillMaxWidth(0.85f),
                        containerColor = Color(0xFFFEE6A5),
                        shape = RoundedCornerShape(8.dp),
                        shadowOffset = 3.dp,
                        borderWidth = 1.5.dp
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.Coffee, contentDescription = null, tint = Color(0xFF313131), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buy Developer a Mug of Tea 🍵", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(130.dp))
        }
    }
}

@Composable
fun SettingGroupNeoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    isExpanded: Boolean,
    onClick: () -> Unit,
    headerColor: Color,
    content: @Composable () -> Unit
) {
    NeoCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color.White,
        shadowColor = Color(0xFF313131),
        shadowOffset = 4.dp,
        borderWidth = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.5.dp, Color(0xFF313131), RoundedCornerShape(8.dp))
                            .background(headerColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = Color(0xFF313131),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF313131)
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ChevronRight,
                    contentDescription = "Expand info block",
                    tint = Color(0xFF313131),
                    modifier = Modifier.size(24.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                content()
            }
        }
    }
}

@Composable
fun WeeklyProgressTopWidget(userName: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        // Top Row: Avatar and User info
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = com.example.R.drawable.ghibli_chef_1779398968152),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Good morning!", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                    val finalName = if (userName.isBlank()) "Sajibur Rahman" else userName
                    Text(finalName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF313131))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Calendar button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White, CircleShape)
                        .border(1.dp, Color(0xFFE0E0E0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = "Calendar",
                        tint = Color(0xFF313131),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Notification button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White, CircleShape)
                        .border(1.dp, Color(0xFFE0E0E0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFF313131),
                        modifier = Modifier.size(20.dp)
                    )
                    // Green dot
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 8.dp)
                            .size(8.dp)
                            .background(Color(0xFF88D880), CircleShape)
                            .border(1.dp, Color.White, CircleShape)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Progress Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFCDE89C), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FlashOn,
                                contentDescription = "Daily Intake",
                                tint = Color(0xFF8DC92B),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Daily intake", fontSize = 12.sp, color = Color(0xFF4A6836), fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your Weekly\nProgress",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E2F15),
                        lineHeight = 26.sp
                    )
                }

                // Progress Circle
                Box(
                    modifier = Modifier.size(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 14.dp.toPx()
                        if (size.minDimension <= strokeWidth) return@Canvas
                        
                        val radius = (size.minDimension - strokeWidth) / 2f
                        
                        // faint outer track
                        drawCircle(
                            color = Color.White.copy(alpha = 0.5f),
                            radius = radius,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                        )
                        // inner pale green filled circle
                        drawCircle(
                            color = Color(0xFFEAF5D4),
                            radius = radius - (strokeWidth / 2f)
                        )
                        // main progress arc
                        drawArc(
                            color = Color(0xFF8DC92B),
                            startAngle = -90f,
                            sweepAngle = 308f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                            topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2f, strokeWidth / 2f),
                            size = androidx.compose.ui.geometry.Size(size.width - strokeWidth, size.height - strokeWidth)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text("6", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C441E))
                        Text("days", fontSize = 11.sp, color = Color(0xFF4A6836), fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionItemRow(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9F6F0), RoundedCornerShape(12.dp))
            .border(1.5.dp, Color(0xFF313131), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(if (isGranted) Color(0xFFCDE89C) else Color(0xFFE8E4D9), CircleShape)
                    .border(1.5.dp, Color(0xFF313131), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF313131),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF313131)
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (isGranted) {
            Box(
                modifier = Modifier
                    .background(Color(0xFFCDE89C), RoundedCornerShape(8.dp))
                    .border(1.5.dp, Color(0xFF1E2F15), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Active",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E2F15)
                )
            }
        } else {
            NeoButton(
                onClick = onRequest,
                containerColor = Color(0xFFFEE6A5),
                shadowOffset = 2.dp,
                borderWidth = 1.5.dp,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Link",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF313131),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}