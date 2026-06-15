plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "yangfentuozi.dsusideloaderplus.hiddenapistub"
    compileSdk = 37

    defaultConfig {
        minSdk = 29
    }
    buildTypes {
        create("miniDebug"){}
    }
}

dependencies {
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
}
