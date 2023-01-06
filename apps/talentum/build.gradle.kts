plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    id("dokka-conventions")
//  id("publishing-conventions") // If everything was configured correctly, you could use it to publish the artifacts. But it is not working with Spring as I thought.
    id("spring-conventions")
}

val kotlinLoggingVersion: String by rootProject.extra

dependencies {
    // L O C A L   D E P E N D E N C I E S
    implementation(project(":shared"))
    implementation(project(":authentication"))

    // S P R I N G
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // 3 R D   P A R T Y
    implementation(libs.bundles.kotlinLogging)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    // D E V   D E P E N D E N C I E S
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // T E S T   D E P E N D E N C I E S
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}
