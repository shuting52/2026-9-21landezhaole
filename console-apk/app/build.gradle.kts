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
        versionCode = 14
        versionName = "1.0.13"
    }

    signingConfigs {
        create("release") {
            // v1.0.12 修复：优先使用仓库内固定签名密钥，保证每次构建签名一致，
            // 老版本控制台才能通过覆盖安装完成自更新（否则签名不同会被系统拒绝）。
            val repoKey = rootProject.file("../signing/lzdz-release.keystore")
            val envKey = System.getenv("CONSOLE_KEYSTORE_PATH")
            storeFile = when {
                envKey != null -> file(envKey)
                repoKey.exists() -> repoKey
                else -> file("${System.getProperty("user.home")}/toolchain/lzdz-release.keystore")
            }
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
