// » plugins = alat tambahan bagi Gradle (program yang membangun APK).
plugins {
    // » Plugin pembuat aplikasi Android.
    id("com.android.application")
    // » Plugin yang memahami bahasa Kotlin.
    id("org.jetbrains.kotlin.android")
}

// » android = pengaturan proyek Android.
android {
    // » namespace = alamat paket kode (com.appcash).
    namespace = "com.appcash"
    // » compileSdk = versi Android (35) yang dipakai saat menyusun kode.
    compileSdk = 35

    // » defaultConfig = pengaturan dasar aplikasi.
    defaultConfig {
        // » applicationId = ID unik aplikasi (com.appcash). Dipakai Android untuk membedakan aplikasi.
        applicationId = "com.appcash"
        // » minSdk = versi Android terendah yang didukung (26 = Android 8.0).
        minSdk = 26
        // » targetSdk = versi Android yang menjadi sasaran (35).
        targetSdk = 35
        // » versionCode = nomor versi untuk sistem (angka naik tiap rilis).
        versionCode = 8
        // » versionName = nama versi yang dibaca manusia (3.3.1, tampil di layar Profil).
        versionName = "3.3.1"
    }

    // » buildTypes = jenis hasil build.
    buildTypes {
        // » release = versi untuk dibagikan.
        release {
            // » isMinifyEnabled = true: kode dikecilkan dan diacak.
            isMinifyEnabled = true
            // » isShrinkResources = true: resource yang tidak terpakai dibuang agar APK kecil.
            isShrinkResources = true
            // » Ditandatangani dengan kunci debug bawaan.
            signingConfig = signingConfigs.getByName("debug")
            // » File aturan ProGuard (pengecil kode):
            proguardFiles(
                // » aturan standar Android,
                getDefaultProguardFile("proguard-android-optimize.txt"),
                // » aturan buatan proyek.
                "proguard-rules.pro"
            )
        }
        // » debug = versi untuk uji coba.
        debug {
            // » Tidak dikecilkan agar cepat dibuild.
            isMinifyEnabled = false
        }
    }

    // » compileOptions = versi bahasa Java yang dipakai:
    compileOptions {
        // » kode sumber setara Java 11,
        sourceCompatibility = JavaVersion.VERSION_11
        // » hasil setara Java 11.
        targetCompatibility = JavaVersion.VERSION_11
    }

    // » kotlinOptions = pengaturan Kotlin:
    kotlinOptions {
        // » target JVM 11.
        jvmTarget = "11"
        // » Mengizinkan fitur Material 3 eksperimental di seluruh proyek.
        freeCompilerArgs = freeCompilerArgs + listOf("-opt-in=androidx.compose.material3.ExperimentalMaterial3Api")
    }

    // » buildFeatures = fitur yang dinyalakan:
    buildFeatures {
        // » Compose (tampilan modern) aktif,
        compose = true
        // » BuildConfig aktif (dari sini VERSION_NAME dibaca layar Profil).
        buildConfig = true
    }

    // » composeOptions = pengaturan kompiler Compose:
    composeOptions {
        // » versi 1.5.12.
        kotlinCompilerExtensionVersion = "1.5.12"
    }

    // » packaging = aturan menyatukan file ke APK:
    packaging {
        // » resources:
        resources {
            // » abaikan file lisensi duplikat.
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// » dependencies = daftar pustaka (library) yang dipakai.
dependencies {
    // » lifecycleVersion = nomor versi pustaka lifecycle (dipakai di beberapa baris).
    val lifecycleVersion = "2.7.0"
    // » Catatan: BOM 2024.02.00 = Compose 1.6.x yang cocok dengan Kotlin 1.9.22 dan kompiler 1.5.12.
    // BOM 2024.02.00 = Compose 1.6.x — compatible with Kotlin 1.9.22 + Compiler 1.5.12
    // » composeBom = daftar versi Compose yang sudah cocok satu sama lain.
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")

    // » Pakai daftar versi tersebut.
    implementation(composeBom)
    // » Fungsi tambahan Android untuk Kotlin (core-ktx).
    implementation("androidx.core:core-ktx:1.12.0")
    // » Lifecycle runtime (mengikuti siklus hidup layar).
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    // » Penghubung ViewModel dengan Compose.
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")
    // » Penghubung Activity dengan Compose.
    implementation("androidx.activity:activity-compose:1.8.2")
    // » Compose UI dasar.
    implementation("androidx.compose.ui:ui")
    // » Compose grafis.
    implementation("androidx.compose.ui:ui-graphics")
    // » Pratinjau tampilan di Android Studio.
    implementation("androidx.compose.ui:ui-tooling-preview")
    // » Material 3 (komponen tampilan).
    implementation("androidx.compose.material3:material3")
    // » Foundation (layout, daftar, canvas).
    implementation("androidx.compose.foundation:foundation")

    // » Retrofit (permintaan internet, rancangan online).
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // » Penerjemah JSON untuk Retrofit.
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // » OkHttp (kurir internet).
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // » Pencatat log jaringan.
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")


    // » JUnit untuk tes biasa.
    testImplementation("junit:junit:4.13.2")
    // » Pustaka tes di perangkat.
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    // » Daftar versi Compose untuk tes.
    androidTestImplementation(composeBom)
    // » Tes UI Compose.
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    // » Alat bantu tampilan untuk debug.
    debugImplementation("androidx.compose.ui:ui-tooling")
    // » Manifest tes UI untuk debug.
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
