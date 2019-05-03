package ru.vegax.xavier.minimonsterx.select_device

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.vegax.xavier.minimonsterx.R
import ru.vegax.xavier.minimonsterx.activities.IODataViewModel


class DeviceSelectFragment : AppCompatDialogFragment() {
    private lateinit var mListener: OnFragmentInteractionListener
    private val viewModel by lazy {
        ViewModelProviders.of(activity!!).get(IODataViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()
        val window = dialog.window ?: return
        val params = window.attributes
        params.width = resources.getDimensionPixelSize(R.dimen.popup_width)
        window.attributes = params
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity?.layoutInflater
        val parent: ViewGroup? = null
        val view = inflater?.inflate(R.layout.fragment_select_device, parent, false)
        builder.setView(view)

        //Initialize the RecyclerView
        val recVListOfDevices: RecyclerView? = view?.findViewById(R.id.recVListOfDevices)

        //Set the Layout Manager
        recVListOfDevices?.layoutManager = LinearLayoutManager(view?.context)

        //Initialize the ArrayLIst that will contain the data

        val currDevice = viewModel.curDevice
        val deviceList = viewModel.allDevices.value
        if (deviceList != null && view != null) {
            val adapter = object : DeviceSelectAdapter(view.context, deviceList, currDevice) {
                override fun onClick(v: View) {
                    if (v is Button) {
                        // delete item
                        onDeleteItem(v.tag as Long)
                    } else {
                        onButtonPressed(v.tag as Long)
                    }
                    dialog.dismiss()
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
        // create a confirmation dialog
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

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }


    interface OnFragmentInteractionListener {
        fun onFragmentResult(deviceId: Long)

        fun onDeleteItem(deviceId: Long)
    }

    companion object
}// Required empty public constructor
