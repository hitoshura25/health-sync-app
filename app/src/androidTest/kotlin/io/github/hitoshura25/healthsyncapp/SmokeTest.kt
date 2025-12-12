package io.github.hitoshura25.healthsyncapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.uiAutomator
import androidx.test.uiautomator.UiAutomatorTestScope
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke test using modern UI Automator 2.4 API.
 *
 * This test works with BOTH debug and release APKs because UI Automator
 * interacts with the app externally (doesn't require debuggable build).
 *
 * Primary purpose: Validate app launches without crashing.
 * For release builds: Validates ProGuard/R8 didn't break critical code paths.
 *
 * @see https://developer.android.com/training/testing/other-components/ui-automator
 */
@RunWith(AndroidJUnit4::class)
class SmokeTest {

    companion object {
        private const val PACKAGE_NAME = "io.github.hitoshura25.healthsyncapp"
    }

    @Test
    fun appLaunches_doesNotCrash() = uiAutomator {
        // Start the app
        startApp(PACKAGE_NAME)

        // Wait for app to be visible
        waitForAppToBeVisible(PACKAGE_NAME)

        // Handle HealthConnect permission dialogs if they appear
        handleHealthConnectPermissions()

        // Verify app is running by checking for any element with our package
        val appElement = onElementOrNull(5000) {
            packageName == PACKAGE_NAME
        }

        assertThat(appElement).isNotNull()
    }

    @Test
    fun appLaunches_hasVisibleContent() = uiAutomator {
        // Start the app
        startApp(PACKAGE_NAME)
        waitForAppToBeVisible(PACKAGE_NAME)

        // Handle permissions
        handleHealthConnectPermissions()

        // Verify app has UI content (didn't crash to blank screen)
        val appStillRunning = onElementOrNull(2000) {
            packageName == PACKAGE_NAME
        }

        assertThat(appStillRunning).isNotNull()
    }

    // =========================================================================
    // HealthConnect Permission Handling
    // =========================================================================

    /**
     * Navigate through HealthConnect permission UI.
     *
     * HealthConnect has a multi-screen permission flow:
     * 1. Data permissions screen - toggle "Allow all" then click "Allow"
     * 2. Background access screen - click "Allow"
     */
    private fun UiAutomatorTestScope.handleHealthConnectPermissions() {
        // Screen 1: Data permissions ("fitness and wellness data")
        val dataPermScreen = onElementOrNull(3000) {
            text?.contains("fitness and wellness data") == true
        }

        if (dataPermScreen != null) {
            // Click "Allow all" toggle to enable all permissions
            onElementOrNull(1000) { text == "Allow all" }?.click()

            // Click "Allow" button at bottom
            onElement {
                text == "Allow" && className == "android.widget.Button"
            }.click()
        }

        // Screen 2: Background access ("access data in the background")
        val backgroundScreen = onElementOrNull(2000) {
            text?.contains("access data in the background") == true
        }

        if (backgroundScreen != null) {
            // Click "Allow" button
            onElement {
                text == "Allow" && className == "android.widget.Button"
            }.click()
        }
    }

    // =========================================================================
    // Standard Runtime Permissions (Optional)
    // =========================================================================

    /**
     * Handle standard Android runtime permission dialogs.
     */
    private fun UiAutomatorTestScope.handleRuntimePermissions() {
        val allowButton = onElementOrNull(1000) {
            text?.matches(Regex("(?i)allow|while using the app")) == true
        }
        allowButton?.click()
    }
}
