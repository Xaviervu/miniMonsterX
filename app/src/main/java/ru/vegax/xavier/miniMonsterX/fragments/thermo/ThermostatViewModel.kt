package ru.vegax.xavier.miniMonsterX.fragments.thermo

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import ru.vegax.xavier.miniMonsterX.R
import ru.vegax.xavier.miniMonsterX.models.Resource
import ru.vegax.xavier.miniMonsterX.models.ThermostatValues

// defUrl = "http://192.168.0.12"
class ThermostatViewModel(val app: Application, private val defUrl: String, private val thermostatN: Int) : AndroidViewModel(app) {
    private val mEvent = MutableLiveData<Resource<ThermostatValues?>>()
    val event: LiveData<Resource<ThermostatValues?>> = mEvent

    //    private val apiService =   ApiServiceFactory.createService(useGson = false)
    private var getValuesJob: Job? = null

    init {
        getThermostatValues()
    }

    fun getThermostatValues() {
        if (getValuesJob == null || getValuesJob?.isCancelled == true || getValuesJob?.isCompleted == true) {
            getValuesJob = viewModelScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        selectThermostat() // sets the correct thermostat as selected
                        Log.d(TAG, "thermostat $thermostatN set")
                        val thermData = Jsoup.connect("$defUrl?therm=").get()
                        Log.d(TAG, "getThermostatValues: $thermData")
                        getRefreshedThermalValues(thermData)
                    }

                } catch (e: Throwable) {
                    Log.e(TAG, "getThermostatValues:error ", e)
                    mEvent.postValue(Resource.error(null, Throwable(app.getString(R.string.setting_thermostat_error))))
                }

            }
        }
    }

    fun setThermostatActive(on: Boolean) {
        if (getValuesJob == null || getValuesJob?.isCancelled == true || getValuesJob?.isCompleted == true) {
            getValuesJob = viewModelScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        selectThermostat() // sets the correct thermostat as selected
                        Log.d(TAG, "thermostat $thermostatN set")
                        val onStr = if (on) "1" else "0"
//                        turn on thermostat 2
//                        http://192.168.0.12/password/?te=1-1
                        Jsoup.connect("$defUrl?te=${thermostatN}-$onStr").execute()
                        val thermData = Jsoup.connect("$defUrl?therm=").get()
                        Log.d(TAG, "getThermostatValues: $thermData")
                        getRefreshedThermalValues(thermData)
                    }

                } catch (e: Throwable) {
                    Log.e(TAG, "getThermostatValues:error ", e)
                    mEvent.postValue(Resource.error(null, Throwable(app.getString(R.string.setting_thermostat_error))))
                }

            }
        }
    }

    fun setTargetTemperature(temperature: Double) {
        if (getValuesJob == null || getValuesJob?.isCancelled == true || getValuesJob?.isCompleted == true) {
            getValuesJob = viewModelScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        selectThermostat() // sets the correct thermostat as selected
                        Log.d(TAG, "thermostat $thermostatN set")
                        //set target t for selected thermostat
                        //http://192.168.0.12/password/?t1s=23.3
                        Jsoup.connect("$defUrl?t1s=${temperature}").execute()
                        val thermData = Jsoup.connect("$defUrl?therm=").get()
                        Log.d(TAG, "getThermostatValues: $thermData")
                        getRefreshedThermalValues(thermData)
                    }

                } catch (e: Throwable) {
                    Log.e(TAG, "getThermostatValues:error ", e)
                    mEvent.postValue(Resource.error(null, Throwable(app.getString(R.string.setting_thermostat_error))))
                }

            }
        }
    }

    fun setHysteresis(temperature: Double) {
        if (getValuesJob == null || getValuesJob?.isCancelled == true || getValuesJob?.isCompleted == true) {
            getValuesJob = viewModelScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        selectThermostat() // sets the correct thermostat as selected
                        Log.d(TAG, "thermostat $thermostatN set")
                        //set target t for selected thermostat
                        //http://192.168.0.12/password/?t1h=1.3
                        Jsoup.connect("$defUrl?t1h=${temperature}").execute()
                        val thermData = Jsoup.connect("$defUrl?therm=").get()
                        Log.d(TAG, "getThermostatValues: $thermData")
                        getRefreshedThermalValues(thermData)
                    }

                } catch (e: Throwable) {
                    Log.e(TAG, "getThermostatValues:error ", e)
                    mEvent.postValue(Resource.error(null, Throwable(app.getString(R.string.setting_thermostat_error))))
                }

            }
        }
    }

    fun setCalTemperature(temperature: Double) {
        if (getValuesJob == null || getValuesJob?.isCancelled == true || getValuesJob?.isCompleted == true) {
            getValuesJob = viewModelScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        selectThermostat() // sets the correct thermostat as selected
                        Log.d(TAG, "thermostat $thermostatN set")
                        //set target t for selected thermostat
                        //http://192.168.0.12/password/?t1c=1.5
                        Jsoup.connect("$defUrl?t1c=${temperature}").execute()
                        val thermData = Jsoup.connect("$defUrl?therm=").get()
                        Log.d(TAG, "getThermostatValues: $thermData")
                        getRefreshedThermalValues(thermData)
                    }

                } catch (e: Throwable) {
                    Log.e(TAG, "getThermostatValues:error ", e)
                    mEvent.postValue(Resource.error(null, Throwable(app.getString(R.string.setting_thermostat_error))))
                }

            }
        }
    }

    private fun getRefreshedThermalValues(thermData: Document) {
        val preBody = thermData.body().getElementsByTag("pre")
        if (preBody.text().contains("[ON]")) {

            val thermostatValues = parseThermostatValues(preBody)
            mEvent.postValue(Resource.success(thermostatValues))
        } else {
            mEvent.postValue(Resource.success(ThermostatValues(false)))
        }
    }

    private fun selectThermostat() {
        Jsoup.connect("$defUrl?therm_fs=${thermostatN}").execute()
    }

    private fun parseThermostatValues(preBody: Elements): ThermostatValues {
        val temperature = preBody[0].textNodes()[1].toString().substringAfter('=').substringBefore('\n').toDouble()
        val isOn = preBody[0].children()[1].textNodes()[0].toString() == "ON"
        val targetTemperature = preBody[0].children()[3].children()[0].toString().substringAfter('"').substringBefore('"').toDouble()
        val hysteresisTemperature = preBody[0].children()[4].children()[0].toString().substringAfter('"').substringBefore('"').toDouble()
        val calibrationTemperature = preBody[0].children()[5].children()[0].toString().substringAfter('"').substringBefore('"').toDouble()
        return ThermostatValues(true, temperature, isOn, targetTemperature, hysteresisTemperature, calibrationTemperature)
    }

    companion object {
        private const val TAG = "ThermostatViewModel"
    }
}

class ThermostatVMFactory(private val app: Application, private val defUrl: String, private val thermostatN: Int) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ThermostatViewModel(app, defUrl, thermostatN) as T
    }
}