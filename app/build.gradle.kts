plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
	alias(libs.plugins.ksp)
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
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

	implementation(libs.androidx.room.runtime)
	implementation(libs.androidx.room.ktx)
	ksp(libs.androidx.room.compiler) // For Kotlin Symbol Processing
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

	implementation("com.github.bumptech.glide:glide:4.12.0")
}
