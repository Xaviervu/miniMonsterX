package ru.vegax.xavier.miniMonsterX.auxiliar

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus.*
import com.google.android.play.core.install.model.UpdateAvailability
import ru.vegax.xavier.miniMonsterX.R


// to use you need to provide updateLayout, btnUpdate, and imgVCloseUpdate for showing update, and snackContainer for showing that the update is ready
class AppUpdater(private val activity: Activity) :
        InstallStateUpdatedListener {


    companion object {
        private const val TAG = "AppUpdater"
        const val REQUEST_IMMEDIATE_UPDATE = 1001
        const val REQUEST_FLEXIBLE_UPDATE = 1002
    }

    private var downLoadStatus: Int = 0
    private var apUpdateInfo: AppUpdateInfo? = null
    private var updateManager = AppUpdateManagerFactory.create(activity)

    private var updateLayout: View? = null

    private var btnUpdate: Button? = null

    private var imgVCloseUpdate: ImageView? = null

    var snackContainer: View? = null

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
        downLoadStatus = status
        when (status) {

            UNKNOWN -> Log.d(TAG, "stateUpdate: Unknown")
            PENDING -> Log.d(TAG, "stateUpdate: Pending")
            DOWNLOADING -> {
                Log.d(TAG, "stateUpdate: Downloading")
                onShowDownloading()
            }
            DOWNLOADED -> {
                Log.d(TAG, "stateUpdate: Downloaded")

                onDownloaded()
            }
            INSTALLING -> Log.d(TAG, "stateUpdate: Installing")
            INSTALLED -> Log.d(TAG, "stateUpdate: Installed")
            FAILED -> Log.d(TAG, "stateUpdate: Installed")
            CANCELED -> Log.d(TAG, "stateUpdate: Cancelled")
        }
    }

    private fun updateIfRequired() {
        Log.d(TAG, "updateIfRequired")
        updateManager.appUpdateInfo
                .addOnSuccessListener { appUpdateInfo ->

                    Log.d(TAG, "updateIfRequired: updateAvailable = ${appUpdateInfo.updateAvailability()} available code = ${appUpdateInfo.availableVersionCode()}")
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                        if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) && shouldUpdateImmediately(appUpdateInfo.availableVersionCode())) {
                            startImmediateUpdate(appUpdateInfo)
                        } else if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                            Log.d(TAG, "updateIfRequired: Flexible update available")
                            apUpdateInfo = appUpdateInfo
                            onShowDownload()
                        }

                    }

                }
    }


    fun checkUpdating() {
        Log.d(TAG, "checkUpdating")
        updateManager.appUpdateInfo
                .addOnSuccessListener { appUpdateInfo ->
                    Log.d(TAG, "checkUpdating: UpdateAvailable = ${appUpdateInfo.updateAvailability()} available code = ${appUpdateInfo.availableVersionCode()}")

                    if (appUpdateInfo.updateAvailability() ==
                            UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS && shouldUpdateImmediately(appUpdateInfo.availableVersionCode())) {
                        startImmediateUpdate(appUpdateInfo)
                    }
                    stateUpdate(appUpdateInfo.installStatus())
                }
    }

    private fun shouldUpdateImmediately(versionCode: Int) = // if the version is a multiple of 10, then the app is going to be updated immediately
            (versionCode > 0 && versionCode.rem(10) == 0)

    fun unRegisterListener() {
        updateManager.unregisterListener(this)
    }

    private fun completeUpdate() {
        updateManager.completeUpdate()
    }

    private fun startImmediateUpdate(it: AppUpdateInfo) {
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

    private fun startFlexibleUpdate() {
        try {
            Log.d(TAG, "startFlexibleUpdate")
            apUpdateInfo?.let {
                updateManager.startUpdateFlowForResult(
                        it,
                        AppUpdateType.FLEXIBLE,
                        activity,
                        REQUEST_FLEXIBLE_UPDATE)
            }
        } catch (ex: Exception) {

            Log.e(TAG, "UpdateFlexible", ex)
        }
    }

    private fun onShowDownload() {
        btnUpdate?.isEnabled = true
        btnUpdate?.text = activity.getString(R.string.update_available)
        updateLayout?.show()
    }

    private fun onShowDownloading() {
        btnUpdate?.isEnabled = false
        btnUpdate?.text = activity.getString(R.string.updating)
        updateLayout?.show()
    }

    private fun onDownloaded() {
        showButtonCompleteUpdate()
    }

    private fun showButtonCompleteUpdate() {
        updateLayout?.show()
        btnUpdate?.text = activity.getString(R.string.update_ready)
        btnUpdate?.isEnabled = true
    }

    fun setUpdateLayout(updateLayout: View, btnUpdate: Button, imgVCloseUpdate: ImageView) {
        this.updateLayout?.let {
            updateLayout.visibility = it.visibility
        }
        this.updateLayout = updateLayout

        this.btnUpdate?.let {
            btnUpdate.text = it.text
            btnUpdate.isEnabled = it.isEnabled
        }
        this.btnUpdate = btnUpdate
        this.imgVCloseUpdate = imgVCloseUpdate
    }

    fun updateClick() {
        btnUpdate?.isEnabled = false
        updateLayout?.hide()
        if (downLoadStatus == DOWNLOADED) {
            completeUpdate()
        } else {
            startFlexibleUpdate()
        }
    }

    fun closeUpdateClick() {
        updateLayout?.hide()
    }

}