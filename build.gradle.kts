plugins {
    java
    jacoco
    checkstyle
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.spotbugs) apply false
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "jacoco")
    apply(plugin = "checkstyle")
    apply(plugin = "com.github.spotbugs")

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

    // JaCoCo and static analysis tool versions — read from version catalog at configuration time
    jacoco {
        toolVersion = rootProject.libs.versions.jacoco.get()
    }

    checkstyle {
        toolVersion = rootProject.libs.versions.checkstyleTool.get()
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

    configure<com.github.spotbugs.snom.SpotBugsExtension> {
        toolVersion = rootProject.libs.versions.spotbugsTool.get()
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
