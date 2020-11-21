package ru.vegax.xavier.miniMonsterX.fragments.iodata.port_select

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import ru.vegax.xavier.miniMonsterX.R
import ru.vegax.xavier.miniMonsterX.activities.IODataViewModel
import ru.vegax.xavier.miniMonsterX.databinding.FSelectDeviceOrPortBinding


class PortSelectFragment : AppCompatDialogFragment() {
    private lateinit var viewModel: IODataViewModel
    private lateinit var binding: FSelectDeviceOrPortBinding

    override fun onResume() {
        super.onResume()
        val window = dialog?.window ?: return
        val params = window.attributes
        params.width = resources.getDimensionPixelSize(R.dimen.popup_width)
        window.attributes = params
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity?.let {
            viewModel = ViewModelProvider(it).get(IODataViewModel::class.java)
        }
        val activity = activity


        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.f_select_device_or_port, null, false)
        val builder = AlertDialog.Builder(activity)
        builder.setView(binding.root)

        with(binding.recVListOfDevices) {
            layoutManager = LinearLayoutManager(context)

            val currDevice = viewModel.curDevice
            if (currDevice != null) {
                val adapter = PortSelectAdapter(
                        currDevice,
                        viewModel.controlData.value?.map { it.isOutput } ?: mutableListOf()
                ) { deviceData ->

                    viewModel.update(deviceData)
                    this.adapter?.notifyDataSetChanged()

                }
                this.adapter = adapter
            }
        }
        return builder.create()
    }

    companion object {
        const val TAG = "PortSelectFragment"
    }
}

