import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties


plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

// ---- Versions used for new deps (safe to tweak later) ----
val ktor = "3.3.1"      // <— was 2.3.10, must match Supabase (Ktor 3)
val coroutines = "1.9.0"
val serializationJson = "1.7.3"
val supabase = "3.2.5"  // you already use 3.2.5 in code; keep this
kotlin {
    // Android target (keeps the warning away)
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
    }

    // iOS targets
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        // ---------- Common ----------
        val commonMain by getting {
            dependencies {
                // Compose (yours)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)

                // Supabase (no BOM in KMP common)
                val supabase = "3.2.5"

                implementation("io.github.jan-tennert.supabase:auth-kt:$supabase")
                implementation("io.github.jan-tennert.supabase:postgrest-kt:$supabase")
                implementation("io.github.jan-tennert.supabase:storage-kt:$supabase")


                // Ktor core + JSON
                implementation("io.ktor:ktor-client-core:$ktor")
                implementation("io.ktor:ktor-client-content-negotiation:$ktor")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor")

                // Coroutines + JSON lib
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationJson")
            }
        }

        // ---------- Android ----------
        val androidMain by getting {
            dependencies {
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)

                // Put uiTooling here (NOT in a top-level dependencies block)
                implementation(compose.uiTooling)

                // Ktor Android engine
                implementation("io.ktor:ktor-client-okhttp:$ktor")
            }
        }

        // ---------- iOS (use concrete sets; don't reference iosMain) ----------
        val iosArm64Main by getting {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:$ktor")
            }
        }
        val iosSimulatorArm64Main by getting {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:$ktor")
            }
        }

        // ---------- Tests ----------
        val commonTest by getting {
            dependencies { implementation(libs.kotlin.test) }
        }
    }
  }




android {
    namespace = "org.assidious.superlocal"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.assidious.superlocal"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        // Read SUPABASE_* from local.properties (safe if file is missing)
        val props = Properties().apply {
            val lp = rootProject.file("local.properties")
            if (lp.exists()) {
                lp.inputStream().use { load(it) }
            }
        }

        buildConfigField("String", "SUPABASE_URL", "\"${props.getProperty("SUPABASE_URL", "")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${props.getProperty("SUPABASE_ANON_KEY", "")}\"")

    }
    buildFeatures {
        buildConfig = true
    }
    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }

    buildTypes {
        getByName("release") { isMinifyEnabled = false }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
