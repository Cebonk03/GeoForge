java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

dependencies {
    implementation(project(":api"))
    compileOnly(libs.paperApi26)
    testImplementation(libs.bundles.junitTesting)
    testImplementation(libs.bundles.mockitoTesting)
    testImplementation(libs.paperApi26)
}
