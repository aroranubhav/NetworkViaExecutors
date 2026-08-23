plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
    explicitApi()
}

dependencies {
    testImplementation(kotlin("test")) //"org.jetbrains.kotlin:kotlin-test"
    testImplementation(libs.junit) //TODO: should they not be declared in it's own catalog file?
}
