package ru.vegax.xavier.minimonsterx.activities

import android.app.Activity
import android.content.Context
import android.content.Intent

import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem


import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import ru.vegax.xavier.minimonsterx.R
import java.util.HashSet
import ru.vegax.xavier.minimonsterx.iodata.IOFragment
import ru.vegax.xavier.minimonsterx.select_device.DeviceSelectFragment

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, DeviceSelectFragment.OnFragmentInteractionListener {
    private var mUrlAddress: String = ""
    private var mUrlPassword: String = ""

    private var mUrlBase: String = ""
    private var mCurrName: String = ""
    private val mDefURL = "http://192.168.0.12" // default URL

    override fun onCreate(savedInstanceState: Bundle?) {
        getPrefs()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)


        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = object : ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {

            override fun onDrawerOpened(drawerView: View) {
                val txtVTitle = findViewById<TextView>(R.id.txtVCurrentDevice)
                if (mCurrName == "") {
                    txtVTitle.setText(R.string.current_device)
                } else {
                    txtVTitle.text = mCurrName
                }
            }
        }

        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        val header = navigationView.getHeaderView(0)
        val txtVCurrDevice = header.findViewById<TextView>(R.id.txtVCurrentDevice)
        if (mCurrName == "") {
            txtVCurrDevice.setText(R.string.current_device)
        } else {
            txtVCurrDevice.text = mCurrName
        }
        navigationView.setNavigationItemSelectedListener(this)
        generateConnStrings()
        if (savedInstanceState == null) {
            val ioFragment = IOFragment.newInstance(mUrlBase)
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.add(R.id.ioFragment, ioFragment).addToBackStack(null).commit()
        }
    }


    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer != null) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START)
            } else {
                val currentFragment = supportFragmentManager.findFragmentById(R.id.ioFragment)
                if (currentFragment != null && currentFragment.isVisible) {
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
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra(EXTRA_NAME, mCurrName)
            if (mCurrName == "") {
                intent.putExtra(EXTRA_URL, mDefURL)
            } else {
                intent.putExtra(EXTRA_URL, mUrlAddress)
            }
            intent.putExtra(EXTRA_PASS, mUrlPassword)
            intent.putExtra(EXTRA_FOR_CREATION, mCurrName == "") // if there is no current device then open for creation
            val currentFragment = supportFragmentManager.findFragmentById(R.id.ioFragment) as IOFragment?
            if (currentFragment != null && currentFragment.isVisible) {
                currentFragment.setConnData(mUrlBase)
                currentFragment.stopUpdating()
            }
            startActivityForResult(intent, MY_REQUEST_ID)
            return true
        }

        return super.onOptionsItemSelected(item)
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_device // choose a device from list of saved devices
            -> {
                // Handle the camera import action (for now display a toast).
                val preferences = applicationContext.getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE)
                val set = preferences.getStringSet(PREFF_DEV_NAME, null)
                if (set != null && set.size > 0) {
                    val deviceSelect = DeviceSelectFragment().newInstance(mCurrName)

                    deviceSelect.show(supportFragmentManager, "selectDialog")
                }
                drawer.closeDrawer(GravityCompat.START)

                return true
            }
            R.id.nav_addDevice -> {
                // Handle the camera import action (for now display a toast).

                val intent = Intent(this, SettingsActivity::class.java)
                intent.putExtra(EXTRA_NAME, "")
                intent.putExtra(EXTRA_URL, mDefURL)
                intent.putExtra(EXTRA_PASS, "")
                intent.putExtra(EXTRA_FOR_CREATION, true)
                val currentFragment = supportFragmentManager.findFragmentById(R.id.ioFragment) as IOFragment?

                if (currentFragment != null && currentFragment.isVisible) {
                    currentFragment.setConnData(mUrlBase)
                    currentFragment.stopUpdating()
                }

                startActivityForResult(intent, MY_REQUEST_ID)

                drawer.closeDrawer(GravityCompat.START)
                return true
            }

            else -> return false
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == MY_REQUEST_ID) {
            if (resultCode == Activity.RESULT_OK) {

                mCurrName = data?.getStringExtra(EXTRA_NAME) ?: ""
                mUrlAddress = data?.getStringExtra(EXTRA_URL)  ?: ""
                mUrlPassword = data?.getStringExtra(EXTRA_PASS) ?: ""
                mUrlAddress = fixUrl(mUrlAddress)
                val forCreation = data?.getBooleanExtra(EXTRA_FOR_CREATION, false) ?: false
                rememberPrefs(forCreation)
                generateConnStrings()
            }
            refreshData()
        }
    }

    private fun generateConnStrings() {
        mUrlBase = joinStrings(mUrlAddress, mUrlPassword, "/")
    }

    private fun rememberPrefs(forCreation: Boolean) {
        val preferences = this.applicationContext.getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        if (forCreation) {
            var set: MutableSet<String>? = preferences.getStringSet(PREFF_DEV_NAME, null)
            if (set != null) {
                set.add(mCurrName)
            } else {
                set = HashSet()
                set.add(mCurrName)
            }
            editor.putStringSet(PREFF_DEV_NAME, set)
        }
        editor.putString(PREFF_URL + mCurrName, mUrlAddress)
        editor.putString(PREFF_PASS + mCurrName, mUrlPassword)
        editor.apply()
    }

    private fun getPrefs() {
        val preferences = this.applicationContext.getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE)
        val currDevice = preferences.getString(PREFF_CURR_DEVICE, "")

        val set = preferences.getStringSet(PREFF_DEV_NAME, null)
        if (set != null && set.size > 0) {
            if (set.contains(currDevice)) {
                mCurrName = currDevice!!
            } else {
                mCurrName = set.toTypedArray()[0] as String
            }
            mUrlAddress = preferences.getString(PREFF_URL + mCurrName, "http://192.168.0.12") ?: "http://192.168.0.12"
            mUrlPassword = preferences.getString(PREFF_PASS + mCurrName, "password") ?: "password"
        } else {
            mCurrName = ""
            mUrlAddress = ""
            mUrlPassword = ""
        }
    }

    private fun joinStrings(urlStr: String?, passStr: String?, endStr: String): String {
        return urlStr + passStr + endStr
    }

    private fun fixUrl(urlStr: String): String {
        val htmlStr = "http://"
        val slashStr = "/"
        var urlComplete = ""
        if (!urlStr.startsWith(htmlStr)) {
            urlComplete = htmlStr
        }
        urlComplete += urlStr
        if (!urlStr.endsWith(slashStr)) {
            urlComplete += slashStr
        }
        return urlComplete
    }

    override fun onFragmentResult(deviceName: String) {
        setNewDevice(deviceName)
    }

    override fun onDeleteItem(deviceName: String) {
        deleteDevice(deviceName)
    }

    private fun deleteDevice(deviceName: String) {
        cancelTasks()
        val preferences = applicationContext.getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.remove(PREFF_URL + deviceName)
        editor.remove(PREFF_PASS + deviceName)
        val set = preferences.getStringSet(PREFF_DEV_NAME, null)
        editor.remove(PREFF_DEV_NAME).apply()
        if (set != null && set.size > 0) {
            set.remove(deviceName)

            if (set.size > 0) {
                editor.putStringSet(PREFF_DEV_NAME, set)
                mCurrName = set.toTypedArray()[0] as String
                mUrlAddress = preferences.getString(PREFF_URL + mCurrName, "") ?: ""
                mUrlPassword = preferences.getString(PREFF_PASS + mCurrName, "") ?: ""
                editor.putString(PREFF_CURR_DEVICE, mCurrName)

            } else {
                editor.putString(PREFF_CURR_DEVICE, "")
                mCurrName = ""
                mUrlAddress = ""
                mUrlPassword = ""

            }
        } else {
            editor.putString(PREFF_CURR_DEVICE, "")
            mCurrName = ""
            mUrlAddress = ""
            mUrlPassword = ""

        }
        editor.apply()
        generateConnStrings()
        cancelTasks()
        refreshData()
    }

    private fun setNewDevice(deviceName: String) {
        val preferences = applicationContext.getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE)
        mCurrName = deviceName
        mUrlAddress = preferences.getString(PREFF_URL + mCurrName, "") ?: ""
        mUrlPassword = preferences.getString(PREFF_PASS + mCurrName, "") ?: ""
        generateConnStrings()
        cancelTasks()
        refreshData()
    }

    private fun cancelTasks() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.ioFragment) as IOFragment?
        if (currentFragment != null && currentFragment.isVisible) {
            currentFragment.clearData()
            currentFragment.cancelTasks()
        }
    }

    private fun refreshData() {
        //remember last selected element
        val preferences = this.applicationContext.getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(PREFF_CURR_DEVICE, mCurrName)
        editor.apply()


        val currentFragment = supportFragmentManager.findFragmentById(R.id.ioFragment) as IOFragment?
        if (currentFragment != null && currentFragment.isVisible) {
            currentFragment.setConnData(mUrlBase)
            currentFragment.refreshData()
        }
    }

    companion object {

        const val MY_REQUEST_ID = 1

        const val EXTRA_NAME = "NAME_EXTRA"
        const val EXTRA_URL = "URL_EXTRA"
        const val EXTRA_PASS = "PASS_EXTRA"
        const val EXTRA_FOR_CREATION = "FOR_CREATION_EXTRA"

        const val MY_PREFS = "my_prefs"
        const val PREFF_DEV_NAME = "PREFF_DEV_NAME"
        const val PREFF_URL = "PREFF_URL"
        const val PREFF_PASS = "PREFF_PASS"
        private const val PREFF_CURR_DEVICE = "PREFF_CURR_DEVICE"
    }
}
