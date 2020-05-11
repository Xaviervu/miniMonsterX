package ru.vegax.xavier.miniMonsterX.activities

import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.lifecycle.ViewModelProvider
import ru.vegax.xavier.miniMonsterX.R
import ru.vegax.xavier.miniMonsterX.R.*


class SettingsActivity : BaseActivity(), OnFocusChangeListener {

    // UI references.
    private lateinit var mTxtVUrl: AutoCompleteTextView
    private lateinit var mTxtVPassword: EditText
    private lateinit var mTxtVDeviceName: TextView
    private var mForCreation: Boolean = false
    private val viewModel =
            ViewModelProvider(this).get(IODataViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isAndroidTV()) {
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        }
        setContentView(layout.activity_settings)
        // Set up the login form.
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(id.settingsToolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mTxtVDeviceName = findViewById(id.deviceName)

        mTxtVDeviceName.onFocusChangeListener = this

        mTxtVUrl = findViewById(id.url)
        mTxtVUrl.onFocusChangeListener = this
        mTxtVPassword = findViewById(id.password)
        mTxtVPassword.onFocusChangeListener = this
        val extras = intent.extras

        mTxtVDeviceName.text = extras?.getString(EXTRA_NAME)
        mTxtVUrl.setText(extras?.getString(EXTRA_URL))
        mTxtVPassword.setText(extras?.getString(EXTRA_PASS))
        mForCreation = extras?.getBoolean(EXTRA_FOR_CREATION) ?: false

        val txtVDialogTitle = findViewById<TextView>(id.dialogTitle)
        if (mForCreation) {
            txtVDialogTitle.text = getString(string.add_device)
            mTxtVDeviceName.isEnabled = !mForCreation
        }
        mTxtVDeviceName.isEnabled = mForCreation
        mTxtVPassword.setOnEditorActionListener { _, id, _ ->

            when (id) {
                EditorInfo.IME_NULL, R.id.password -> {
                    setLoginData()
                    true
                }

                else -> false
            }

        }

        val btnOk = findViewById<Button>(id.btn_ok)
        btnOk.setOnClickListener { setLoginData() }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // handle arrow click here
        if (item.itemId == android.R.id.home) {
            setResult(Activity.RESULT_CANCELED, null)
            finish() // close this activity and return to preview activity (if there is any)
        }
        return true
//        return super.onOptionsItemSelected(item)
    }

    private fun isAndroidTV(): Boolean {
        val uiModeManager = getSystemService(UI_MODE_SERVICE) as? UiModeManager
        return (uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION)
    }


    private fun setLoginData() {
        // Reset errors.
        mTxtVUrl.error = null
        mTxtVPassword.error = null

        // Store values at the time of the login attempt.
        val devName = mTxtVDeviceName.text.toString()
        val address = mTxtVUrl.text.toString()
        val password = mTxtVPassword.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a device deviceName, valid password, if the user entered one.
        if (TextUtils.isEmpty(devName)) {
            mTxtVDeviceName.error = getString(string.dev_name_required)
            cancel = true
        } else {
            if (mForCreation) {

                //Retrieve the values
                val set = viewModel.allDevices.value?.map { it.deviceName }?.toHashSet()
                if (set != null && set.contains(devName)) {
                    focusView = mTxtVDeviceName
                    mTxtVDeviceName.error = getString(string.dev_name_already_exists)
                    cancel = true
                }

            }
        }

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mTxtVPassword.error = getString(string.error_invalid_password)
            focusView = mTxtVPassword
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(address)) {
            mTxtVUrl.error = getString(string.error_field_required)
            focusView = mTxtVUrl
            cancel = true
        } else if (!isURLValid(address)) {
            mTxtVUrl.error = getString(string.error_invalid_address)
            focusView = mTxtVUrl
            cancel = true
        }

        if (cancel) {
            focusView?.requestFocus()
        } else {

            val resultData = Intent()
            resultData.putExtra(EXTRA_NAME, mTxtVDeviceName.text.toString())
            resultData.putExtra(EXTRA_URL, fixUrl(mTxtVUrl.text.toString()))
            resultData.putExtra(EXTRA_PASS, mTxtVPassword.text.toString())
            resultData.putExtra(EXTRA_FOR_CREATION, mForCreation)
            setResult(Activity.RESULT_OK, resultData)
            finish()
        }

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

    private fun isURLValid(url: String): Boolean {
        return url.length > 1
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 1
    }

    companion object {
        private const val TAG = "SettingsActivity"
        private const val EXTRA_ID = "ID_EXTRA"
        const val EXTRA_NAME = "NAME_EXTRA"
        const val EXTRA_URL = "URL_EXTRA"
        const val EXTRA_PASS = "PASS_EXTRA"
        const val EXTRA_FOR_CREATION = "FOR_CREATION_EXTRA"
        fun newSettingsIntent(context: Context, id: Long, name: String, url: String, password: String, forCreation: Boolean): Intent {
            val intent = Intent(context, SettingsActivity::class.java)
            intent.putExtra(EXTRA_ID, id)
            intent.putExtra(EXTRA_NAME, name)
            intent.putExtra(EXTRA_URL, url)
            intent.putExtra(EXTRA_PASS, password)
            intent.putExtra(EXTRA_FOR_CREATION, forCreation) // if there is no current device then open for creation
            return intent
        }
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (hasFocus) {
            imm.showSoftInput(v, 0)
        } else {
            imm.hideSoftInputFromWindow(v?.windowToken, 0)
        }
    }
}
