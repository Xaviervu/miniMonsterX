package ru.vegax.xavier.miniMonsterX.iodata

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ru.vegax.xavier.miniMonsterX.R
import ru.vegax.xavier.miniMonsterX.activities.IODataViewModel
import ru.vegax.xavier.miniMonsterX.activities.MainActivity.Companion.PREFF_DEV_ID
import ru.vegax.xavier.miniMonsterX.repository.LoadingStatus
import java.util.*


class IOFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {


    private lateinit var mIoData: ArrayList<IOItem>
    private lateinit var mAdapter: IOAdapter

    private lateinit var mRecyclerView: RecyclerView
    private val viewModel by lazy {
        ViewModelProviders.of(activity!!).get(IODataViewModel::class.java)
    }
    private lateinit var swipeContainer: SwipeRefreshLayout

    private var resetDataSet: Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.io_data_fragment, container, false)

        swipeContainer = v.findViewById(R.id.swipe_container)
        swipeContainer.setOnRefreshListener(this)

        mRecyclerView = v.findViewById(R.id.recyclerView)

        mRecyclerView.layoutManager = LinearLayoutManager(v.context)

        mIoData = ArrayList()


        mAdapter = object : IOAdapter(v.context, mIoData) {
            override fun onLongClick(v: View?): Boolean {

                if (v != null) {
                    showNameDialog(v.tag as Int)
                }
                return true
            }

            override fun onClick(v: View) {
                if (v is Switch) {
                    setOutput(v)

                } else if (v is ImageView) {
                    impulseOutput(v)
                }
            }

        }

        mRecyclerView.adapter = mAdapter


        return v
    }

    private fun showNameDialog(curPos: Int) {
        val curContext = context
        if (curContext != null) {
            val d = Dialog(curContext)
            d.setContentView(R.layout.dialog_set_name)
            val btnOk = d.findViewById(R.id.btnSet) as TextView
            val btnCancel = d.findViewById(R.id.btnCancel) as TextView
            val portName = d.findViewById(R.id.edTextPortName) as EditText
            portName.setText(mIoData[curPos].itemName)
            btnOk.setOnClickListener {
                val name = portName.text.toString()
                if (name != "") {
                    val curDevice = viewModel.curDevice
                    if (curDevice != null) {
                        val currentItem = mIoData[curPos]
                        currentItem.itemName = name
                        curDevice.portNames[curPos] = name
                        viewModel.update(curDevice)
                        mAdapter.notifyItemChanged(curPos)
                    }
                    d.dismiss()
                } else {
                    Toast.makeText(curContext, getString(R.string.empty_name), Toast.LENGTH_SHORT).show()
                }
            }
            btnCancel.setOnClickListener {
                d.dismiss()
            }
            d.show()
        }
    }

    override fun onResume() {
        super.onResume()
        observeViewModel()
        getPrefs()

    }

    private fun getPrefs() {
        resetDataSet = true
        val preferences = activity?.getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE)
        val currDeviceID = preferences?.getLong(PREFF_DEV_ID, 0L)
        currDeviceID?.let { viewModel.getDevice(it) }
    }

    private fun rememberPrefs(homeId: Long) {
        val preferences = activity?.getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE)
        val editor = preferences?.edit()
        editor?.putLong(PREFF_DEV_ID, homeId)
        editor?.apply()
    }

    private fun observeViewModel() {
        viewModel.controlData.observe(this, Observer { item ->


            val difference = mIoData.mapIndexed { i, ioItem ->
                ioItem.isOn.xor(item[i].isOn) || ioItem.isOutput.xor(item[i].isOutput)
            }
            mIoData.clear()
            mIoData.addAll(item)
            difference.forEachIndexed { i, different ->
                if (different) mAdapter.notifyItemChanged(i)
            }
            if (resetDataSet) {
                resetDataSet = false
                mAdapter.notifyDataSetChanged()
            }
        })
        viewModel.currentDeviceLiveData.observe(this, Observer { curDevice ->
            if (curDevice != null) {
                resetDataSet = true
                rememberPrefs(curDevice.deviceId)
            }
        })
        viewModel.loadingStatus.observe(this, Observer { loadingStatus ->
            if (loadingStatus != null) {
                when (loadingStatus) {
                    LoadingStatus.LOADING -> swipeContainer.isRefreshing = true
                    LoadingStatus.NOT_LOADING -> swipeContainer.isRefreshing = false
                    LoadingStatus.ERROR -> {
                        updateAdapter()
                    }
                }
            }
        })
    }

    private fun updateAdapter() {
        swipeContainer.isRefreshing = false
        mIoData.clear()
        mAdapter.notifyDataSetChanged()
        Toast.makeText(activity, getString(R.string.cant_update), Toast.LENGTH_SHORT).show()
    }

    override fun onRefresh() {
        refreshData()
    }

    override fun onDestroy() {
        viewModel.stopLoading()
        super.onDestroy()
    }

    fun refreshData() {
        resetDataSet = true
        viewModel.curDevice?.let {
            viewModel.getDataCyclically()
        }
        if (viewModel.curDevice == null) {
            updateAdapter()
        }
    }
    //handle click from item

    fun setOutput(view: View) {
        val url = viewModel.curDevice?.url
        if (url != null) {
            val curPos = view.tag as Int
            val currentItem = mIoData[curPos]
            if (currentItem.isImpulse) {
                currentItem.isChanging = false
                viewModel.setImpulse(curPos + 1)
                (view as Switch).isChecked = currentItem.isOn
            } else {
                (view as Switch).isChecked = currentItem.isOn
                currentItem.isChanging = false
                viewModel.setOutput(curPos + 1, !currentItem.isOn)
            }
        }
    }
    //handle long click from item

    fun impulseOutput(view: View) {
        val curDevice = viewModel.curDevice
        if (curDevice != null) {
            val curPos = view.tag as Int
            val currentItem = mIoData[curPos]
            curDevice.impulseTypes[curPos] = !curDevice.impulseTypes[curPos]
            currentItem.isImpulse = curDevice.impulseTypes[curPos]
            viewModel.update(curDevice)
            mAdapter.notifyItemChanged(curPos)
            currentItem.isChanging = false
        }
    }

    fun stopUpdating() {
        viewModel.stopLoading()
    }


    fun clearData() {
        mIoData.clear()
        synchronized(mRecyclerView) {
            mRecyclerView.adapter?.notifyDataSetChanged()
        }
    }

    fun cancelTasks() {
        viewModel.stopLoading()
    }

    companion object {
        const val TAG = "XavvIOFragment"
        const val MY_PREFS = "my_prefs"

    }
}
