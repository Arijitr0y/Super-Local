import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties


// --- Generate Env.kt from local.properties into build/ (not in VCS) ---
//import java.util.Properties

val props = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

val genDir = layout.buildDirectory.dir("generated/env/commonMain/kotlin")
val generateEnv by tasks.registering {
    // Fail fast if missing
    val url = props.getProperty("SUPABASE_URL")
        ?: error("Missing SUPABASE_URL in local.properties")
    val key = props.getProperty("SUPABASE_ANON_KEY")
        ?: error("Missing SUPABASE_ANON_KEY in local.properties")

    outputs.dir(genDir)

    doLast {
        val pkg = "org.assidious.superlocal.config"
        val outFile = genDir.get().file("${pkg.replace('.', '/')}/Env.kt").asFile
        outFile.parentFile.mkdirs()
        outFile.writeText(
            """
            package $pkg

            // Auto-generated from local.properties during build
            object Env {
                const val SUPABASE_URL = "$url"
                const val SUPABASE_ANON_KEY = "$key"
            }
            """.trimIndent()
        )
    }
}

// Make the generated dir part of commonMain sources
kotlin.sourceSets.getByName("commonMain").kotlin.srcDir(genDir)

// Ensure generation happens before any Kotlin compilation (Android/iOS)
tasks.configureEach {
    if (name.contains("Kotlin", ignoreCase = true)) {
        dependsOn(generateEnv)
    }
}
// Also make Android's preBuild depend on it (handy in Android Studio)
tasks.matching { it.name == "preBuild" }.configureEach {
    dependsOn(generateEnv)
}



plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlin.serialization)
    //id("com.codingfeline.buildkonfig") version "0.15.1"


}
//kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
// ---- Versions (single source of truth) ----
val ktor = "3.3.1"
val coroutines = "1.9.0"
val serializationJson = "1.7.3"
val supabase = "3.2.5"

kotlin {
    // ✅ Register Android target (removes your warning)
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
    }

    // iOS targets
    iosArm64()
    iosSimulatorArm64()
    val voyager = "1.0.0"
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
                implementation(compose.runtime)
                // ❌ REMOVE BOM / platform(...) — not supported here
                // implementation(platform("io.github.jan-tennert.supabase:bom:3.2.5"))

                // ✅ Pin Supabase modules explicitly to the same version
                val supabaseVersion = "3.0.1"

                implementation("io.github.jan-tennert.supabase:auth-kt:$supabaseVersion")
                implementation("io.github.jan-tennert.supabase:postgrest-kt:$supabaseVersion")
                implementation("io.github.jan-tennert.supabase:storage-kt:$supabaseVersion")

                // Ktor core + JSON
                implementation("io.ktor:ktor-client-core:$ktor")
                implementation("io.ktor:ktor-client-content-negotiation:$ktor")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor")

                // Coroutines + Serialization
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationJson")

                //voyager nav
                implementation("cafe.adriel.voyager:voyager-navigator:${voyager}")
                implementation("cafe.adriel.voyager:voyager-tab-navigator:${voyager}")
                implementation("cafe.adriel.voyager:voyager-transitions:${voyager}")

                implementation(compose.materialIconsExtended)
                //russhwolf
                implementation("com.russhwolf:multiplatform-settings:1.3.0")
            }
        }

        // ---------- Android ----------
        val androidMain by getting {
            dependencies {
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)
                implementation(compose.uiTooling)

                // ✅ Use ONE Ktor version; OkHttp is the typical Android engine
                implementation("io.ktor:ktor-client-okhttp:$ktor")
            }
        }

        // ---------- iOS ----------
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

        // Optional: read Supabase keys from local.properties
        val props = Properties().apply {
            val lp = rootProject.file("local.properties")
            if (lp.exists()) lp.inputStream().use { load(it) }
        }
        buildConfigField("String", "SUPABASE_URL", "\"${props.getProperty("SUPABASE_URL", "")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${props.getProperty("SUPABASE_ANON_KEY", "")}\"")
    }

    buildFeatures { buildConfig = true }

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
