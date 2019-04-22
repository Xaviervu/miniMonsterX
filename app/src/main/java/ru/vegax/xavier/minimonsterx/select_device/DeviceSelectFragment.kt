package ru.vegax.xavier.minimonsterx.select_device

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.vegax.xavier.minimonsterx.activities.MainActivity
import ru.vegax.xavier.minimonsterx.R

class DeviceSelectFragment : AppCompatDialogFragment() {
    private lateinit var mListener: OnFragmentInteractionListener

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
        val  parent : ViewGroup? = null
        val view = inflater?.inflate(R.layout.fragment_select_device,parent,false)
        builder.setView(view)

        //Initialize the RecyclerView
        val recVListOfDevices: RecyclerView? = view?.findViewById(R.id.recVListOfDevices)

        //Set the Layout Manager
        recVListOfDevices?.layoutManager = LinearLayoutManager(view?.context)

        //Initialize the ArrayLIst that will contain the data
        val preferences = view?.context?.getSharedPreferences(MainActivity.MY_PREFS, Context.MODE_PRIVATE)
        val set = preferences?.getStringSet(MainActivity.PREFF_DEV_NAME, null)
        val currDevice = arguments?.getString(EXTRA_DEVICE_NAME)
        if (set?.size ?: 0 > 0 && view != null && currDevice != null) {
            val adapter = object : DeviceSelectAdapter(view.context, set as Set<String>, currDevice) {
                override fun onClick(v: View) {
                    if (v is Button) {
                        // delete item
                        onDeleteItem(v.getTag() as String)
                    } else {
                        onButtonPressed((v as TextView).text.toString())
                    }
                    dialog.dismiss()
                }
            }


            //Initialize the adapter and set it ot the RecyclerView
            recVListOfDevices?.adapter = adapter
        }
        return builder.create()
    }

    fun onButtonPressed(deviceName: String) {
        mListener.onFragmentResult(deviceName)
    }

    fun onDeleteItem(deviceName: String) {
        // create a confirmation dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete a device")
        builder.setMessage("Are you sure you want to delete a device? tehe settings and password will be deleted as well")
        builder.setPositiveButton("YES") { _, _ ->
            mListener.onDeleteItem(deviceName)

        }
        builder.setNegativeButton("NO") { _, _ ->
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


    fun newInstance(currentDevice: String): DeviceSelectFragment {
        val fragment = DeviceSelectFragment()

        val bundle = Bundle()
        bundle.putString(EXTRA_DEVICE_NAME, currentDevice)
        fragment.arguments = bundle

        return fragment
    }


    interface OnFragmentInteractionListener {
        fun onFragmentResult(deviceName: String)

        fun onDeleteItem(deviceName: String)
    }

    companion object {

        private const val EXTRA_DEVICE_NAME = "DEVICE_NAME_EXTRA"
    }
}// Required empty public constructor
