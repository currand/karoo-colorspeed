import groovy.json.JsonBuilder
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.devtools.ksp)
}

val projectName = "karoo-colorspeed"
val screenshotBaseNames = listOf(
    "example1.png", "example2.png", "example3.png", "example4.png",
    "config_screen.png", "config_screen2.png"
)
val projectLabel = "Karoo Color Speed"
val projectDescription = "Colored Speed and icon indicator based on ride or lap average speed"
val projectDeveloper = "currand"

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget("17")
    }
}

android {
    namespace = "com.currand60.karoocolorspeed"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.currand60.karoocolorspeed"
        minSdk = 23
        targetSdk = 34
        versionCode = 250100102
        versionName = "0.4.5"
        signingConfig = signingConfigs.getByName("debug")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            merges += "META-INF/LICENSE.md"
            merges += "META-INF/LICENSE-notice.md"
        }
    }
}

dependencies {
    implementation(libs.hammerhead.karoo.ext)
    implementation(libs.androidx.core.ktx)
    implementation(libs.bundles.androidx.lifeycle)
    implementation(libs.androidx.activity.compose)
    implementation(libs.bundles.compose.ui)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.preview)
    implementation(libs.androidx.glance.appwidget.preview)
    implementation(libs.androidx.navigation.runtime.android)
    implementation(libs.javax.inject)
    implementation(libs.timber)
    implementation(libs.kotlinx.coroutines.debug)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.androidx.material.icons.extended)
}

tasks.register("generateManifest") {
    description = "Generates manifest.json with current version information"
    group = "build"

    // Retrieve the preReleaseVersion property at configuration time.
    // This makes it a declared input to the task.
    val preReleaseVersion = project.providers.gradleProperty("preReleaseVersion")
        .getOrElse("")

    // Declare the preReleaseVersion as an input property for Gradle's up-to-date checks.
    // If this value doesn't change, the task won't re-run.
    inputs.property("preReleaseVersion", preReleaseVersion)
    val manifestOutputFile = layout.projectDirectory.file("manifest.json")
    outputs.file(manifestOutputFile).withPropertyName("manifestFile")

    // The task action, equivalent to the @TaskAction method in the class-based approach.
    doLast {
        val androidExtension = project.extensions.getByName("android") as com.android.build.gradle.AppExtension
        val defaultConfig = androidExtension.defaultConfig

        val manifestFile = manifestOutputFile.asFile

        // Use the 'preReleaseVersion' value obtained at configuration time
        val currentPreReleaseVersion = preReleaseVersion.takeIf { it.isNotBlank() }
        println("Debug: preReleaseVersion from task input: $currentPreReleaseVersion")

        val releasePathComponent = if (currentPreReleaseVersion != null) {
            "download/$currentPreReleaseVersion" // e.g., "download/v0.4.2-pre-release"
        } else {
            "latest/download" // Default for stable releases or local builds
        }

        val baseUrl = "https://github.com/$projectDeveloper/$projectName/releases/$releasePathComponent"
        val apkFileName = "app-release.apk"

        val screenshotUrls = screenshotBaseNames.map { "$baseUrl/$it" }

        // Construct the manifest as a Map
        val manifest = mapOf(
            "label" to projectLabel,
            "packageName" to androidExtension.namespace,
            "latestApkUrl" to "$baseUrl/$apkFileName",
            "latestVersion" to defaultConfig.versionName,
            "latestVersionCode" to defaultConfig.versionCode,
            "developer" to projectDeveloper,
            "description" to projectDescription,
            "screenshotUrls" to screenshotUrls
        )

        // Use groovy.json.JsonBuilder to serialize the Map to a pretty-printed JSON string
        val gson = JsonBuilder(manifest).toPrettyString()
        manifestFile.writeText(gson)
        println("Generated manifest.json with download path: $releasePathComponent at ${manifestFile.absolutePath}")
    }
}


tasks.named("assemble") {
    dependsOn("generateManifest")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}