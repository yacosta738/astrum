plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
}

dependencies {
    // L O C A L   D E P E N D E N C I E S
    implementation(project(":shared"))

    // S P R I N G
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security:spring-security-oauth2-jose")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.zalando:problem-spring-webflux:0.28.0-RC.0")
    implementation("org.springframework:spring-tx:6.0.3")

    // 3 R D   P A R T Y
    implementation(libs.bundles.kotlinLogging)

    // T E S T   D E P E N D E N C I E S
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
}