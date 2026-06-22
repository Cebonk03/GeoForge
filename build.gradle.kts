plugins {
    java
    jacoco
    checkstyle
    id("com.gradleup.shadow") version "9.4.2" apply false
    id("com.github.spotbugs") version "6.5.8" apply false
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "jacoco")
    apply(plugin = "checkstyle")
    apply(plugin = "com.github.spotbugs")
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }

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
        jvmArgs("-XX:+UseParallelGC", "-XX:ParallelGCThreads=2")
    }

    dependencies {
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        testImplementation("org.assertj:assertj-core:3.27.7")
    }

    jacoco {
        toolVersion = "0.8.15"
    }

    checkstyle {
        toolVersion = "13.6.0"
        isIgnoreFailures = false
        maxWarnings = 0
        maxErrors = 0
        configFile = rootProject.layout.projectDirectory.file("config/checkstyle/checkstyle.xml").asFile
    }

    tasks.withType<Checkstyle> {
        reports {
            xml.required.set(false)
            html.required.set(true)
        }
    }
    // SpotBugs — extension-level config
    configure<com.github.spotbugs.snom.SpotBugsExtension> {
        toolVersion = "4.10.2"
        ignoreFailures.set(false)
        showStackTraces.set(true)
        showProgress.set(true)
    }

    afterEvaluate {
        tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
            excludeFilter.set(
                project.rootProject.layout.projectDirectory.file("spotbugs-exclude.xml"))
            reports.create("html") {
                required.set(true)
            }
        }
    }
}
