package com.example.jawwna.mapscreen.viewmodel
// MapViewModel.kt
import android.app.Application
import android.content.res.Configuration
import android.location.Address
import android.location.Geocoder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jawwna.R
import com.example.jawwna.datasource.model.shared_preferences_helper.PreferencesLocationHelper
import com.example.jawwna.datasource.model.shared_preferences_helper.PreferencesSettingsHelper
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException


    class MapViewModel(application: Application) : ViewModel() {
        private val preferencesLocationHelper: PreferencesLocationHelper = PreferencesLocationHelper(application)

    // LiveData for location
    private val geocoder = Geocoder(application)

    // Initialize with default location from LocationData singleton
    private val _location = MutableLiveData<LatLng>()
    val location: LiveData<LatLng> get() = _location

    // LiveData for place name
    private val _placeName = MutableLiveData<String>()
    val placeName: LiveData<String> get() = _placeName

    // LiveData for map mode
    private val _mapMode = MutableLiveData<Int>()
    val mapMode: LiveData<Int> get() = _mapMode

        // LiveData to hold the card settings field background color
        private val _cardSettingsFieldBackgroundLightMode = MutableLiveData<Int>()
        val cardSettingsFieldBackgroundLightModeLiveData: LiveData<Int> get() = _cardSettingsFieldBackgroundLightMode
 //LiveData Chanege icon
        private val _icon = MutableLiveData<Int>()
        val icon: LiveData<Int> get() = _icon

        // Initialize with default location
        init {
            val defaultName = preferencesLocationHelper.getLocationName()
            val defaultLatitude = preferencesLocationHelper.getLocationLatitude()
            val defaultLongitude = preferencesLocationHelper.getLocationLongitude()
            // Update the LocationData singleton
            _location.value = LatLng(defaultLatitude, defaultLongitude)
            _placeName.value = defaultName ?: "Unknown Place"


        }

    // Method to update the location
    fun updateLocation(latitude: Double, longitude: Double) {
        // Update the LocationData singleton

        _location.value = LatLng(latitude, longitude)
        // Update the LiveData with the default location
        // Update the LiveData with the new location
        //Feactch place name
        fetchPlaceName(latitude, longitude)

    }

    @Suppress("DEPRECATION")
    private fun fetchPlaceName(latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0].getAddressLine(0) ?: "Unknown Place"
                    _placeName.postValue(address)
                } else {
                    _placeName.postValue("Unknown Place")
                }
            } catch (e: IOException) {
                e.printStackTrace()
                _placeName.postValue("Unknown Place")
            }
        }
    }


    fun getMapStyle(packageName: String,nightModeFlags: Int) {
        val mode = when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> R.raw.map_style_night
            Configuration.UI_MODE_NIGHT_NO ->R.raw.map_style_light
            else -> R.raw.map_style_light
        }
        _mapMode.value = mode
    }


        fun setCardSettingsFieldBackgroundLightMode(packageName: String, nightModeFlags: Int) {
            val colorResId = when (nightModeFlags) {
                Configuration.UI_MODE_NIGHT_YES -> R.drawable.card_settings_field_background_night_mode
                Configuration.UI_MODE_NIGHT_NO -> R.drawable.card_settings_field_background_light_mode
                else -> R.drawable.card_settings_field_background_light_mode
            }
            _cardSettingsFieldBackgroundLightMode.value = colorResId

        }
        fun setIcon(packageName: String, nightModeFlags: Int) {
            val colorResId = when (nightModeFlags) {
                Configuration.UI_MODE_NIGHT_YES -> R.drawable.save_location_night_mode
                Configuration.UI_MODE_NIGHT_NO -> R.drawable.save_location_light_mode
                else -> R.drawable.save_location_light_mode
            }
            _icon.value = colorResId
        }

        // Method to save location data
        fun saveLocationData(placeName: String, latitude: Double, longitude: Double) {
            // Here you can save the data to shared preferences or any other storage as needed
            preferencesLocationHelper.saveLocationName(placeName)
            preferencesLocationHelper.saveLocationLatitude(latitude)
            preferencesLocationHelper.saveLocationLongitude(longitude)

        }

}

