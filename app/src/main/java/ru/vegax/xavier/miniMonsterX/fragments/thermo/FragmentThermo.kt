package ru.vegax.xavier.miniMonsterX.fragments.thermo

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ru.vegax.xavier.miniMonsterX.R
import ru.vegax.xavier.miniMonsterX.activities.BaseActivity
import ru.vegax.xavier.miniMonsterX.auxiliar.setVisible
import ru.vegax.xavier.miniMonsterX.databinding.FThermostatPropertiesBinding
import ru.vegax.xavier.miniMonsterX.fragments.BaseFragment
import ru.vegax.xavier.miniMonsterX.models.Status


class FragmentThermo : BaseFragment(), SwipeRefreshLayout.OnRefreshListener {
    override val fragmentTag: String
        get() = TAG
    private lateinit var viewBinding: FThermostatPropertiesBinding
    private lateinit var viewModel: ThermostatViewModel
    var thermostatN = 0
    private var baseActivity: BaseActivity? = null
    override fun onAttach(context: Context) {
        baseActivity = (activity as? BaseActivity)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val bundle = arguments
        if (bundle != null) {
            activity?.let { fragmentActivity ->
                thermostatN = bundle.getInt(THERMOSTAT_NUMBER_BUNDLE)
                val defUrl = bundle.getString(DEF_URL_BUNDLE) ?: throw  IllegalArgumentException()
                setHasOptionsMenu(true)
                viewModel = ViewModelProvider(this, ThermostatVMFactory(fragmentActivity.application,
                        defUrl, thermostatN)).get(ThermostatViewModel::class.java)

            }
        } else {
            throw IllegalStateException()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = DataBindingUtil.inflate(inflater, R.layout.f_thermostat_properties, container, false)
        viewBinding.swipeThermo.setOnRefreshListener(this)
        return viewBinding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewBinding) {
            switchThermostat.text = getString(R.string.thermostat_n, (thermostatN + 1).toString())
            btnOkTherm.setOnClickListener { onSuccess() }
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeViewModel()
    }

    override fun onRefresh() {
        refreshData()
    }

    private fun refreshData() {
        viewBinding.swipeThermo.isRefreshing = false
        setProgress(true)
        viewModel.getThermostatValues()
    }

    private fun back() {
        baseActivity?.supportFragmentManager?.popBackStack()
    }

    private fun observeViewModel() {
        viewModel.event.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> setProgress(true)

                Status.SUCCESS -> {

                    setProgress(false)
                    with(viewBinding) {
                        val data = resource.data
                        data?.let {
                            switchThermostat.isChecked = it.thermostatIsOn
                            switchThermostat.setOnClickListener {
                                setProgress(true)
                                viewModel.setThermostatActive(switchThermostat.isChecked)
                            }
                            txtVTempPlusCal.text = getString(R.string.temperature, it.tPlusCal.toString())
                            txtVTargetTemperature.text = getString(R.string.temperature, it.targetT.toString())
                            txtVTargetHysteresis.text = getString(R.string.temperature, it.hysteresis.toString())
                            txtVCalibrationTemperature.text = getString(R.string.temperature, it.calT.toString())
                        }
                        layoutThermostat.setVisible(data?.thermostatIsOn == true)
                    }
                }
                Status.ERROR -> resource.error?.let { err ->
                    handleError(err)
                    setProgress(false)
                }
            }
        }
    }

    private fun handleError(it: Throwable) {
        it.message?.let { message ->
            this.context?.let {
                Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onSuccess() {
        baseActivity?.finish()
    }

    private fun setProgress(progress: Boolean) {
        with(viewBinding) {
            progBar.progBarWait.setVisible(progress)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> back()
        }
        return super.onOptionsItemSelected(item)
    }
    private fun String.isValidTemperature(): Boolean = this.isNotEmpty()// todo: XV check its a double within bounds

    companion object {
        private const val TAG = "FragmentThermo"
        private const val THERMOSTAT_NUMBER_BUNDLE = "$TAG.thermostat_number"
        private const val DEF_URL_BUNDLE = "$TAG.def_url"

        fun newInstance(defUrl: String, thermostatN: Int): FragmentThermo {
            return FragmentThermo().apply {
                arguments = Bundle().apply {
                    putInt(THERMOSTAT_NUMBER_BUNDLE, thermostatN)
                    putString(DEF_URL_BUNDLE, defUrl)
                }
            }
        }
    }
}