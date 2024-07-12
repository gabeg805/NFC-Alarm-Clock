// Imports need to come first. Android Studio was showing syntax errors when
// this was after plugins
import java.util.Properties
import java.io.FileInputStream

// Plugins
plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
	id("com.google.dagger.hilt.android")
	id("com.google.devtools.ksp")
}

// Create a variable called keystorePropertiesFile, and initialize it to your
// keystore.properties file, in the rootProject folder.
val keystorePropertiesFile = rootProject.file("keystore.properties")

// Initialize a new Properties() object called keystoreProperties.
val keystoreProperties = Properties()

// Load your keystore.properties file into the keystoreProperties object.
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

android {

	// Version to compile the SDK
	compileSdk = 34

	defaultConfig {

		// Default app stuff
		applicationId = "com.nfcalarmclock"
		minSdk = 23
		targetSdk = 34
		versionCode = 388
		versionName = "12.3.0-beta001"

		// Set output filename
		setProperty("archivesBaseName", "nfc_alarm_clock_v${versionName}")

		// Location to export Room database schema
		ksp {
			arg("room.schemaLocation", "${projectDir}/schemas")
		}

	}

	// Configuration for signing the app
	signingConfigs {
		create("config") {
			keyAlias = keystoreProperties["keyAlias"] as String
			keyPassword = keystoreProperties["keyPassword"] as String
			storePassword = keystoreProperties["storePassword"] as String
			storeFile = file(keystoreProperties["storeFile"] as String)
		}
	}

	buildTypes {

		// Setup when creating a release build
		getByName("release") {
			isMinifyEnabled = true
			isShrinkResources = true
			proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
		}

	}

	// Enable being able to use and import the BuildConfig package
	buildFeatures {
		buildConfig = true
	}

	// Build variants
	flavorDimensions += "version"

	productFlavors {

		// FOSS
		create("foss") {
			dimension = "version"
		}

		// Google play
		create("googleplay") {
			dimension = "version"
		}

	}

	compileOptions {
		// Sets Java version the same as the Kotlin version
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	// Set the same JVM version as the compile options
	kotlinOptions {
		jvmTarget = "17"
	}

	// Lint setup
    lint {
        disable += "UnnecessaryInterfaceModifier"
        enable += "ConvertToWebp" + "DalvikOverride" + "DuplicateStrings" + "IconExpectedSize" +
				"MinSdkTooLow" + "MissingRegistered" + "NegativeMargin" + "Registered" +
				"TypographyQuotes"
    }

	namespace = "com.nfcalarmclock"
	dataBinding.enable = true

}

// Define the Google play build configuration
val googleplayImplementation by configurations

dependencies {

	// ------------------------------------------------------------------------
	// All Build Variants
	// ------------------------------------------------------------------------

	// Android
	implementation("androidx.annotation:annotation:1.8.0")
	implementation("androidx.appcompat:appcompat:1.7.0")
	implementation("androidx.cardview:cardview:1.0.0")
	implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
	implementation("androidx.core:core-ktx:1.13.1")
	implementation("androidx.fragment:fragment-ktx:1.8.1")
	implementation("androidx.lifecycle:lifecycle-process:2.8.3")
	implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
	implementation("androidx.media3:media3-exoplayer:1.1.1")
	implementation("androidx.media3:media3-common:1.1.1")
	implementation("androidx.preference:preference-ktx:1.2.1")
	implementation("androidx.recyclerview:recyclerview:1.3.2")
	implementation("androidx.viewpager:viewpager:1.0.0")
	implementation("com.google.android.material:material:1.12.0")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
	implementation("androidx.dynamicanimation:dynamicanimation:1.0.0")

	// Room database (Any later requires API > 34
	implementation("androidx.room:room-runtime:2.6.1")
	annotationProcessor("androidx.room:room-compiler:2.6.1")

	// Room kotlin extensions and coroutines
	ksp("androidx.room:room-compiler:2.6.1")
	implementation("androidx.room:room-ktx:2.6.1")

	// Dependency injection with Hilt
	implementation("com.google.dagger:hilt-android:2.48")
	ksp("com.google.dagger:hilt-android-compiler:2.48")

	// ------------------------------------------------------------------------
	// Google Play Build Variant
	// ------------------------------------------------------------------------

	// Google Play billing and in-app review
	googleplayImplementation("com.android.billingclient:billing:7.0.0")
	googleplayImplementation("com.google.android.play:review:2.0.1")
	googleplayImplementation("com.google.android.play:review-ktx:2.0.1")

}
