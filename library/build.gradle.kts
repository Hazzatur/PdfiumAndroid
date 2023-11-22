plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "com.shockwave.pdfium"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/kotlin")
            resources.srcDirs("src/main/res")
            manifest.srcFile("src/main/AndroidManifest.xml")
            assets.srcDirs("src/main/assets")
            jniLibs.srcDirs("src/main/libs")
        }
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
                version = "2.0.0"
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
