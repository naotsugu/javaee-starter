plugins {
    id("javaee8.starter.java-common-conventions")
    war
}

dependencies {
    annotationProcessor("org.hibernate:hibernate-jpamodelgen:5.4.27.Final")
    providedCompile("fish.payara.extras:payara-embedded-all:5.2021.2")
}
