java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

dependencies {
    testImplementation(libs.bundles.junitTesting)
    testImplementation(libs.archunitJunit5)
}
