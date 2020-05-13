package ru.vegax.xavier.miniMonsterX.fragments.thermo

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ru.vegax.xavier.miniMonsterX.R
import ru.vegax.xavier.miniMonsterX.activities.BaseActivity
import ru.vegax.xavier.miniMonsterX.auxiliar.setVisible
import ru.vegax.xavier.miniMonsterX.databinding.FThermostatPropertiesBinding
import ru.vegax.xavier.miniMonsterX.fragments.BaseFragment
import ru.vegax.xavier.miniMonsterX.models.Status
import kotlin.math.round


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
            txtVTargetTemperature.setOnClickListener {
                showNumberPickerDialog(
                        value = viewModel.event.value?.data?.targetT ?: 0.0, // in kilograms
                        stepSize = 0.1,
                        range = -55.0..125.0,
                        formatToString = { getString(R.string.temperature, it.toString()) },
                        valueChooseAction = {
                            viewModel.setTargetTemperature(it)
                        }
                )

            }
            txtVTargetHysteresis.setOnClickListener {
                showNumberPickerDialog(
                        value = viewModel.event.value?.data?.hysteresis ?: 0.0, // in kilograms
                        stepSize = 0.1,
                        range = -10.0..10.0,
                        formatToString = { getString(R.string.temperature, it.toString()) },
                        valueChooseAction = {
                            viewModel.setHysteresis(it)
                        }
                )

            }
            txtVCalibrationTemperature.setOnClickListener {
                showNumberPickerDialog(
                        value = viewModel.event.value?.data?.calT ?: 0.0, // in kilograms
                        stepSize = 0.1,
                        range = -10.0..10.0,
                        formatToString = { getString(R.string.temperature, it.toString()) },
                        valueChooseAction = {
                            viewModel.setCalTemperature(it)
                        }
                )

            }
        }
    }


    private fun Fragment.showNumberPickerDialog(
            value: Double,
            range: ClosedRange<Double>,
            stepSize: Double,
            formatToString: (Double) -> String,
            valueChooseAction: (Double) -> Unit
    ) {
        context?.let {
            val d = Dialog(it)
            d.setContentView(R.layout.d_picker_select)
            val btnOk = d.findViewById(R.id.btnSet) as TextView
            val btnCancel = d.findViewById(R.id.btnMenuCancel) as TextView
            val numberPicker = d.findViewById(R.id.numberPicker) as NumberPicker
            val actualMinValue = (range.start / stepSize).toInt()
            numberPicker.apply {
                setFormatter { tempString -> formatToString(((tempString.toDouble() + actualMinValue) * stepSize).round(1)) }
                wrapSelectorWheel = false
                minValue = 0
                maxValue = (range.endInclusive / stepSize).toInt() - actualMinValue
                this.value = (value / stepSize).toInt() - actualMinValue

                // NOTE: workaround for a bug that rendered the selected value wrong until user scrolled, see also: https://stackoverflow.com/q/27343772/3451975
                (NumberPicker::class.java.getDeclaredField("mInputText").apply { isAccessible = true }.get(this) as EditText).filters = emptyArray()
            }
            btnOk.setOnClickListener {
                valueChooseAction(((numberPicker.value.toDouble() + actualMinValue) * stepSize).round(1))
                d.dismiss()
            }
            btnCancel.setOnClickListener {
                d.dismiss()
            }
            d.show()
        }
    }

    private fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return round(this * multiplier) / multiplier
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