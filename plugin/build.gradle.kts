plugins {
    id("com.gradleup.shadow") version "9.4.2"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

dependencies {
    implementation(project(":api"))
    implementation(project(":engine"))
    implementation(project(":adapters:v1_21_x"))
    implementation(project(":adapters:v26_x"))
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.0")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.110.0")
    testImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}

tasks.shadowJar {
    archiveBaseName.set("GeoForge")
    archiveClassifier.set("")
    relocate("com.gradleup", "com.geoforge.shadow.gradleup")
}
