plugins {
    alias(libs.plugins.kotlin)
    application
}

dependencies {
    implementation(libs.bundles.logging)
    implementation(libs.bundles.coroutines)
    implementation(libs.bundles.vertx)
}

kotlin {
    jvmToolchain(19)
}

application {
    // Define the main class for the application.
    mainClass.set("demo.AppKt")
}
