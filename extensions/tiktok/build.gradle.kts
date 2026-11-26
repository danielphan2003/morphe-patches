// The TikTok SEA extension, at upstream's path (icysymmetra/tiktok-patches-for-morphe,
// see CONTRIBUTING.md). Upstream's build also runs its Robolectric tests and screenshot tasks;
// those are not carried here, only what builds the extension.
dependencies {
    compileOnly(project(":extensions:shared:library"))
    compileOnly(project(":extensions:tiktok:stub"))
    compileOnly(libs.annotation)
}

extension {
    name = "extensions/tiktok.mpe"
}

android {
    namespace = "app.morphe.extension.tiktok"

    defaultConfig {
        minSdk = 23
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    lint {
        checkOnly += "NewApi"
        error += "NewApi"
        checkOnly += "ObsoleteSdkInt"
        error += "ObsoleteSdkInt"
        abortOnError = true
    }
}
