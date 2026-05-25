package com.example.presentation.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.Recipe
import com.example.presentation.CookrViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PhotoAnalyzerScreen(
    viewModel: CookrViewModel,
    onSuccessSave: () -> Unit
) {
    var imageUri by remember { mutableStateOf<String?>(null) }
    var step by remember { mutableStateOf(0) } // 0: Select, 1: Analyzing, 2: Result, 3: Edit
    var detectedDish by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    var manualDishName by remember { mutableStateOf("") }
    var isUnrecognized by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Recipe Intelligence",
                modifier = Modifier.padding(24.dp).padding(top = 24.dp),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when (step) {
                0 -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(Icons.Outlined.PhotoCamera, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("What's cooking?", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Snap or upload a photo of a dish, and our AI will analyze it to create a recipe for you.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        Spacer(modifier = Modifier.height(48.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Button(
                                onClick = {
                                    imageUri = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500"
                                    step = 1
                                    scope.launch {
                                        delay(2500)
                                        step = 2
                                        detectedDish = "Avocado Toast with Poached Egg"
                                    }
                                },
                                modifier = Modifier.weight(1f).height(56.dp)
                            ) {
                                Icon(Icons.Outlined.PhotoCamera, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Camera")
                            }

                            FilledTonalButton(
                                onClick = {
                                    imageUri = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500" // Simulated unknown
                                    step = 1
                                    scope.launch {
                                        delay(2500)
                                        step = 2
                                        isUnrecognized = true
                                    }
                                },
                                modifier = Modifier.weight(1f).height(56.dp)
                            ) {
                                Icon(Icons.Outlined.PhotoLibrary, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Gallery")
                            }
                        }
                    }
                }
                1 -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Analyzing Culinary Masterpiece...", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Text("Detecting ingredients and cooking methods.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                2 -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (isUnrecognized) {
                            Text("A True Chef's Experiment! 🧑‍🍳", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Your creation is so unique I couldn't find an exact match! I love your creativity. Let's document this original recipe together. What should we call it?", textAlign = TextAlign.Center)
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            OutlinedTextField(
                                value = manualDishName,
                                onValueChange = { manualDishName = it },
                                label = { Text("Dish Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { step = 3; detectedDish = manualDishName.ifEmpty { "My Custom Dish" } },
                                modifier = Modifier.fillMaxWidth().height(56.dp)
                            ) {
                                Text("Create Recipe")
                            }
                        } else {
                            Text("Match Found!", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                            Text(detectedDish, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            AsyncImage(
                                model = imageUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp))
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Button(
                                onClick = { step = 3 },
                                modifier = Modifier.fillMaxWidth().height(56.dp)
                            ) {
                                Text("Review & Edit Recipe")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = { step = 0; isUnrecognized = false }) {
                            Text("Try another photo")
                        }
                    }
                }
                3 -> {
                    // Editor
                    EditableRecipe(detectedDish, imageUri, onSuccessSave)
                }
            }
        }
    }
}

@Composable
fun EditableRecipe(initialTitle: String, imageUrl: String?, onSuccessSave: () -> Unit) {
    var title by remember { mutableStateOf(initialTitle) }
    var steps by remember { mutableStateOf("1. Heat pan.\n2. Cook ingredients.\n3. Serve hot.") }
    var ingredients by remember { mutableStateOf("Avocado, Bread, Salt") }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Text("Edit Your Recipe", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Recipe Name") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = ingredients,
            onValueChange = { ingredients = it },
            label = { Text("Ingredients (comma separated)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = steps,
            onValueChange = { steps = it },
            label = { Text("Steps") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { onSuccessSave() },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Save Recipe to Cookbook")
        }
        
        Spacer(modifier = Modifier.height(100.dp)) // padding for bottom nav
    }
}
