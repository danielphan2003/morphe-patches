group = "app.danielphan2003"

patches {
    about {
        name = "danielphan2003 Patches"
        description = "Patches for Wattpad 11.40.5, Kiki Auto 26.04.01.02 and TikTok SEA 46.9.3 " +
            "(com.ss.android.ugc.trill), the TikTok ones ported from icysymmetra/tiktok-patches-for-morphe."
        source = "git@github.com:danielphan2003/morphe-patches.git"
        author = "danielphan2003"
        contact = "https://github.com/danielphan2003/morphe-patches/issues"
        website = "https://github.com/danielphan2003/morphe-patches"
        license = "GNU General Public License v3.0"
    }
}

dependencies {
    // Used by PatchListGenerator and the TikTok patches.
    implementation(libs.gson)

    // smali needs it to compile; the patcher provides it at run time, so it stays out of the bundle.
    implementation(libs.guava)

    // Android API stubs the TikTok patches compile against.
    compileOnly(project(":patches:stub"))
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        // jar, not build: the generator reads build/libs/patches-<version>.mpp.
        dependsOn(jar)

        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("app.morphe.util.PatchListGeneratorKt")
        args(project.version.toString())
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
