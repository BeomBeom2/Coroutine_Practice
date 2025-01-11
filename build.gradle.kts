plugins {
    kotlin("jvm") version "1.9.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-test", "1.7.3")
    implementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    implementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")

}


tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}
