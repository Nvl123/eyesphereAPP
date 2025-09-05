import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
}

// Load local.properties
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}

android {
    namespace = "com.dicoding.eyesphere_nav"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.dicoding.eyesphere_nav"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Add BuildConfig fields for API keys
        val geminiApiKey = localProperties.getProperty("GEMINI_API_KEY") ?: ""
        if (geminiApiKey.isNotEmpty()) {
            buildConfigField("String", "GEMINI_API_KEY", "\"${geminiApiKey.trim()}\"")
        } else {
            buildConfigField("String", "GEMINI_API_KEY", "\"\"")
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
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
    kotlinOptions {
        jvmTarget = "11"
    }
    

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)
    
    // HTTP client for server communication
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    
    // LocalBroadcastManager (for backward compatibility)
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")
    
    // Gemini AI for translation
    implementation("com.google.ai.client.generativeai:generativeai:0.1.2")
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Shimmer effect library
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.github.ibrahimsn98:SmoothBottomBar:1.7.9")
    implementation ("androidx.camera:camera-core:1.3.2")
    implementation ("androidx.camera:camera-camera2:1.3.2")
    implementation ("androidx.camera:camera-lifecycle:1.3.2")
    implementation ("androidx.camera:camera-view:1.3.2")
    implementation("androidx.camera:camera-extensions:1.3.2")
    implementation("com.github.mmmelik:RoundedImageView:v1.0.1")
    implementation("com.github.Cutta:GifView:1.6")
    
    // No external SharedPreferences library needed - using standard Android SharedPreferences

}