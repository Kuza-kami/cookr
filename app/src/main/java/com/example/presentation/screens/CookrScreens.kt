package com.example.presentation.screens

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.CalorieLog
import com.example.domain.model.GroceryItem
import com.example.domain.model.Recipe
import com.example.presentation.AiModelState
import com.example.presentation.CookrThemeSelection
import com.example.presentation.CookrViewModel
import com.example.presentation.AuthState
import com.example.presentation.core.components.NeoButton
import com.example.presentation.core.components.NeoCard
import com.example.presentation.core.components.NeoTextField
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun MainLayout(viewModel: CookrViewModel) {
    val authState by viewModel.authState.collectAsState()
    
    when (authState) {
        AuthState.SPLASH -> SplashScreen(viewModel = viewModel)
        AuthState.AUTHENTICATION -> IntroScreen(onSignUp = { viewModel.authState.value = AuthState.WELCOME })
        AuthState.WELCOME -> WelcomeScreen(viewModel = viewModel)
        AuthState.PROFILE_SETUP -> ProfileSetupScreen(viewModel = viewModel)
        AuthState.AUTHENTICATED -> AppContent(viewModel = viewModel)
        else -> AppContent(viewModel = viewModel)
    }
}
@Composable
fun OnboardingScreen(viewModel: CookrViewModel, onComplete: () -> Unit) {
    // Basic onboarding implementation
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Tell us about you", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        // Removed chef name input as requested
        OutlinedTextField(value = viewModel.calorieGoal.collectAsState().value, onValueChange = { viewModel.calorieGoal.value = it }, label = { Text("Calorie Goal") })
        Button(onClick = onComplete) { Text("Get Started") }
    }
}

@Composable
fun FullScreenIntro(onNext: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary).clickable { onNext() }, contentAlignment = Alignment.Center) {
        Text("FULLSCREEN INTRO", color = Color.White, fontSize = 24.sp)
    }
}

@Composable
fun CookrIntroScreen(onComplete: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.secondary).clickable { onComplete() }, contentAlignment = Alignment.Center) {
        Text("Cookr Introduction", color = Color.White, fontSize = 24.sp)
    }
}

@Composable
fun AppContent(viewModel: CookrViewModel) {
    val themeSel by viewModel.themeSelection.collectAsState()
    var currentTab by remember { mutableStateOf(0) } // 0: Home, 1: Discover, 2: Add, 3: Inbox, 4: Profile
    var activeDetailRecipe by remember { mutableStateOf<Recipe?>(null) }
    var showActiveCooking by remember { mutableStateOf<Recipe?>(null) }
    var showOnboarding by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("Kiki") }
    var dailyCalorieTarget by remember { mutableStateOf(2000) }

    // Toggle onboarding on first run
    LaunchedEffect(Unit) {
        delay(500)
        showOnboarding = true
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Main Content Selection
            Crossfade(targetState = currentTab, label = "tabs") { tab ->
                when (tab) {
                    0 -> HomeScreen(
                        viewModel = viewModel,
                        onRecipeClick = { activeDetailRecipe = it },
                        onProfileClick = { currentTab = 3 }
                    )
                    1 -> DiscoverScreen(
                        viewModel = viewModel,
                        onRecipeClick = { activeDetailRecipe = it }
                    )
                    2 -> PhotoAnalyzerScreen(
                        viewModel = viewModel,
                        onSuccessSave = { currentTab = 1 } // Redirect to Discover to see it!
                    )
                    3 -> ProfileScreen(
                        viewModel = viewModel,
                        userName = userName,
                        onNameChange = { userName = it },
                        dailyCalTarget = dailyCalorieTarget,
                        onTargetChange = { dailyCalorieTarget = it }
                    )
                }
            }

            // Custom FLOATING Bottom Navigation Bar! (Styled exactly like the requested image capsule)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp),
                    shape = RoundedCornerShape(34.dp),
                    color = Color(0xFF121212), // Solid dark black/charcoal capsule
                    shadowElevation = 10.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            Triple(0, "Home", Icons.Outlined.Home),
                            Triple(1, "Discover", Icons.Outlined.Explore),
                            Triple(2, "Add", Icons.Outlined.MotionPhotosOn),
                            Triple(3, "Profile", Icons.Outlined.Person)
                        ).forEach { (index, label, icon) ->
                            val active = currentTab == index
                            if (active) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFEE6A5)) // Beautiful warm Ghibli cream gold accent
                                        .clickable { currentTab = index }
                                        .testTag(if (index == 0) "home_tab" else if (index == 1) "discover_tab" else if (index == 2) "add_recipe_tab" else "profile_tab"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = Color(0xFF121212), // Dark text to stand out in gold circle
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .clickable { currentTab = index }
                                        .testTag(if (index == 0) "home_tab" else if (index == 1) "discover_tab" else if (index == 2) "add_recipe_tab" else "profile_tab"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = Color.White.copy(alpha = 0.6f), // Clean light off-white strokes
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }



            // Active Step-by-Step cooking screen
            if (showActiveCooking != null) {
                ActiveCookingOverlay(
                    recipe = showActiveCooking!!,
                    viewModel = viewModel,
                    onDismiss = { showActiveCooking = null }
                )
            }

            // Recipe Detail screen modal
            if (activeDetailRecipe != null) {
                RecipeDetailOverlay(
                    recipe = activeDetailRecipe!!,
                    viewModel = viewModel,
                    onDismiss = { activeDetailRecipe = null },
                    onStartCooking = { recipe ->
                        activeDetailRecipe = null
                        showActiveCooking = recipe
                    }
                )
            }
        }
    }
}

// Onboarding and User Config settings Overlay Card
@Composable
fun BadgeCapsule(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color, RoundedCornerShape(20.dp))
            .border(2.dp, Color(0xFF313131), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF313131)
        )
    }
}

@Composable
fun IntroScreen(onSignUp: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(true) }

    Scaffold(contentWindowInsets = WindowInsets.systemBars) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFE6E3DB))
                .padding(16.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.82f) // Slightly tighter to match image proportion
                    .align(Alignment.TopCenter),
                shape = RoundedCornerShape(40.dp),
                shadowElevation = 12.dp,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Beautiful cozy forest Ghibli gradient backdrop
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF3F4E24), // Rich Ghibli moss-forest green
                                        Color(0xFF263314)
                                    )
                                )
                            )
                    ) {
                        // Soft elegant warming kitchen fire ambient light shapes
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = Color(0xFFFEE6A5).copy(alpha = 0.16f),
                                radius = 240.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.15f)
                            )
                            drawCircle(
                                color = Color(0xFFC4DF76).copy(alpha = 0.12f),
                                radius = 200.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.75f)
                            )
                        }
                    }
                    
                    // The Overlay Content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(26.dp))

                        // Adorable Ghibli cooking stove warmth welcome logo
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = "Cookr stove logo",
                                tint = Color(0xFFFEE6A5),
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isSignUp) "Create Kitchen Account" else "Welcome Back, Chef!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = if (isSignUp) "Unleash your cozy culinary journey" else "Preheat the stoves and prepare your skillet!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Pill Nav
                        Row(
                            modifier = Modifier
                                .background(Color(0xFF1E280C).copy(alpha = 0.7f), RoundedCornerShape(50.dp))
                                .border(1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(50.dp))
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val activeColor = Color(0xFFC4DF76)
                            
                            Box(modifier = Modifier
                                .background(if (isSignUp) activeColor else Color.Transparent, RoundedCornerShape(50.dp))
                                .padding(horizontal = 24.dp, vertical = 10.dp)
                                .clickable { isSignUp = true }) {
                                Text("SIGN UP", fontWeight = FontWeight.Black, fontSize = 11.sp, color = if (isSignUp) Color(0xFF1E280C) else Color.White)
                            }
                            Box(modifier = Modifier
                                .background(if (!isSignUp) activeColor else Color.Transparent, RoundedCornerShape(50.dp))
                                .padding(horizontal = 24.dp, vertical = 10.dp)
                                .clickable { isSignUp = false }) {
                                Text("LOG IN", fontWeight = FontWeight.Black, fontSize = 11.sp, color = if (!isSignUp) Color(0xFF1E280C) else Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Beautiful, accessible input fields
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            TextField(
                                value = email,
                                onValueChange = { email = it },
                                placeholder = { Text("Your e-mail address", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .border(2.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(28.dp))
                                    .clip(RoundedCornerShape(28.dp)),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Black.copy(alpha = 0.35f),
                                    unfocusedContainerColor = Color.Black.copy(alpha = 0.25f),
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = Color(0xFFC4DF76)
                                ),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "email", tint = Color.White.copy(alpha = 0.8f)) }
                            )

                            TextField(
                                value = password,
                                onValueChange = { password = it },
                                placeholder = { Text(if (isSignUp) "Create secure password" else "Enter kitchen key code", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .border(2.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(28.dp))
                                    .clip(RoundedCornerShape(28.dp)),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Black.copy(alpha = 0.35f),
                                    unfocusedContainerColor = Color.Black.copy(alpha = 0.25f),
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = Color(0xFFC4DF76)
                                ),
                                singleLine = true,
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "password", tint = Color.White.copy(alpha = 0.8f)) }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = onSignUp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .border(2.dp, Color(0xFF313131), RoundedCornerShape(28.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC4DF76)) // Lovely matching Ghibli bright accent green
                        ) {
                            Text(
                                text = if (isSignUp) "SIGN UP" else "LOG IN",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = Color(0xFF1E280C)
                            )
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                    }
                }
            }

            // Social Area & Footer (outside card)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            ) {
                Text("OR SIGN UP WITH", fontWeight = FontWeight.Bold, color = Color(0xFF555555), fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    // Placeholder circular buttons with generic icons (assuming Apple/Facebook/Google logos might need custom drawables)
                    val icons = listOf(Icons.Default.Person, Icons.Default.Share, Icons.Default.Email)
                    icons.forEach { icon ->
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color(0xFF313131), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = icon, contentDescription = "Social", tint = Color.Black, modifier = Modifier.size(24.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text("Terms of use  |  Privacy policy  |  Copyrights", color = Color.Gray, fontSize = 9.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}

// --- HEALTH DASHBOARD COMPONENT ---
@Composable
fun HealthDashboard(score: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Circular Health Score
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
            // Very simplified donut - for actual nice donut you would use a Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = Color.Green,
                    startAngle = 0f,
                    sweepAngle = (score / 10f) * 360f,
                    useCenter = false,
                    style = Stroke(width = 16f)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = score.toString(),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black
                )
                Text("your health score", fontSize = 12.sp, color = Color.Gray)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Systems list placeholder
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Health systems", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Eco, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Endocrine system", fontWeight = FontWeight.SemiBold)
                    LinearProgressIndicator(progress = { 0.83f }, modifier = Modifier.fillMaxWidth())
                }
                Text("8.3", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun OnboardingOverlay(
    name: String,
    onNameChange: (String) -> Unit,
    targetCalories: Int,
    onTargetChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var favoriteStyle by remember { mutableStateOf("Bacon & Eggs") }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        NeoCard(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {},
            containerColor = MaterialTheme.colorScheme.background,
            shadowColor = Color(0xFF313131),
            borderWidth = 3.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                if (step == 1) {
                    // Slide 1: Welcome matching Left Mockup Screen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, Color(0xFF313131), RoundedCornerShape(16.dp))
                    ) {
                        Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ghibli_chef_1779398968152),
                            contentDescription = "Ghibli Chef Cooking",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Pills row: "Cook", "smarter", "not", "harder"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BadgeCapsule("Cook", Color(0xFFB9D7FB))
                        Spacer(modifier = Modifier.width(6.dp))
                        BadgeCapsule("smarter", Color(0xFFE1CEFC))
                        Spacer(modifier = Modifier.width(6.dp))
                        BadgeCapsule("not", Color(0xFFF9D3CD))
                        Spacer(modifier = Modifier.width(6.dp))
                        BadgeCapsule("harder", Color(0xFFDDF29D))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Quick & easy lunch recipes, ready\nin under 30 minutes",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF616161),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Dots row + next button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // dots
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            (1..3).forEach { d ->
                                val active = d == 1
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 3.dp)
                                        .height(8.dp)
                                        .width(if (active) 20.dp else 8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (active) Color(0xFF313131) else Color(0xFF313131).copy(alpha = 0.35f))
                                )
                            }
                        }

                        // Next button stylized like dark capsule in Mockup
                        Button(
                            onClick = { step = 2 },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF313131)),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                            modifier = Modifier.border(2.dp, Color.Black, RoundedCornerShape(20.dp))
                        ) {
                            Text("next", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                } else if (step == 2) {
                    // Slide 2: Name Input
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=300",
                        contentDescription = "Chef profile avatar",
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .border(3.dp, Color(0xFF313131), CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Enter Chef Name 🧑‍🍳",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF313131)
                    )

                    Text(
                        text = "Customize your culinary profile experience",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                    )

                    NeoTextField(
                        value = name,
                        onValueChange = onNameChange,
                        placeholder = { Text("Sophie Hatter, Kiki...") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // dots
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            (1..3).forEach { d ->
                                val active = d == 2
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 3.dp)
                                        .height(8.dp)
                                        .width(if (active) 20.dp else 8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (active) Color(0xFF313131) else Color(0xFF313131).copy(alpha = 0.35f))
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (name.isBlank()) {
                                    Toast.makeText(context, "Please enter a cooking name!", Toast.LENGTH_SHORT).show()
                                } else {
                                    step = 3
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF313131)),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.border(2.dp, Color.Black, RoundedCornerShape(20.dp))
                        ) {
                            Text("next", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                } else {
                    // Slide 3: Calories & Ghibli Favorite Style Design
                    Text(
                        text = "Setup Daily Goals 🎯",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF313131)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Nice to meet you, $name!",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$targetCalories kcal / day",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF313131)
                    )

                    Slider(
                        value = targetCalories.toFloat(),
                        onValueChange = { onTargetChange(it.toInt()) },
                        valueRange = 1200f..3500f,
                        steps = 23,
                        modifier = Modifier.padding(horizontal = 6.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF313131),
                            activeTrackColor = Color(0xFFD4E95A)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Taste Preferences",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF313131)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Bacon & Eggs", "Ham Ramen", "Sweet Bento").forEach { item ->
                            val selected = favoriteStyle == item
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        width = 1.5.dp,
                                        color = Color(0xFF313131),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selected) Color(0xFFD4E95A) else Color.Transparent)
                                    .clickable { favoriteStyle = item }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(item, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF313131))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { step = 2 }) {
                            Text("Back", fontWeight = FontWeight.Black, color = Color(0xFF313131))
                        }

                        NeoButton(
                            onClick = onDismiss,
                            containerColor = Color(0xFFD4E95A),
                            borderWidth = 2.dp,
                            shadowOffset = 4.dp
                        ) {
                            Text("Let's Cook! 🧑‍🍳", fontWeight = FontWeight.Black, color = Color(0xFF313131))
                        }
                    }
                }
            }
        }
    }
}

