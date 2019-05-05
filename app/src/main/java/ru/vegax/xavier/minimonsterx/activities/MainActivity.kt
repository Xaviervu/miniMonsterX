package ru.vegax.xavier.minimonsterx.activities


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.navigation.NavigationView
import ru.vegax.xavier.minimonsterx.R
import ru.vegax.xavier.minimonsterx.activities.SettingsActivity.Companion.EXTRA_FOR_CREATION
import ru.vegax.xavier.minimonsterx.activities.SettingsActivity.Companion.EXTRA_NAME
import ru.vegax.xavier.minimonsterx.activities.SettingsActivity.Companion.EXTRA_PASS
import ru.vegax.xavier.minimonsterx.activities.SettingsActivity.Companion.EXTRA_URL
import ru.vegax.xavier.minimonsterx.activities.SettingsActivity.Companion.newSettingsIntent
import ru.vegax.xavier.minimonsterx.iodata.IOFragment
import ru.vegax.xavier.minimonsterx.repository.DeviceData
import ru.vegax.xavier.minimonsterx.select_device.DeviceSelectFragment


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, DeviceSelectFragment.OnFragmentInteractionListener {

    private var mDeviceList: List<DeviceData>? = null
    private lateinit var mTxtVCurrDevice: TextView
    private lateinit var mIOFragment: IOFragment
    private val viewModel by lazy {
        ViewModelProviders.of(this).get(IODataViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)


        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = object : ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {

            override fun onDrawerOpened(drawerView: View) {
//                val txtVTitle = findViewById<TextView>(R.id.txtVCurrentDevice)


            }
        }

        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        val header = navigationView.getHeaderView(0)
        mTxtVCurrDevice = header.findViewById(R.id.txtVCurrentDevice)

        navigationView.setNavigationItemSelectedListener(this)
        if (savedInstanceState == null) {
            mIOFragment = IOFragment()
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.add(R.id.ioFragment, mIOFragment).addToBackStack(null).commit()
        }
        observeViewModel()
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


    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer != null) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START)
            } else {

                if (mIOFragment.isVisible) {
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
            mIOFragment.stopUpdating()

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
            R.id.nav_device // choose a device from list of saved devices
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
                // Handle the camera import action (for now display a toast).
                mIOFragment.stopUpdating()

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
        }
    }

    private fun refreshData() {
        mIOFragment.refreshData()
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
        if (mIOFragment.isVisible) {
            mIOFragment.clearData()
            mIOFragment.cancelTasks()
        }
    }


    companion object {
        const val SETTINGS = 1
        const val PORT_NUMBER = 6


        const val PREFF_DEV_ID = "PREFF_DEV_ID"

        private const val DEFAULT_URL = "http://192.168.0.12" // default URL
        private const val DEFAULT_PASS = "password" // default URL
    }
}
