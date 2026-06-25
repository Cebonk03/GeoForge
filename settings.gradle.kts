rootProject.name = "geoforge"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

include("engine")
include("api")
include("adapters:v1_21_x")
include("adapters:v26_x")
include("plugin")
