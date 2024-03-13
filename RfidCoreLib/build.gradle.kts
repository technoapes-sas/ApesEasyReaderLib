@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    id("java-library")
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    //implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.arr", "*.jar"))))
    //api(files("libs/API3_LIB-release-2.0.2.110.aar"))
    implementation("com.zebra:rfid-android:2.0.2.110")
    implementation(libs.androidx.support.v4)
    implementation(libs.coroutine.core)
}