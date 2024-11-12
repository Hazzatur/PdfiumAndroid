plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

val versionName = "2.0.0"

android {
    namespace = "com.shockwave.pdfium"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        buildConfigField("String", "VERSION_NAME", "\"${versionName}\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
    }

    sourceSets {
        getByName("main") {
            java.srcDir("src/main/kotlin")
            manifest.srcFile("src/main/AndroidManifest.xml")
            jniLibs.srcDirs("src/main/jniLibs")
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }

    buildTypes {
        release { isMinifyEnabled = false }
    }
}

tasks.register<Jar>("sourceJar") {
    from(android.sourceSets["main"].java.srcDirs)
    from(fileTree(mapOf("dir" to "src/libs", "include" to listOf("*.jar"))))
    archiveClassifier.set("sources")
}

tasks.register<Jar>("androidSourcesJar") {
    archiveClassifier.set("sources")
    from(android.sourceSets["main"].java.srcDirs)
}

afterEvaluate {
    publishing {
        publications {
            create("release", MavenPublication::class) {
                from(components["release"])
                groupId = "com.github.hazzatur"
                artifactId = "pdfium-android"
                version = versionName
            }
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(libs.androidx.collection.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
}
