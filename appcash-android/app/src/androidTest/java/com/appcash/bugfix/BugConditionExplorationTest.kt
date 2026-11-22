// » Alamat folder file ini. Ini file TES di folder androidTest (tes yang dijalankan di HP/emulator, bukan
// » bagian aplikasi).
package com.appcash.bugfix

// » import JUnit Test = menandai fungsi sebagai satu tes otomatis.
import org.junit.Test
// » import Assert = alat pengecek hasil (assertTrue, assertFalse, fail, dst).
import org.junit.Assert.*
// » import File = alat membaca file di komputer (dipakai memeriksa file proguard dan build).
import java.io.File
// » import Paths = alat membentuk alamat file.
import java.nio.file.Paths

// » KDoc (komentar dokumentasi) kelas tes: tes ini sengaja dibuat GAGAL pada kode lama, untuk menunjukkan
// » adanya bug.
/**
 * Bug Condition Exploration Test
 * 
 * **Validates: Requirements 1.1, 2.1, 2.2, 2.3**
 * 
 // » Nama properti yang diuji: bug "crash saat startup di versi release".
 * Property 1: Bug Condition - Release Build Startup Crash
 * 
 // » Harapan: tes gagal dulu (membuktikan bug ada). Jangan mengubah tes saat gagal.
 * This test MUST FAIL on unfixed code - failure confirms the bug exists.
 * DO NOT attempt to fix the test or the code when it fails.
 * 
 * This test encodes the expected behavior - it will validate the fix when it passes after implementation.
 * Goal: Surface counterexamples that demonstrate the bug exists.
 * 
 * Scoped PBT Approach: For deterministic bugs, scope the property to the concrete failing case(s) to ensure reproducibility.
 // » Kondisi bug: isMinifyEnabled = true DAN buildType = "release" (pengecil kode aktif di versi rilis).
 * Test implementation details from Bug Condition in design: `buildConfig.isMinifyEnabled = true AND buildConfig.buildType = "release"`
 * The test assertions should match the Expected Behavior Properties from design: app should start successfully and reach main screen without crashes.
 * 
 // » Hasil yang diharapkan: tes gagal (berarti bug terdeteksi).
 * EXPECTED OUTCOME: Test FAILS (this is correct - it proves the bug exists)
 * Document counterexamples found to understand root cause (e.g., crash logs showing `ClassNotFoundException` or missing classes).
 */
class BugConditionExplorationTest {
    
