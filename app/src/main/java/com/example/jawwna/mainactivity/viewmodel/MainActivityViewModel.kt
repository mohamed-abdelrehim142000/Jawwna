package com.example.jawwna.mainactivity.viewmodel

import android.app.Application
import android.content.res.Configuration
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.example.jawwna.R
import com.example.jawwna.datasource.repository.IRepository
import com.example.jawwna.helper.UpdateLocale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainActivityViewModel(private val repository: IRepository) : ViewModel() {

    private val _videoUri = MutableStateFlow<Uri>(Uri.EMPTY)
    val videoUri: StateFlow<Uri> get() = _videoUri

    private val _buttonNavigationBar = MutableStateFlow<Int>(0)
    val buttonNavigationBar: StateFlow<Int> get() = _buttonNavigationBar

    private val _iconFavorite = MutableStateFlow<Int>(0)
    val iconFavorite: StateFlow<Int> get() = _iconFavorite
    private val _iconSetting = MutableStateFlow<Int>(0)
    val iconSetting: StateFlow<Int> get() = _iconSetting
    private val _iconHome = MutableStateFlow<Int>(0)
    val iconHome: StateFlow<Int> get() = _iconHome

    private val _iconAlarm = MutableStateFlow<Int>(0)
    val iconAlarm: StateFlow<Int> get() = _iconAlarm

    private val _updateLocale = MutableStateFlow<String>("")
    val updateLocale: StateFlow<String> get() = _updateLocale

    private val _isNetworkAvailable = MutableStateFlow<Boolean>(false)
    val isNetworkAvailable: StateFlow<Boolean> get() = _isNetworkAvailable

    fun setVideoUri(packageName: String, nightModeFlags: Int) {
        val uri = when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> Uri.parse("android.resource://$packageName/${R.raw.nightmodebacground}")
            Configuration.UI_MODE_NIGHT_NO -> Uri.parse("android.resource://$packageName/${R.raw.lightmodebacground}")
            else -> Uri.parse("android.resource://$packageName/${R.raw.lightmodebacground}")
        }
        _videoUri.value = uri
    }
    fun setUpdateLocale(language: String) {
        _updateLocale.value = repository.getLanguage()!!
    }

    fun setIconUri(packageName: String, nightModeFlags: Int) {
        val favoriteUri: Int
        val settingsUri: Int
        val homeUri: Int
        val alarmUri: Int

        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> {
                favoriteUri =R.drawable.ic_favorite_night_mode
                settingsUri = R.drawable.round_settings_24_night_mode
                homeUri = R.drawable.round_home_24_night_mode
                alarmUri = R.drawable.add_alarm_nav_night_mode

            }
            Configuration.UI_MODE_NIGHT_NO -> {
                favoriteUri = R.drawable.ic_favorite_light_mode
                settingsUri = R.drawable.round_settings_24_light_mode
                homeUri = R.drawable.round_home_24_light_mode
                alarmUri = R.drawable.add_alarm_nav_light_mode
                }
            else -> {
                favoriteUri = R.drawable.ic_favorite_light_mode
                settingsUri = R.drawable.round_settings_24_light_mode
                homeUri = R.drawable.round_home_24_light_mode
                alarmUri = R.drawable.add_alarm_nav_light_mode
            }
        }

        // Set the icon URIs to the respective LiveData or MutableState variables
        _iconFavorite.value = favoriteUri
        _iconSetting.value = settingsUri
        _iconHome.value = homeUri
        _iconAlarm.value = alarmUri
    }



    fun setbuttonBackground(packageName: String, nightModeFlags: Int) {
        val colorResId = when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> R.drawable.bottom_bar_background_night_mode
            Configuration.UI_MODE_NIGHT_NO -> R.drawable.bottom_bar_background_light_mode
            else -> R.drawable.bottom_bar_background_light_mode
        }
        _buttonNavigationBar.value = colorResId


    }
    fun isNetworkAvailable() {
        _isNetworkAvailable.value = repository.isNetworkAvailable()
    }


}
