plugins {
    idea
    id("org.springframework.boot")
    id("com.google.devtools.ksp")
    kotlin("plugin.allopen")
}

apply(plugin = "io.spring.dependency-management")

sourceSets {
    main {
        java {
            srcDir("build/generated/ksp/main/kotlin")
        }
    }
}

idea.module {
    generatedSourceDirs.add(file("build/generated/ksp/main/kotlin"))
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(project(":komapper-spring-boot-starter-r2dbc"))
    ksp(project(":komapper-processor"))
    runtimeOnly("io.r2dbc:r2dbc-h2:0.8.4.RELEASE")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

ksp {
    arg("komapper.namingStrategy", "UPPER_SNAKE_CASE")
}

allOpen {
    annotation("org.springframework.context.annotation.Configuration")
    annotation("org.springframework.transaction.annotation.Transactional")
}

springBoot {
    mainClass.set("example.ApplicationKt")
}