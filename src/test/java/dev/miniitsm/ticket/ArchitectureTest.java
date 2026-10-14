package dev.miniitsm.ticket;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "dev.miniitsm.ticket", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule ticketPackagesAreFreeOfCycles = slices()
            .matching("dev.miniitsm.ticket.(*)..")
            .should().beFreeOfCycles();

    @ArchTest
    static final ArchRule domainDoesNotDependOnOuterLayers = noClasses()
            .that().resideInAPackage("dev.miniitsm.ticket.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "dev.miniitsm.ticket.application..",
                    "dev.miniitsm.ticket.api..",
                    "dev.miniitsm.ticket.persistence..");
}
