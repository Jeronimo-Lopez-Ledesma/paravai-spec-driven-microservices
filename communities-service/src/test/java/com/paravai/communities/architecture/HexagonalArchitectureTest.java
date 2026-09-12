package com.paravai.communities.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.paravai.communities", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {
    @ArchTest
    static final ArchRule domain_depends_only_on_java_and_domain = classes()
            .that().resideInAPackage("..domain..")
            .should().onlyDependOnClassesThat().resideInAnyPackage("java..", "..domain..");

    @ArchTest
    static final ArchRule application_has_no_framework_or_adapter_dependencies = classes()
            .that().resideInAPackage("..application..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(
                    "java..", "reactor..", "org.reactivestreams..", "..domain..", "..application..");

    @ArchTest
    static final ArchRule rest_does_not_depend_on_mongo = noClasses()
            .that().resideInAPackage("..adapter.in.rest..")
            .should().dependOnClassesThat().resideInAnyPackage("..adapter.out..", "org.springframework.data..", "com.mongodb..");

    @ArchTest
    static final ArchRule mongo_does_not_depend_on_rest_or_input_ports = noClasses()
            .that().resideInAPackage("..adapter.out.mongo..")
            .should().dependOnClassesThat().resideInAnyPackage("..adapter.in..", "..application.port.in..");

    @ArchTest
    static final ArchRule adapters_do_not_depend_on_application_implementation = noClasses()
            .that().resideInAPackage("..adapter..")
            .should().dependOnClassesThat().haveFullyQualifiedName("com.paravai.communities.application.CommunityService");

    @ArchTest
    static final ArchRule ports_are_interfaces = classes()
            .that().resideInAPackage("..application.port..")
            .and().areTopLevelClasses()
            .should().beInterfaces();
}
