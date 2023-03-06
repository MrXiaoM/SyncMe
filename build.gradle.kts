plugins {
    kotlin("jvm") version "1.8.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}
allprojects {
    group = "top.mrxiaom"
    version = "1.0.0"

    apply(plugin = "org.jetbrains.kotlin.jvm")
    kotlin {
        jvmToolchain(8)
    }
    repositories {
        maven("https://repo.huaweicloud.com/repository/maven/")
        mavenCentral()
        maven("https://mvn.lumine.io/repository/maven/")
        maven("https://maven.fastmirror.net/repositories/minecraft/")
        maven("https://ci.nyaacat.com/maven/")
        maven("https://jitpack.io/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/groups/public/")
    }
}
dependencies {
    compileOnly("org.spigotmc:spigot-api:1.19-R0.1-SNAPSHOT")
    testCompileOnly("org.spigotmc:spigot-api:1.19-R0.1-SNAPSHOT")
    testCompileOnly("org.spigotmc:spigot:1.19-R0.1-SNAPSHOT")

    implementation("com.github.MrXiaoM:SQLHelper:1.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation(kotlin("reflect"))
    api(project("nms"))
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveClassifier.set("")

        arrayOf(
            "top.mrxiaom.sqlhelper" to "sql",
            "kotlin" to "kotlin",
            "org.jetbrains.annotations" to "annotations",
            "org.intellij.lang.annotations" to "intellij.annotations",
        ).forEach {
            relocate(it.first, "top.mrxiaom.syncme.libs.${it.second}")
        }

        minimize()
    }
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from(sourceSets.main.get().resources.srcDirs) {
            expand("version" to version)
            include("*.yml")
        }
        from(File(rootDir, "LICENSE"))
    }
}