extension {
    name = "extensions/wattpad.mpe"
}

dependencies {
    // Wattpad ships OkHttp 4; the extension only compiles against its API.
    compileOnly("com.squareup.okhttp3:okhttp:4.12.0")
}
