package com.example.presentation.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.Recipe
import com.example.presentation.CookrViewModel
import com.example.presentation.AiModelState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@Composable
fun PhotoAnalyzerScreen(
    viewModel: CookrViewModel,
    onSuccessSave: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Observe state from ViewModel
    val analysisState by viewModel.photoAnalysisState.collectAsState()
    val analyzedRecipe by viewModel.analyzedRecipe.collectAsState()

    var imageUri by remember { mutableStateOf<String?>(null) }
    var activeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var step by remember { mutableStateOf(0) } // 0: Select, 1: Analyzing/Editing

    // Launcher for system gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri.toString()
            scope.launch {
                try {
                    val bitmap = withContext(Dispatchers.IO) {
                        val contentResolver = context.contentResolver
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            val source = ImageDecoder.createSource(contentResolver, uri)
                            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                                decoder.isMutableRequired = true
                            }
                        } else {
                            @Suppress("DEPRECATION")
                            MediaStore.Images.Media.getBitmap(contentResolver, uri)
                        }
                    }
                    val scaled = scaleBitmapDown(bitmap, 800)
                    activeBitmap = scaled
                    Toast.makeText(context.applicationContext, "Image loaded successfully!", Toast.LENGTH_SHORT).show()
                } catch (e: Throwable) {
                    e.printStackTrace()
                    Toast.makeText(context.applicationContext, "Failed to load image: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Default high-quality preset images so user has immediate clicking option on emulators
    val presets = listOf(
        PresetImage("Breakfast Avocado", "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500"),
        PresetImage("Artisan Ramen", "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=500"),
        PresetImage("Sweet Pancakes", "https://images.unsplash.com/photo-1528207776546-365bb710ee93?w=500")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Dynamic Elegant Top App Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 28.dp, bottom = 12.dp)
            ) {
                Text(
                    "Recipe Intelligence",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Let Gemini transform your cooking photos into smart editable cookbook entries.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (step == 0) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Main visual preview frame
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                try {
                                    galleryLauncher.launch("image/*")
                                } catch (e: Throwable) {
                                    Toast.makeText(context, "System gallery app not available on this device", Toast.LENGTH_LONG).show()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri != null) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Active image to parse",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Decorative change badge
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Change Photo", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Outlined.PhotoCamera,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Choose Food Image", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                Text("Tap to select from photo gallery", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    galleryLauncher.launch("image/*")
                                } catch (e: Throwable) {
                                    Toast.makeText(context, "System gallery app not available on this device", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Outlined.PhotoLibrary, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Gallery", fontSize = 14.sp)
                        }

                        Button(
                            onClick = {
                                // Simulate Quick Camera Snapshot using mock local resource loading as sample simulation
                                imageUri = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500"
                                scope.launch {
                                    try {
                                        // Load default testing bitmap asynchronously on IO thread, then assign on main thread
                                        val testBitmap = withContext(Dispatchers.IO) {
                                            Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
                                        }
                                        activeBitmap = testBitmap
                                    } catch (e: Throwable) {
                                        e.printStackTrace()
                                    }
                                }
                                Toast.makeText(context, "Camera snapshot simulated!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Outlined.PhotoCamera, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simulate Camera", fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Preset Quick-Select Options Section for instant testing on streaming emulator
                    Text(
                        "No local images? Try a dynamic masterwork preset:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    presets.forEach { preset ->
                        Card(
                            onClick = {
                                imageUri = preset.url
                                scope.launch {
                                    try {
                                        val dummy = withContext(Dispatchers.IO) {
                                            Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
                                        }
                                        activeBitmap = dummy
                                    } catch (e: Throwable) {
                                        e.printStackTrace()
                                    }
                                }
                                Toast.makeText(context, "Loaded preset: ${preset.title}", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (imageUri == preset.url) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (imageUri == preset.url) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = preset.url,
                                    contentDescription = preset.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(preset.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Analyze to extract custom culinary properties", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    // AI Scan Button
                    Button(
                        onClick = {
                            val targetBitmap = activeBitmap
                            if (targetBitmap != null) {
                                viewModel.analyzeFoodImage(targetBitmap)
                                step = 1
                            } else {
                                Toast.makeText(context, "Please choose or simulate an image first!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Analyze Culinary Photo", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(80.dp)) // Nav bar buffer
                }
            } else {
                // Step 1: Loading, Success, or Reviewing AI model outcome
                when (analysisState) {
                    AiModelState.LOADING -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("Gemini Is Cooking up a Storm...", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Dissecting image composition, seasonings, and instructions.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    AiModelState.SUCCESS -> {
                        val recipe = analyzedRecipe
                        if (recipe != null) {
                            EditableRecipe(
                                viewModel = viewModel,
                                initialRecipe = recipe,
                                customImageUri = imageUri,
                                onSaveSuccess = {
                                    viewModel.photoAnalysisState.value = AiModelState.IDLE
                                    onSuccessSave()
                                },
                                onCancel = {
                                    viewModel.photoAnalysisState.value = AiModelState.IDLE
                                    step = 0
                                }
                            )
                        } else {
                            step = 0
                        }
                    }
                    else -> {
                        // Error fallback
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Red)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(" Culinary parsing anomaly occurred.")
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = { step = 0 }) {
                                Text("Try Again")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableRecipe(
    viewModel: CookrViewModel,
    initialRecipe: Recipe,
    customImageUri: String?,
    onSaveSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf(initialRecipe.title) }
    var description by remember { mutableStateOf(initialRecipe.description) }
    var prepTime by remember { mutableStateOf(initialRecipe.prepTime) }
    var cookTime by remember { mutableStateOf(initialRecipe.cookTime) }
    var servings by remember { mutableStateOf(initialRecipe.servings.toString()) }
    var category by remember { mutableStateOf(initialRecipe.category) }

    // Convert string array to single string separated by newlines
    var ingredientsText by remember { mutableStateOf(initialRecipe.ingredients.joinToString("\n")) }
    var instructionsText by remember { mutableStateOf(initialRecipe.instructions.joinToString("\n")) }

    // Macro properties
    var calories by remember { mutableStateOf(initialRecipe.calories.toString()) }
    var protein by remember { mutableStateOf(initialRecipe.protein.toString()) }
    var carbs by remember { mutableStateOf(initialRecipe.carbs.toString()) }
    var fat by remember { mutableStateOf(initialRecipe.fat.toString()) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Verify AI Recipe Blueprint", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Fine tune name, macro measurements, and steps below dynamically.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Recipe Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Basic details Row
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = prepTime,
                    onValueChange = { prepTime = it },
                    label = { Text("Prep (e.g. 10m)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = cookTime,
                    onValueChange = { cookTime = it },
                    label = { Text("Cook (e.g. 15m)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = servings,
                    onValueChange = { servings = it },
                    label = { Text("Yield") },
                    modifier = Modifier.weight(0.8f),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Category Selection Composed
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Recipe Category", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val categories = listOf("Breakfast", "Quick Meals", "Vegan", "Desserts", "Main Course")
                    categories.forEach { c ->
                        FilterChip(
                            selected = category == c,
                            onClick = { category = c },
                            label = { Text(c, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Nutrition Card Edit Block
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Micro/Mega Nutrient Estimations", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = calories,
                        onValueChange = { calories = it },
                        label = { Text("Cals (kcal)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it },
                        label = { Text("Protein (g)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it },
                        label = { Text("Carbs (g)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = fat,
                        onValueChange = { fat = it },
                        label = { Text("Fat (g)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Raw Ingredients Block
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Ingredients (One item per line)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = ingredientsText,
                    onValueChange = { ingredientsText = it },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Raw Instructions Step block
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Steps (One step per line)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = instructionsText,
                    onValueChange = { instructionsText = it },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(0.8f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    val finalIngredients = ingredientsText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                    val finalInstructions = instructionsText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                    
                    if (title.isEmpty()) {
                        Toast.makeText(context, "Recipe title cannot be blank!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val finalRecipe = Recipe(
                        id = initialRecipe.id,
                        title = title,
                        description = description,
                        prepTime = prepTime,
                        cookTime = cookTime,
                        servings = servings.toIntOrNull() ?: 2,
                        ingredients = finalIngredients,
                        instructions = finalInstructions,
                        imageUrl = customImageUri ?: initialRecipe.imageUrl,
                        category = category,
                        isSaved = true,
                        rating = 4.8f,
                        calories = calories.toIntOrNull() ?: 350,
                        protein = protein.toIntOrNull() ?: 15,
                        carbs = carbs.toIntOrNull() ?: 45,
                        fat = fat.toIntOrNull() ?: 10,
                        author = "My Kitchen Intel",
                        isUserSubmitted = true,
                        photoRank = 0
                    )

                    viewModel.addRecipe(finalRecipe)
                    Toast.makeText(context, "Successfully saved recipe to catalog! 🎉", Toast.LENGTH_SHORT).show()
                    onSaveSuccess()
                },
                modifier = Modifier
                    .weight(1.2f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Cookbook")
            }
        }

        Spacer(modifier = Modifier.height(100.dp)) // padding safe zone
    }
}

data class PresetImage(
    val title: String,
    val url: String
)

private fun scaleBitmapDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
    val srcWidth = bitmap.width
    val srcHeight = bitmap.height
    val (newWidth, newHeight) = if (srcWidth >= srcHeight) {
        if (srcWidth <= maxDimension) return bitmap
        val ratio = srcHeight.toFloat() / srcWidth.toFloat()
        val targetWidth = maxDimension
        val targetHeight = (maxDimension * ratio).toInt()
        Pair(targetWidth, targetHeight)
    } else {
        if (srcHeight <= maxDimension) return bitmap
        val ratio = srcWidth.toFloat() / srcHeight.toFloat()
        val targetHeight = maxDimension
        val targetWidth = (maxDimension * ratio).toInt()
        Pair(targetWidth, targetHeight)
    }
    return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
}
