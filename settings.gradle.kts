@file:Suppress("UnstableApiUsage")

rootProject.name = "KotlinModTemplate"

val mindustryVersion: String = settings.extra["mindustryVersion"].toString()
val kspVersion: String = settings.extra["kspVersion"].toString()
val kotlinpoetVersion: String = settings.extra["kotlinpoetVersion"].toString()
gradle.extra["mindustryVersion"] = mindustryVersion
gradle.extra["gameJar"] = settings.extra["gameJar"].toString()


dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories{

        // 从 Mindustry 发布版下载依赖 JAR；不使用真实仓库，但最可靠
        ivy {
            url = uri("https://github.com/")
            patternLayout {
                artifact(when (mindustryVersion) {
                    "latest" -> "/[organisation]/[module]/releases/[revision]/download/dependencies.jar"
                    "be"     -> "/[organisation]/[module]/releases/download/master/[revision].jar"
                    else     -> "/[organisation]/[module]/releases/download/[revision]/dependencies.jar"
                })
            }
            metadataSources { artifact() }
            content {
                if (mindustryVersion == "be") {
                    includeVersion("Anuken", "MindustryBuilds", "latest")
                } else {
                    includeVersion("Anuken", "Mindustry", mindustryVersion)
                }
            }
        }

        mavenCentral()
        google()
        maven ("https://www.jitpack.io")
    }

    versionCatalogs{
        create("libs") {
            if (mindustryVersion == "be") {
                library("mindustry", "Anuken", "MindustryBuilds").version("latest")
            } else {
                library("mindustry", "Anuken", "Mindustry").version(mindustryVersion)
            }

            library("ksp-api", "com.google.devtools.ksp", "symbol-processing-api").version(kspVersion)
            library("kotlinpoet", "com.squareup", "kotlinpoet").version(kotlinpoetVersion)
            library("kotlinpoet-ksp", "com.squareup", "kotlinpoet-ksp").version(kotlinpoetVersion)
        }
    }
}