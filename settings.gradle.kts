


pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
   // versionCatalogs {
   //     create("awssdk") {
   //         from("aws.sdk.kotlin:version-catalog:1.6.17")
   //     }
   // }
}

//plugins {
//    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
//}

rootProject.name = "Campico"
include(":app")
