plugins { id("com.android.application") }

android {
    namespace = "cl.esucar.tutor"
    compileSdk = 37
    defaultConfig {
        applicationId = "cl.esucar.tutor"
        minSdk = 26
        targetSdk = 37
        versionCode = 2
        versionName = "1.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    testOptions { unitTests.isIncludeAndroidResources = true }
}

dependencies {
    implementation("androidx.activity:activity-ktx:1.13.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.17")
}

tasks.withType<Test>().configureEach {
    systemProperty("robolectric.dependency.repo.url", "https://repo.maven.apache.org/maven2")
    // Mantiene descargas y bloqueos de las pruebas dentro del proyecto, sin escribir en el perfil personal.
    val testHome = layout.buildDirectory.dir("test-home").get().asFile
    val testTemp = layout.buildDirectory.dir("test-temp").get().asFile
    systemProperty("user.home", testHome.absolutePath)
    systemProperty("java.io.tmpdir", testTemp.absolutePath)
    doFirst { testHome.mkdirs(); testTemp.mkdirs() }
}
