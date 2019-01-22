package ru.vegax.xavier.miniMonsterX;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;


import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import ru.vegax.xavier.miniMonsterX.IOData.IOFragment;
import ru.vegax.xavier.miniMonsterX.selectDevice.DeviceSelectFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DeviceSelectFragment.OnFragmentInteractionListener {

    public static final int MY_REQUEST_ID = 1;

    public static final String EXTRA_NAME = "NAME_EXTRA";
    public static final String EXTRA_URL = "URL_EXTRA";
    public static final String EXTRA_PASS = "PASS_EXTRA";
    public static final String EXTRA_FOR_CREATION = "FOR_CREATION_EXTRA";

    public static final String MY_PREFS = "my_prefs";
    public static final String PREFF_DEV_NAME = "PREFF_DEV_NAME";
    public static final String PREFF_URL = "PREFF_URL";
    public static final String PREFF_PASS = "PREFF_PASS";
    private static final String PREFF_CURR_DEVICE = "PREFF_CURR_DEVICE";
    public static String PACKAGE_NAME;
    public String _urlAddress;
    public String _urlPassword;

    public static final String _urlSufixMain = "/?main=";
    public static final String _urlSufixJSon = "/?js=";

    String _urlJson;
    String _urlMain;
    String _urlBase;
    private String _currName;
    private String _defURL = "http://192.168.0.12/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getPrefs();

        PACKAGE_NAME = getApplicationContext().getPackageName();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                TextView txtVTitle = findViewById(R.id.txtVCurrentDevice);
                if (_currName == null || _currName.equals("")) {
                    txtVTitle.setText(R.string.current_device);
                } else {
                    txtVTitle.setText(_currName);
                }
            }
        };

        if (drawer != null) {
            drawer.addDrawerListener(toggle);
        }
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            View header = navigationView.getHeaderView(0);
            TextView txtVCurrDevice = header.findViewById(R.id.txtVCurrentDevice);
            if (_currName == null || _currName.equals("")) {
                txtVCurrDevice.setText(R.string.current_device);
            } else {
                txtVCurrDevice.setText(_currName);
            }
            navigationView.setNavigationItemSelectedListener(this);
        }

        generateConnStrings();

        if (savedInstanceState == null) {
            IOFragment ioFragment = IOFragment.newInstance(_urlJson, _urlMain, _urlBase);
            // Get the FragmentManager and start a transaction.
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            // Add the SimpleFragment.
            fragmentTransaction.add(R.id.ioFragment, ioFragment).addToBackStack(null).commit();
        }
    }


    /**
     * Handles the Back button: closes the nav drawer.
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer != null) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.ioFragment);
                if (currentFragment != null && currentFragment.isVisible()) {
                    finish();
                } else {
                    super.onBackPressed();
                }

            }
        }

    }

    /**
     * Inflates the options menu.
     *
     * @param menu Menu to inflate
     * @return Returns true if menu is inflated.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Handles a click on the Settings item in the options menu.
     *
     * @param item Item in options menu that was clicked.
     * @return Returns true if the item was Settings.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            refreshData();
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra(EXTRA_NAME, _currName);
            if (_currName.equals("")) {
                intent.putExtra(EXTRA_URL, _defURL);
            } else {
                intent.putExtra(EXTRA_URL, _urlAddress);
            }
            intent.putExtra(EXTRA_PASS, _urlPassword);
            intent.putExtra(EXTRA_FOR_CREATION, _currName.equals("")); // if there is no current device then open for creation
            IOFragment currentFragment = (IOFragment) getSupportFragmentManager().findFragmentById(R.id.ioFragment);

            if (currentFragment != null && currentFragment.isVisible()) {
                currentFragment.setConnData(_urlJson, _urlMain, _urlBase);
                currentFragment.stopUpdating();
            }
            startActivityForResult(intent, MY_REQUEST_ID);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Handles a navigation drawer item click. It detects which item was
     * clicked and displays a toast message showing which item.
     *
     * @param item Item in the navigation drawer
     * @return Returns true after closing the nav drawer
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_device: // choose a device from list of saved devices


                // Handle the camera import action (for now display a toast).
                SharedPreferences preferences = getApplicationContext().getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE);
                Set<String> set = preferences.getStringSet(PREFF_DEV_NAME, null);
                if (set != null && set.size() > 0) {
                    DeviceSelectFragment deviceSelect = new DeviceSelectFragment().newInstance(_currName);

                    deviceSelect.show(getSupportFragmentManager(), "selectDialog");
                }
                drawer.closeDrawer(GravityCompat.START);


                return true;
            case R.id.nav_addDevice:
                // Handle the camera import action (for now display a toast).

                Intent intent = new Intent(this, SettingsActivity.class);
                intent.putExtra(EXTRA_NAME, "");
                intent.putExtra(EXTRA_URL, _defURL);
                intent.putExtra(EXTRA_PASS, "");
                intent.putExtra(EXTRA_FOR_CREATION, true);
                IOFragment currentFragment = (IOFragment) getSupportFragmentManager().findFragmentById(R.id.ioFragment);

                if (currentFragment != null && currentFragment.isVisible()) {
                    currentFragment.setConnData(_urlJson, _urlMain, _urlBase);
                    currentFragment.stopUpdating();
                }

                startActivityForResult(intent, MY_REQUEST_ID);

                drawer.closeDrawer(GravityCompat.START);
                return true;

            default:
                return false;
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_REQUEST_ID) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    _currName = data.getStringExtra(EXTRA_NAME);
                    _urlAddress = data.getStringExtra(EXTRA_URL);
                    _urlPassword = data.getStringExtra(EXTRA_PASS);
                    _urlAddress = fixUrl(_urlAddress);
                    boolean forCreation = data.getBooleanExtra(EXTRA_FOR_CREATION, false);

                    rememberPrefs(forCreation);
                    generateConnStrings();

                }

            }
            refreshData();
        }
    }

    private void generateConnStrings() {
        _urlJson = joinStrings(_urlAddress, _urlPassword, _urlSufixJSon);
        _urlMain = joinStrings(_urlAddress, _urlPassword, _urlSufixMain);
        _urlBase = joinStrings(_urlAddress, _urlPassword, "/");
    }

    /**
     * Displays a toast message.
     *
     * @param message Message to display in toast
     */
    public void displayToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void rememberPrefs(boolean forCreation) {
        SharedPreferences preferences = this.getApplicationContext().getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        if (forCreation) {
            Set<String> set = preferences.getStringSet(PREFF_DEV_NAME, null);
            if (set != null) {
                set.add(_currName);
            } else {
                set = new HashSet<>();
                set.add(_currName);
            }
            editor.putStringSet(PREFF_DEV_NAME, set);
        }
        editor.putString(PREFF_URL + _currName, _urlAddress);
        editor.putString(PREFF_PASS + _currName, _urlPassword);
        editor.apply();
    }

    private void getPrefs() {
        SharedPreferences preferences = this.getApplicationContext().getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE);
        String currDevice = preferences.getString(PREFF_CURR_DEVICE, "");

        Set<String> set = preferences.getStringSet(PREFF_DEV_NAME, null);
        if (set != null && set.size() > 0) {
            if (set.contains(currDevice)) {
                _currName = currDevice;
            } else {
                _currName = (String) Objects.requireNonNull(set.toArray())[0];
            }
            _urlAddress = preferences.getString(PREFF_URL + _currName, "http://192.168.0.12/");
            _urlPassword = preferences.getString(PREFF_PASS + _currName, "klmvts");
        } else {
            _currName = "";
            _urlAddress = "";
            _urlPassword = "";
        }
    }

    private String joinStrings(String urlStr, String passStr, String endStr) {
        return (urlStr + passStr + endStr);
    }

    @NonNull
    private String fixUrl(String urlStr) {
        String htmlStr = "http://";
        String slashStr = "/";
        String urlComplete = "";
        if (!urlStr.startsWith(htmlStr)) {
            urlComplete = htmlStr;
        }
        urlComplete = urlComplete + urlStr;
        if (!urlStr.endsWith(slashStr)) {
            urlComplete = urlComplete + slashStr;
        }
        return urlComplete;
    }

    @Override
    public void onFragmentResult(String deviceName) {
        setNewDevice(deviceName);
    }

    @Override
    public void onDeleteItem(String deviceName) {
        deleteDevice(deviceName);
    }

    private void deleteDevice(String deviceName) {
        cancelTasks();
        SharedPreferences preferences = getApplicationContext().getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(PREFF_URL + deviceName);
        editor.remove(PREFF_PASS + deviceName);
        Set<String> set = preferences.getStringSet(PREFF_DEV_NAME, null);
        editor.remove(PREFF_DEV_NAME).apply();
        if (set != null && set.size() > 0) {
            set.remove(deviceName);

            if (set.size() > 0) {
                editor.putStringSet(PREFF_DEV_NAME, set);
                _currName = (String) Objects.requireNonNull(set.toArray())[0];
                _urlAddress = preferences.getString(PREFF_URL + _currName, "");
                _urlPassword = preferences.getString(PREFF_PASS + _currName, "");
                editor.putString(PREFF_CURR_DEVICE, _currName);

            } else {
                editor.putString(PREFF_CURR_DEVICE, "");
                _currName = "";
                _urlAddress = "";
                _urlPassword = "";

            }
        } else {
            editor.putString(PREFF_CURR_DEVICE, "");
            _currName = "";
            _urlAddress = "";
            _urlPassword = "";

        }
        editor.apply();
        generateConnStrings();
        cancelTasks();
        refreshData();
    }

    private void setNewDevice(String deviceName) {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE);
        _currName = deviceName;
        _urlAddress = preferences.getString(PREFF_URL + _currName, "");
        _urlPassword = preferences.getString(PREFF_PASS + _currName, "");
        generateConnStrings();
        cancelTasks();
        refreshData();
    }

    private void cancelTasks() {
        IOFragment currentFragment = (IOFragment) getSupportFragmentManager().findFragmentById(R.id.ioFragment);
        if (currentFragment != null && currentFragment.isVisible()) {
            currentFragment.clearData();
            currentFragment.cancelTasks();
        }
    }

    private void refreshData() {
        //remember last selected element
        SharedPreferences preferences = this.getApplicationContext().getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREFF_CURR_DEVICE, _currName);
        editor.apply();


        IOFragment currentFragment = (IOFragment) getSupportFragmentManager().findFragmentById(R.id.ioFragment);
        if (currentFragment != null && currentFragment.isVisible()) {
            currentFragment.setConnData(_urlJson, _urlMain, _urlBase);
            currentFragment.refreshData();
        }
    }
}
