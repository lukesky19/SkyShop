plugins {
    java
    `maven-publish`
}

group = "com.github.lukesky19"
version = "2.1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven("https://jitpack.io") {
        name = "jitpack"
    }
    maven("https://repo.rosewooddev.io/repository/public/") {
        name = "RoseWood"
    }
    mavenLocal()
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    compileOnly("com.github.lukesky19:SkyLib:1.3.1.0")

    // Hooks
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("org.black_ixx:playerpoints:3.3.3")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks {
    jar {
        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }
        archiveClassifier.set("")
    }

    // This allows usage of @apiNode in javadocs
    javadoc {
        (options as StandardJavadocDocletOptions).tags("apiNote:a:API Note:")
    }

    build {
        dependsOn(publishToMavenLocal)
        dependsOn(javadoc)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}