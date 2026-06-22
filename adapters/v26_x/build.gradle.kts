java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

dependencies {
    implementation(project(":api"))
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.0")
    testImplementation("org.mockito:mockito-core:5.23.0")
    testImplementation("io.papermc.paper:paper-api:26.1.2.build.+")
    testImplementation("org.mockito:mockito-junit-jupiter:5.23.0")
}
