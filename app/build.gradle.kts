plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
	id("kotlin-parcelize")
}

android {
    namespace = "com.example.budgiebudgettracking"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.budgiebudgettracking"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        // Enable Java 8+ API desugaring
        isCoreLibraryDesugaringEnabled = true
        // Java compatibility
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // Desugaring library for Java 8+ APIs
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.8")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    implementation("net.objecthunter:exp4j:0.4.8")
    implementation("com.github.skydoves:colorpickerview:2.2.4")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
}