// ------------------- BROWSE TAB SCREEN -------------------
@Composable
fun BrowseScreen(
    viewModel: CookrViewModel,
    onRecipeClick: (Recipe) -> Unit,
    onProfileClick: () -> Unit
) {
    val recipes by viewModel.allRecipes.collectAsState()
    val themeSel by viewModel.themeSelection.collectAsState()
    var searchTxt by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf("All") }

    val filteredRecipes = recipes.filter {
        (selectedCat == "All" || it.category == selectedCat) &&
                (it.title.lowercase().contains(searchTxt.lowercase()) ||
                        it.description.lowercase().contains(searchTxt.lowercase()))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // App top header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Cookr",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Aesthetic Ghibli Cooking",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Theme toggle selection grid & profile
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Rapid Custom Personalization Theme toggles (Citrus, Forest, Cosmic)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF5722))
                        .border(2.dp, Color.Black, CircleShape)
                        .clickable { viewModel.themeSelection.value = CookrThemeSelection.CITRUS_FUSION }
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2E7D32))
                        .border(2.dp, Color.Black, CircleShape)
                        .clickable { viewModel.themeSelection.value = CookrThemeSelection.FOREST_SAGE }
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7C4DFF))
                        .border(2.dp, Color.Black, CircleShape)
                        .clickable { viewModel.themeSelection.value = CookrThemeSelection.COSMIC_TWILIGHT }
                )

                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier
                        .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .size(36.dp)
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = "Onboarding Profile")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search text field
        NeoTextField(
            value = searchTxt,
            onValueChange = { searchTxt = it },
            placeholder = { Text("Search recipes, ingredients...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Food Categories Horizontal Scroll Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Breakfast", "Quick Meals", "Vegan", "Desserts", "Kids Selection").forEach { cat ->
                val active = selectedCat == cat
                Box(
                    modifier = Modifier
                        .border(
                            width = 2.5.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        .clickable { selectedCat = cat }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = cat,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Custom Pinterest-style Card Lists
        AnimatedVisibility(
            visible = filteredRecipes.isEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No Ghibli dishes match your criteria! Cook another day. 🌾",
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        
        AnimatedVisibility(
            visible = filteredRecipes.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                filteredRecipes.forEach { recipe ->
                    RecipePinterestCard(recipe = recipe, onClick = { onRecipeClick(recipe) }, onHeartToggle = { viewModel.toggleSaveRecipe(recipe) })
                }
            }
        }
    }
}

  // Gorgeous High-contrast Pinterest style Card Layout
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecipePinterestCard(
    recipe: Recipe,
    onClick: () -> Unit,
    onHeartToggle: () -> Unit,
    isTall: Boolean = false
) {
    NeoCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        shadowColor = Color(0xFF313131),
        shadowOffset = 4.dp,
        borderWidth = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Food image at the top with a beautiful rounded corner and thick border, with dynamic height based on isTall
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isTall) 160.dp else 125.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, Color(0xFF313131), RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = recipe.imageUrl,
                    contentDescription = recipe.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Info rows below
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tagText = if (recipe.rating >= 4.7) "Classic" else "Popular"
                val tagColor = if (recipe.rating >= 4.7) Color(0xFFE1CEFC) else Color(0xFFF9D3CD)
                val tagIcon = if (recipe.rating >= 4.7) Icons.Filled.Restaurant else Icons.Filled.Whatshot

                Box(
                    modifier = Modifier
                        .background(tagColor, RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF313131), RoundedCornerShape(10.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(tagIcon, contentDescription = "tag icon", tint = Color(0xFF313131), modifier = Modifier.size(9.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(tagText, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF313131))
                    }
                }

                IconButton(
                    onClick = onHeartToggle,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (recipe.isSaved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Save recipe",
                        tint = if (recipe.isSaved) Color.Red else Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Title
            Text(
                text = recipe.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF313131),
                maxLines = 1
            )

            // Subtitle
            Text(
                text = recipe.description,
                fontSize = 11.sp,
                color = Color.Gray,
                maxLines = 1,
                modifier = Modifier.padding(top = 1.dp, bottom = 6.dp)
            )

            // Badges wrap or column
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Star rating badge
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFEE6A5), RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFF313131), RoundedCornerShape(6.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = "rating", tint = Color(0xFFFFA000), modifier = Modifier.size(8.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(recipe.rating.toString(), fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color(0xFF313131))
                        }
                    }

                    // Duration badge
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE1CEFC), RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFF313131), RoundedCornerShape(6.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AccessTime, contentDescription = "time", tint = Color(0xFF313131), modifier = Modifier.size(8.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(recipe.prepTime, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF313131))
                        }
                    }
                }

                // Level badge
                Box(
                    modifier = Modifier
                        .background(Color(0xFFF9D3CD), RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFF313131), RoundedCornerShape(6.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .wrapContentWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.BarChart, contentDescription = "level", tint = Color(0xFF313131), modifier = Modifier.size(8.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        val levelStr = if (recipe.calories > 450) "Heavy" else "Medium"
                        Text(levelStr, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF313131))
                    }
                }
            }
        }
    }
}

