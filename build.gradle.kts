import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
plugins {
  `java-library`
  id("io.freefair.lombok") version "8.6"
  id("io.github.goooler.shadow") version "8.1.8"
  id("io.papermc.paperweight.userdev") version "1.7.2"
  id("xyz.jpenilla.run-paper") version "2.3.0" // Adds runServer and runMojangMappedServer tasks for testing
  id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.1.1" // Generates plugin.yml based on the Gradle config
}

group = "dev.arubik.realmcraft.realmcraft"
version = "1.1.0"
description = "A minecraft plugin for the realmcraft server"

java {
  // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 11 installed for example.
  toolchain.languageVersion = JavaLanguageVersion.of(21)
}

// 1)
// For >=1.20.5 when you don't care about supporting spigot
// paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

// 2)
// For 1.20.4 or below, or when you care about supporting Spigot on >=1.20.5
// Configure reobfJar to run when invoking the build task
/*
tasks.assemble {
  dependsOn(tasks.reobfJar)
}
 */

repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://nexus.phoenixdevt.fr/repository/maven-public/")
    maven(url ="https://nexus.phoenixdvpt.fr/repository/maven-public/")
    maven (url = "https://repo.codemc.io/repository/maven-public/")
    maven (url = "https://repo.codemc.io/repository/maven-releases/")
    maven (url = "https://repo.codemc.io/repository/maven-snapshots/")
    maven(url = "https://repo.papermc.io/repository/maven-public/")
    maven(url = "https://repo.dmulloy2.net/repository/public/")
    maven(url="https://repo.inventivetalent.org/repository/public/")
    maven(url ="https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven(url="https://mvn.lumine.io/repository/maven-public/")
    maven(url = "https://artifacts.wolfyscript.com/artifactory/gradle-dev")
    maven(url = "https://artifacts.wolfyscript.com/artifactory/gradle-dev-local")
    maven(url = "https://repo.maven.apache.org/maven2/")
    maven(url = "https://mvn.lumine.io/repository/maven-public/")
    maven(url = "https://repo.oraxen.com/releases")
    maven(url = "https://jitpack.io")
    maven(url = "https://repo.extendedclip.com/content/repositories/placeholderapi/")
    
    flatDir {
        dirs("libs")
    }
}
dependencies {
  paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")

  compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
	compileOnly("org.apache.commons:commons-lang3:3.12.0")
	compileOnly("com.google.guava:guava:21.0") 
	compileOnly("org.javassist:javassist:3.22.0-CR1")
	implementation("org.reflections:reflections:0.9.10") {
    exclude(module = "javassist")
    exclude(module = "javax")
  }
	compileOnly("org.geysermc.geyser:api:2.1.0-SNAPSHOT")
	compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
	compileOnly("net.kyori:adventure-text-minimessage:4.14.0")
	compileOnly("net.Indyuce:MMOItems-API:6.10-SNAPSHOT")
	compileOnly("net.Indyuce:MMOCore-API:1.12.1-SNAPSHOT")
	compileOnly("me.clip:placeholderapi:2.11.2")
	compileOnly("net.luckperms:api:5.4")
	compileOnly("io.lumine:Mythic-Dist:5.6.1")
	compileOnly("com.github.retrooper:packetevents-spigot:2.4.0")
	
}

tasks {
  compileJava {
    // Set the release flag. This configures what version bytecode the compileOnlyr will emit, as well as what JDK APIs are usable.
    // See https://openjdk.java.net/jeps/247 for more information.
    options.release = 21
    options.compilerArgs.add("-Xlint:-deprecation")
    options.compilerArgs.add("-nowarn")
  }
  javadoc {
    options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
  }

}
paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.REOBF_PRODUCTION
tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("")
    dependencies{
        include { true }
    }
    relocate("org.reflections", "dev.arubik.libs.reflections")
    //relocate("com.google", "dev.arubik.libs.guava")
    relocate("javax", "dev.arubik.libs.javax")
  minimize(){
    exclude("com.google")
  }
}
// Configure plugin.yml generation
// - name, version, and description are inherited from the Gradle project.
bukkitPluginYaml {
  main = "dev.arubik.realmcraft.realmcraft"
  load = BukkitPluginYaml.PluginLoadOrder.STARTUP
  authors.add("Arubiku")
  apiVersion = "1.20"
  depend.add("MythicLib")
  loadBefore.add("PacketUpgrades")
  loadBefore.add("MMOItems")
}
