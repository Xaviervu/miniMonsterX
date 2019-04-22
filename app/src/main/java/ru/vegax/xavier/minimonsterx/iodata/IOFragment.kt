package ru.vegax.xavier.minimonsterx.iodata

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.io_data_fragment.*

import java.util.ArrayList

import ru.vegax.xavier.minimonsterx.activities.MainActivity
import ru.vegax.xavier.minimonsterx.R
import ru.vegax.xavier.minimonsterx.activities.IODataViewModel
import ru.vegax.xavier.minimonsterx.repository.LoadingStatus


class IOFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener  {


    val TAG = "XavvIOFragment"

    private var mUrlBase: String = ""

    private lateinit var mIoData: ArrayList<IOItem>
    private lateinit var mAdapter: IOAdapter

    private lateinit var mRecyclerView: RecyclerView
    private val viewModel by lazy {
        ViewModelProviders.of(this).get(IODataViewModel::class.java)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            mUrlBase = arguments?.getString(ARG_URL_BASE) ?: ""

          //  viewModel.getDataCyclically(mUrlBase)
    }



    private lateinit var swipeContainer: SwipeRefreshLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.io_data_fragment, container, false)

        swipeContainer = v.findViewById(R.id.swipe_container)
        swipeContainer.setOnRefreshListener (this)

        mRecyclerView = v.findViewById(R.id.recyclerView)

        mRecyclerView.layoutManager = LinearLayoutManager(v.context)

        mIoData = ArrayList()


        mAdapter = object : IOAdapter(v.context, mIoData) {
            override fun onLongClick(v: View): Boolean {
                impulseOutput(v)
                return true
            }

            override fun onClick(v: View) {
                setOutput(v)
            }

        }

        mRecyclerView.adapter = mAdapter
//        val sharedPreferences = v.context.getSharedPreferences(MainActivity.MY_PREFS, Context.MODE_PRIVATE)
        observeViewModel()
        return v
    }
    private fun observeViewModel() {
        viewModel.controlData.observe(this, Observer {
            mIoData.clear()
            mIoData.addAll(it)
            mAdapter.notifyDataSetChanged()
        })
        viewModel.loadingStatus.observe(this,Observer{
            when(it!!){
                LoadingStatus.LOADING -> swipeContainer.isRefreshing = true

                LoadingStatus.NOT_LOADING  -> swipeContainer.isRefreshing = false
                LoadingStatus.ERROR -> {
                    swipeContainer.isRefreshing = false
                    Toast.makeText(activity,getString(R.string.cant_update),Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
    override fun onRefresh() {
        viewModel.getDataCyclically(mUrlBase)
    }
    override fun onDestroy() {
        viewModel.dispose()
        super.onDestroy()
    }
    

    fun setConnData( urlBase: String) {
        mUrlBase = urlBase
        viewModel.getDataCyclically(mUrlBase)
    }

    //handle click from item

    fun setOutput(view: View) {
        val curPos = view.tag as Int
        val currentItem = mIoData[curPos]
//        val suffix: String
        if (currentItem.isImpulse) {
            currentItem.isChanging = false
            viewModel.setImpulse(mUrlBase,curPos+1)
            (view as Switch).isChecked = currentItem.isOn
        } else {
            (view as Switch).isChecked = currentItem.isOn
            currentItem.isChanging = false
            viewModel.setOutput(mUrlBase,curPos+1,!currentItem.isOn)
        }
    }
    //handle long click from item

    fun impulseOutput(view: View) {
        val curPos = view.tag as Int
        val currentItem = mIoData[curPos]

        currentItem.isImpulse = !currentItem.isImpulse
        val preferences = context?.getSharedPreferences(MainActivity.MY_PREFS, Context.MODE_PRIVATE)
        val editor = preferences?.edit()
        editor?.putBoolean(PREFF_IMPULSE + mUrlBase + curPos, currentItem.isImpulse)
        editor?.apply()
        mAdapter.notifyItemChanged(curPos)
        currentItem.isChanging = false
    }

    fun stopUpdating() {
        viewModel.dispose()
    }

    fun refreshData() {
        if (mUrlBase != "") {
            viewModel.getDataCyclically(mUrlBase)
        } else {
            mIoData.clear()
            mRecyclerView.adapter?.notifyDataSetChanged()
        }
    }

    fun clearData() {
        mIoData.clear()
        synchronized(mRecyclerView) {
            mRecyclerView.adapter?.notifyDataSetChanged()
        }
    }

    fun cancelTasks() {
        viewModel.dispose()
    }

    companion object {
        private const val ARG_URL_BASE = "URL_EXTRA_BASE"
        const val PREFF_IMPULSE = "PREFF_IMPULSE"

        fun newInstance(urlBase: String): IOFragment {
            val fragment = IOFragment()
            val args = Bundle()
            args.putString(ARG_URL_BASE, urlBase)
            fragment.arguments = args
            return fragment
        }
    }
}
