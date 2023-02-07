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
    implementation("org.springframework.boot:spring-boot-starter-data-neo4j")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springdoc:springdoc-openapi-webflux-core:1.6.14")

    // 3 R D   P A R T Y
    implementation(libs.bundles.kotlinLogging)
    implementation("eu.michael-simons.neo4j:neo4j-migrations-spring-boot-starter:2.0.3")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.2")


    // T E S T   D E P E N D E N C I E S
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter:1.17.6")
    testImplementation("org.testcontainers:testcontainers:1.17.6")
    testImplementation("org.testcontainers:neo4j:1.17.6")
}

kotlin.sourceSets["main"].kotlin.srcDirs("src/main")
kotlin.sourceSets["test"].kotlin.srcDirs("src/test")

sourceSets["main"].resources.srcDirs("src/main/resources")
sourceSets["test"].resources.srcDirs("src/test/resources")
