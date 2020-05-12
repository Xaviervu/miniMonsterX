package ru.vegax.xavier.miniMonsterX.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.databinding.DataBindingUtil
import ru.vegax.xavier.miniMonsterX.R.*
import ru.vegax.xavier.miniMonsterX.databinding.AFragmentContainerWithToolbarBinding
import ru.vegax.xavier.miniMonsterX.fragments.thermo.FragmentThermo


class ThermostatActivity : BaseActivity(), OnFocusChangeListener {

    private lateinit var viewBinding: AFragmentContainerWithToolbarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent.extras
        val url = extras?.getString(EXTRA_URL) ?: throw IllegalArgumentException()
        val thermostatN = extras.getInt(EXTRA_THERMOSTAT_N)

        if (isAndroidTV()) {
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        }

        viewBinding = DataBindingUtil.setContentView(
                this,
                layout.a_fragment_container_with_toolbar)

        setContentView(viewBinding.root)

        setToolbar(viewBinding.vToolbar.toolbar)
        title = getString(string.thermostat_properties, (thermostatN + 1).toString())
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (supportFragmentManager.findFragmentById(id.container) == null) {
            val thermostatFragment = FragmentThermo.newInstance(url, thermostatN)
            replaceFragment(thermostatFragment, id.container, thermostatFragment.fragmentTag)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish() // close this activity and return to preview activity (if there is any)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val TAG = "ThermostatActivity"
        const val EXTRA_URL = "URL_EXTRA"
        const val EXTRA_THERMOSTAT_N = "EXTRA_THERMOSTAT_N"
        fun newInstance(context: Context, url: String, thermostatN: Int): Intent {
            val intent = Intent(context, ThermostatActivity::class.java)
            intent.putExtra(EXTRA_URL, url)
            intent.putExtra(EXTRA_THERMOSTAT_N, thermostatN)
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
