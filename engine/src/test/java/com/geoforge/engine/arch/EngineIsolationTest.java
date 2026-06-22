package com.geoforge.engine.arch;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
    packages = "com.geoforge.engine",
    importOptions = ImportOption.DoNotIncludeTests.class
)
public class EngineIsolationTest {

    @ArchTest
    static final ArchRule engine_must_not_depend_on_bukkit =
        noClasses()
            .that().resideInAPackage("com.geoforge.engine..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.bukkit..", "io.papermc..", "net.minecraft..")
            .allowEmptyShould(true);
}
