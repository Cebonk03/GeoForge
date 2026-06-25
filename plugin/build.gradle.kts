plugins {
    alias(libs.plugins.shadow)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

dependencies {
    implementation(project(":api"))
    implementation(project(":engine"))
    implementation(project(":adapters:v1_21_x"))
    implementation(project(":adapters:v26_x"))
    compileOnly(libs.paperApi26)
    testImplementation(libs.bundles.junitTesting)
    testImplementation(libs.bundles.mockbukkitTesting)
    testImplementation(libs.bundles.mockitoTesting)
    testImplementation(libs.paperApi121)
}

tasks.shadowJar {
    archiveBaseName.set("GeoForge")
    archiveClassifier.set("")
    relocate("com.gradleup", "com.geoforge.shadow.gradleup")
}
