package ru.vegax.xavier.miniMonsterX.select_device

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.vegax.xavier.miniMonsterX.R
import ru.vegax.xavier.miniMonsterX.activities.IODataViewModel


class DeviceSelectFragment : AppCompatDialogFragment() {
    private lateinit var mListener: OnFragmentInteractionListener
    private lateinit var viewModel: IODataViewModel


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
        val builder = AlertDialog.Builder(activity)
        val inflater = activity?.layoutInflater
        val parent: ViewGroup? = null
        val view = inflater?.inflate(R.layout.f_select_device, parent, false)
        builder.setView(view)
        val recVListOfDevices: RecyclerView? = view?.findViewById(R.id.recVListOfDevices)

        recVListOfDevices?.layoutManager = LinearLayoutManager(view?.context)

        val currDevice = viewModel.curDevice
        val deviceList = viewModel.allDevices.value
        if (deviceList != null && view != null) {
            val adapter = object : DeviceSelectAdapter(view.context, deviceList, currDevice) {
                override fun onClick(v: View) {
                    if (v is Button) {
                        onDeleteItem(v.tag as Long)
                    } else {
                        onButtonPressed(v.tag as Long)
                    }
                    dialog?.dismiss()
                }
            }


            recVListOfDevices?.adapter = adapter
        }
        return builder.create()
    }

    fun onButtonPressed(deviceId: Long) {
        mListener.onFragmentResult(deviceId)
    }

    fun onDeleteItem(deviceId: Long) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(getString(R.string.delete_device))
        builder.setMessage(getString(R.string.sure_delete_device))
        builder.setPositiveButton(getString(R.string.yes)) { _, _ ->
            mListener.onDeleteItem(deviceId)

        }
        builder.setNegativeButton(getString(R.string.no)) { _, _ ->
            onStop()
        }
        val dialog: AlertDialog = builder.create()

        dialog.show()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }


    interface OnFragmentInteractionListener {
        fun onFragmentResult(deviceId: Long)

        fun onDeleteItem(deviceId: Long)
    }

}
