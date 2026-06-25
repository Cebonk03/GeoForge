java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

dependencies {
    implementation(project(":api"))
    compileOnly(libs.paperApi121)
    testImplementation(libs.bundles.junitTesting)
    testImplementation(libs.bundles.mockbukkitTesting)
    testImplementation(libs.bundles.mockitoTesting)
}
