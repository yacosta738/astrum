plugins {
    id("com.gradle.enterprise") version("3.10.3")
}

rootProject.name = "gradle-kotlin-spring-starter"

val apps = File("apps")
val libs = File("libs")

loadSubProjects(listOf(apps, libs))

fun loadSubProjects(modules: List<File>) {
    modules.forEach { module ->
        if (module.exists()) {
            if (module.isDirectory) {
                module.listFiles()?.forEach { submodule ->
                    if (submodule.isDirectory) {
                        println("Loading submodule \uD83D\uDCE6: ${submodule.name}")
                        include(":${submodule.name}")
                        project(":${submodule.name}").projectDir = File("${module.name}/${submodule.name}")
                    } else {
                        println("${submodule.name} is not a directory \uD83D\uDDFF - skipping")
                    }
                }
            } else {
                println("${module.name} is not a directory \uD83D\uDE12 - ${module.name}")
            }
        } else {
            println("${module.name} directory does not exist \uD83D\uDEAB - ${module.name}")
        }
    }
}

if (!System.getenv("CI").isNullOrEmpty() && !System.getenv("BUILD_SCAN_TOS_ACCEPTED").isNullOrEmpty()) {
    gradleEnterprise {
        buildScan {
            termsOfServiceUrl = "https://gradle.com/terms-of-service"
            termsOfServiceAgree = "yes"
            tag("CI")
        }
    }
}
