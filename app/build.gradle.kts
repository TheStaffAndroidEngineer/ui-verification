plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinCompose)
}

android {
    namespace = "staff.android.uiverification"
    compileSdk {
        version = release(37) { minorApiLevel = 1 }
    }
    buildToolsVersion = "37.0.0"
    defaultConfig {
        applicationId = "staff.android.uiverification"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val selectedClass = providers.gradleProperty("testClass").orNull
        when {
            providers.gradleProperty("brokenReturn").isPresent -> {
                testInstrumentationRunnerArguments["class"] = "staff.android.uiverification.NavigationObservationTest"
                testInstrumentationRunnerArguments["brokenReturn"] = "true"
            }
            selectedClass != null -> testInstrumentationRunnerArguments["class"] = selectedClass
            else -> testInstrumentationRunnerArguments["notAnnotation"] = "staff.android.uiverification.ExplicitSelection"
        }

    }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_26
        targetCompatibility = JavaVersion.VERSION_26
    }
}
kotlin {
    jvmToolchain(27)
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_26) }
}

dependencies {
    implementation(project(":core"))
    implementation(libs.androidxFragment)
    implementation(libs.androidxNavigation)
    implementation(libs.androidxCore)
    implementation(libs.androidxActivity)
    implementation(libs.androidxActivityCompose)
    implementation(libs.androidxLifecycleViewModel)
    implementation(libs.androidxLifecycleRuntime)
    implementation(libs.androidxLifecycleRuntimeCompose)
    implementation(libs.androidxLifecycleSavedState)
    implementation(libs.dataStore)
    implementation(libs.coroutinesAndroid)
    val composeBom = platform(libs.composeBom)
    implementation(composeBom)
    implementation(libs.composeUi)
    implementation(libs.composeFoundation)
    implementation(libs.composeMaterial3)
    implementation(libs.composeToolingPreview)
    debugImplementation(libs.composeTooling)
    debugImplementation(libs.composeTestManifest)
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.composeTestJunit4)
    androidTestImplementation(libs.androidxTestJunit)
    androidTestImplementation(libs.androidxTestRunner)
    androidTestImplementation(libs.espressoCore)
    androidTestImplementation(libs.composeTestAccessibility)
    androidTestImplementation(libs.espressoAccessibility)
}