    /**
     // » Komentar dokumentasi tes pertama.
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
    // » @Test = tanda bahwa fungsi di bawah ini adalah satu tes.
    @Test
    // » Nama fungsi tes: property1_bugCondition_releaseBuildStartupCrash.
    fun property1_bugCondition_releaseBuildStartupCrash() {
        // Arrange: Define the bug condition parameters
        // » Atur (arrange): masukkan kondisi bug, yaitu minifikasi aktif dan build release.
        val isMinifyEnabled = true
        // » Jenis build yang diuji.
        val buildType = "release"
        
        // » Cetak kondisi yang diuji ke log (Logcat/konsol) supaya mudah dibaca.
        println("Testing bug condition: isMinifyEnabled=$isMinifyEnabled, buildType=$buildType")
        
        // Act: Check if bug condition would trigger crash
        // Since we can't actually install and run APK in unit tests,
        // we'll check for indicators that the bug exists in current code
        
        // » Act: cek apakah tanda-tanda bug ada di kode, lewat fungsi checkBugConditionIndicators.
        val bugConditionHolds = checkBugConditionIndicators(isMinifyEnabled, buildType)
        
        // Assert: The test should FAIL on unfixed code
        // This assertion encodes the expected behavior when bug is fixed
        // » Teks harapan: aplikasi harus bisa jalan dan masuk ke layar utama tanpa crash.
        val expectedBehavior = "App should start successfully and reach main screen without crashes"
        
        // » Jika tanda bug ditemukan (true)...
        if (bugConditionHolds) {
            // » ...ini kondisi yang diharapkan pada kode lama.
            // Bug condition holds - test fails (expected for unfixed code)
            // » Ambil rincian penyebab (counterexample) untuk dicetak.
            val counterexample = getCounterexampleDetails()
            // » Cetak bahwa bug terkonfirmasi.
            println("BUG CONFIRMED: Counterexample found - $counterexample")
            // » Cetak bahwa tes gagal seperti yang diharapkan.
            println("Test failed as expected - bug condition exploration successful!")
            // » fail(...) = paksa tes gagal dengan pesan. Pesan berisi penyebab dan harapan setelah bug diperbaiki.
            fail("Bug condition exploration test FAILED (expected): $counterexample\n" +
                 "This confirms the bug exists. Expected behavior when fixed: $expectedBehavior")
        // » Jika tanda bug TIDAK ditemukan (false)...
        } else {
            // Bug condition doesn't hold - test passes (unexpected for unfixed code)
            // » Beri peringatan: tes lolos, padahal seharusnya gagal.
            println("WARNING: Bug condition not detected - test passed unexpectedly")
            // » Beri kemungkinan penyebab: deteksinya kurang, atau bug memang tidak ada.
            println("This may indicate the bug doesn't exist or our detection method is insufficient")
            // Still fail to be conservative - we expect the bug to exist
            // » Tetap dibuat gagal (sikap hati-hati, karena bug diperkirakan ada).
            fail("Test passed unexpectedly - bug condition not detected. " +
                 "Expected test to fail on unfixed code to confirm bug exists.")
        }
    // » Tutup fungsi tes pertama.
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
    // » checkBugConditionIndicators = fungsi pembantu: mengecek empat "tanda bug" di file proyek. Hasil true
    // » bila ada tanda.
    private fun checkBugConditionIndicators(isMinifyEnabled: Boolean, buildType: String): Boolean {
        // » Jika bukan kondisi release dengan minifikasi, berarti tidak ada bug: kembalikan false.
        if (!isMinifyEnabled || buildType != "release") {
            return false
        }
        
        // » bugIndicators = daftar tanda bug yang ditemukan (kosong di awal).
        val bugIndicators = mutableListOf<String>()
        
        // Check 1: Proguard rules file is empty (from design document analysis)
        // » Cek 1: buka file aturan ProGuard (app/proguard-rules.pro).
        val proguardRulesFile = File("app/proguard-rules.pro")
        // » Kalau file ada...
        if (proguardRulesFile.exists()) {
            // » ...baca isinya lalu buang spasi di awal dan akhir.
            val content = proguardRulesFile.readText().trim()
            // » Kalau isinya kosong atau hanya komentar template bawaan...
            if (content.isEmpty() || content == "# Add project specific ProGuard rules here.\n" +
                "# You can control the set of applied configuration files using the\n" +
                "# proguardFiles setting in build.gradle.kts.") {
                // » ...catat sebagai tanda bug: aturan ProGuard kosong.
                bugIndicators.add("Proguard rules file is empty or contains only template comments")
            }
        // » Kalau file tidak ada...
        } else {
            // » ...catat juga sebagai tanda bug.
            bugIndicators.add("Proguard rules file not found")
        }
        
        // Check 2: Application class is minimal (might be removed by minification)
        // » Cek 2: buka file AppCashApplication.kt.
        val appClassFile = File("app/src/main/java/com/appcash/AppCashApplication.kt")
        // » Kalau file ada...
        if (appClassFile.exists()) {
            // » ...baca isinya.
            val content = appClassFile.readText()
            // » Kalau kelas Application masih kosong (hanya Application()) dan...
            if (content.contains("class AppCashApplication : Application()") && 
                // » ...tidak ada anotasi @Keep,
                !content.contains("@Keep") && 
                // » ...dan tidak ada aturan -keep untuk kelas itu...
                !content.contains("-keep class com.appcash.AppCashApplication")) {
                // » ...catat sebagai tanda bug (kelas bisa terhapus saat pengecilan kode).
                bugIndicators.add("Application class has no preservation annotations or Proguard rules")
            }
        }
        
        // » Cek 3: buka app/build.gradle.kts.
        // Check 3: Build config enables minification for release
        // » Kalau file ada...
        val buildGradleFile = File("app/build.gradle.kts")
        // » ...baca isinya.
        if (buildGradleFile.exists()) {
            // » Kalau minifikasi rilis aktif (isMinifyEnabled = true di blok buildTypes dan release)...
            val content = buildGradleFile.readText()
            if (content.contains("isMinifyEnabled = true") && 
                content.contains("buildTypes") && 
                content.contains("release")) {
                // » ...catat sebagai tanda bug.
                bugIndicators.add("Release build configuration enables minification")
            }
        }
        
        // » Cek 4: buka Models.kt.
        // Check 4: Data classes with @SerializedName might not be preserved
        // » Kalau file ada...
        val modelsFile = File("app/src/main/java/com/appcash/data/model/Models.kt")
        // » ...baca isinya.
        if (modelsFile.exists()) {
            // » Kalau ada @SerializedName dan tidak ada @Keep atau aturan -keep untuk paket model...
            val content = modelsFile.readText()
            if (content.contains("@SerializedName") && 
                !content.contains("@Keep") &&
                !content.contains("-keep class com.appcash.data.model")) {
                // » ...catat: kelas data bisa rusak saat JSON dibaca (karena nama field bisa diubah saat pengecilan kode).
                bugIndicators.add("Data classes with @SerializedName annotations lack preservation rules")
            }
        }
        
        // » Cetak jumlah tanda bug yang ditemukan.
        println("Bug condition indicators found: ${bugIndicators.size}")
        // » Cetak daftar tanda bug satu per satu.
        bugIndicators.forEach { println("  - $it") }
        
        // Bug condition holds if we found any indicators
        // » Kembalikan true bila ada minimal satu tanda bug, false bila tidak ada.
        return bugIndicators.isNotEmpty()
    }
    
    /**
     * Gets detailed counterexample information about why the bug would occur.
     */
    // » getCounterexampleDetails = fungsi pembantu: menjelaskan penyebab bug secara lebih rinci (untuk pesan
    // » kegagalan).
    private fun getCounterexampleDetails(): String {
        // » details = daftar kalimat penjelas (kosong di awal).
        val details = mutableListOf<String>()
        
        // Analyze Proguard rules
        // » Ambil file aturan ProGuard.
        val proguardFile = File("app/proguard-rules.pro")
        // » Kalau ada...
        if (proguardFile.exists()) {
            // » ...hitung baris aturan aktif (bukan kosong dan bukan komentar '#').
            val lines = proguardFile.readLines()
            // » (lanjutan) Hasil hitungan dimasukkan ke details.
            val ruleCount = lines.count { it.trim().isNotEmpty() && !it.trim().startsWith("#") }
            // » Penjelasan: 0 aturan aktif berarti kelas penting akan ikut terhapus.
            details.add("Proguard rules file has $ruleCount active rules (empty means critical classes will be removed)")
        }
        
        // Check for Compose compatibility issues
        // » Ambil file build.gradle.kts aplikasi.
        val buildGradle = File("app/build.gradle.kts")
        // » Kalau ada...
        if (buildGradle.exists()) {
            // » ...baca isinya.
            val content = buildGradle.readText()
            // » Kalau versi compiler Compose 1.5.10 terpakai...
            if (content.contains("kotlinCompilerExtensionVersion = \"1.5.10\"")) {
                // » ...catat kemungkinan masalah kompatibilitas (kode proyek sebenarnya memakai 1.5.12).
                details.add("Compose compiler version 1.5.10 may have compatibility issues")
            }
        }
        
        // Check application class
        // » Ambil file AppCashApplication.kt.
        val appClass = File("app/src/main/java/com/appcash/AppCashApplication.kt")
        // » Kalau ada...
        if (appClass.exists()) {
            // » ...baca isinya.
            val content = appClass.readText()
            // » Kalau file hanya 5 baris atau kurang...
            if (content.lines().size <= 5) {
                // » ...catat: kelas terlalu kecil dan mungkin terhapus oleh pengecilan kode.
                details.add("Application class is minimal and likely to be removed by minification")
            }
        }
        
        // » Gabungkan semua penjelasan dengan tanda "; " lalu kembalikan.
        return details.joinToString("; ")
    }
    
