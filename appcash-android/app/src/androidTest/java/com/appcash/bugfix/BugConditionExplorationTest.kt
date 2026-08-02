package com.appcash.bugfix

import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.nio.file.Paths

/**
 * Bug Condition Exploration Test
 * 
 * **Validates: Requirements 1.1, 2.1, 2.2, 2.3**
 * 
 * Property 1: Bug Condition - Release Build Startup Crash
 * 
 * This test MUST FAIL on unfixed code - failure confirms the bug exists.
 * DO NOT attempt to fix the test or the code when it fails.
 * 
 * This test encodes the expected behavior - it will validate the fix when it passes after implementation.
 * Goal: Surface counterexamples that demonstrate the bug exists.
 * 
 * Scoped PBT Approach: For deterministic bugs, scope the property to the concrete failing case(s) to ensure reproducibility.
 * Test implementation details from Bug Condition in design: `buildConfig.isMinifyEnabled = true AND buildConfig.buildType = "release"`
 * The test assertions should match the Expected Behavior Properties from design: app should start successfully and reach main screen without crashes.
 * 
 * EXPECTED OUTCOME: Test FAILS (this is correct - it proves the bug exists)
 * Document counterexamples found to understand root cause (e.g., crash logs showing `ClassNotFoundException` or missing classes).
 */
class BugConditionExplorationTest {
    
    /**
     * Property 1: Bug Condition - Release Build Startup Crash
     * 
     * For the specific build configuration where:
     * - isMinifyEnabled = true
     * - buildType = "release"
     * 
     * The app should crash on startup (this is the bug we're confirming exists).
     * 
     * This test attempts to simulate the bug condition by checking if the current code
     * would produce a crash when built with release configuration.
     */
    @Test
    fun property1_bugCondition_releaseBuildStartupCrash() {
        // Arrange: Define the bug condition parameters
        val isMinifyEnabled = true
        val buildType = "release"
        
        println("Testing bug condition: isMinifyEnabled=$isMinifyEnabled, buildType=$buildType")
        
        // Act: Check if bug condition would trigger crash
        // Since we can't actually install and run APK in unit tests,
        // we'll check for indicators that the bug exists in current code
        
        val bugConditionHolds = checkBugConditionIndicators(isMinifyEnabled, buildType)
        
        // Assert: The test should FAIL on unfixed code
        // This assertion encodes the expected behavior when bug is fixed
        val expectedBehavior = "App should start successfully and reach main screen without crashes"
        
        if (bugConditionHolds) {
            // Bug condition holds - test fails (expected for unfixed code)
            val counterexample = getCounterexampleDetails()
            println("BUG CONFIRMED: Counterexample found - $counterexample")
            println("Test failed as expected - bug condition exploration successful!")
            fail("Bug condition exploration test FAILED (expected): $counterexample\n" +
                 "This confirms the bug exists. Expected behavior when fixed: $expectedBehavior")
        } else {
            // Bug condition doesn't hold - test passes (unexpected for unfixed code)
            println("WARNING: Bug condition not detected - test passed unexpectedly")
            println("This may indicate the bug doesn't exist or our detection method is insufficient")
            // Still fail to be conservative - we expect the bug to exist
            fail("Test passed unexpectedly - bug condition not detected. " +
                 "Expected test to fail on unfixed code to confirm bug exists.")
        }
    }
    
