plugins {
    id("com.android.application")
}

android {
    namespace = "com.yuntai"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.yuntai"
        minSdk = 24
        targetSdk = 36
        versionCode = 4
        versionName = "1.0.3"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // FileProvider（控制台自更新安装 APK 用）
    implementation("androidx.core:core:1.13.1")
}
