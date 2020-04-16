package ru.vegax.xavier.miniMonsterX.activities

import android.app.Activity
import android.app.UiModeManager
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.internal.NavigationMenuView
import com.google.android.material.navigation.NavigationView
import com.google.android.play.core.install.model.ActivityResult.RESULT_IN_APP_UPDATE_FAILED
import ru.vegax.xavier.miniMonsterX.R
import ru.vegax.xavier.miniMonsterX.activities.SettingsActivity.Companion.EXTRA_FOR_CREATION
import ru.vegax.xavier.miniMonsterX.activities.SettingsActivity.Companion.EXTRA_NAME
import ru.vegax.xavier.miniMonsterX.activities.SettingsActivity.Companion.EXTRA_PASS
import ru.vegax.xavier.miniMonsterX.activities.SettingsActivity.Companion.EXTRA_URL
import ru.vegax.xavier.miniMonsterX.activities.SettingsActivity.Companion.newSettingsIntent
import ru.vegax.xavier.miniMonsterX.auxiliar.AppUpdater
import ru.vegax.xavier.miniMonsterX.databinding.ActivityMainBinding
import ru.vegax.xavier.miniMonsterX.fragments.iodata.IOFragment
import ru.vegax.xavier.miniMonsterX.repository.DeviceData
import ru.vegax.xavier.miniMonsterX.select_device.DeviceSelectFragment

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, DeviceSelectFragment.OnFragmentInteractionListener {

    private lateinit var viewBinding: ActivityMainBinding

    private lateinit var appUpdater: AppUpdater
    private var mDeviceList: List<DeviceData>? = null
    private lateinit var mTxtVCurrDevice: TextView
    private var mIOFragment: IOFragment? = null
    private val viewModel by lazy {
        ViewModelProviders.of(this).get(IODataViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isAndroidTV()) {
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        }
        appUpdater = AppUpdater(this)
        viewBinding = DataBindingUtil.setContentView(
                this,
                R.layout.activity_main)
        setContentView(viewBinding.root)

        with(viewBinding.mainIOLayout.vToolbar.updateView) {
            setUpdateLayout(updateLayout, btnUpdate, imgVCloseUpdate)
        }
        appUpdater.snackContainer = viewBinding.mainIOLayout.mainIOLayout
        val toolbar = viewBinding.mainIOLayout.vToolbar.toolbar
        setSupportActionBar(toolbar)


        val drawer = viewBinding.drawerLayout
        val navigationView = viewBinding.navView
        val toggle = object : ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {

            override fun onDrawerOpened(drawerView: View) {
                if (navigationView.requestFocus()) {
                    val navigationMenuView = navigationView.focusedChild as NavigationMenuView
                    navigationMenuView.descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
                }
            }
        }

        drawer.addDrawerListener(toggle)
        toggle.syncState()


        val header = navigationView.getHeaderView(0)
        mTxtVCurrDevice = header.findViewById(R.id.txtVCurrentDevice)

        navigationView.setNavigationItemSelectedListener(this)
        if (savedInstanceState == null) {
            val fragment = IOFragment.newInstance()
            mIOFragment = fragment

            supportFragmentManager.beginTransaction()
                    .add(R.id.ioFragment, fragment, fragment.fragmentTag)
                    .addToBackStack(null).commit()
        } else {
            mIOFragment = supportFragmentManager.findFragmentByTag(IOFragment.TAG) as? IOFragment
        }
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        appUpdater.checkUpdating()
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdater.unRegisterListener()
    }

    fun updateClick(button: View) {
        appUpdater.updateClick()

    }

    fun closeUpdateClick(close: View) {
        appUpdater.closeUpdateClick()
    }

    private fun observeViewModel() {
        viewModel.currentDeviceLiveData.observe(this, Observer { curDevice ->
            if (curDevice != null) {
                mTxtVCurrDevice.text = curDevice.deviceName
            }
        })
        viewModel.allDevices.observe(this, Observer {
            if (it != null) {
                mDeviceList = it
            }
        })
    }

    private fun isAndroidTV(): Boolean {
        val uiModeManager = getSystemService(UI_MODE_SERVICE) as? UiModeManager
        return (uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION)
    }

    private fun setUpdateLayout(updateLayout: View, btnUpdate: Button, imgVCloseUpdate: ImageView) {
        appUpdater.setUpdateLayout(updateLayout, btnUpdate, imgVCloseUpdate)

    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer != null) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START)
            } else {

                if (mIOFragment?.isVisible == true) {
                    finish()
                } else {
                    super.onBackPressed()
                }

            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_refresh) {
            refreshData()
        } else if (id == R.id.action_settings) {
            mIOFragment?.stopUpdating()

            val curDevice = viewModel.curDevice
            val intent = newSettingsIntent(this, curDevice?.deviceId ?: 0, curDevice?.deviceName
                    ?: "",
                    curDevice?.url ?: DEFAULT_URL, curDevice?.password
                    ?: DEFAULT_PASS, curDevice == null)

            startActivityForResult(intent, SETTINGS)
            return true
        }

        return super.onOptionsItemSelected(item)
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)

        when (item.itemId) {
            R.id.nav_device
            -> {
                if (!mDeviceList.isNullOrEmpty()) {
                    val deviceSelect = DeviceSelectFragment()
                    deviceSelect.show(supportFragmentManager, "selectDialog")
                } else {
                    Toast.makeText(this, getString(R.string.no_devices), Toast.LENGTH_SHORT).show()
                }
                drawer.closeDrawer(GravityCompat.START)

                return true
            }
            R.id.nav_addDevice -> {
                mIOFragment?.stopUpdating()

                val intent = newSettingsIntent(this, 0, "",
                        DEFAULT_URL, DEFAULT_PASS, true)

                startActivityForResult(intent, SETTINGS)
                drawer.closeDrawer(GravityCompat.START)
                return true
            }

            else -> return false
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                val forCreation = data?.getBooleanExtra(EXTRA_FOR_CREATION, false) ?: false
                val name = data?.getStringExtra(EXTRA_NAME) ?: ""
                val urlAddress = data?.getStringExtra(EXTRA_URL) ?: ""
                val password = data?.getStringExtra(EXTRA_PASS) ?: ""
                if (forCreation) {
                    val newDevice = DeviceData(name, urlAddress, password, MutableList(PORT_NUMBER) { "port$it" }, MutableList(PORT_NUMBER) { false })
                    viewModel.insert(newDevice)
                } else {
                    val curDevice = viewModel.curDevice
                    if (curDevice != null) {
                        curDevice.deviceName = name
                        curDevice.url = urlAddress
                        curDevice.password = password
                        viewModel.insert(curDevice)
                    }
                }

            } else {
                refreshData()
            }
        } else if (requestCode == REQUEST_CODE_UPDATE) {
            if (resultCode != RESULT_OK) {
                if (resultCode == RESULT_CANCELED) {
                    finish()
                } else if (resultCode == RESULT_IN_APP_UPDATE_FAILED) {
                    Toast.makeText(application, getString(R.string.could_not_update), Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    private fun refreshData() {
        mIOFragment?.refreshData()
    }

    override fun onFragmentResult(deviceId: Long) {
        selectDevice(deviceId)
    }

    override fun onDeleteItem(deviceId: Long) {
        deleteDevice(deviceId)
    }

    private fun deleteDevice(deviceId: Long) {
        cancelTasks()
        viewModel.remove(deviceId)
    }

    private fun selectDevice(deviceId: Long) {
        cancelTasks()
        viewModel.getDevice(deviceId)
    }

    private fun cancelTasks() {
        if (mIOFragment?.isVisible == true) {
            mIOFragment?.clearData()
            mIOFragment?.cancelTasks()
        }
    }


    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CODE_UPDATE = 1001
        const val SETTINGS = 1
        const val PORT_NUMBER = 6


        const val PREFF_DEV_ID = "PREFF_DEV_ID"

        private const val DEFAULT_URL = "http://192.168.0.12" // default URL
        private const val DEFAULT_PASS = "" // default URL
    }
}