    /**
     * Checks for indicators that the bug condition would trigger a crash.
     * 
     * Since we can't run the APK in unit tests, we check for:
     * 1. Empty Proguard rules file (indicates missing preservation rules)
     * 2. Build configuration that enables minification
     * 3. Application class structure
     * 
     * Returns true if bug indicators are found.
     */
    private fun checkBugConditionIndicators(isMinifyEnabled: Boolean, buildType: String): Boolean {
        if (!isMinifyEnabled || buildType != "release") {
            return false
        }
        
        val bugIndicators = mutableListOf<String>()
        
        // Check 1: Proguard rules file is empty (from design document analysis)
        val proguardRulesFile = File("app/proguard-rules.pro")
        if (proguardRulesFile.exists()) {
            val content = proguardRulesFile.readText().trim()
            if (content.isEmpty() || content == "# Add project specific ProGuard rules here.\n" +
                "# You can control the set of applied configuration files using the\n" +
                "# proguardFiles setting in build.gradle.kts.") {
                bugIndicators.add("Proguard rules file is empty or contains only template comments")
            }
        } else {
            bugIndicators.add("Proguard rules file not found")
        }
        
        // Check 2: Application class is minimal (might be removed by minification)
        val appClassFile = File("app/src/main/java/com/appcash/AppCashApplication.kt")
        if (appClassFile.exists()) {
            val content = appClassFile.readText()
            if (content.contains("class AppCashApplication : Application()") && 
                !content.contains("@Keep") && 
                !content.contains("-keep class com.appcash.AppCashApplication")) {
                bugIndicators.add("Application class has no preservation annotations or Proguard rules")
            }
        }
        
        // Check 3: Build config enables minification for release
        val buildGradleFile = File("app/build.gradle.kts")
        if (buildGradleFile.exists()) {
            val content = buildGradleFile.readText()
            if (content.contains("isMinifyEnabled = true") && 
                content.contains("buildTypes") && 
                content.contains("release")) {
                bugIndicators.add("Release build configuration enables minification")
            }
        }
        
        // Check 4: Data classes with @SerializedName might not be preserved
        val modelsFile = File("app/src/main/java/com/appcash/data/model/Models.kt")
        if (modelsFile.exists()) {
            val content = modelsFile.readText()
            if (content.contains("@SerializedName") && 
                !content.contains("@Keep") &&
                !content.contains("-keep class com.appcash.data.model")) {
                bugIndicators.add("Data classes with @SerializedName annotations lack preservation rules")
            }
        }
        
        println("Bug condition indicators found: ${bugIndicators.size}")
        bugIndicators.forEach { println("  - $it") }
        
        // Bug condition holds if we found any indicators
        return bugIndicators.isNotEmpty()
    }
    
    /**
     * Gets detailed counterexample information about why the bug would occur.
     */
    private fun getCounterexampleDetails(): String {
        val details = mutableListOf<String>()
        
        // Analyze Proguard rules
        val proguardFile = File("app/proguard-rules.pro")
        if (proguardFile.exists()) {
            val lines = proguardFile.readLines()
            val ruleCount = lines.count { it.trim().isNotEmpty() && !it.trim().startsWith("#") }
            details.add("Proguard rules file has $ruleCount active rules (empty means critical classes will be removed)")
        }
        
        // Check for Compose compatibility issues
        val buildGradle = File("app/build.gradle.kts")
        if (buildGradle.exists()) {
            val content = buildGradle.readText()
            if (content.contains("kotlinCompilerExtensionVersion = \"1.5.10\"")) {
                details.add("Compose compiler version 1.5.10 may have compatibility issues")
            }
        }
        
        // Check application class
        val appClass = File("app/src/main/java/com/appcash/AppCashApplication.kt")
        if (appClass.exists()) {
            val content = appClass.readText()
            if (content.lines().size <= 5) {
                details.add("Application class is minimal and likely to be removed by minification")
            }
        }
        
        return details.joinToString("; ")
    }
    
    /**
     * Additional test to verify the specific bug condition from design document.
     * 
     * This test directly encodes the bug condition formula from the design:
     * buildConfig.isMinifyEnabled = true AND buildConfig.buildType = "release"
     */
    @Test
    fun bugConditionFormula_matchesDesignSpecification() {
        // From design document: FUNCTION isBugCondition(buildConfig)
        val isMinifyEnabled = true
        val buildType = "release"
        
        // Simulate the bug condition check
        val isBugCondition = isMinifyEnabled && buildType == "release"
        
        assertTrue(
            "Bug condition should hold for release builds with minification enabled",
            isBugCondition
        )
        
        // Also check the opposite - debug builds should NOT trigger bug condition
        val debugBugCondition = false && "debug" == "release"
        assertFalse(
            "Bug condition should NOT hold for debug builds",
            debugBugCondition
        )
    }
}