package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.presentation.CookrViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import androidx.test.core.app.ApplicationProvider
import com.example.CookrApplication

@RunWith(RobolectricTestRunner::class)
class AppCrashTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testIntroScreenDoesNotCrash() {
        composeTestRule.setContent {
            com.example.presentation.screens.IntroScreen(onSignUp = {})
        }
    }
    
    @Test
    fun testAppContentDoesNotCrash() {
        val application = ApplicationProvider.getApplicationContext<CookrApplication>()
        val repository = application.container.cookrRepository
        val viewModel = CookrViewModel(application, repository)
        composeTestRule.setContent {
            com.example.presentation.screens.AppContent(viewModel)
        }
    }

    @Test
    fun testMainActivity() {
        org.robolectric.Robolectric.buildActivity(com.example.MainActivity::class.java).setup().get()
    }
}
