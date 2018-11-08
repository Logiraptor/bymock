import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

plugins {
    kotlin("jvm") version "1.3.0"
    `maven-publish`
    signing
}

group = "io.poyarzun"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("test"))
    compile(kotlin("test-junit"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val javadoc by tasks

val javadocJar by tasks.creating(Jar::class) {
    classifier = "javadoc"
    from(javadoc)
}

val sourcesJar by tasks.creating(Jar::class) {
    classifier = "sources"
    from(kotlin.sourceSets["main"].kotlin)
}

val sonatypePassword: String by project
val sonatypeUsername: String by project

publishing {
    publications {
        create("ProductionJar", MavenPublication::class.java) {

            pom {
                name.set("ByMock")
                description.set("A tiny library for mocking in kotlin")
                url.set("https://github.com/Logiraptor/bymock")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                scm {
                    url.set("https://github.com/Logiraptor/bymock")
                }
                developers {
                    developer {
                        id.set("poyarzun")
                        name.set("Patrick Oyarzun")
                        email.set("patrick@poyarzun.io")
                    }
                }
            }

            from(components["java"])

            artifact(sourcesJar)
            artifact(javadocJar)
        }
    }

    repositories {
        maven {
            name = "Central"
            url = when {
                version.toString().endsWith("SNAPSHOT") -> URI("https://oss.sonatype.org/content/repositories/snapshots/")
                else -> URI("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            }
            credentials {
                password = sonatypePassword
                username = sonatypeUsername
            }
        }
    }
}

signing {
    sign(publishing.publications["ProductionJar"])
}
