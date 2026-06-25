java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

dependencies {
    implementation(project(":engine"))
    compileOnly(libs.paperApi121)
    testImplementation(libs.bundles.junitTesting)
}
