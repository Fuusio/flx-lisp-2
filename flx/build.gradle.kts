plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.vanniktech.maven.publish)
}

android {
    namespace = "org.fuusio.flx"

    compileSdk {
        version = release(libs.versions.compileSdk.get().toInt())
    }

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.javaCompatibility.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.javaCompatibility.get())
    }
}

// No experimental `-XXLanguage` flags here: binaries compiled with unstable language features are
// marked pre-release, and apps built with a stable Kotlin compiler would refuse to use them.

dependencies {
    implementation(libs.androidx.core.ktx)
    // `api`: JSON types (JsonObject, JsonArray, …) appear in the library's public API.
    api(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.collections.immutable)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

/*
 * Publishing to Maven Central (Central Portal). The plugin publishes the `release` variant with a
 * sources jar. Group and version come from gradle.properties (GROUP, VERSION_NAME); credentials
 * and the signing key are never stored in the repository. See RELEASING.md.
 */
mavenPublishing {
    publishToMavenCentral()
    // Maven Central requires signed artifacts and rejects unsigned uploads. Signing is skipped
    // only when no key is configured, so that `publishToMavenLocal` works without one.
    if (providers.gradleProperty("signingInMemoryKey").isPresent ||
        providers.gradleProperty("signing.keyId").isPresent
    ) {
        signAllPublications()
    }

    coordinates(
        groupId = providers.gradleProperty("GROUP").get(),
        artifactId = "flx",
        version = providers.gradleProperty("VERSION_NAME").get(),
    )

    pom {
        name.set("FLX Lisp")
        description.set("A small, embeddable Lisp for Kotlin and Android.")
        inceptionYear.set("2016")
        url.set("https://github.com/Fuusio/flx-lisp-2")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("markosalmela")
                name.set("Marko Salmela")
                url.set("https://github.com/Fuusio")
            }
        }
        scm {
            url.set("https://github.com/Fuusio/flx-lisp-2")
            connection.set("scm:git:git://github.com/Fuusio/flx-lisp-2.git")
            developerConnection.set("scm:git:ssh://git@github.com/Fuusio/flx-lisp-2.git")
        }
    }
}
