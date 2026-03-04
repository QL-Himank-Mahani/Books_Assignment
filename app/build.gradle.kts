import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.parcelize)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
localProperties.load(FileInputStream(localPropertiesFile))
localPropertiesFile.inputStream().use {
    localProperties.load(it)
}
val nytApiKey = localProperties.getProperty("NYT_API_KEY") ?: ""

android {
    namespace = "com.himank.booksassignment"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.himank.booksassignment"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "NYT_API_KEY", "\"$nytApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures{
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.fragment.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    implementation("androidx.datastore:datastore-preferences:1.2.0")

    // Alternatively - without an Android dependency.
    implementation("androidx.datastore:datastore-preferences-core:1.2.0")
    implementation("androidx.datastore:datastore-preferences-rxjava2:1.2.0")

    // optional - RxJava3 support
    implementation("androidx.datastore:datastore-preferences-rxjava3:1.2.0")
    implementation("androidx.datastore:datastore:1.2.0")

    // Alternatively - without an Android dependency.
    implementation("androidx.datastore:datastore-core:1.2.0")

    implementation("androidx.datastore:datastore-rxjava2:1.2.0")

    // optional - RxJava3 support
    implementation("androidx.datastore:datastore-rxjava3:1.2.0")

    implementation("androidx.palette:palette-ktx:1.0.0")

    // implementation("com.google.android.material:material:1.11.0")

    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("androidx.compose.material3:material3-window-size-class:1.4.0")
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.5.0-alpha14")

    // splash screen
    implementation("androidx.core:core-splashscreen:1.0.1")
}
