package ru.vegax.xavier.minimonsterx.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import ru.vegax.xavier.minimonsterx.R
import ru.vegax.xavier.minimonsterx.R.*


class SettingsActivity : AppCompatActivity() {

    // UI references.
    private lateinit var mTxtVurl: AutoCompleteTextView
    private lateinit var mTxtVpassword: EditText
    private lateinit var mTxtVdeviceName: TextView
    private var mForCreation: Boolean = false
    private val viewModel by lazy {
        ViewModelProviders.of(this).get(IODataViewModel::class.java)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_settings)
        // Set up the login form.
        mTxtVurl = findViewById(id.url)
        mTxtVdeviceName = findViewById(id.deviceName)
        mTxtVpassword = findViewById(id.password)
        val extras = intent.extras

        mTxtVdeviceName.text = extras?.getString(EXTRA_NAME)
        mTxtVurl.setText(extras?.getString(EXTRA_URL))
        mTxtVpassword.setText(extras?.getString(EXTRA_PASS))
        mForCreation = extras?.getBoolean(EXTRA_FOR_CREATION) ?: false

        val txtVDialogTitle = findViewById<TextView>(id.dialogTitle)
        if (mForCreation) {
            txtVDialogTitle.text = getString(string.add_new_device)
            mTxtVdeviceName.isEnabled = !mForCreation
        }
        mTxtVdeviceName.isEnabled = mForCreation
        mTxtVpassword.setOnEditorActionListener { _, id, _ ->

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



    private fun setLoginData() {
        // Reset errors.
        mTxtVurl.error = null
        mTxtVpassword.error = null

        // Store values at the time of the login attempt.
        val devName = mTxtVdeviceName.text.toString()
        val address = mTxtVurl.text.toString()
        val password = mTxtVpassword.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a device deviceName, valid password, if the user entered one.
        if (TextUtils.isEmpty(devName)) {
            mTxtVdeviceName.error = getString(string.dev_name_required)
            cancel = true
        } else {
            if (mForCreation) {

                //Retrieve the values
                val set = viewModel.allDevices.value?.map { it.deviceName }?.toHashSet()
                if (set != null && set.contains(devName)) {
                    focusView = mTxtVdeviceName
                    mTxtVdeviceName.error = getString(string.dev_name_already_exists)
                    cancel = true
                }

            }
        }

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mTxtVpassword.error = getString(string.error_invalid_password)
            focusView = mTxtVpassword
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(address)) {
            mTxtVurl.error = getString(string.error_field_required)
            focusView = mTxtVurl
            cancel = true
        } else if (!isURLValid(address)) {
            mTxtVurl.error = getString(string.error_invalid_address)
            focusView = mTxtVurl
            cancel = true
        }

        if (cancel) {
            focusView?.requestFocus()
        } else {

            val resultData = Intent()
            resultData.putExtra(EXTRA_NAME, mTxtVdeviceName.text.toString())
            resultData.putExtra(EXTRA_URL, fixUrl(mTxtVurl.text.toString()))
            resultData.putExtra(EXTRA_PASS, mTxtVpassword.text.toString())
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
}
