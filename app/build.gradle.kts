plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
}

import java.util.Properties
import java.io.FileInputStream

// Create a variable called keystorePropertiesFile, and initialize it to your
// keystore.properties file, in the rootProject folder.
val keystorePropertiesFile = rootProject.file("keystore.properties")

// Initialize a new Properties() object called keystoreProperties.
val keystoreProperties = Properties()

// Load your keystore.properties file into the keystoreProperties object.
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

android {
	compileSdk = 33

	defaultConfig {
		applicationId = "com.nfcalarmclock"
		minSdk = 21
		targetSdk = 33
		versionCode = 328
		versionName = "11.0.0-beta4"

		javaCompileOptions {
			annotationProcessorOptions {
				arguments += mapOf("room.schemaLocation" to "${projectDir}/schemas")
			}
		}
	}

	signingConfigs {
		create("config") {
			keyAlias = keystoreProperties["keyAlias"] as String
			keyPassword = keystoreProperties["keyPassword"] as String
			storePassword = keystoreProperties["storePassword"] as String
			storeFile = file(keystoreProperties["storeFile"] as String)
		}
	}

	buildTypes {
		getByName("release") {
			//signingConfig = signingConfigs.release
			isMinifyEnabled = true
			isShrinkResources = true
			proguardFiles(
				getDefaultProguardFile("proguard-android.txt"),
				"proguard-rules.pro")
		}
	}

	compileOptions {
		// Flag to enable support for the new language APIs
		// Error: coreLibraryDesugaring configuration contains no dependencies.
		// If you intend to enable core library desugaring, please add dependencies
		// to coreLibraryDesugaring configuration
		// coreLibraryDesugaringEnabled true

		// Sets Java compatibility to Java 8
		sourceCompatibility = JavaVersion.VERSION_1_8
		targetCompatibility = JavaVersion.VERSION_1_8
	}

    lint {
        disable += "UnnecessaryInterfaceModifier"
        enable += "ConvertToWebp" + "DalvikOverride" + "DuplicateStrings" + "IconExpectedSize" +
				"MinSdkTooLow" + "MissingRegistered" + "NegativeMargin" + "Registered" +
				"TypographyQuotes"
    }

	configurations {
		all {
			exclude(group = "com.google.firebase", module = "firebase-core")
			exclude(group = "com.google.firebase", module = "firebase-iid")
		}
	}

	namespace = "com.nfcalarmclock"
	dataBinding.enable = true

}

dependencies {

	// Android
	implementation("androidx.annotation:annotation:1.7.0")
	implementation("androidx.appcompat:appcompat:1.6.1")
	implementation("androidx.cardview:cardview:1.0.0")
	implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
	implementation("androidx.core:core:1.10.0") // 1.12.0 is only for compile API >= 34
	implementation("androidx.fragment:fragment:1.6.1")
	implementation("androidx.lifecycle:lifecycle-process:2.6.2")
	implementation("androidx.preference:preference:1.2.1")
	implementation("androidx.recyclerview:recyclerview:1.3.1")
	implementation("androidx.viewpager:viewpager:1.0.0")

	// Material
	implementation("com.google.android.material:material:1.9.0")

	// Room database
	implementation("androidx.room:room-runtime:2.5.2")
	annotationProcessor("androidx.room:room-compiler:2.5.2")

	// Media player
	implementation("com.google.android.exoplayer:exoplayer:2.19.1")

	// Google Play in-app review
	implementation("com.google.android.play:core:1.10.3")

	// Google billing
	implementation("com.android.billingclient:billing:6.0.1")

	// Kotlin due to "Duplicate class found" issue
	implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.0"))

	//implementation "com.spotify.sdk:spotify-auth-release:1.1.0@aar"
	//implementation "com.spotify.sdk:spotify-app-remote-release:0.6.1@aar"
	//implementation "com.google.code.gson:gson:2.8.5"
	//implementation "com.github.kaaes:spotify-web-api-android:0.4.1@aar"
	//implementation "com.squareup.retrofit:retrofit:1.9.0"
	//implementation "com.squareup.retrofit2:retrofit:2.5.0"
	//implementation "com.squareup.okhttp3:okhttp:3.14.1"
	//implementation "com.squareup.okhttp:okhttp:2.2.0"
}
