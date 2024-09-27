package com.example.jawwna.splashscreen

import android.content.Intent
import android.content.res.Configuration
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.example.jawwna.R
import com.example.jawwna.customui.CustomAlertDialog
import com.example.jawwna.databinding.ActivitySplashScreenBinding
import com.example.jawwna.datasource.repository.Repository
import com.example.jawwna.mainactivity.MainActivity
import com.example.jawwna.mainactivity.viewmodel.MainActivityViewModel
import com.example.jawwna.mainactivity.viewmodel.MainActivityViewModelFactory
import com.example.jawwna.splashscreen.viewmodel.SplashViewModel
import com.example.jawwna.splashscreen.viewmodel.SplashViewModelFactory
import kotlinx.coroutines.launch
import java.util.Locale
import android.content.pm.PackageManager
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.util.Log

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private lateinit var lottieAnimationView: LottieAnimationView
    private lateinit var welcomeText: TextView
    private lateinit var viewModel: SplashViewModel
    private var isNightMode = false
    private var isCurrenLocationAvailable = false
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val splashDuration = 3000L  // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel =
            ViewModelProvider(
                this,
                SplashViewModelFactory(Repository.getRepository(this.application))
            ).get(
                SplashViewModel::class.java
            )
        viewModel.setUpdateLocale(Locale.getDefault().language)

        // Collect the updated locale
        lifecycleScope.launch {
            viewModel.updateLocale.collect { language ->
                setLocale(language)
                applyThemeBasedOnLocaleAndMode()
            }
        }

        // Check if current location is available
        viewModel.IsCurrenLocationAvailable()
        lifecycleScope.launch {
            viewModel.isCurrenLocationAvailable.collect { isAvailable ->
                isCurrenLocationAvailable = isAvailable
            }
        }

        // Initialize View Binding
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val customAlert = CustomAlertDialog(this)

        lottieAnimationView = binding.lottieAnimationView
        welcomeText = binding.welcomeText

        // Get current night mode and set animation resource in ViewModel
        val nightModeFlags =
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        isNightMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES

        viewModel.setAnimationResource(nightModeFlags)

        // Observe the animation resource LiveData
        lifecycleScope.launch {
            viewModel.animationResource.collect { animationResource ->
                lottieAnimationView.setAnimation(animationResource)
                lottieAnimationView.playAnimation()
                animateTextView()
            }
        }

        // Start the splash timer in ViewModel
        viewModel.startSplashTimer(splashDuration)

        // Create an animation instance for fade-out effect
        val fadeOutAnimation: Animation = AnimationUtils.loadAnimation(this, R.anim.fade_out)

        // Set Animation Listener for fade-out effect
        fadeOutAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                if (!isCurrenLocationAvailable) {
                    customAlert.showDialog(
                        message = getString(R.string.location_dialog_message),
                        title = getString(R.string.location_dialog_title),
                        isDarkTheme = isNightMode,
                        positiveText = getString(R.string.map),
                        negativeText = getString(R.string.gps),
                        positiveAction = {
                            // Navigate to MainActivity
                            startActivity(Intent(this@SplashActivity, MainActivity::class.java).apply {
                                putExtra("MapFragment", "mapFragment") // Pass any data if necessary
                            })
                        },
                        negativeAction = {
                            checkGpsEnabled()
                        }
                    )
                } else {
                    // Start MainActivity when animation ends
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        // Schedule fade-out animation after splash duration
        welcomeText.startAnimation(fadeOutAnimation)
    }

    private fun animateTextView() {
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 2000 // 2 seconds
            fillAfter = true
        }
        welcomeText.startAnimation(fadeIn)
    }

    // Function to set the application's locale
    fun setLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)

        // Create a new Configuration object and set the new locale
        val config = Configuration(this.resources.configuration).apply {
            setLocale(locale) // For Android 7.0 (Nougat) and higher
        }

        // Update the resources with the new configuration
        this.resources.updateConfiguration(config, this.resources.displayMetrics)

        // For devices running Android N and higher, create a configuration context
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.createConfigurationContext(config)
        }
    }

    private fun applyThemeBasedOnLocaleAndMode() {
        val currentLocale = resources.configuration.locales[0]
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isNightMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES

        // Apply the correct theme based on locale and night mode
        if (currentLocale.language == "ar") {
            if (isNightMode) {
                setTheme(R.style.Theme_Jawwna_Arabic_Night)  // Arabic Night mode theme
            } else {
                setTheme(R.style.Theme_Jawwna_Arabic)  // Arabic Light mode theme
            }
        } else {
            if (isNightMode) {
                setTheme(R.style.Theme_Jawwna_Night)  // Default locale Night mode theme
            } else {
                setTheme(R.style.Theme_Jawwna)  // Default locale Light mode theme
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Reapply the correct theme when the configuration changes
        applyThemeBasedOnLocaleAndMode()
        recreate()  // Restart the activity to apply the new theme
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel the animation when the activity is destroyed
        lottieAnimationView.cancelAnimation()
    }

    // Check if GPS is enabled
    private fun checkGpsEnabled() {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // GPS is not enabled, open settings
            openLocationSettings()
        } else {
            // If GPS is enabled, retrieve location
            getLocation()
        }
    }

    // Retrieve location using FusedLocationProviderClient
    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        // Use the location here
                        val latitude = location.latitude
                        val longitude = location.longitude
                        // You can now use the coordinates (latitude, longitude)
                    } else {
                        // Handle case where location is null
                    }
                }
                .addOnFailureListener { e ->
                    // Handle failure to get location
                    Log.e("SplashActivity", "Failed to get location: ${e.message}")
                }
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    // Handle the permission request response
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get the location
                getLocation()
            } else {
                // Permission denied
                showLocationPermissionDeniedDialog()
            }
        }
    }

    // Show a dialog to the user to grant location permission
    private fun showLocationPermissionDeniedDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.location_permission_required_title))
            .setMessage(getString(R.string.location_permission_required_message))
            .setPositiveButton(getString(R.string.settings)) { dialog, _ ->
                openAppSettings()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    // Open the app settings for the user to enable permissions
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    // Open location settings
    private fun openLocationSettings() {
        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }
}
