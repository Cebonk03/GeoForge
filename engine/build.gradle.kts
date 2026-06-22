java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.0")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.4.2")
}
