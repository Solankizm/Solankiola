package com.solankizm.ola.data.net

/**
 * Apps you can install onto the head unit. Solankizm does NOT bundle any of
 * these binaries — it downloads each from its own public URL only when you tap
 * Install, exactly as you would yourself. They are third-party, closed-source
 * apps; install them at your own risk.
 *
 * Android Auto and the music mod are not here because they have no public URL
 * (S1Mods ships them as private bundled files). Use "Install your own APK" for
 * those, or any other app you already have.
 */
data class CatalogApp(
    val id: String,
    val name: String,
    val packageName: String,
    val url: String,
    val sizeText: String,
    val tag: String,
    val description: String,
    val needsOverlay: Boolean = false,
)

object AppCatalog {
    val apps = listOf(
        CatalogApp(
            id = "openplay",
            name = "OpenPlay (CarPlay)",
            packageName = "com.asjohn720.openplay",
            url = "https://storage.googleapis.com/publish-apk/openplay-olas1-release.apk",
            sizeText = "~18 MB",
            tag = "CarPlay",
            description = "Apple CarPlay & phone projection on the MoveOS display.",
            needsOverlay = true,
        ),
        CatalogApp(
            id = "firefox_focus",
            name = "Firefox Focus",
            packageName = "org.mozilla.focus",
            url = "https://archive.mozilla.org/pub/focus/releases/159.0/android/focus-159.0-android-arm64-v8a/focus-159.0.multi.android-arm64-v8a.apk",
            sizeText = "~85 MB",
            tag = "Browser",
            description = "Private web browser for the head unit. Heavy on RAM.",
        ),
    )
}
