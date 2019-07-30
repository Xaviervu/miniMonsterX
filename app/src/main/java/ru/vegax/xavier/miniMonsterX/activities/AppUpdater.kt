package ru.vegax.xavier.miniMonsterX.activities

import android.app.Activity
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus.*
import com.google.android.play.core.install.model.UpdateAvailability


class AppUpdater(private val activity: Activity, private val updateListener: UpdateListener) :
        InstallStateUpdatedListener {
    companion object {
        private const val TAG = "AppUpdater"
        const val REQUEST_IMMEDIATE_UPDATE = 1001
        const val REQUEST_FLEXIBLE_UPDATE = 1002
    }

    private var apUpdateInfo: AppUpdateInfo? = null
    private var updateManager = AppUpdateManagerFactory.create(activity)

    init {
        updateManager.registerListener(this)
        updateIfRequired()
    }

    override fun onStateUpdate(state: InstallState) {
        Log.d(TAG, "onStateUpdate")
        val status = state.installStatus()
        stateUpdate(status)
    }

    private fun stateUpdate(status: Int) {
        when (status) {

            UNKNOWN -> Log.d(TAG, "stateUpdate: Unknown")
            REQUIRES_UI_INTENT -> Log.d(TAG, "stateUpdate: Requires ui intent")
            PENDING -> Log.d(TAG, "stateUpdate: Pending")
            DOWNLOADING -> {
                Log.d(TAG, "stateUpdate: Downloading")
                updateListener.onShowDownloading()
            }
            DOWNLOADED -> {
                Log.d(TAG, "stateUpdate: Downloaded")
                updateListener.onDownloaded()
            }
            INSTALLING -> Log.d(TAG, "stateUpdate: Installing")
            INSTALLED -> Log.d(TAG, "stateUpdate: Installed")
            FAILED -> Log.d(TAG, "stateUpdate: Installed")
            CANCELED -> Log.d(TAG, "stateUpdate: Cancelled")
        }
    }

    private fun updateIfRequired() {
        updateManager.appUpdateInfo
                .addOnSuccessListener {

                    Log.d(TAG, "updateIfRequired: ppdateAvailable = ${it.updateAvailability()} available code = ${it.availableVersionCode()}")
                    if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {

                        val versionCode = it.availableVersionCode()
                        if (it.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) && shouldUpdateImmediately(versionCode)) {
                            startImmediateUpdate(it)
                        } else if (it.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                            Log.d(TAG, "updateIfRequired: Flexible update available")
                            apUpdateInfo = it
                            updateListener.onShowDownload()
                        }

                    }

                }
    }


    fun checkUpdating() {
        updateManager.appUpdateInfo
                .addOnSuccessListener {
                    Log.d(TAG, "checkUpdating: UpdateAvailable = ${it.updateAvailability()} available code = ${it.availableVersionCode()}")

                    if (it.updateAvailability() ==
                            UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS && shouldUpdateImmediately(it.availableVersionCode())) {
                        startImmediateUpdate(it)
                    }
                    stateUpdate(it.installStatus())
                }
    }

    private fun shouldUpdateImmediately(versionCode: Int) = // if the version is a multiple of 10, then the app is going to be updated immediately
            (versionCode > 0 && versionCode.rem(10) == 0)

    fun unRegisterListener() {
        updateManager.unregisterListener(this)
    }

    fun completeUpdate() {
        updateManager.completeUpdate()
    }

    private fun startImmediateUpdate(it: AppUpdateInfo?) {
        try {
            Log.d(TAG, "startImmediateUpdate")
            updateManager.startUpdateFlowForResult(
                    it,
                    AppUpdateType.IMMEDIATE,
                    activity,
                    REQUEST_IMMEDIATE_UPDATE)
        } catch (ex: Exception) {
            Log.e(TAG, "immediateUpdate", ex)
        }
    }

    fun startFlexibleUpdate() {
        try {
            Log.d(TAG, "startFlexibleUpdate")
            updateManager.startUpdateFlowForResult(
                    apUpdateInfo,
                    AppUpdateType.FLEXIBLE,
                    activity,
                    REQUEST_FLEXIBLE_UPDATE)
        } catch (ex: Exception) {
            Log.e(TAG, "UpdateFlexible", ex)
        }
    }

    interface UpdateListener {
        fun onShowDownload()
        fun onShowDownloading()
        fun onDownloaded()
    }
}

