java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

dependencies {
    // @SuppressFBWarnings annotations have source+class retention, needed at compile time
    compileOnly(libs.spotbugsAnnotations)
    testImplementation(libs.bundles.junitTesting)
    testImplementation(libs.archunitJunit5)
    testImplementation("org.openjdk.jmh:jmh-core:1.37")
    testAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

tasks.register<JavaExec>("jmh") {
    group = "benchmark"
    description = "Run JMH microbenchmarks"
    classpath = sourceSets["test"].runtimeClasspath
    mainClass = "org.openjdk.jmh.Main"
    args(".*")
}
