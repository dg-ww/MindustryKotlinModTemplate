import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    kotlin("jvm") version "2.4.0"
    kotlin("kapt") version "2.4.0"
}

version = "1.0"

val mindustryVersion = gradle.extra["mindustryVersion"].toString()
val isWindows = System.getProperty("os.name").lowercase().contains("windows")
val sdkRootP: String? = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
val libsDir = layout.buildDirectory.dir("libs").get().asFile

sourceSets.main{
    kotlin.srcDirs("src")
}


val gameJar = gradle.extra["gameJar"].toString()
dependencies {
    compileOnly(libs.mindustry)
//    compileOnly("com.github.Anuken.Arc:arc-core:159.5")
//    compileOnly("com.github.Anuken.Mindustry:core:v159.5")
}
tasks.withType<KotlinCompile>().configureEach{
    compilerOptions{
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

tasks.withType<JavaCompile>().configureEach{
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
    options.release.set(8)
}

tasks.register("jarAndroid") {
    description = "Build Android jar"
    dependsOn("jar")

    doLast {
        if (sdkRootP.isNullOrEmpty() || !File(sdkRootP).exists())
            throw GradleException("No valid Android SDK found")

        val platformRoot = File("$sdkRootP/platforms/").listFiles()?.sortedArray()?.reversed()?.find { f ->
            File(f, "android.jar").exists()
        } ?: throw GradleException("No android.jar found")


        val desktopJar = file("$libsDir/${project.name}Desktop.jar")

        val d8 = if (isWindows) "d8.bat" else "d8"
        val outputJar = "$libsDir/${project.name}Android.jar"

        val command = mutableListOf(d8)
        command.add("--lib")
        command.add(File(platformRoot, "android.jar").path)
        configurations.runtimeClasspath.get().files.forEach { file ->
            command.add("--classpath")
            command.add(file.path)
        }
        command.add("--min-api")
        command.add("14")
        command.add("--output")
        command.add(outputJar)
        command.add(desktopJar.path)

        val proc = ProcessBuilder(command)
            .directory(libsDir)
            .redirectErrorStream(true)
            .start()

        // 没有这行jarAndriod任务一直卡住
        val output = proc.inputStream.bufferedReader().readText()
        proc.waitFor()

        if (output.isNotBlank()) {
            logger.error(output)
        }
        if (proc.exitValue() != 0) {
            throw GradleException("d8 failed with exit code ${proc.exitValue()}")
        }
    }
}

tasks.jar {
    archiveFileName.set("${project.name}Desktop.jar")

    duplicatesStrategy = DuplicatesStrategy.WARN

    from({
        configurations.runtimeClasspath.get().files.map { file ->
            if (file.isDirectory()) file else zipTree(file)
        }
    })

    from(projectDir) {
        include("mod.hjson")
    }

    from("assets/") {
        include("**")
        exclude("content/items","content/test","sprites/icon")
    }
}

tasks.register<Jar>("deployGitHub") {
    description = ""
    dependsOn("jarAndroid", "jar")
    archiveFileName.set("${project.name}.jar")

    from(zipTree(file("$libsDir/${project.name}Desktop.jar")))
    from(zipTree(file("$libsDir/${project.name}Android.jar")))

    doLast {
        delete(file("$libsDir/${project.name}Desktop.jar"))
        delete(file("$libsDir/${project.name}Android.jar"))
    }
}

tasks.register<Jar>("deploy") {
    description = ""
    dependsOn("jarAndroid", "jar")
    archiveFileName.set("${project.name}-${project.version}.jar")

    from(zipTree(file("$libsDir/${project.name}Desktop.jar")))
    from(zipTree(file("$libsDir/${project.name}Android.jar")))

    doLast {
//        delete(file("$libsDir/${project.name}Desktop.jar"))
        delete(file("$libsDir/${project.name}Android.jar"))
    }
}

tasks.register<DefaultTask>("install"){
    description = "将mod复制到mindustry/mods"
    dependsOn("deploy")
    val modFile = file("$libsDir/${project.name}-${project.version}.jar")
    val base = System.getenv("APPDATA")
    val modsDir = File("$base/Mindustry/mods").also { it.mkdirs() }
    doFirst {
        modFile.copyTo(File(modsDir, modFile.name), overwrite = true)
    }
}


tasks.register<JavaExec>("debug"){
    dependsOn("install")
    description = "调试"
    classpath = files(gameJar)
    args = listOf(
        "-debug",
        "-gl",
        "3.0"
    )
    jvmArgs = listOf("-Xmx2G")
}