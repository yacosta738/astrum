plugins {
    id("kotlin-conventions")
    id("testing-conventions")
    id("dokka-conventions")
    id("spring-conventions")
}
val apache_commons_collections_version: String by project
val apache_commons_validator_version: String by project
val guava_version: String by project
val redisson_version: String by project
val embedded_redis_version: String by project
val jackson_version: String by project
val json_patch_version: String by project
val bson_version: String by project
val embed_mongo_version: String by project
val embed_neo4j_version: String by project

dependencies {
    implementation(project(":ulid"))
    implementation(project(":event"))
    implementation(project(":util"))

    implementation(project(":coroutine-test"))

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    implementation("org.springframework:spring-tx:6.0.3")
    // D A T A B A S E S
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-data-neo4j")
    implementation("eu.michael-simons.neo4j:neo4j-migrations-spring-boot-starter:2.0.3")

    implementation("io.r2dbc:r2dbc-h2")
    implementation("io.r2dbc:r2dbc-pool")
    implementation("org.postgresql:r2dbc-postgresql")
    implementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:$embed_mongo_version")
    implementation("org.neo4j:neo4j:$embed_neo4j_version")

    runtimeOnly("com.h2database:h2")

    implementation("com.fasterxml.jackson.core:jackson-core:$jackson_version")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jackson_version")

    implementation("org.redisson:redisson-spring-boot-starter:$redisson_version")
    implementation("it.ozimov:embedded-redis:$embedded_redis_version") {
        exclude("org.slf4j")
        exclude("ch.qos.logback")
    }

    implementation("com.google.guava:guava:$guava_version")
    implementation("org.apache.commons:commons-collections4:$apache_commons_collections_version")
    implementation("commons-validator:commons-validator:$apache_commons_validator_version")


    implementation("org.junit.jupiter:junit-jupiter-api")
    runtimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

kotlin.sourceSets["main"].kotlin.srcDirs("src/main")
kotlin.sourceSets["test"].kotlin.srcDirs("src/test")

sourceSets["main"].resources.srcDirs("src/main/resources")
sourceSets["test"].resources.srcDirs("src/test/resources")
