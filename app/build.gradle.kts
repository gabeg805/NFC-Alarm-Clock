plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
	id("com.google.dagger.hilt.android")
	id("com.google.devtools.ksp")
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
		versionCode = 338
		versionName = "12.0.0-beta003"

		ksp {
			arg("room.schemaLocation", "${projectDir}/schemas")
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

		// Sets Java version the same as the Kotlin version
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17

		//// Sets Java compatibility to Java 8
		//sourceCompatibility = JavaVersion.VERSION_1_8
		//targetCompatibility = JavaVersion.VERSION_1_8
	}

	kotlinOptions {
		jvmTarget = "17"
	}

    lint {
        disable += "UnnecessaryInterfaceModifier"
        enable += "ConvertToWebp" + "DalvikOverride" + "DuplicateStrings" + "IconExpectedSize" +
				"MinSdkTooLow" + "MissingRegistered" + "NegativeMargin" + "Registered" +
				"TypographyQuotes"
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
	implementation("androidx.core:core-ktx:1.10.0") // 1.12.0 is only for compile API >= 34
	implementation("androidx.fragment:fragment-ktx:1.6.1")
	implementation("androidx.lifecycle:lifecycle-process:2.6.2")
	implementation("androidx.preference:preference-ktx:1.2.1")
	implementation("androidx.recyclerview:recyclerview:1.3.1")
	implementation("androidx.viewpager:viewpager:1.0.0")

	// Material
	implementation("com.google.android.material:material:1.9.0")

	// Room database (Any later requires API > 34
	implementation("androidx.room:room-runtime:2.5.2")
	annotationProcessor("androidx.room:room-compiler:2.5.2")

	// Room kotlin extensions and coroutines
	ksp("androidx.room:room-compiler:2.5.2")
	implementation("androidx.room:room-ktx:2.5.2")

	// Dependency injection with Hilt
	implementation("com.google.dagger:hilt-android:2.48")
	ksp("com.google.dagger:hilt-android-compiler:2.48")

	// Media player
	implementation("androidx.media3:media3-exoplayer:1.1.1")
	implementation("androidx.media3:media3-common:1.1.1")

	// Google Play in-app review
	implementation("com.google.android.play:core:1.10.3")

	// Google billing
	implementation("com.android.billingclient:billing:6.0.1")

	// Kotlin due to "Duplicate class found" issue
	implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.0"))

	// Kotline coroutines
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
	implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
}
