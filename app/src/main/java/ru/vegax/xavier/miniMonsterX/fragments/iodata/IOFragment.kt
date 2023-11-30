package ru.vegax.xavier.miniMonsterX.fragments.iodata

import android.app.Dialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ru.vegax.xavier.miniMonsterX.R
import ru.vegax.xavier.miniMonsterX.activities.BaseActivity
import ru.vegax.xavier.miniMonsterX.activities.IODataViewModel
import ru.vegax.xavier.miniMonsterX.activities.MainActivity
import ru.vegax.xavier.miniMonsterX.activities.MainActivity.Companion.PREFF_DEV_ID
import ru.vegax.xavier.miniMonsterX.databinding.IoDataFragmentBinding
import ru.vegax.xavier.miniMonsterX.fragments.BaseFragment
import ru.vegax.xavier.miniMonsterX.fragments.iodata.port_select.PortSelectFragment
import ru.vegax.xavier.miniMonsterX.repository.LoadingStatus


class IOFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener {


    private lateinit var ioDataFull: ArrayList<IOItem>
    private lateinit var ioData: ArrayList<IOItem>
    private lateinit var adapter: IOAdapter
    private lateinit var swipeController: SwipeController

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: IODataViewModel
    private lateinit var swipeContainer: SwipeRefreshLayout

    private var baseActivity: BaseActivity? = null
    private var resetDataSet: Boolean = true
    private lateinit var viewBinding: IoDataFragmentBinding
    override val fragmentTag: String
        get() = TAG

