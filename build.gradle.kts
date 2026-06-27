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

    // spotbugs-annotations for @SuppressFBWarnings on compile classpath
    dependencies {
        compileOnly(rootProject.libs.spotbugsAnnotations)
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

    // JaCoCo and static analysis tool versions — read from version catalog at configuration time
    jacoco {
        toolVersion = rootProject.libs.versions.jacoco.get()
    }

    tasks.withType<JacocoReport> {
        reports {
            html.required.set(true)
            xml.required.set(true)
            csv.required.set(false)
        }
    }

    tasks.withType<JacocoCoverageVerification> {
        violationRules {
            rule {
                enabled = true
                limit {
                    counter = "LINE"
                    value = "COVEREDRATIO"
                    minimum = "0.60".toBigDecimal()
                }
                limit {
                    counter = "BRANCH"
                    value = "COVEREDRATIO"
                    minimum = "0.50".toBigDecimal()
                }
            }
        }
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

// --- Banned API scan (replaces fragile CI grep script) ---
tasks.register("bannedApiScan") {
    description = "Scan Java source files for banned API usage"
    group = "verification"

    // Capture serializable values at configuration time
    val scanRoot: File = layout.projectDirectory.asFile
    val bannedPatterns: Map<Regex, List<String>> = mapOf(
        Regex("Registry\\.BIOME") to emptyList(),
        Regex("Biome\\.valueOf") to emptyList(),
        Regex("Biome\\.values\\(\\)") to emptyList(),
        Regex("Material\\.valueOf") to emptyList(),
        Regex("Material\\.getMaterial") to emptyList(),
        Regex("Material\\.matchMaterial") to emptyList(),
        Regex("getScheduler\\(\\)\\.run") to emptyList(),
        Regex("chunkData\\.setBiome\\(") to emptyList(),
        Regex("net\\.minecraft\\.") to emptyList(),
        Regex("craftbukkit\\.") to emptyList(),
        Regex("ReentrantLock") to emptyList(),
        Regex("synchronized") to emptyList(),
        Regex("Class\\.forName") to listOf("FoliaDetector.java")
    )

    doLast {
        val sourceFiles = scanRoot.walkTopDown()
            .filter { it.isFile && it.extension == "java" }
            .filter { file ->
                val rel = scanRoot.toPath().relativize(file.toPath()).toString()
                !rel.contains("build/")
            }
            .filter { it.name != "EngineIsolationTest.java" }
            .toList()

        println("Scanning ${sourceFiles.size} Java source files for banned APIs...")
        println()

        var errors = 0

        for ((pattern, exclusions) in bannedPatterns) {
            val matches = mutableListOf<String>()

            for (file in sourceFiles) {
                val relativePath = scanRoot.toPath().relativize(file.toPath()).toString()

                if (exclusions.any { relativePath.contains(it) }) continue

                file.readLines().forEachIndexed { index, line ->
                    if (line.trimStart().startsWith("*")) return@forEachIndexed
                    if (pattern.containsMatchIn(line)) {
                        matches.add("$relativePath:${index + 1}: ${line.trim()}")
                    }
                }
            }

            if (matches.isNotEmpty()) {
                println("FAIL: $pattern found in:")
                matches.forEach { println("  $it") }
                errors++
            }
        }

        println()
        if (errors == 0) {
            println("PASS: No banned APIs found")
        } else {
            throw GradleException("$errors banned API pattern(s) detected")
        }
    }
}
