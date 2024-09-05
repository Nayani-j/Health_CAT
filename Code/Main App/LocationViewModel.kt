package com.example.health_cat

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class LocationViewModel : ViewModel() {
    private val _location = mutableStateOf<LocationData?>(null)
    val location: State<LocationData?> = _location

    private val _selectedLocation = MutableLiveData<LocationData>()
    val selectedLocation: LiveData<LocationData> = _selectedLocation

    fun setSelectedLocation(location: LocationData) {
        _selectedLocation.value = location
    }

    private val _address = mutableStateOf<List<GeocodingResult>>(emptyList())
    val address: State<List<GeocodingResult>> = _address

    fun updateLocation(newLocation: LocationData) {
        _location.value = newLocation
    }

    fun fetchAddress(latlng: String) {
        try {
            viewModelScope.launch {
                val result = RetrofitClient.create().getAddressFromCoordinates(
                    latlng,
                    "AIzaSyCLXjRPYj-hPp4WxfdZ4rSjWjp2evYgouU"
                )
                _address.value = result.results
            }
        } catch (e: Exception) {
            Log.d("LocationViewModel", "Error fetching address: ${e.message}")
        }
    }
}