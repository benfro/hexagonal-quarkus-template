package se.example.order.bootstrap;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;

/**
 * Maven already enforces module direction. These rules catch what Maven cannot,
 * such as framework imports in the core or one adapter calling another.
 */
@AnalyzeClasses(packages = "se.example.order", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule hexagonal = onionArchitecture()
            .domainModels("..domain.model..")
            .domainServices("..domain.service..")
            .applicationServices("..application..")
            .adapter("rest", "..adapter.in.rest..")
            .adapter("persistence", "..adapter.out.persistence..")
            .withOptionalLayers(true);

    @ArchTest
    static final ArchRule domain_is_framework_free = classes()
            .that().resideInAPackage("..domain..")
            .should().onlyDependOnClassesThat().resideInAnyPackage("java..", "..domain..", "lombok..");

    @ArchTest
    static final ArchRule application_knows_no_quarkus_jpa_or_http = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "io.quarkus..", "org.hibernate..", "jakarta.persistence..", "jakarta.ws.rs..", "org.mapstruct..");

    @ArchTest
    static final ArchRule jpa_stays_in_persistence_adapter = noClasses()
            .that().resideOutsideOfPackage("..adapter.out.persistence..")
            .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..", "io.quarkus.hibernate..");
}
