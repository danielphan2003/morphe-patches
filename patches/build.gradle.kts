group = "app.danielphan2003"

patches {
    about {
        name = "danielphan2003 Patches"
        description = "Patches for Wattpad and Kiki Auto"
        source = "git@github.com:danielphan2003/morphe-patches.git"
        author = "danielphan2003"
        contact = "https://github.com/danielphan2003/morphe-patches/issues"
        website = "https://github.com/danielphan2003/morphe-patches"
        license = "GNU General Public License v3.0"
    }
}

dependencies {
    // Used by PatchListGenerator.
    implementation(libs.gson)
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("app.morphe.util.PatchListGeneratorKt")
    }
    // Used by gradle-semantic-release-plugin.
    publish {
        dependsOn("generatePatchesList")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}
