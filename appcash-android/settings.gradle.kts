// » pluginManagement = dari mana Gradle mengambil plugin.
pluginManagement {
    // » Daftar alamat sumber:
    repositories {
        // » Google (plugin Android),
        google()
        // » Maven Central (pustaka umum),
        mavenCentral()
        // » Gradle Plugin Portal (plugin Gradle).
        gradlePluginPortal()
    }
}

// » dependencyResolutionManagement = dari mana pustaka diunduh.
dependencyResolutionManagement {
    // » Larang modul menambah alamat sendiri (semua harus lewat sini).
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    // » Daftar alamat sumber pustaka:
    repositories {
        // » Google,
        google()
        // » Maven Central.
        mavenCentral()
    }
}

// » Nama proyek: AppCash.
rootProject.name = "AppCash"
// » Modul yang dimasukkan: app (isi aplikasi sebenarnya).
include(":app")
