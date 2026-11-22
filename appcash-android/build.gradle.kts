// » Komentar bawaan: file build TINGKAT ATAS. Berisi pengaturan yang dipakai bersama oleh semua modul (di
// » proyek ini hanya modul "app").
// Top-level build file where you can add configuration options common to all sub-projects/modules.
// » plugins = daftar alat tambahan untuk Gradle (program pembangun). "apply false" = plugin hanya
// » didaftarkan versinya di sini, baru dipasang di app/build.gradle.kts.
plugins {
    // » Plugin Android (pembuat aplikasi .apk) versi 8.6.0.
    id("com.android.application") version "8.6.0" apply false
    // » Plugin Kotlin versi 1.9.23.
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
}

// » tasks.register("clean") = membuat perintah "clean" (membersihkan hasil build).
tasks.register("clean", Delete::class) {
    // » delete(...) = yang dihapus adalah folder build di tingkat atas proyek.
    delete(rootProject.layout.buildDirectory)
}
