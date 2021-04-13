plugins {
    id("javaee8.starter.java-common-conventions")
    application
}
application {
    applicationDefaultJvmArgs = listOf("--add-opens",
        "java.base/jdk.internal.loader=ALL-UNNAMED")
}