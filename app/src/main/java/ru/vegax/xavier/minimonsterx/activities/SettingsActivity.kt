package ru.vegax.xavier.minimonsterx.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ru.vegax.xavier.minimonsterx.R
import ru.vegax.xavier.minimonsterx.R.*

class SettingsActivity : AppCompatActivity() {

    // UI references.
    private var mTxtVurl: AutoCompleteTextView? = null
    private var mTxtVpassword: EditText? = null
    private var mTxtVdeviceName: TextView? = null
    private var mForCreation: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_settings)
        // Set up the login form.
        mTxtVurl = findViewById(id.url)
        mTxtVdeviceName = findViewById(id.deviceName)
        mTxtVpassword = findViewById(id.password)
        val extras = intent.extras

        mTxtVdeviceName?.text = extras?.getString(MainActivity.EXTRA_NAME)
        mTxtVurl?.setText(extras?.getString(MainActivity.EXTRA_URL))
        mTxtVpassword?.setText(extras?.getString(MainActivity.EXTRA_PASS))
        mForCreation = extras?.getBoolean(MainActivity.EXTRA_FOR_CREATION) ?: false

        val txtVdialogTitle = findViewById<TextView>(id.dialogTitle)
        if (mForCreation) {
            txtVdialogTitle.text = getString(string.add_new_device)
            mTxtVdeviceName?.isEnabled = !mForCreation
        }
        mTxtVdeviceName?.isEnabled = mForCreation
        mTxtVpassword?.setOnEditorActionListener { _, id, _ ->

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


    private fun setLoginData() {
        // Reset errors.
        mTxtVurl?.error = null
        mTxtVpassword?.error = null

        // Store values at the time of the login attempt.
        val devName = mTxtVdeviceName?.text.toString()
        val address = mTxtVurl?.text.toString()
        val password = mTxtVpassword?.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a device deviceName, valid password, if the user entered one.
        if (TextUtils.isEmpty(devName)) {
            mTxtVdeviceName!!.error = getString(string.dev_name_required)
            cancel = true
        } else {
            if (mForCreation) {
                val preferences = this.applicationContext.getSharedPreferences(MainActivity.MY_PREFS, Context.MODE_PRIVATE)
                //Retrieve the values
                val set = preferences.getStringSet(MainActivity.PREFF_DEV_NAME, null)
                if (set != null && set.contains(devName)) {
                    focusView = mTxtVdeviceName
                    mTxtVdeviceName?.error = getString(string.dev_name_already_exists)
                    cancel = true
                }

            }
        }

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mTxtVpassword?.error = getString(string.error_invalid_password)
            focusView = mTxtVpassword
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(address)) {
            mTxtVurl?.error = getString(string.error_field_required)
            focusView = mTxtVurl
            cancel = true
        } else if (!isURLValid(address)) {
            mTxtVurl?.error = getString(string.error_invalid_address)
            focusView = mTxtVurl
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.

            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            val resultData = Intent()
            resultData.putExtra(MainActivity.EXTRA_NAME, mTxtVdeviceName?.text.toString())
            resultData.putExtra(MainActivity.EXTRA_URL, mTxtVurl?.text.toString())
            resultData.putExtra(MainActivity.EXTRA_PASS, mTxtVpassword?.text.toString())
            resultData.putExtra(MainActivity.EXTRA_FOR_CREATION, mForCreation)
            setResult(Activity.RESULT_OK, resultData)
            finish()
        }
    }

    private fun isURLValid(url: String): Boolean {
        return url.length > 1
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length > 1
    }
}
