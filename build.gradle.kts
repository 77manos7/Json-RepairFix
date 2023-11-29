plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("com.melloware:jintellitype:1.4.1")
    implementation ("com.fasterxml.jackson.core:jackson-core:2.16.0")
    implementation ("com.fasterxml.jackson.core:jackson-databind:2.16.0")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}