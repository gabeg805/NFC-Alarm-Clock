// Imports need to come first. Android Studio was showing syntax errors when
// this was after plugins
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import java.io.FileInputStream

// Plugins
plugins {
	id("com.android.application")
	id("com.google.dagger.hilt.android")
	id("com.google.devtools.ksp")
}

// Create a variable called keystorePropertiesFile, and initialize it to your
// keystore.properties file, in the rootProject folder.
val keystorePropertiesFile = rootProject.file("keystore.properties")

// Initialize a new Properties() object called keystoreProperties.
val keystoreProperties = Properties()

// Load your keystore.properties file into the keystoreProperties object if it exists
if (keystorePropertiesFile.exists()) {
	keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {

	// Version to compile the SDK
	compileSdk = 36

	defaultConfig {
		applicationId = "com.nfcalarmclock"
		minSdk = 24
		targetSdk = 36
		versionCode = 588
		versionName = "12.6.5-beta016"
	}

	// Configuration for signing the app on release builds. The keystore.properties file must exist
	signingConfigs {
		create("release") {
			if (keystorePropertiesFile.exists()) {
				keyAlias = keystoreProperties["keyAlias"] as String
				keyPassword = keystoreProperties["keyPassword"] as String
				storePassword = keystoreProperties["storePassword"] as String
				storeFile = file(keystoreProperties["storeFile"] as String)
			}
		}
	}

	// Setup when creating a release build
	buildTypes {
		getByName("release") {

			// Remove unused code and resources, and optimize the code
			isMinifyEnabled = true
			isShrinkResources = true

			// Set do not obfuscate flag in proguard so that the build is reproducible
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)

			// Set the signing config for release builds, if the keystore.properties file exists
			if (keystorePropertiesFile.exists()) {
				signingConfig = signingConfigs.getByName("release")
			}

		}
	}

	// Disable dependency metadata when building apks and bundles
	dependenciesInfo {
		includeInApk = false
		includeInBundle = false
	}

	// Enable being able to use and import the BuildConfig package
	buildFeatures {
		buildConfig = true
		viewBinding = true
	}

	// Build variants, FOSS and Google Play
	flavorDimensions += "version"

	productFlavors {
		create("foss") {
			dimension = "version"
		}

		create("googleplay") {
			dimension = "version"
		}
	}

	// Enable automatic per-app language support
	androidResources {
		generateLocaleConfig = true
	}

	compileOptions {
		// Sets Java version the same as the Kotlin version
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	// Lint setup
    lint {
        disable += "UnnecessaryInterfaceModifier"
        enable += "ConvertToWebp" + "DalvikOverride" + "DuplicateStrings" + "IconExpectedSize" +
				"MinSdkTooLow" + "MissingRegistered" + "NegativeMargin" + "Registered" +
				"TypographyQuotes"
    }

	// Data binding
	namespace = "com.nfcalarmclock"
	dataBinding.enable = true

}

// Set output filename
base {
	archivesName = "nfc_alarm_clock_v${android.defaultConfig.versionName}"
}

// Set the same JVM version as the compile options
kotlin {
	compilerOptions {
		jvmTarget = JvmTarget.JVM_17
	}
}

// Location to export Room database schema
ksp {
	arg("room.schemaLocation", "${projectDir}/schemas")
}

// Define the Google Play build configuration
val googleplayImplementation = configurations.getByName("googleplayImplementation")

dependencies {

	// ------------------------------------------------------------------------
	// All Build Variants
	// ------------------------------------------------------------------------

	// Android
	implementation("androidx.annotation:annotation:1.10.0")
	implementation("androidx.appcompat:appcompat:1.8.0")
	implementation("androidx.cardview:cardview:1.0.0")
	implementation("androidx.coordinatorlayout:coordinatorlayout:1.3.0")
	implementation("androidx.constraintlayout:constraintlayout:2.2.2")
	// Upgrading to 1.19.0 requires API 37
	implementation("androidx.core:core-ktx:1.18.0")
	implementation("androidx.fragment:fragment-ktx:1.9.0")
	implementation("androidx.lifecycle:lifecycle-process:2.11.0")
	implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
	implementation("androidx.media3:media3-exoplayer:1.11.0")
	implementation("androidx.media3:media3-common:1.11.0")
	implementation("androidx.preference:preference-ktx:1.2.1")
	implementation("androidx.recyclerview:recyclerview:1.4.0")
	implementation("androidx.viewpager:viewpager:1.1.0")
	implementation("com.google.android.material:material:1.14.0")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
	implementation("androidx.dynamicanimation:dynamicanimation:1.1.0")
	implementation("androidx.navigation:navigation-fragment-ktx:2.9.8")
	implementation("androidx.navigation:navigation-ui-ktx:2.9.8")

	// Room database (Any later requires API > 34
	implementation("androidx.room:room-runtime:2.8.4")
	implementation("androidx.room:room-ktx:2.8.4")
	ksp("androidx.room:room-compiler:2.8.4")

	// Room kotlin extensions and coroutines

	// Dependency injection with Hilt
	implementation("com.google.dagger:hilt-android:2.59.2")
	ksp("com.google.dagger:hilt-android-compiler:2.59.2")

	// ------------------------------------------------------------------------
	// Google Play Build Variant
	// ------------------------------------------------------------------------

	// Google Play billing and in-app review
	googleplayImplementation("com.android.billingclient:billing:9.1.0")
	googleplayImplementation("com.google.android.play:review:2.0.2")
	googleplayImplementation("com.google.android.play:review-ktx:2.0.2")

}
