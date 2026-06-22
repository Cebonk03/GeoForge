plugins {
    java
    jacoco
    id("com.gradleup.shadow") version "9.4.2" apply false
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "jacoco")

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    // Compile with all warnings + faster incremental
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.isIncremental = true
        options.compilerArgs.add("-Xlint:all,-processing,-path,-serial")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        maxParallelForks = 2
        forkEvery = 100
        minHeapSize = "128m"
        maxHeapSize = "512m"
        jvmArgs(
            "-XX:+UseParallelGC",
            "-XX:ParallelGCThreads=2",
            "-Dorg.gradle.appname=geoforge-tests"
        )
    }
    dependencies {
        // JUnit Platform Launcher required by Gradle 9.x for test execution
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    extensions.configure<JacocoPluginExtension> {
        toolVersion = "0.8.15"
    }
}