    override fun onAttach(context: Context) {
        baseActivity = (activity as? BaseActivity)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(context as androidx.fragment.app.FragmentActivity).get(IODataViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        Log.d(TAG, "onCreateView")
        viewBinding = IoDataFragmentBinding.inflate(inflater, container, false)

        swipeContainer = viewBinding.swipeContainer
        swipeContainer.setOnRefreshListener(this)

        recyclerView = viewBinding.recyclerView

        recyclerView.layoutManager = LinearLayoutManager(context)

        ioDataFull = ArrayList()
        ioData = ArrayList()
        context?.let {
            adapter = IOAdapter(it, ioData,clickListener = {position ->
                setOutput(position)
            },editNameListener = {position ->
                showNameDialog(position)
            },editTypeListener = {position ->
                impulseOutput(position)
            },tempClickListener = {position ->
                setThermostat(position)
            })

            recyclerView.adapter = adapter
            swipeController = SwipeController(object : SwipeControllerActions() {
                override fun onRightClicked(position: Int) {
                    adapter.mItemsData[position].isHidden = true
                    viewModel.curDevice?.let { deviceData ->
                        deviceData.hiddenInputs[adapter.mItemsData[position].portId] = true
                        viewModel.update(deviceData)
                    }
                }

                override fun onLeftClicked(position: Int) {
                   setThermostat(adapter.mItemsData[position].portId)
                }
            })
            val itemTouchHelper = ItemTouchHelper(swipeController)
            itemTouchHelper.attachToRecyclerView(recyclerView)
            recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                    swipeController.onDraw(c)
                }
            })
        }
        try {
            context?.let {
                viewBinding.txtVVersion.text = getString(R.string.version, it.packageManager
                        .getPackageInfo(it.packageName, 0).versionName)
            }
        } catch (e: Throwable) {
            Log.d(TAG, "onCreateView: no context")
        }
        viewBinding.fabAddPort.setOnClickListener {
            val portSelect = PortSelectFragment()
            activity?.supportFragmentManager?.let { it1 -> portSelect.show(it1, PortSelectFragment.TAG) }
        }
        viewBinding.fabAddPort.setColorFilter(Color.WHITE)
        return viewBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "onActivityCreated")
        observeViewModel()
    }

    private fun showNameDialog(curPos: Int) {
        context?.let { curContext ->
            val d = Dialog(curContext)
            d.setContentView(R.layout.d_set_name)
            val btnOk = d.findViewById(R.id.btnSet) as TextView
            val btnCancel = d.findViewById(R.id.btnCancel) as TextView
            val portName = d.findViewById(R.id.txtVMessage) as EditText
            portName.setText(ioDataFull[curPos].itemName)
            btnOk.setOnClickListener {
                val name = portName.text.toString()
                if (name != "") {
                    val curDevice = viewModel.curDevice
                    if (curDevice != null) {
                        val currentItem = ioDataFull[curPos]
                        currentItem.itemName = name
                        curDevice.portNames[curPos] = name
                        viewModel.update(curDevice)
                        adapter.notifyItemChanged(curPos)
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
        Log.d(TAG, "observeViewModel")
        viewModel.controlData.observe(viewLifecycleOwner) { itemsList ->
            if (ioDataFull.size != itemsList.size) {
                ioDataFull.clear()
                ioDataFull.addAll(itemsList)
            }
            ioData.clear()
            ioData.addAll(itemsList.filter { !it.isHidden })
            var position = -1
            var addPosition = -1
            for (index in itemsList.indices) {
                if (!ioDataFull[index].isHidden) position++
                if (!itemsList[index].isHidden) addPosition++
                if (!ioDataFull[index].isHidden && itemsList[index].isHidden) {
                    adapter.notifyItemRemoved(position)
                } else if (ioDataFull[index].isHidden && !itemsList[index].isHidden) {
                    adapter.notifyItemInserted(addPosition)
                }
            }

            position = -1
            ioDataFull.forEachIndexed { index, ioItem ->
                if (!itemsList[index].isHidden) position++
                if ((ioItem.isOn.xor(itemsList[index].isOn) || ioItem.isOutput.xor(itemsList[index].isOutput) ||
                                ioItem.temperature != itemsList[index].temperature) && !itemsList[index].isHidden) {
                    adapter.notifyItemChanged(position)
                }
            }

            ioDataFull.clear()
            itemsList.forEach { ioItem ->
                with(ioItem) {
                    ioDataFull.add(IOItem(portId,
                            itemName,
                            isOutput,
                            isOn,
                            temperature,
                            isImpulse,
                            isChanging,
                            isHidden))
                }
            }
            if (resetDataSet) {
                resetDataSet = false
                adapter.notifyDataSetChanged()
            }
        }
        viewModel.currentDeviceLiveData.observe(viewLifecycleOwner) { curDevice ->
            if (curDevice != null) {
                resetDataSet = true
                rememberPrefs(curDevice.deviceId)
            }
        }
        viewModel.loadingStatus.observe(viewLifecycleOwner) { loadingStatus ->
            when (loadingStatus) {
                LoadingStatus.LOADING -> swipeContainer.isRefreshing = true
                LoadingStatus.NOT_LOADING -> swipeContainer.isRefreshing = false
                LoadingStatus.ERROR -> {
                    errorLoading()
                }
                else ->{}
            }
        }
    }

    private fun errorLoading() {
        swipeContainer.isRefreshing = false
        ioDataFull.clear()
        adapter.notifyDataSetChanged()
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
            errorLoading()
        }
    }
    //handle click from item

    fun setOutput(curPos:Int) {
        val url = viewModel.curDevice?.url
        if (url != null && curPos < ioDataFull.size) {
            val currentItem = ioDataFull[curPos]
            if (currentItem.isImpulse) {
                currentItem.isChanging = false
                viewModel.setImpulse(curPos + 1)
            } else {
                currentItem.isChanging = false
                viewModel.setOutput(curPos + 1, !currentItem.isOn)
            }
        }
    }

    private fun impulseOutput(curPos:Int) {
        onChangeOutputMode(curPos)
    }

    private fun setThermostat(curPos: Int) {
        val url = "${viewModel.curDevice?.url}${viewModel.curDevice?.password}/"
        if (viewModel.curDevice != null) {
            stopUpdating()
            (baseActivity as? MainActivity)?.startThermalActivity(url, curPos)
        }
    }

    private fun onChangeOutputMode(curPos: Int) {
        (activity as MainActivity).showAlertDialog(getString(R.string.change_output), getString(R.string.sure_change_output_type)) {
            toggleOutputType(curPos)
        }
    }

    private fun toggleOutputType(curPos: Int) {
        val curDevice = viewModel.curDevice
        if (curDevice != null) {
            val currentItem = ioDataFull[curPos]
            curDevice.impulseTypes[curPos] = !curDevice.impulseTypes[curPos]
            currentItem.isImpulse = curDevice.impulseTypes[curPos]
            viewModel.update(curDevice)
            adapter.notifyItemChanged(curPos)
            currentItem.isChanging = false
        }
    }

    fun stopUpdating() {
        viewModel.stopLoading()
    }


    fun clearData() {
        ioDataFull.clear()
        synchronized(recyclerView) {
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }

    fun cancelTasks() {
        viewModel.stopLoading()
    }

    companion object {
        const val TAG = "IOFragment"
        const val MY_PREFS = "my_prefs"
        fun newInstance() = IOFragment()

    }
}