// ------------------- RECIPE DETAIL DIALOG SCREEN OVERLAY -------------------
@Composable
fun RecipeDetailOverlay(
    recipe: Recipe,
    viewModel: CookrViewModel,
    onDismiss: () -> Unit,
    onStartCooking: (Recipe) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    
    var inputServings by remember { mutableStateOf(recipe.servings) }
    var selectedTabByStep by remember { mutableStateOf(0) } // 0: Ingredients, 1: Steps, 2: Nutrition, 3: Reviews
    
    // Tappable star rating state
    var dynamicRating by remember { mutableStateOf(recipe.rating) }
    
    // Timer state for Steps tab
    var cookTimerSeconds by remember { mutableStateOf(recipe.instructions.size * 120) }
    var cookTimerTotalSeconds by remember { mutableStateOf(recipe.instructions.size * 120) }
    var isTimerRunning by remember { mutableStateOf(false) }
    
    // Dialog overlays
    var showPrintPreview by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    var showNfcDialog by remember { mutableStateOf(false) }

    // Reviews local state
    var reviewerName by remember { mutableStateOf("") }
    var reviewText by remember { mutableStateOf("") }
    var reviewRating by remember { mutableStateOf(5) }
    val initialReviews = remember {
        mutableStateListOf(
            "Chihiro Ogino" to "Tasted exactly like the golden buns! Warm, perfectly fluffy structure, highly nostalgic.",
            "Calcifer" to "The fire-roasted touch on the sesame seeds made it perfectly cozy. High heat was key, 5/5 stars!"
        )
    }

    // Ingredients checked state mapping
    val checkedIngredients = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            while (isActive && cookTimerSeconds > 0) {
                delay(1000)
                cookTimerSeconds--
            }
            isTimerRunning = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxHeight(0.92f)
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.background,
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .border(
                    BorderStroke(3.dp, MaterialTheme.colorScheme.outline),
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .clickable(enabled = false) {}
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Massive header image
                Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                    AsyncImage(
                        model = recipe.imageUrl,
                        contentDescription = recipe.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Floating close button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .padding(16.dp)
                            .background(Color.White, CircleShape)
                            .border(2.dp, Color.Black, CircleShape)
                            .size(36.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.Black)
                    }
                }

                Column(modifier = Modifier.padding(20.dp)) {
                    // Title
                    Text(
                        text = recipe.title,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Presented by ${recipe.author}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            (1..5).forEach { star ->
                                val active = dynamicRating.toInt() >= star
                                Icon(
                                    imageVector = if (active) Icons.Filled.Star else Icons.Filled.StarBorder,
                                    contentDescription = "Star",
                                    tint = if (active) Color(0xFFFFD700) else Color.Gray,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clickable {
                                            dynamicRating = star.toFloat()
                                            Toast.makeText(context, "You rated this recipe $star/5 stars! 🌟", Toast.LENGTH_SHORT).show()
                                        }
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("(%.1f)".format(dynamicRating), fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    // --- ACTION BANK: SHARE / SAVE (HEART) / PRINT / QR / NFC ---
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // SAVE
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (recipe.isSaved) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                                .clickable { viewModel.toggleSaveRecipe(recipe) }
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (recipe.isSaved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = "Heart",
                                    tint = if (recipe.isSaved) Color.Red else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(if (recipe.isSaved) "Saved" else "Save", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // PRINT
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable { showPrintPreview = true }
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Print, contentDescription = "Print", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Print", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // QR Share
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable { showQrDialog = true }
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.QrCode, contentDescription = "QR Share", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("QR", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // NFC Beam
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable { showNfcDialog = true }
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Nfc, contentDescription = "NFC Beam", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("NFC", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Card description
                    Text(
                        text = recipe.description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Times & portions row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("PREP", recipe.prepTime, MaterialTheme.colorScheme.surfaceVariant),
                            Triple("COOK", recipe.cookTime, MaterialTheme.colorScheme.surfaceVariant),
                            Triple("CALORIES", "${recipe.calories} kcal", MaterialTheme.colorScheme.secondaryContainer)
                        ).forEach { (lbl, valStr, colorScheme) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                                    .background(colorScheme)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(lbl, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text(valStr, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }

                    // --- FOUR TAB OPTIONS FOR INFO CORE ---
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Ingredients", "Steps & Timers", "Nutrition", "Reviews").forEachIndexed { idx, tabTitle ->
                            val active = selectedTabByStep == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .background(if (active) MaterialTheme.colorScheme.secondary else Color.Transparent)
                                    .clickable { selectedTabByStep = idx }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tabTitle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (active) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Switch tab content
                    when (selectedTabByStep) {
                        0 -> {
                            // --- INGREDIENTS TAB ---
                            // Interactive ingredient checklist progress
                            val totalIngredients = recipe.ingredients.size
                            val checkedCount = checkedIngredients.filter { it.value }.size
                            val completionRatio = if (totalIngredients > 0) checkedCount.toFloat() / totalIngredients.toFloat() else 0f
                            val completionPercentage = (completionRatio * 100).toInt()

                            NeoCard(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                shadowColor = MaterialTheme.colorScheme.outline
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Prep Checklist Progress ✅", fontSize = 12.sp, fontWeight = FontWeight.Black)
                                    Text("You have checked $checkedCount of $totalIngredients ingredients ($completionPercentage%)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { completionRatio },
                                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).border(1.dp, Color.Black, RoundedCornerShape(4.dp)),
                                        color = Color(0xFF4CAF50),
                                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    )
                                }
                            }

                            // Dynamic Ingredients Servings Multiplier Changer!
                            NeoCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                shadowColor = MaterialTheme.colorScheme.outline
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Portion Scaler", fontSize = 12.sp, fontWeight = FontWeight.Black)
                                        Text("Calculates proportions", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { if (inputServings > 1) inputServings-- },
                                            modifier = Modifier
                                                .background(Color.White, CircleShape)
                                                .border(2.dp, Color.Black, CircleShape)
                                                .size(28.dp)
                                        ) {
                                            Icon(Icons.Filled.Remove, contentDescription = "less", modifier = Modifier.size(12.dp), tint = Color.Black)
                                        }
                                        Text("$inputServings", fontSize = 16.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 10.dp))
                                        IconButton(
                                            onClick = { inputServings++ },
                                            modifier = Modifier
                                                .background(Color.White, CircleShape)
                                                .border(2.dp, Color.Black, CircleShape)
                                                .size(28.dp)
                                        ) {
                                            Icon(Icons.Filled.Add, contentDescription = "more", modifier = Modifier.size(12.dp), tint = Color.Black)
                                        }
                                    }
                                }
                            }

                            // Interactive ingredient items list with checkboxes
                            recipe.ingredients.forEach { originalIngredient ->
                                val words = originalIngredient.split(" ")
                                val scaled = try {
                                    val firstNum = words.firstOrNull()?.toDoubleOrNull()
                                    if (firstNum != null) {
                                        val ratio = inputServings.toDouble() / recipe.servings.toDouble()
                                        val newlyScaled = (firstNum * ratio)
                                        val formatted = if (newlyScaled % 1.0 == 0.0) newlyScaled.toInt().toString() else "%.1f".format(newlyScaled)
                                        "$formatted " + words.drop(1).joinToString(" ")
                                    } else {
                                        originalIngredient
                                    }
                                } catch (e: Exception) {
                                    originalIngredient
                                }

                                val isChecked = checkedIngredients[originalIngredient] ?: false
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { checkedIngredients[originalIngredient] = !isChecked }
                                        .padding(vertical = 4.dp)
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checkedIngredients[originalIngredient] = it }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = scaled,
                                        fontSize = 13.sp,
                                        textDecoration = if (isChecked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                        color = if (isChecked) Color.Gray else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Action items imports
                            NeoButton(
                                onClick = {
                                    viewModel.importIngredientsToGroceries(recipe.ingredients)
                                    Toast.makeText(context, "Added ingredients to shopping list!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = MaterialTheme.colorScheme.secondary
                            ) {
                                Icon(Icons.Filled.PostAdd, contentDescription = "Add groceries")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add To Shopping List", fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        1 -> {
                            // --- STEPS TAB ---
                            // Interactive Cooking Timers box
                            val timerProgress = if (cookTimerTotalSeconds > 0) cookTimerSeconds.toFloat() / cookTimerTotalSeconds.toFloat() else 0f
                            NeoCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                shadowColor = MaterialTheme.colorScheme.outline
                            ) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Cook Timer Tracker 🕒", fontSize = 13.sp, fontWeight = FontWeight.Black)
                                        Box(
                                            modifier = Modifier
                                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "%02d:%02d".format(cookTimerSeconds / 60, cookTimerSeconds % 60),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Black,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Real-time wavy timer progress
                                    com.example.presentation.core.components.WavyTimerIndicator(
                                        progress = timerProgress,
                                        modifier = Modifier.fillMaxWidth().height(24.dp),
                                        waveColor = MaterialTheme.colorScheme.primary
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Selection triggers
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(60, 180, 300, 600).forEach { s ->
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                                                    .background(if (cookTimerSeconds == s) MaterialTheme.colorScheme.secondaryContainer else Color.White)
                                                    .clickable { 
                                                        cookTimerSeconds = s
                                                        cookTimerTotalSeconds = s
                                                        isTimerRunning = false 
                                                    }
                                                    .padding(vertical = 4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("${s / 60}m", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        NeoButton(
                                            onClick = { isTimerRunning = !isTimerRunning },
                                            modifier = Modifier.weight(1f),
                                            containerColor = if (isTimerRunning) Color(0xFFE53935) else Color(0xFF4CAF50),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(if (isTimerRunning) "Pause" else "Start", fontWeight = FontWeight.Bold, color = Color.White)
                                        }

                                        NeoButton(
                                            onClick = { 
                                                cookTimerSeconds = recipe.instructions.size * 120
                                                cookTimerTotalSeconds = recipe.instructions.size * 120
                                                isTimerRunning = false 
                                            },
                                            modifier = Modifier.weight(1f),
                                            containerColor = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Reset", fontWeight = FontWeight.Bold, color = Color.Black)
                                        }
                                    }
                                }
                            }

                            // Steps with unique AI illustration stamp decoration on each step
                            recipe.instructions.forEachIndexed { sIdx, step ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(0.72f).padding(end = 8.dp)) {
                                        Text(
                                            text = "Step ${sIdx + 1}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = step,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp
                                        )
                                        
                                        // Specific step timer hijacker button
                                        val stepMinutes = sIdx + 2
                                        NeoButton(
                                            onClick = {
                                                cookTimerSeconds = stepMinutes * 60
                                                cookTimerTotalSeconds = stepMinutes * 60
                                                isTimerRunning = true
                                                Toast.makeText(context, "Timer hijacked for Step ${sIdx + 1}: ${stepMinutes}m! ⏱️", Toast.LENGTH_SHORT).show()
                                            },
                                            containerColor = MaterialTheme.colorScheme.secondary,
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.padding(top = 8.dp),
                                            borderWidth = 1.dp,
                                            shadowOffset = 2.dp
                                        ) {
                                            Text("⏱️ Hijack with $stepMinutes min", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Jp Hand drawn illustration stamp stamp matching Ghibli styles
                                    Box(modifier = Modifier.weight(0.28f), contentAlignment = Alignment.Center) {
                                        AiIllustrationStamp(stepNumber = sIdx + 1)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            NeoButton(
                                onClick = { onStartCooking(recipe) },
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = MaterialTheme.colorScheme.primary
                             ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = "Start Cook instructions")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Interactive Screen Cooking", fontWeight = FontWeight.Bold)
                            }
                        }

                        2 -> {
                            // --- NUTRITION TAB ---
                            val targetRatio = recipe.calories.toFloat() / 2000f
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Spa, contentDescription = "health", tint = Color(0xFF4CAF50))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Macro Tracker Profile ⚖️", fontSize = 15.sp, fontWeight = FontWeight.Black)
                                }
                                Text("Calculated macronutrients for nostalgic dining", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
                                Spacer(modifier = Modifier.height(10.dp))

                                // Calories vs standard
                                Text("Total Energy: ${recipe.calories} kcal of 2000 kcal recommendation", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { targetRatio },
                                    modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)).border(1.5.dp, Color.Black, RoundedCornerShape(6.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                // Macros Grid Row
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Protein
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(1.5.dp, Color.Black, RoundedCornerShape(10.dp))
                                            .background(Color(0xFFE3F2FD))
                                            .padding(8.dp), 
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("PROTEIN", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Blue)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${recipe.protein}g", fontSize = 15.sp, fontWeight = FontWeight.Black)
                                        Text("Muscle Fuel", fontSize = 9.sp, color = Color.Gray)
                                    }
                                    // Carbs
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(1.5.dp, Color.Black, RoundedCornerShape(10.dp))
                                            .background(Color(0xFFFFF3E0))
                                            .padding(8.dp), 
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("CARBS", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFFE65100))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${recipe.carbs}g", fontSize = 15.sp, fontWeight = FontWeight.Black)
                                        Text("Energy Spark", fontSize = 9.sp, color = Color.Gray)
                                    }
                                    // Fat
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(1.5.dp, Color.Black, RoundedCornerShape(10.dp))
                                            .background(Color(0xFFE8F5E9))
                                            .padding(8.dp), 
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("FAT", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF1B5E20))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${recipe.fat}g", fontSize = 15.sp, fontWeight = FontWeight.Black)
                                        Text("Soul Warmth", fontSize = 9.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }

                        3 -> {
                            // --- REVIEWS TAB ---
                            // Write review inputs form
                            NeoCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                shadowColor = MaterialTheme.colorScheme.outline
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Submit Community Review ✒️", fontSize = 13.sp, fontWeight = FontWeight.Black)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    NeoTextField(
                                        value = reviewerName,
                                        onValueChange = { reviewerName = it },
                                        placeholder = { Text("Your name (Sophie Hatter, etc.)", fontSize = 11.sp) }
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    NeoTextField(
                                        value = reviewText,
                                        onValueChange = { reviewText = it },
                                        placeholder = { Text("Describe details of the culinary finish...", fontSize = 11.sp) }
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Interactive Star rating selectors
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Score Rating:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        (1..5).forEach { star ->
                                            IconButton(
                                                onClick = { reviewRating = star },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (reviewRating >= star) Icons.Filled.Star else Icons.Filled.StarBorder,
                                                    contentDescription = "rating selectors",
                                                    tint = if (reviewRating >= star) Color(0xFFFFD700) else Color.Gray,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    NeoButton(
                                        onClick = {
                                            if (reviewerName.isNotBlank() && reviewText.isNotBlank()) {
                                                initialReviews.add(0, Pair(reviewerName, reviewText))
                                                reviewerName = ""
                                                reviewText = ""
                                                Toast.makeText(context, "Review uploaded successfully! 🧑‍🍳", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Please fill out all fields!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Submit Review to Syndicate Feed", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Dynamic reviews list
                            for (pair in initialReviews) {
                                val usr = pair.first
                                val review = pair.second
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                                        .background(Color.White)
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.secondaryContainer)
                                            .border(1.dp, Color.Black, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(usr.take(1).uppercase(), fontSize = 14.sp, fontWeight = FontWeight.Black)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(usr, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                            Row {
                                                (1..5).forEach { _ ->
                                                    Icon(Icons.Filled.Star, contentDescription = "star", tint = Color(0xFFFFD700), modifier = Modifier.size(10.dp))
                                                }
                                            }
                                        }
                                        Text(review, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline, lineHeight = 15.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }

        // --- RETRO PARCHMENT RECEIPT MOCK PRINT PREVIEW DIALOG ---
        if (showPrintPreview) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .clickable { showPrintPreview = false }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                NeoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clickable(enabled = false) {},
                    containerColor = Color(0xFFFFF9C4), // Vintage yellow parchment paper
                    shadowColor = Color.Black
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "== COOKR RECIPE REPORT == ",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "STORY DELICATESSEN LTD",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color.Black
                        )
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "-------------------------",
                            fontFamily = FontFamily.Monospace,
                            color = Color.Black
                        )
                        
                        Text(
                            text = recipe.title.uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Black
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "PREP TIME: ${recipe.prepTime}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color.Black,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Text(
                            text = "COOK TIME: ${recipe.cookTime}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color.Black,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Text(
                            text = "SERVINGS : $inputServings PORTION",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color.Black,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Text(
                            text = "-------------------------",
                            fontFamily = FontFamily.Monospace,
                            color = Color.Black
                        )

                        Text(
                            text = "INGREDIENTS CHECKLIST:",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.Start),
                            color = Color.Black
                        )

                        recipe.ingredients.forEach { ing ->
                            Text(
                                text = "- [ ] $ing",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                modifier = Modifier.align(Alignment.Start),
                                color = Color.Black
                            )
                        }

                        Text(
                            text = "-------------------------",
                            fontFamily = FontFamily.Monospace,
                            color = Color.Black
                        )

                        Text(
                            text = "THANK YOU FOR COOKING!",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.Black
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = { showPrintPreview = false }) {
                                Text("Cancel", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                            NeoButton(
                                onClick = {
                                    Toast.makeText(context, "Transmitting data to local Ghibli Printer... 📠", Toast.LENGTH_LONG).show()
                                    showPrintPreview = false
                                },
                                containerColor = Color.White
                            ) {
                                Text("Print Now", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // --- MOCK QR CODE OVERLAY ---
        if (showQrDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .clickable { showQrDialog = false }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                NeoCard(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .wrapContentHeight()
                        .clickable(enabled = false) {},
                    containerColor = MaterialTheme.colorScheme.background,
                    shadowColor = Color.Black
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Ghibli QR Syndicate Share", fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Text("Hold camera near matrix to import into another Cookr instance", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))
                        
                        // Fake QR Code Matrix
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                (1..8).forEach { r ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        (1..8).forEach { c ->
                                            val filled = (r + c) % 3 == 0 || (r == 1 && c == 1) || (r == 1 && c == 8) || (r == 8 && c == 1) || (r in 5..6 && c in 5..6)
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .background(if (filled) Color.Black else Color.White)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        NeoButton(
                            onClick = { showQrDialog = false },
                            containerColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Dismiss QR Link", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- MOCK NFC BEAM OVERLAY ---
        if (showNfcDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .clickable { showNfcDialog = false }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                NeoCard(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .wrapContentHeight()
                        .clickable(enabled = false) {},
                    containerColor = MaterialTheme.colorScheme.background,
                    shadowColor = Color.Black
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Nfc,
                            contentDescription = "NFC active",
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("NFC Sharing Transceiver Active", fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Text(
                            text = "Ghibli Beam wave is active for \"${recipe.title}\"! Hold devices spine-to-spine to automatically beam ingredients.",
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                        )
                        
                        // Wave animation visualization
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            listOf(0.4f, 0.7f, 1f, 0.7f, 0.4f).forEach { op ->
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = op))
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        NeoButton(
                            onClick = { showNfcDialog = false },
                            containerColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Deactivate NFC Beam", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Retro Styled Woodblock Wooden cut Stamp for step illustrations
@Composable
fun AiIllustrationStamp(stepNumber: Int) {
    Box(
        modifier = Modifier
            .size(70.dp)
            .padding(4.dp)
            .rotate(-5f + (stepNumber * 3) % 11f)
            .border(2.dp, Color(0xFFC62828).copy(alpha = 0.8f), RoundedCornerShape(8.dp))
            .background(Color(0xFFFFEBEE).copy(alpha = 0.5f))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.5.dp, Color(0xFFC62828).copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "ステップ",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC62828),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "$stepNumber",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFC62828),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "AI印 ✨",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC62828).copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ------------------- ACTIVE COOK MODE DIALOG OVERLAY (VOICE/TIMERS/ANIMATIONS) -------------------
@Composable
fun ActiveCookingOverlay(
    recipe: Recipe,
    viewModel: CookrViewModel,
    onDismiss: () -> Unit
) {
    val steps = recipe.instructions
    var currentIndex by remember { mutableStateOf(0) }
    var totalDragX by remember { mutableStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Timers
    val timerRemaining by viewModel.timerRemainingSeconds.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val timerTotal by viewModel.timerTotalSeconds.collectAsState()

    // Protected current step text
    val currentStepText = remember(currentIndex, steps) {
        if (currentIndex in steps.indices) steps[currentIndex] else ""
    }

    // Narrate instruction out loud when step changes safely
    LaunchedEffect(currentIndex, currentStepText) {
        if (currentStepText.isNotEmpty()) {
            viewModel.speakInstruction(currentStepText)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss, modifier = Modifier.border(2.dp, Color.Black, CircleShape).size(36.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Active Kitchen Instruction",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
                Text(
                    text = "Step ${currentIndex + 1} / ${steps.size}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Ghibli cozy step illustration card
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.42f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                AsyncImage(
                    model = recipe.imageUrl,
                    contentDescription = "Current Step illustration image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .border(3.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                // TTS Speaker quick trigger
                IconButton(
                    onClick = { if (currentStepText.isNotEmpty()) viewModel.speakInstruction(currentStepText) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .background(Color.White, CircleShape)
                        .border(1.5.dp, Color.Black, CircleShape)
                        .size(40.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Speaker TTS", tint = Color.Black)
                }
            }

            // Instruction content card (neo-brutalist)
            NeoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 12.dp)
                    .pointerInput(steps) {
                        // Gesture controls integration: swipe left to advance, swipe right to retreat on release
                        detectDragGestures(
                            onDragEnd = {
                                if (totalDragX < -150f) {
                                    if (currentIndex < steps.size - 1) {
                                        currentIndex++
                                    }
                                } else if (totalDragX > 150f) {
                                    if (currentIndex > 0) {
                                        currentIndex--
                                    }
                                }
                                totalDragX = 0f
                            },
                            onDragCancel = {
                                totalDragX = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                totalDragX += dragAmount.x
                            }
                        )
                    },
                containerColor = MaterialTheme.colorScheme.surface,
                shadowOffset = 5.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentStepText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 26.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "👈 Swipe LEFT to Next | Swipe RIGHT to Previous 👉",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Interactive Cooking Timer widget
            if (timerRemaining > 0) {
                NeoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    shadowColor = MaterialTheme.colorScheme.outline
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val min = timerRemaining / 60
                            val sec = timerRemaining % 60
                            Text("Active Process Timer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("%02d:%02d".format(min, sec), fontSize = 28.sp, fontWeight = FontWeight.Black)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = {
                                    if (isTimerRunning) viewModel.pauseTimer() else viewModel.resumeTimer()
                                },
                                modifier = Modifier
                                    .background(Color.White, CircleShape)
                                    .border(1.5.dp, Color.Black, CircleShape)
                                    .size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isTimerRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = "play-pause timer",
                                    tint = Color.Black
                                )
                            }
                            IconButton(
                                onClick = { viewModel.resetTimer() },
                                modifier = Modifier
                                    .background(Color.White, CircleShape)
                                    .border(1.5.dp, Color.Black, CircleShape)
                                    .size(36.dp)
                            ) {
                                Icon(Icons.Filled.Refresh, contentDescription = "ref", tint = Color.Black)
                            }
                        }
                    }
                }
            } else {
                // Timer Quick launchers based on step instructions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("1 Min Timer" to 60, "3 Min Timer" to 180, "5 Min Timer" to 300).forEach { pair ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .clickable { viewModel.startStepTimer(pair.second) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(pair.first, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Action navigation footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ElevatedButton(
                    onClick = { if (currentIndex > 0) currentIndex-- },
                    enabled = currentIndex > 0,
                    modifier = Modifier.border(2.dp, Color.Black, RoundedCornerShape(12.dp))
                ) {
                    Text("Previous")
                }

                if (currentIndex == steps.size - 1) {
                    NeoButton(
                        onClick = {
                            viewModel.logCalorieMeal(recipe.title, recipe.calories, recipe.protein, recipe.carbs, recipe.fat)
                            // Clean completion toast and exit
                            Toast.makeText(context, "Dish Completed! Logged ${recipe.calories} kcal! 🏆", Toast.LENGTH_LONG).show()
                            onDismiss()
                        },
                        containerColor = Color(0xFF4CAF50)
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Complete")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Finish Cooking & Log Meal", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {
                    ElevatedButton(
                        onClick = { if (currentIndex < steps.size - 1) currentIndex++ },
                        enabled = currentIndex < steps.size - 1,
                        modifier = Modifier.border(2.dp, Color.Black, RoundedCornerShape(12.dp))
                    ) {
                        Text("Next Step")
                    }
                }
            }
        }
    }
}

// ------------------- AI CHEF TAB SCREEN (GENERATOR + PHOTO RECON + CALS TRACKER) -------------------
@Composable
fun AiChefScreen(
    viewModel: CookrViewModel,
    dailyCalTarget: Int,
    onRecipeClick: (Recipe) -> Unit
) {
    val aiState by viewModel.aiModelState.collectAsState()
    val genRecipe by viewModel.generatedRecipe.collectAsState()
    val calorieLogs by viewModel.calorieLogs.collectAsState()
    val context = LocalContext.current

    // Local controller states
    var textInputState by remember { mutableStateOf("") }
    var subTabSelector by remember { mutableStateOf(0) } // 0: AI Generator, 1: Scan camera, 2: Health Logger

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "AI Chef Kitchen Core",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Automated Recipe generation & wellness logs",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Three sub tab switchers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("AI Generator", "Fridge Scanner", "Daily Logging").forEachIndexed { index, title ->
                val active = subTabSelector == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { subTabSelector = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        title,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (subTabSelector) {
            0 -> {
                // --- RECIPE GENERATOR SUB VIEW ---
                Text("What are you craving today?", fontSize = 14.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(8.dp))

                NeoTextField(
                    value = textInputState,
                    onValueChange = { textInputState = it },
                    placeholder = { Text("E.g. Sweet organic baking with honey and orange wedges", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Prebuilt prompt suggest pills
                Text("Try suggestion triggers:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Sweet lemon cake", "Spicy hot ramen miso", "Healthy green tofu soup").forEach { sug ->
                        Box(
                            modifier = Modifier
                                .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { textInputState = sug }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(sug, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Call VM to generate
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (aiState == AiModelState.LOADING) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("AI Cooking generator heating the frying pan...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        NeoButton(
                            onClick = {
                                if (textInputState.isNotEmpty()) {
                                    viewModel.generateAiRecipe(textInputState)
                                } else {
                                    Toast.makeText(context, "Write your crave prompt first!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = "Magic")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate AI Ghibli Dish", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Gen results
                if (genRecipe != null && aiState == AiModelState.SUCCESS) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Generated masterpiece:", fontSize = 14.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(10.dp))
                    RecipePinterestCard(
                        recipe = genRecipe!!,
                        onClick = { onRecipeClick(genRecipe!!) },
                        onHeartToggle = { viewModel.toggleSaveRecipe(genRecipe!!) }
                    )
                }
            }
            1 -> {
                // --- FRIDGE DETECT CAMERA SCANNING SIMULATOR ---
                IngredientCameraScannerSimulator(viewModel, onRecipeResult = { recipe ->
                    viewModel.generatedRecipe.value = recipe
                    viewModel.aiModelState.value = AiModelState.SUCCESS
                    subTabSelector = 0 // Navigate back to view results
                })
            }
            2 -> {
                // --- CALORIE & WELLNESS LOG SHEET Tracker ---
                LogDailyCalorieTracker(viewModel, dailyCalTarget)
            }
        }
    }
}

// INGREDIENTS SCANNER SIMULATOR VIEW (SIMULATED DYNAMIC CAMERA SELECTIONS)
@Composable
fun IngredientCameraScannerSimulator(viewModel: CookrViewModel, onRecipeResult: (Recipe) -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var stepScan by remember { mutableStateOf(1) } // 1: Snap frame, 2: Check box results, 3: Success recommendations
    var loadingScan by remember { mutableStateOf(false) }

    val scanIngredients = remember {
        mutableStateListOf(
            "Fresh wild mushrooms" to true,
            "Thick butter segment" to true,
            "Heritage duck eggs" to false,
            "Pork belly bacon" to false,
            "Ripe sweet yellow peaches" to false
        )
    }

    NeoCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        shadowColor = MaterialTheme.colorScheme.outline
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (stepScan == 1) {
                Text("AI Ingredient Camera Search", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Search your counter for matching recipes", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)

                Spacer(modifier = Modifier.height(16.dp))

                // Simulated Viewfinder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(3.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=600",
                        contentDescription = "Simulated Camera counter",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Target scanning box overlay
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .border(width = 2.dp, color = Color.White, shape = RoundedCornerShape(8.dp))
                            .align(Alignment.Center)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (loadingScan) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                } else {
                    NeoButton(onClick = {
                        loadingScan = true
                        coroutineScope.launch {
                            delay(1500)
                            loadingScan = false
                            stepScan = 2
                        }
                    }) {
                        Icon(Icons.Filled.Camera, contentDescription = "Cam")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simulate Ingredient Snap Shot", fontWeight = FontWeight.ExtraBold)
                    }
                }
            } else {
                Text("Scan Complete! Matches Found", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Check ingredients to generate a custom dish:", fontSize = 11.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Column {
                    scanIngredients.forEachIndexed { index, pair ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scanIngredients[index] = pair.first to !pair.second
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(checked = pair.second, onCheckedChange = {
                                scanIngredients[index] = pair.first to it
                            })
                            Text(pair.first, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                NeoButton(
                    onClick = {
                        val activeIngs = scanIngredients.filter { it.second }.map { it.first }
                        val prompt = "Recipe using these ingredients: " + activeIngs.joinToString(", ")
                        viewModel.generateAiRecipe(prompt)
                        stepScan = 1 // Reset for next time
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = "magic search")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm Ingredients Selection", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

// LOG DAILY WELLNESS AND CALORIE PROGRESS RING METER
@Composable
fun LogDailyCalorieTracker(viewModel: CookrViewModel, dailyCalTarget: Int) {
    val logs by viewModel.calorieLogs.collectAsState()

    // Calculate dynamic totals
    val totalCalories = logs.sumOf { it.calories }
    val totalProtein = logs.sumOf { it.protein }
    val totalCarbs = logs.sumOf { it.carbs }
    val totalFat = logs.sumOf { it.fat }

    // Dynamic AI Health Nudge derived from current consumption ratios
    val currentNudgeText = viewModel.getHealthNudge(totalCalories, totalProtein, totalCarbs, totalFat)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Today's Metabolic Intake", fontSize = 14.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(10.dp))

        // Large Circle Progress Tracker Metric
        NeoCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            shadowOffset = 5.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Interactive clean ring meter drawing using custom canvas
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(100.dp)) {
                        val stroke = Stroke(width = 12.dp.toPx())
                        val pct = if (totalCalories > 0) totalCalories.toFloat() / dailyCalTarget.toFloat() else 0f
                        // Draw empty gray bg path
                        drawArc(
                            color = Color.LightGray.copy(alpha = 0.4f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = stroke
                        )
                        // Draw progress sweep arc path
                        drawArc(
                            color = Color(0xFFFF5722), // Citrus/brutalist Orange Glow accent
                            startAngle = -90f,
                            sweepAngle = (pct * 360f).coerceAtMost(360f),
                            useCenter = false,
                            style = stroke
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$totalCalories", fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Text("/ $dailyCalTarget", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("kcal", fontSize = 9.sp, color = Color.Gray)
                    }
                }

                // Breakdown text metrics (protein, sugar, carbs)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("🍖 Protein: ${totalProtein}g", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("🌾 Carbs: ${totalCarbs}g", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("🥑 Fats: ${totalFat}g", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // AI Health Nudges Block Card (Studio-ghibli cozy tip style)
        Box(
            modifier = Modifier
                .border(2.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.CardGiftcard,
                    contentDescription = "gift icon nudge",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("AI Chef Health Nudge:", fontSize = 12.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    Text(currentNudgeText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, lineHeight = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Meal logging forms
        Text("Quick Log meals snippets", fontSize = 13.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                Triple("Log Bacon & Eggs", 680, 32),
                Triple("Log Ham Ramen", 540, 24),
                Triple("Log Sweet Bento", 420, 18)
            ).forEach { tuple ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { viewModel.logCalorieMeal(tuple.first, tuple.second, tuple.third, 45, 12) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(tuple.first, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        Text("+${tuple.second} kcal", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Current Logs item listing
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Logged Items History", fontSize = 13.sp, fontWeight = FontWeight.Black)
            TextButton(onClick = { viewModel.clearAllCalorieLogs() }) {
                Text("Clear All", fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }

        if (logs.isEmpty()) {
            Text("No food items logged for today.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 10.dp))
        } else {
            logs.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(item.foodName, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        Text("🥩 Protein: ${item.protein}g | Carbs: ${item.carbs}g", fontSize = 10.sp, color = Color.DarkGray)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("+${item.calories} kcal", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { viewModel.deleteCalorieLog(item.id) }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Filled.Delete, contentDescription = "delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// ------------------- GROCERY CHECKLIST SHOPPING TAB SCREEN -------------------
@Composable
fun GroceryListScreen(viewModel: CookrViewModel) {
    val items by viewModel.groceryItems.collectAsState()
    var inputItemName by remember { mutableStateOf("") }
    var inputAmount by remember { mutableStateOf("") }
    var inputUnit by remember { mutableStateOf("qty") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Shopping Checklist",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Keep track of items needed for active recipes",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Direct Checklist item insertion
        NeoCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            shadowOffset = 4.dp
        ) {
            Column {
                Text("Insert manual item to checklist", fontSize = 12.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(10.dp))

                NeoTextField(
                    value = inputItemName,
                    onValueChange = { inputItemName = it },
                    placeholder = { Text("E.g. Sweet white onion, organic flour cubes", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NeoTextField(
                        value = inputAmount,
                        onValueChange = { inputAmount = it },
                        placeholder = { Text("Amount, e.g. 2", fontSize = 12.sp) },
                        modifier = Modifier.weight(1.5f)
                    )

                    NeoTextField(
                        value = inputUnit,
                        onValueChange = { inputUnit = it },
                        placeholder = { Text("E.g. g, cup, pcs", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                NeoButton(
                    onClick = {
                        if (inputItemName.isNotEmpty()) {
                            val amt = inputAmount.toDoubleOrNull() ?: 1.0
                            viewModel.addGroceryItem(inputItemName, amt, inputUnit.ifEmpty { "qty" })
                            inputItemName = ""
                            inputAmount = ""
                            inputUnit = "qty"
                        } else {
                            Toast.makeText(context, "Item title cannot be blank!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Filled.AddCircle, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Grocery Item", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Checklist listing view
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Your Checklist Items (${items.size})", fontSize = 14.sp, fontWeight = FontWeight.Black)
            TextButton(onClick = { viewModel.clearGroceryList() }) {
                Text("Clear All Items", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No checklist items! Grab recipes above to auto populate. 🧺",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            items.forEach { grocery ->
                val lineThrough = if (grocery.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                val op = if (grocery.isCompleted) 0.5f else 1.0f

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                        .background(if (grocery.isCompleted) MaterialTheme.colorScheme.background else Color.White)
                        .clickable { viewModel.toggleGroceryItemCompletion(grocery) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Checkbox(
                            checked = grocery.isCompleted,
                            onCheckedChange = { viewModel.toggleGroceryItemCompletion(grocery) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = grocery.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textDecoration = lineThrough,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = op)
                            )
                            Text(
                                text = "${grocery.amount} ${grocery.unit}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    IconButton(onClick = { viewModel.deleteGroceryItem(grocery.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete Item", tint = Color.Red)
                    }
                }
            }
        }
    }
}

// ------------------- COMMUNITY TAB SCREEN (RATINGS / FEEDS / COMMISSION TRACKER) -------------------
@Composable
fun CommunityScreen(viewModel: CookrViewModel, onRecipeClick: (Recipe) -> Unit) {
    val recipes by viewModel.allRecipes.collectAsState()
    val submissionVotesVal by viewModel.submissionVotes.collectAsState()
    val context = LocalContext.current

    // Local submissions creator commissions metrics
    val premiumRatedCount = recipes.filter { it.rating >= 4.8f }.size
    val premiumCommissionsGained = premiumRatedCount * 12.50

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Cookr Community Social",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Creator community, rankings & reward commissions",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // commission metric reward card
        NeoCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            shadowColor = MaterialTheme.colorScheme.outline
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.MonetizationOn, contentDescription = "money badge", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Your Commissions Pool Tracker", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "As recipes gain 5-star rating benchmarks, creators earn commissions on syndicated affiliate sales on portals.",
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("SYNDICATES RATE", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("$premiumRatedCount Dishes Elite", fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }
                    Column {
                        Text("ESTIMATED FEES EARNED", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("$%.2f".format(premiumCommissionsGained), fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF4CAF50))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Food Photography uploads rankings row
        Text("Meal Photography Rankings Feed", fontSize = 14.sp, fontWeight = FontWeight.Black)
        Text("Stir the community! Rate & upvote beautiful meal finishes", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)

        Spacer(modifier = Modifier.height(10.dp))

        // Community ranking items list cards
        recipes.forEach { recipe ->
            val additionalVotes = submissionVotesVal[recipe.id] ?: 0
            val votingSum = recipe.photoRank + additionalVotes

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .border(2.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .clickable { onRecipeClick(recipe) }
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    AsyncImage(
                        model = recipe.imageUrl,
                        contentDescription = recipe.title,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.5.dp, Color.Black, RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(recipe.title, fontSize = 12.sp, fontWeight = FontWeight.Black, maxLines = 1)
                        Text("Uploaded by creative ${recipe.author}", fontSize = 10.sp, color = Color.Gray)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.ThumbUp, contentDescription = "vote count", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$votingSum photography upvotes", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Upvote button
                IconButton(
                    onClick = {
                        viewModel.upvotePhoto(recipe.id)
                        Toast.makeText(context, "Upvoted submission photography!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        .border(1.dp, Color.Black, CircleShape)
                        .size(32.dp)
                ) {
                    Icon(Icons.Filled.ThumbUp, contentDescription = "Vote Up", tint = Color.Black, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

// Helper Composable for each card in the swiper deck
@Composable
fun SwipeCardContent(
    recipe: Recipe,
    onSelect: (() -> Unit)? = null
) {
    NeoCard(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        shadowColor = null,
        borderWidth = 3.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().weight(0.55f)) {
                AsyncImage(
                    model = recipe.imageUrl,
                    contentDescription = recipe.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Rating stamp badge
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                        .border(1.5.dp, Color.Black, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Star, contentDescription = "rating", tint = Color(0xFFFFD700), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(recipe.rating.toString(), fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Black)
                }
            }
            
            Column(modifier = Modifier.weight(0.45f).padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(recipe.title, fontSize = 15.sp, fontWeight = FontWeight.Black, maxLines = 1)
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(recipe.description, fontSize = 11.sp, color = Color.Gray, maxLines = 2, lineHeight = 14.sp)
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Prep: ${recipe.prepTime} | ${recipe.calories} kcal", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    
                    if (onSelect != null) {
                        NeoButton(
                            onClick = onSelect,
                            containerColor = MaterialTheme.colorScheme.secondary,
                            shape = RoundedCornerShape(6.dp),
                            borderWidth = 1.dp,
                            shadowOffset = 2.dp
                        ) {
                            Text("Open card", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ------------------- HOME SCREEN -------------------
@Composable
fun HomeScreen(
    viewModel: CookrViewModel,
    onRecipeClick: (Recipe) -> Unit,
    onProfileClick: () -> Unit
) {
    val context = LocalContext.current
    val recipes by viewModel.allRecipes.collectAsState()
    var selectedCat by remember { mutableStateOf("All") }

    // Swipe Card Deck state
    val swipeRecipes = remember(recipes) { recipes.take(5) }
    var activeSwipeIndex by remember { mutableStateOf(0) }
    var dragXOffset by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()

    // Find a featured recipe for the AI Recommendation hero card
    val featuredRecipe = remember(recipes) {
        recipes.firstOrNull { it.rating >= 4.8f } ?: recipes.firstOrNull()
    }

    val filteredRecipes = recipes.filter {
        selectedCat == "All" || it.category == selectedCat
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // App top header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Cookr",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Aesthetic Ghibli Cooking",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = onProfileClick,
                modifier = Modifier
                    .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .size(36.dp)
            ) {
                Icon(Icons.Filled.Settings, contentDescription = "Profile Settings")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- BENTO BOX WIDGETS ---
        Row(
            modifier = Modifier.fillMaxWidth().height(140.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Widget 1: Timer & Progress
            Surface(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Icon(Icons.Filled.Timer, contentDescription = "Timer", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    Column {
                        Text("Active Timer", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                        Text("12:45", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        com.example.presentation.core.components.WavyTimerIndicator(
                            progress = 0.6f,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(12.dp),
                            waveColor = MaterialTheme.colorScheme.primary,
                            waveAmplitude = 3f
                        )
                    }
                }
            }
            
            // Widget 2: Daily Ideas & Streak
            Surface(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Icon(Icons.Filled.LocalDining, contentDescription = "Idea", tint = MaterialTheme.colorScheme.secondary)
                        Box(modifier = Modifier.background(MaterialTheme.colorScheme.secondary, CircleShape).padding(horizontal = 8.dp, vertical = 2.dp)) {
                            Text("New", fontSize=10.sp, color=MaterialTheme.colorScheme.onSecondary)
                        }
                    }
                    Column {
                        Text("Meal Ideas", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text("3 suggested", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- AI RECOMMENDATION HERO CARD ---
        featuredRecipe?.let { recipe ->
            NeoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                shadowColor = null,
                borderWidth = 3.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "AI Choice of the Day",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        Row {
                            Icon(Icons.Filled.Star, contentDescription = "rating", tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(recipe.rating.toString(), fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = recipe.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = recipe.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(6.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(recipe.prepTime, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("${recipe.calories} kcal", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        NeoButton(
                            onClick = { onRecipeClick(recipe) },
                            containerColor = MaterialTheme.colorScheme.secondary,
                            shadowOffset = 3.dp,
                            borderWidth = 2.dp,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("View Recipe", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Filled.Restaurant, contentDescription = null, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }

        // --- SWIPE CARD DECK MULTI-LAYER HERO ---
        Text(
            text = "Culinary Matcher 🪄",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Swipe right to SAVE, swipe left to SKIP recommendations!",
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (activeSwipeIndex < swipeRecipes.size) {
                // Layer 3 (Backmost): index + 2
                val nextNextIndex = activeSwipeIndex + 2
                if (nextNextIndex < swipeRecipes.size) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(290.dp)
                            .graphicsLayer {
                                translationY = 24f // standard pixel scale
                                scaleX = 0.9f
                                scaleY = 0.9f
                                alpha = 0.4f
                            }
                    ) {
                        SwipeCardContent(recipe = swipeRecipes[nextNextIndex])
                    }
                }

                // Layer 2 (Middle): index + 1
                val nextIndex = activeSwipeIndex + 1
                if (nextIndex < swipeRecipes.size) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(290.dp)
                            .graphicsLayer {
                                translationY = 12f // standard pixel scale
                                scaleX = 0.95f
                                scaleY = 0.95f
                                alpha = 0.8f
                            }
                    ) {
                        SwipeCardContent(recipe = swipeRecipes[nextIndex])
                    }
                }

                // Layer 1 (Frontmost Active card): activeSwipeIndex
                val currentSwipeRecipe = swipeRecipes[activeSwipeIndex]

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp)
                        .graphicsLayer {
                            translationX = dragXOffset
                            rotationZ = dragXOffset / 22f
                        }
                        .pointerInput(activeSwipeIndex) {
                            detectDragGestures(
                                onDragStart = {
                                    dragXOffset = 0f
                                },
                                onDragEnd = {
                                    val finalDragX = dragXOffset
                                    scope.launch {
                                        if (finalDragX > 300f) {
                                            val anim = androidx.compose.animation.core.Animatable(finalDragX)
                                            viewModel.toggleSaveRecipe(currentSwipeRecipe)
                                            Toast.makeText(context, "Saved \"${currentSwipeRecipe.title}\"! ❤️", Toast.LENGTH_SHORT).show()
                                            anim.animateTo(1200f) {
                                                dragXOffset = this.value
                                            }
                                            activeSwipeIndex++
                                            dragXOffset = 0f
                                        } else if (finalDragX < -300f) {
                                            val anim = androidx.compose.animation.core.Animatable(finalDragX)
                                            Toast.makeText(context, "Skipped recommendation. 💨", Toast.LENGTH_SHORT).show()
                                            anim.animateTo(-1200f) {
                                                dragXOffset = this.value
                                            }
                                            activeSwipeIndex++
                                            dragXOffset = 0f
                                        } else {
                                            val anim = androidx.compose.animation.core.Animatable(finalDragX)
                                            anim.animateTo(0f, androidx.compose.animation.core.spring(dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy)) {
                                                dragXOffset = this.value
                                            }
                                        }
                                    }
                                },
                                onDragCancel = {
                                    val finalDragX = dragXOffset
                                    scope.launch {
                                        val anim = androidx.compose.animation.core.Animatable(finalDragX)
                                        anim.animateTo(0f) {
                                            dragXOffset = this.value
                                        }
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragXOffset += dragAmount.x
                                }
                            )
                        }
                ) {
                    SwipeCardContent(
                        recipe = currentSwipeRecipe,
                        onSelect = { onRecipeClick(currentSwipeRecipe) }
                    )

                    // Drag status stamp indicators
                    val dragVal = dragXOffset
                    if (dragVal > 60f) {
                        val alphaVal = (dragVal / 250f).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(20.dp)
                                .rotate(-12f)
                                .border(3.5.dp, Color(0xFF4CAF50), RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.95f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .graphicsLayer { alpha = alphaVal }
                        ) {
                            Text(
                                "SAVE",
                                color = Color(0xFF4CAF50),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    } else if (dragVal < -60f) {
                        val alphaVal = (-dragVal / 250f).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(20.dp)
                                .rotate(12f)
                                .border(3.5.dp, Color(0xFFE53935), RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.95f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .graphicsLayer { alpha = alphaVal }
                        ) {
                            Text(
                                "SKIP",
                                color = Color(0xFFE53935),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            } else {
                // Deck spent complete state
                NeoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    shadowColor = MaterialTheme.colorScheme.outline
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Weekly Deck Completed! 🪄", fontSize = 17.sp, fontWeight = FontWeight.Black)
                        Text("You swiped through all 5 recipes successfully.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp, bottom = 14.dp))
                        NeoButton(
                            onClick = { activeSwipeIndex = 0 },
                            containerColor = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Shuffle Recommendations Deck", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- DECK DOTS TRACKER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            (0..4).forEach { dotIdx ->
                val selected = dotIdx == activeSwipeIndex
                val isDone = dotIdx < activeSwipeIndex
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (selected) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary
                            else if (isDone) Color(0xFF4CAF50)
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (selected) Color.Black else Color.Transparent,
                            shape = CircleShape
                        )
                )
            }
        }

        // --- CATEGORY PILLS ---
        Text(
            text = "Categories",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Breakfast", "Quick Meals", "Vegan", "Desserts", "Kids Selection").forEach { cat ->
                val active = selectedCat == cat
                Box(
                    modifier = Modifier
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        .clickable { selectedCat = cat }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cat,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- MASONRY RECIPE GRID WITH TALL/SHORT CARDS ---
        Text(
            text = "Chef Selection",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (filteredRecipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No Ghibli dishes match this category! 🌾",
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            // Split into two columns for genuine masonry
            val leftColumnRecipes = filteredRecipes.filterIndexed { index, _ -> index % 2 == 0 }
            val rightColumnRecipes = filteredRecipes.filterIndexed { index, _ -> index % 2 != 0 }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    leftColumnRecipes.forEachIndexed { idx, recipe ->
                        RecipePinterestCard(
                            recipe = recipe,
                            onClick = { onRecipeClick(recipe) },
                            onHeartToggle = { viewModel.toggleSaveRecipe(recipe) },
                            isTall = idx % 2 == 0 // Alternates height nicely
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rightColumnRecipes.forEachIndexed { idx, recipe ->
                        RecipePinterestCard(
                            recipe = recipe,
                            onClick = { onRecipeClick(recipe) },
                            onHeartToggle = { viewModel.toggleSaveRecipe(recipe) },
                            isTall = idx % 2 != 0 // Alternates height nicely
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(115.dp))
    }
}

// ------------------- DISCOVER TABS SCREEN -------------------
@Composable
fun DiscoverScreen(
    viewModel: CookrViewModel,
    onRecipeClick: (Recipe) -> Unit
) {
    val recipes by viewModel.allRecipes.collectAsState()
    val submissionVotesVal by viewModel.submissionVotes.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCuisine by remember { mutableStateOf("All Cuisines") }

    // Cuisine mapping helper
    val cuisineFilters = listOf("All Cuisines", "Japanese Miso", "French Bakery", "Italian Pasta", "Campfire", "Palace Delights", "Forest Foraged")

    val filteredRecipes = recipes.filter { recipe ->
        val matchesCuisine = when (selectedCuisine) {
            "All Cuisines" -> true
            "Japanese Miso" -> recipe.category == "Quick Meals" || recipe.title.lowercase().contains("ramen") || recipe.title.lowercase().contains("soup")
            "French Bakery" -> recipe.title.lowercase().contains("bun") || recipe.title.lowercase().contains("bread") || recipe.title.lowercase().contains("cake") || recipe.category == "Desserts"
            "Italian Pasta" -> recipe.title.lowercase().contains("pasta") || recipe.title.lowercase().contains("bacon")
            "Campfire" -> recipe.title.lowercase().contains("bacon") || recipe.title.lowercase().contains("egg")
            "Palace Delights" -> recipe.rating >= 4.8f
            "Forest Foraged" -> recipe.ingredients.any { it.lowercase().contains("onion") || it.lowercase().contains("mushroom") || it.lowercase().contains("apple") || it.lowercase().contains("herb") }
            else -> true
        }

        val matchesSearch = recipe.title.lowercase().contains(searchQuery.lowercase()) ||
                recipe.description.lowercase().contains(searchQuery.lowercase()) ||
                recipe.ingredients.any { it.lowercase().contains(searchQuery.lowercase()) }

        matchesCuisine && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Discover",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Search trendings, traditional cuisines & community favorites",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- SEARCH BAR ---
        NeoTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search dishes, ingredients, cuisines...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search icon") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- CUISINE PILLS ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            cuisineFilters.forEach { cuisine ->
                val active = selectedCuisine == cuisine
                Box(
                    modifier = Modifier
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (active) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface)
                        .clickable { selectedCuisine = cuisine }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cuisine,
                        fontWeight = FontWeight.Bold,
                        color = if (active) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- TRENDING RANKED LIST WITH STATUS BADGES ---
        Text(
            text = "Trending Rankings",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val trendingRecipes = remember(recipes, submissionVotesVal) {
            recipes.sortedByDescending { it.rating + (submissionVotesVal[it.id] ?: 0) * 0.1f }.take(5)
        }

        trendingRecipes.forEachIndexed { index, recipe ->
            val rankIcon = when (index) {
                0 -> "🏆 #1"
                1 -> "🥈 #2"
                2 -> "🥉 #3"
                else -> "⭐ #${index + 1}"
            }

            val statusBadge = when (index) {
                0 -> "Viral Choice 🔥"
                1 -> "98% Food Rating"
                2 -> "Cozy Classic"
                else -> "Ghibli Heritage"
            }

            val statusColor = when (index) {
                0 -> Color(0xFFFF9800)
                1 -> Color(0xFFE91E63)
                2 -> Color(0xFF03A9F4)
                else -> Color(0xFF4CAF50)
            }

            NeoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onRecipeClick(recipe) },
                containerColor = MaterialTheme.colorScheme.surface,
                shadowColor = MaterialTheme.colorScheme.outline,
                shadowOffset = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Position rank
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rankIcon,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    AsyncImage(
                        model = recipe.imageUrl,
                        contentDescription = recipe.title,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = recipe.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = "Created by ${recipe.author}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Status Badge
                        Box(
                            modifier = Modifier
                                .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .border(1.dp, statusColor, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = statusBadge,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = statusColor
                            )
                        }
                    }
                }
            }
        }

        var isGridView by remember { mutableStateOf(false) }

        if (filteredRecipes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            // --- GRID VS LIST SWITCHER COMPONENT ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cuisine Query Results",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(
                    modifier = Modifier
                        .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // List toggle
                    Box(
                        modifier = Modifier
                            .background(if (!isGridView) MaterialTheme.colorScheme.secondary else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { isGridView = false }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = "list toggle button",
                            tint = if (!isGridView) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    // Grid toggle
                    Box(
                        modifier = Modifier
                            .background(if (isGridView) MaterialTheme.colorScheme.secondary else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { isGridView = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.GridView,
                            contentDescription = "grid toggle button",
                            tint = if (isGridView) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (!isGridView) {
                // --- LIST VIEW LAYOUT ---
                filteredRecipes.forEach { recipe ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { onRecipeClick(recipe) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = recipe.imageUrl,
                            contentDescription = recipe.title,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(recipe.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text("${recipe.prepTime} | ${recipe.category}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            } else {
                // --- GRID VIEW LAYOUT (TWO COLUMNS) ---
                val chunked = filteredRecipes.chunked(2)
                chunked.forEach { rowPair ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowPair.forEach { recipe ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable { onRecipeClick(recipe) }
                                    .padding(8.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    AsyncImage(
                                        model = recipe.imageUrl,
                                        contentDescription = recipe.title,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1.2f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = recipe.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        maxLines = 1,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "${recipe.prepTime} | ${recipe.category}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.outline,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        if (rowPair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(115.dp))
    }
}

// ------------------- ADD RECIPE TAB SCREEN -------------------
@Composable
fun AddRecipeScreen(
    viewModel: CookrViewModel,
    onSuccessSave: () -> Unit
) {
    val context = LocalContext.current
    val genRecipe by viewModel.generatedRecipe.collectAsState()
    val aiState by viewModel.aiModelState.collectAsState()

    // Wizard step state: 1 = Photo / AI, 2 = Details, 3 = Steps
    var currentWizardStep by remember { mutableStateOf(1) }

    // Form states
    var isAiGenerateMode by remember { mutableStateOf(false) }
    var aiPromptQuery by remember { mutableStateOf("") }

    var imageUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var prepTime by remember { mutableStateOf("15 min") }
    var cookTime by remember { mutableStateOf("20 min") }
    var servings by remember { mutableStateOf(2) }
    var calories by remember { mutableStateOf(350) }
    var selectedCategory by remember { mutableStateOf("Breakfast") }

    val ingredientTags = remember { mutableStateListOf<String>() }
    var ingredientInput by remember { mutableStateOf("") }

    val instructionSteps = remember { mutableStateListOf("Preparate ingredients.", "Heat skillet with fresh butter.", "Fry softly.", "Serve warm with local tea.") }
    var instructionInput by remember { mutableStateOf("") }

    // Preset food images choices
    val presetImages = listOf(
        "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500", // Skillet pancakes
        "https://images.unsplash.com/photo-1529139574466-a303027c1d8b?w=500", // Eggs / bacon
        "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=500", // Breads / bakery
        "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=500"  // Ramen bowl
    )

    // Synchronize generated recipe
    LaunchedEffect(genRecipe) {
        if (aiState == AiModelState.SUCCESS) {
            genRecipe?.let { recipe ->
                title = recipe.title
                description = recipe.description
                prepTime = recipe.prepTime
                cookTime = recipe.cookTime
                servings = recipe.servings
                calories = recipe.calories
                selectedCategory = recipe.category
                
                ingredientTags.clear()
                ingredientTags.addAll(recipe.ingredients)
                
                instructionSteps.clear()
                instructionSteps.addAll(recipe.instructions)
                
                Toast.makeText(context, "AI populated the recipe wizard cleanly! ✨", Toast.LENGTH_LONG).show()
                // Done loading, reset state to idle
                viewModel.aiModelState.value = AiModelState.IDLE
                // Move directly to step 2 to review AI output
                currentWizardStep = 2
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Create Recipe",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Draft manual Ghibli classics, or invoke generating AI engines",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // --- STEP WIZARD STATUS ROW WITH GREEN INDICATORS ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            (1..3).forEach { step ->
                val isActive = currentWizardStep == step
                val isDone = currentWizardStep > step
                val stepTitle = when (step) {
                    1 -> "Photo / AI"
                    2 -> "Details"
                    3 -> "Steps"
                    else -> ""
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDone) Color(0xFF4CAF50) // Done step is green
                                else if (isActive) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            )
                            .border(2.dp, Color.Black, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDone) {
                            Icon(Icons.Filled.Check, contentDescription = "Step complete", tint = Color.White, modifier = Modifier.size(16.dp))
                        } else {
                            Text(
                                text = "$step",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isActive) MaterialTheme.colorScheme.onPrimary else Color.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(stepTitle, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                if (step < 3) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .background(if (currentWizardStep > step) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            .padding(horizontal = 4.dp)
                    )
                }
            }
        }

        // --- STEP 1 CONTENT: PHOTO, CATEGORY, AI GENERATOR ---
        if (currentWizardStep == 1) {
            Text("Select Showcase Photo", fontSize = 16.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(10.dp))

            // Preset photo choice grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetImages.forEach { img ->
                    val isSelected = imageUrl == img
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .border(
                                width = if (isSelected) 3.dp else 1.5.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { imageUrl = img }
                    ) {
                        AsyncImage(
                            model = img,
                            contentDescription = "Image Choice Preset",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text("Or Type Custom Image URL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            NeoTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                placeholder = { Text("https://example.com/food.jpg") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Category picker
            Text("Choose Category", fontSize = 16.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Breakfast", "Quick Meals", "Vegan", "Desserts", "Kids Selection").forEach { cat ->
                    val active = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (active) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (active) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // AI Toggle section
            NeoCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                shadowColor = MaterialTheme.colorScheme.outline
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(0.7f)) {
                            Text("Gemini Recipe Generator ✨", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Leverage LLM logic to populate details instantly", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        }
                        Switch(
                            checked = isAiGenerateMode,
                            onCheckedChange = { isAiGenerateMode = it }
                        )
                    }

                    if (isAiGenerateMode) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Write your creative prompt query:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        NeoTextField(
                            value = aiPromptQuery,
                            onValueChange = { aiPromptQuery = it },
                            placeholder = { Text("E.g., Nostalgic bowl of golden mushroom ramen soup with roasted tea", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (aiState == AiModelState.LOADING) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Gemini is baking the recipe... 📝", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            NeoButton(
                                onClick = {
                                    if (aiPromptQuery.isNotBlank()) {
                                        viewModel.generateAiRecipe(aiPromptQuery)
                                        viewModel.aiModelState.value = AiModelState.LOADING
                                    } else {
                                        Toast.makeText(context, "Give Gemini a description first!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = MaterialTheme.colorScheme.primary
                            ) {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = "Sparkle", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Auto Populate Details", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            NeoButton(
                onClick = { currentWizardStep = 2 },
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Text("Proceed to Step 2: Details ➡️", fontWeight = FontWeight.Bold)
            }
        }

        // --- STEP 2 CONTENT: DETAILS & INGREDIENT BUILDER ---
        if (currentWizardStep == 2) {
            Text("Recipe Specifics", fontSize = 16.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(12.dp))

            Text("Dish Title", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            NeoTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("E.g., Auntie's Lavender Meadow Tea Buns") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Summary description", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            NeoTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("A cozy, golden bun that carries scents of nostalgic valleys...") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Prep Time", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    NeoTextField(
                        value = prepTime,
                        onValueChange = { prepTime = it },
                        placeholder = { Text("15 min") }
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Cook Time", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    NeoTextField(
                        value = cookTime,
                        onValueChange = { cookTime = it },
                        placeholder = { Text("20 min") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Yield Servings", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    NeoTextField(
                        value = servings.toString(),
                        onValueChange = { servings = it.toIntOrNull() ?: 2 },
                        placeholder = { Text("2") }
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Calories (kcal)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    NeoTextField(
                        value = calories.toString(),
                        onValueChange = { calories = it.toIntOrNull() ?: 350 },
                        placeholder = { Text("350") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Ingredient Builder
            Text("Dynamic Ingredient Builder", fontSize = 14.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    NeoTextField(
                        value = ingredientInput,
                        onValueChange = { ingredientInput = it },
                        placeholder = { Text("E.g., 2 spoons Wild Herbs", fontSize = 11.sp) }
                    )
                }
                NeoButton(
                    onClick = {
                        if (ingredientInput.isNotBlank()) {
                            ingredientTags.add(ingredientInput.trim())
                            ingredientInput = ""
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Add", fontWeight = FontWeight.Bold)
                }
            }

            // Tags viewer scroll
            if (ingredientTags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ingredientTags.forEach { tag ->
                        Row(
                            modifier = Modifier
                                .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tag, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Remove tag",
                                modifier = Modifier
                                    .size(12.dp)
                                    .clickable { ingredientTags.remove(tag) },
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NeoButton(
                    onClick = { currentWizardStep = 1 },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("⬅️ Step 1", fontWeight = FontWeight.Bold)
                }
                NeoButton(
                    onClick = { currentWizardStep = 3 },
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Text("Proceed to Steps ➡️", fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- STEP 3 CONTENT: DYNAMIC CREATOR STEPS & PUBLISH ---
        if (currentWizardStep == 3) {
            Text("Cooking Guidelines Steps", fontSize = 16.sp, fontWeight = FontWeight.Black)
            Text("Organize sequential milestones for final preparation", fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))

            // Steps addition row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    NeoTextField(
                        value = instructionInput,
                        onValueChange = { instructionInput = it },
                        placeholder = { Text("E.g., Whisk gently under soft candle light...", fontSize = 11.sp) }
                    )
                }
                NeoButton(
                    onClick = {
                        if (instructionInput.isNotBlank()) {
                            instructionSteps.add(instructionInput.trim())
                            instructionInput = ""
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Step", modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic Step Checklist List
            if (instructionSteps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No steps recorded. Add steps to draft instructions!", fontSize = 11.sp, color = Color.Gray)
                }
            } else {
                instructionSteps.forEachIndexed { idx, st ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                .border(1.dp, Color.Black, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${idx + 1}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = st,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { instructionSteps.removeAt(idx) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete step",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NeoButton(
                    onClick = { currentWizardStep = 2 },
                    modifier = Modifier.weight(0.4f)
                ) {
                    Text("⬅️ Step 2", fontWeight = FontWeight.Bold)
                }

                NeoButton(
                    onClick = {
                        if (title.isBlank() || description.isBlank()) {
                            Toast.makeText(context, "Please write a title and description first!", Toast.LENGTH_SHORT).show()
                            currentWizardStep = 2
                            return@NeoButton
                        }

                        if (ingredientTags.isEmpty()) {
                            Toast.makeText(context, "Please add at least one ingredient inside Step 2!", Toast.LENGTH_SHORT).show()
                            currentWizardStep = 2
                            return@NeoButton
                        }

                        if (instructionSteps.isEmpty()) {
                            Toast.makeText(context, "Please register at least one cooking step!", Toast.LENGTH_SHORT).show()
                            return@NeoButton
                        }

                        // Construct and save recipe
                        val recipe = Recipe(
                            id = "custom_" + UUID.randomUUID().toString().take(6),
                            title = title,
                            description = description,
                            prepTime = prepTime,
                            cookTime = cookTime,
                            servings = servings,
                            ingredients = ingredientTags.toList(),
                            instructions = instructionSteps.toList(),
                            imageUrl = imageUrl,
                            category = selectedCategory,
                            isSaved = false,
                            rating = 4.7f,
                            calories = calories,
                            protein = 16,
                            carbs = 32,
                            fat = 11,
                            author = "You (Creative Chef)",
                            isUserSubmitted = true
                        )

                        viewModel.addRecipe(recipe)
                        Toast.makeText(context, "Successfully published \"${recipe.title}\"! 🍳", Toast.LENGTH_LONG).show()
                        onSuccessSave()
                    },
                    modifier = Modifier.weight(0.6f),
                    containerColor = MaterialTheme.colorScheme.primary,
                    borderWidth = 3.dp,
                    shadowOffset = 5.dp
                ) {
                    Icon(Icons.Filled.Check, contentDescription = "Publish", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Publish Masterpiece", fontWeight = FontWeight.Black)
                }
            }
        }
        Spacer(modifier = Modifier.height(115.dp))
    }
}

// ------------------- INBOX SCREEN -------------------
@Composable
fun InboxScreen(viewModel: CookrViewModel) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("All") }
    
    // Initializing messages from Studio Ghibli characters
    val messages = remember {
        mutableStateListOf(
            InboxMessage(
                id = "1",
                sender = "Kiki",
                avatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150", 
                senderRole = "Elite Courier • Witch Delivery Service",
                subject = "Needs Energy-Packed Witch Fuel! 🚲🥐",
                body = "Hey Chef! I've got a massive morning bread delivery rush today. Could you suggest a high-protein breakfast that travels well? Calcifer said you'd know exactly what to make! If you have any ideas, let me know or just create a new recipe using 'Add' and tag it 'Breakfast'!",
                category = "Direct",
                quickReplies = listOf("Try Golden Bacon Toast! 🥓", "Pack a high-protein bowl! 🍚", "Witch delivery potion? 🪄"),
                isQuest = true,
                questCompleted = false,
                rewardCoins = 35
            ),
            InboxMessage(
                id = "2",
                sender = "Calcifer",
                avatarUrl = "https://images.unsplash.com/photo-1518199266791-5375a83190b7?w=150", 
                senderRole = "Castle Fire Demon • Hearth Master",
                subject = "SKILLET IGNITION COMPLETE! 🔥🍳",
                body = "WHOA! That heavy skillet you recommended holds heat like an absolute beast! Howl tried to cook standard bacon without my special heating touch and failed miserably, but when I blew up the fire-heat, it sizzled to perfection! Keep the fire recipes rolling!",
                category = "Direct",
                quickReplies = listOf("Feed Calcifer wood pieces! 🪵", "Only the best heat control! 🔥", "No grease fires please! 🧯"),
                isQuest = false,
                questCompleted = false
            ),
            InboxMessage(
                id = "3",
                sender = "Howl Jenkins",
                avatarUrl = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150", 
                senderRole = "Wizard of Pendragon Castle",
                subject = "Soul-Warming Soup for Sophie 🌸✨",
                body = "A delicate query, colleague: Sophie is feeling slightly under the weather today after cleaning the library. Do you have a comforting herbal or rich soup recipe that restores vitality without taxing her energy? I would love to make it for her tonight.",
                category = "Direct",
                quickReplies = listOf("Make Chamomile-Rice Soup 🌾", "Soothe with warm ginger broth 🍵", "Magical healing stew! 🧪"),
                isQuest = true,
                questCompleted = false,
                rewardCoins = 50
            ),
            InboxMessage(
                id = "4",
                sender = "Chihiro Ogino",
                avatarUrl = "https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=150",
                senderRole = "Steamhouse Kitchen Helper",
                subject = "Gold Dust Found for Golden Buns! 🌾🥟",
                body = "Hello! I successfully tracked down some authentic culinary-grade gold dust from the spirit river! My parents are safe and we are finally back home in the human world. Thank you for teaching me how comforting meals can heal anyone! Warmest regards.",
                category = "Direct",
                quickReplies = listOf("Congratulations, Chihiro! 🎉", "Always cook with love! 💛", "Keep the gold safe! 💰"),
                isQuest = false,
                questCompleted = false
            ),
            InboxMessage(
                id = "5",
                sender = "Ghibli Syndicate Portal",
                avatarUrl = "",
                senderRole = "Official Merchant Council",
                subject = "Bake 3 Pastry Masters Challenge! 🏆🧇",
                body = "SPECIAL SYNDICATE QUEST: Bake at least 3 custom pastry or dessert recipes on Cookr. This unlocks a legendary 'Gilded Whisk' certificate on your Profile screen and amasses high Ghibli reputational standing!",
                category = "Quest",
                quickReplies = emptyList(),
                isQuest = true,
                questCompleted = false,
                rewardCoins = 100
            )
        )
    }

    var expandedMessageId by remember { mutableStateOf<String?>(null) }
    var replyText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Inbox",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Letters & Quest Commissions from Ghibli Companions",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // --- SUB-TABS (All, Direct, Quests) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Direct", "Quest").forEach { cat ->
                val active = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (cat == "Quest") "Quests" else if (cat == "Direct") "Direct Mail" else "All Messages",
                        fontWeight = FontWeight.ExtraBold,
                        color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- MESSAGES LIST ---
        val filteredMessages = messages.filter {
            selectedCategory == "All" || it.category == selectedCategory
        }

        if (filteredMessages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Your mail tray is perfectly clear! 🕊️", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            filteredMessages.forEach { msg ->
                val expanded = expandedMessageId == msg.id
                
                NeoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .animateContentSize(),
                    containerColor = if (msg.isQuest && !msg.questCompleted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
                    shadowColor = MaterialTheme.colorScheme.outline
                ) {
                    Column(
                        modifier = Modifier
                            .clickable {
                                expandedMessageId = if (expanded) null else msg.id
                                msg.isRead = true
                            }
                            .padding(14.dp)
                    ) {
                        // Header with sender identity
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (msg.avatarUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = msg.avatarUrl,
                                        contentDescription = msg.sender,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = msg.sender.take(1).uppercase(),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(10.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = msg.sender,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                    
                                    // Status badges
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        if (!msg.isRead) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(MaterialTheme.colorScheme.error)
                                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                                            ) {
                                                Text("NEW", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        if (msg.isQuest) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        if (msg.questCompleted) Color(0xFF4CAF50) 
                                                        else MaterialTheme.colorScheme.primary
                                                    )
                                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (msg.questCompleted) "COMPLETED" else "QUEST", 
                                                    color = Color.White, 
                                                    fontSize = 8.sp, 
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                                Text(
                                    text = msg.senderRole,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Subject Line
                        Text(
                            text = msg.subject,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Short excerpt or full body
                        Text(
                            text = if (expanded) msg.body else (msg.body.take(65) + "..."),
                            fontSize = 12.sp,
                            color = Color.Gray,
                            lineHeight = 16.sp
                        )

                        // If expanded, show interaction controls
                        if (expanded) {
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            // TTS Speak out loud button
                            NeoButton(
                                onClick = {
                                    viewModel.speakInstruction("Message from ${msg.sender}: ${msg.body}")
                                    Toast.makeText(context, "Reading message out loud... 🎧", Toast.LENGTH_SHORT).show()
                                },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = "Listen", modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Read Aloud", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            if (msg.isQuest && !msg.questCompleted) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    NeoButton(
                                        onClick = {
                                            msg.questCompleted = true
                                            Toast.makeText(context, "Accepted quest from ${msg.sender}! Check Commissions.", Toast.LENGTH_LONG).show()
                                        },
                                        containerColor = Color(0xFF4CAF50),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Accept Quest (+${msg.rewardCoins}g)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                    }

                                    NeoButton(
                                        onClick = {
                                            expandedMessageId = null
                                        },
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(0.5f)
                                    ) {
                                        Text("Ignore", fontSize = 11.sp)
                                    }
                                }
                            }

                            // Quick reply and general text answer options
                            if (msg.quickReplies.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Text("Quick Suggestion Reply:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    msg.quickReplies.forEach { reply ->
                                        Box(
                                            modifier = Modifier
                                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                                .clickable {
                                                    Toast.makeText(context, "Replied: \"$reply\" successfully!", Toast.LENGTH_SHORT).show()
                                                    expandedMessageId = null
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(reply, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Custom message input
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = replyText,
                                    onValueChange = { replyText = it },
                                    placeholder = { Text("Write custom letter to ${msg.sender}...", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                NeoButton(
                                    onClick = {
                                        if (replyText.isNotEmpty()) {
                                            val responseMsg = when (msg.sender) {
                                                "Kiki" -> "Kiki received your letter, hopped on her broom, and flew off happily! 🧹"
                                                "Calcifer" -> "Calcifer crackled wildly and ate your letter like seasoned firewood! 🔥"
                                                "Howl" -> "Howl smiled charmingly, saying Pendragon is deeply indebted! 🏰"
                                                else -> "Your culinary letter has been successfully dispatched! 📬"
                                            }
                                            Toast.makeText(context, responseMsg, Toast.LENGTH_LONG).show()
                                            replyText = ""
                                            expandedMessageId = null
                                        }
                                    },
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Filled.Send, contentDescription = "Send", modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(115.dp))
    }
}

class InboxMessage(
    val id: String,
    val sender: String,
    val avatarUrl: String,
    val senderRole: String,
    val subject: String,
    val body: String,
    val category: String,
    val quickReplies: List<String>,
    val isQuest: Boolean,
    var questCompleted: Boolean,
    val rewardCoins: Int = 0,
    var isRead: Boolean = false
)

// ------------------- PROFILE SCREEN -------------------
@Composable
fun LegacyProfileScreen(
    viewModel: CookrViewModel,
    userName: String,
    onNameChange: (String) -> Unit,
    dailyCalTarget: Int,
    onTargetChange: (Int) -> Unit
) {
    val context = LocalContext.current
    val recipes by viewModel.allRecipes.collectAsState()
    val savedCount = recipes.filter { it.isSaved }.size

    val voiceEnabled by viewModel.voiceInstructionsEnabled.collectAsState()
    val gesturesEnabled by viewModel.gestureControlsEnabled.collectAsState()

    // Dietary selection
    val dietaryPrefs = remember { mutableStateListOf("Vegan", "Low-Carb") }

    // Commissions task checklist from database/original screen
    val commissionToggles = remember { mutableStateListOf(false, false, false, false) }
    val commissionNames = listOf(
        "Calcifer Stove Ignition Duty (Standard Level)",
        "Kiki Bakery Delivery Commission (Elite Courier)",
        "Howl Moving Castle Bacon Skillet License (Master Chef)",
        "Soot Sprite Coal Boiler Helper"
    )

    val activeCommissionsCount = commissionToggles.count { it }
    val commissionRewardsPool = activeCommissionsCount * 18.25

    // State for expanded settings categories (exactly matching the options list in mockup)
    var isPhotosExpanded by remember { mutableStateOf(false) }
    var isAbonelikExpanded by remember { mutableStateOf(false) }
    var isStatsExpanded by remember { mutableStateOf(false) }
    var isSettingsExpanded by remember { mutableStateOf(false) }

    // Use a custom vertical-graded background to replicate the cozy dusty lavender/grey look of the mockup!
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE2E6EF), // Soft, icy dusty blue-grey
                        Color(0xFFCED2DD), // Cool lavender slate
                        Color(0xFFBFC4D0)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // --- HEADER ROW ("Profil" + Notification/Bell Icon) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profil",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Serif,
                    color = Color(0xFF2C2D31)
                )

                // Bell notification button styled exactly like the mockup!
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.5f))
                        .border(1.dp, Color.Black.copy(alpha = 0.05f), CircleShape)
                        .clickable {
                            Toast.makeText(context, "No new culinary notifications! 🔔", Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFF2C2D31),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- HEALTH DASHBOARD ---
            HealthDashboard(score = 8.8f)

            Spacer(modifier = Modifier.height(20.dp))

            // --- HEADER AVATAR: Centered Portrait with glowing aura ---
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.4f))
                        .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Styled circular avatar displaying our warm Ghibli profile photo!
                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ghibli_chef_1779398968152),
                        contentDescription = "Chef profile avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // --- PRIMARY PREMIUM CARD (Houses Name, Stats Pills, and Credit balance capsule) ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = Color.White.copy(alpha = 0.75f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // User Name & Premium Badge Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (userName.isBlank()) "Duygu Özarslan" else userName,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2C2D31)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "@${(if (userName.isBlank()) "elmasekeri" else userName).replace(" ", "").lowercase()}",
                                fontSize = 13.sp,
                                color = Color.Black.copy(alpha = 0.45f)
                            )
                        }

                        // Premium Chip Badge with gold crown icon
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF2C2D31))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WorkspacePremium,
                                contentDescription = "Premium Crown",
                                tint = Color(0xFFFFD700), // Sparkling gold!
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Premium",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Column of Stats pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Stat 1: Shared/Gonderi
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "4",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF2C2D31)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Gönderi", fontSize = 10.sp, color = Color.Black.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Stat 2: Followers/Takipçi
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "22",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF2C2D31)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Takipçi", fontSize = 10.sp, color = Color.Black.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Stat 3: Following/Takip
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "15",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF2C2D31)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Takip", fontSize = 10.sp, color = Color.Black.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Kitchen credit banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF2C2D31)) // Dark slate-charcoal capsule in mockup
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "Star Icon",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Kredin Tükendi!",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Kredi al, üretmeye devam et.",
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            // Buy button: white container, black text
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .clickable {
                                        Toast.makeText(context, "Kitchen Fuel credits replenished! 🌾🍳", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Satın Al",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF2C2D31)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- FUNCTIONAL GLASS LIST OPTIONS CARD ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                color = Color.White.copy(alpha = 0.78f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                shadowElevation = 3.dp
            ) {
                Column(modifier = Modifier.padding(vertical = 10.dp)) {
                    // Option 1: Fotoğraflarım
                    ProfileOptionItem(
                        icon = Icons.Outlined.PhotoLibrary,
                        title = "Fotoğraflarım",
                        isExpanded = isPhotosExpanded,
                        onClick = { isPhotosExpanded = !isPhotosExpanded }
                    ) {
                        // Grid of exquisite Ghibli food images!
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Text("Your Culinary Memories 📸", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C2D31))
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "Bacon & Eggs" to "🔥",
                                    "Soot Candy" to "☄️",
                                    "Kiki Cake" to "🎂",
                                    "Spirited Buns" to "🥯"
                                ).forEach { (dish, emoji) ->
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(emoji, fontSize = 24.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(dish, fontSize = 8.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color.Black.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))

                    // Option 2: Abonelik & Kredi
                    ProfileOptionItem(
                        icon = Icons.Outlined.StarRate,
                        title = "Abonelik & Kredi",
                        isExpanded = isAbonelikExpanded,
                        onClick = { isAbonelikExpanded = !isAbonelikExpanded }
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2D31)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Active Membership: Cookr Premium Elite", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Unlimited macro calculations, dynamic smart scales, Ghibli syndicate rewards.", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { Toast.makeText(context, "Credits refreshed!", Toast.LENGTH_SHORT).show() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Recharge 100 Credits - $1.99", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color.Black.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))

                    // Option 3: İstatistikler
                    ProfileOptionItem(
                        icon = Icons.Outlined.BarChart,
                        title = "İstatistikler",
                        isExpanded = isStatsExpanded,
                        onClick = { isStatsExpanded = !isStatsExpanded }
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Flame Streak 🔥", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text("5 Days Active", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFFE65100))
                                    }
                                }

                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Saved Recipes ⭐", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text("$savedCount Favorited", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF4A148C))
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color.Black.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))

                    // Option 4: Ayarlar (This is where we elegant embed the original functional controls)
                    ProfileOptionItem(
                        icon = Icons.Outlined.Settings,
                        title = "Ayarlar",
                        isExpanded = isSettingsExpanded,
                        onClick = { isSettingsExpanded = !isSettingsExpanded }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text("Cooking Preferences", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Name Fields
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Chef Name", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    NeoTextField(value = userName, onValueChange = onNameChange)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Daily Cal Target", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    NeoTextField(value = dailyCalTarget.toString(), onValueChange = { onTargetChange(it.toIntOrNull() ?: 2000) })
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Theme customization
                            Text("Color Themes", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            val themeMode by viewModel.themeMode.collectAsState()
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                com.example.presentation.ThemeMode.values().forEach { mode ->
                                    val active = themeMode == mode
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(width = 1.5.dp, color = Color.Gray, shape = RoundedCornerShape(10.dp))
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (active) Color(0xFF2C2D31) else Color.Transparent)
                                            .clickable { viewModel.themeMode.value = mode }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            mode.name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (active) Color.White else Color.Black
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // App palette
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    Triple(CookrThemeSelection.CITRUS_FUSION, "Citrus", Color(0xFFFF5722)),
                                    Triple(CookrThemeSelection.FOREST_SAGE, "Forest", Color(0xFF2E7D32)),
                                    Triple(CookrThemeSelection.COSMIC_TWILIGHT, "Cosmic", Color(0xFF7C4DFF)),
                                    Triple(CookrThemeSelection.SWEET_LAVENDER, "Lavend", Color(0xFF6D28D9))
                                ).forEach { (themeType, label, visualCol) ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .border(width = 1.dp, color = Color.Black.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(visualCol.copy(alpha = 0.15f))
                                            .clickable { viewModel.themeSelection.value = themeType }
                                            .padding(6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(visualCol)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(label, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Sensory Switch controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Voice Synthesis Instructions", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Speak cookbook instructions dynamically", fontSize = 10.sp, color = Color.Gray)
                                }
                                Switch(checked = voiceEnabled, onCheckedChange = { viewModel.voiceInstructionsEnabled.value = it })
                            }

                            if (voiceEnabled) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = { viewModel.speakInstruction("Kitchen Fuel stove is hot and ready!") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2D31)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Test Voice Synthesis 🔊", fontSize = 11.sp, color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Gesture Navigation Controls", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Navigate cooking steps with gestures", fontSize = 10.sp, color = Color.Gray)
                                }
                                Switch(checked = gesturesEnabled, onCheckedChange = { viewModel.gestureControlsEnabled.value = it })
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Comissions tracker from standard Screen
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Ghibli Syndicate Commissions", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    commissionNames.forEachIndexed { idx, title ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = commissionToggles[idx],
                                                onCheckedChange = { commissionToggles[idx] = it }
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(title, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Commissions Revenue: $%.2f USD".format(commissionRewardsPool), fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(130.dp))
        }
    }
}

@Composable
fun LegacyProfileOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    isExpanded: Boolean,
    onClick: () -> Unit,
    expandedContent: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFF2C2D31),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C2D31)
                )
            }

            Icon(
                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ChevronRight,
                contentDescription = "Expand Indicator",
                tint = Color.Black.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            expandedContent()
        }
    }
}

@Composable
fun ProfileOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    isExpanded: Boolean,
    onClick: () -> Unit,
    expandedContent: @Composable () -> Unit
) {
    LegacyProfileOptionItem(icon, title, isExpanded, onClick, expandedContent)
}