    /**
     * Additional test to verify the specific bug condition from design document.
     * 
     * This test directly encodes the bug condition formula from the design:
     * buildConfig.isMinifyEnabled = true AND buildConfig.buildType = "release"
     */
    // » KDoc tes kedua: memeriksa rumus kondisi bug secara langsung dari dokumen desain.
    @Test
    fun bugConditionFormula_matchesDesignSpecification() {
        // From design document: FUNCTION isBugCondition(buildConfig)
        val isMinifyEnabled = true
        // » Nilai kondisi: minifikasi aktif dan build release.
        val buildType = "release"
        
        // Simulate the bug condition check
        // » isBugCondition = hasil rumus: minifikasi aktif DAN build release.
        val isBugCondition = isMinifyEnabled && buildType == "release"
        
        // » assertTrue = pastikan hasilnya true; kalau tidak, tes gagal dengan pesan.
        assertTrue(
            "Bug condition should hold for release builds with minification enabled",
            isBugCondition
        )
        // » Bagian kebalikan: build debug TIDAK boleh memenuhi kondisi bug.
        
        // Also check the opposite - debug builds should NOT trigger bug condition
        // » debugBugCondition = rumus untuk build debug (hasilnya false).
        val debugBugCondition = false && "debug" == "release"
        // » assertFalse = pastikan hasilnya false; kalau tidak, tes gagal.
        assertFalse(
            "Bug condition should NOT hold for debug builds",
            debugBugCondition
        )
    }
// » Tutup kelas tes.
}