plugins {
    id("javaee8.starter.java-application-conventions")
}

dependencies {
    implementation("fish.payara.extras:payara-embedded-all:5.2021.2")
    runtimeOnly(project(":web", "archives"))
}

application {
    mainClass.set("com.mammb.javaee8.starter.App")

    // development mode
    applicationDefaultJvmArgs += listOf(
        "-Denv=dev",
        "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005")
}
