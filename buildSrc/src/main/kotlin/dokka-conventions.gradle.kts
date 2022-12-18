import com.acosta.education.gradle.extension.fromComponent
import com.acosta.education.gradle.extension.getGitLabToken
import com.acosta.education.gradle.extension.gitLab

plugins {
    id("org.jetbrains.dokka")
}

dependencies {
    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin")
}

tasks.dokkaJavadoc {
    outputDirectory.set(buildDir.resolve("javadoc"))
}
