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
        versionCode = 12
        versionName = "1.0.11"
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("CONSOLE_KEYSTORE_PATH") ?: "${System.getProperty("user.home")}/toolchain/lzdz-release.keystore")
            storePassword = System.getenv("CONSOLE_STORE_PASSWORD") ?: "lzdz123456"
            keyAlias = "lzdz-release"
            keyPassword = System.getenv("CONSOLE_KEY_PASSWORD") ?: "lzdz123456"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
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
