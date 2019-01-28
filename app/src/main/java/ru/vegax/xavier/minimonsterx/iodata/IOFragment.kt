package ru.vegax.xavier.minimonsterx.iodata

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

import java.util.ArrayList

import ru.vegax.xavier.minimonsterx.iodata.dataLoader.DataLoader
import ru.vegax.xavier.minimonsterx.MainActivity
import ru.vegax.xavier.minimonsterx.R


class IOFragment : Fragment() {


    private var mUrlJSon: String =""
    private var mUrlHtml: String = ""
    private var mUrlBase: String = ""

    private lateinit var mProgBarDownload: ProgressBar 
    private lateinit var mIoData: ArrayList<IOItem> 
    private lateinit var mAdapter: IOAdapter
    private lateinit var mTxtVdeviceName: TextView
    private lateinit var mDataLoader: DataLoader
    private lateinit var mRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

            mUrlJSon = arguments?.getString(ARG_URL_JSON) ?: ""
            mUrlHtml = arguments?.getString(ARG_URL_HTML) ?: ""
            mUrlBase = arguments?.getString(ARG_URL_BASE) ?: ""
      
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.io_data_fragment, container, false)

        mProgBarDownload = v.findViewById(R.id.progBarDownloadJS)

        mProgBarDownload.progress = 100


        mTxtVdeviceName = v.findViewById(R.id.txtVdeviceName)

        //Initialize the RecyclerView
        mRecyclerView = v.findViewById(R.id.recyclerView)

        //Set the Layout Manager
        mRecyclerView.layoutManager = LinearLayoutManager(v.context)

        //Initialize the ArrayLIst that will contain the data
        mIoData = ArrayList()

        //Initialize the adapter and set it ot the RecyclerView

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

        val sharedPreferences = v.context.getSharedPreferences(MainActivity.MY_PREFS, Context.MODE_PRIVATE)

        mDataLoader = object : DataLoader(mIoData, sharedPreferences, mUrlJSon, mUrlHtml, mUrlBase) {
            override fun notifyDataChanged() {
                var isButtonChanging = false
                for (i in 0 until DataLoader.PORT_NUMBER) {

                    isButtonChanging = isButtonChanging or mIoData[i].isChanging // if at least one element is being edited

                }
                if (!isButtonChanging) {
                    mAdapter.notifyDataSetChanged()
                }
                mProgBarDownload.isIndeterminate = false
                mRecyclerView.visibility = View.VISIBLE


            }

            override fun notifyError(e: String) {
                Toast.makeText(v.context, getString(R.string.no_conn), Toast.LENGTH_SHORT).show()
                mProgBarDownload.isIndeterminate = false
                mRecyclerView.visibility = View.INVISIBLE
                mTxtVdeviceName.text = getString(R.string.no_conn)
            }

            override fun setDeviceId(deviceId: String) {
                v.post {
                    mTxtVdeviceName.text = deviceId//send to a UI thread!
                }

            }

        }
        refreshData()

        return v
    }


    override fun onDestroy() {
        mDataLoader.cancelTasks()

        super.onDestroy()
    }
    

    fun setConnData(urlJSon: String, urlHtml: String, urlBase: String) {
        mUrlJSon = urlJSon
        mUrlHtml = urlHtml
        mUrlBase = urlBase
        mDataLoader.setConnData(urlJSon, urlHtml, urlBase)
    }

    //handle click from item

    fun setOutput(view: View) {
        val curPos = view.tag as Int
        val currentItem = mIoData[curPos]
        val suffix: String
        if (currentItem.isImpulse) {
            // set the suffix string for pressing turning the output on or off for a period of time
            suffix = URL_SUFFIX_IMPULSE + (curPos + 1) //
            currentItem.isChanging = false
            mDataLoader.setOutputs(suffix)
            (view as Switch).isChecked = currentItem.isOn
        } else {
            (view as Switch).isChecked = currentItem.isOn
            // set the suffix string for pressing turning the output on or off
            suffix = URL_SUFFIX_SET + (curPos + 1) + "-" + if (currentItem.isOn) "0" else "1" //"/?sw=i-1" for turning on i - port number starting from 1
            currentItem.isChanging = false
            mDataLoader.setOutputs(suffix)

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
        mProgBarDownload.isIndeterminate = false
        mDataLoader.stopUptading()
    }

    fun refreshData() {
        if (mUrlHtml != "") {
            mProgBarDownload.isIndeterminate = true

            mDataLoader.refreshData()
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
        mDataLoader.stopTasks()
        mProgBarDownload.isIndeterminate = false
    }

    companion object {
        private const val ARG_URL_JSON = "URL_EXTRA_JSON"
        private const val ARG_URL_HTML = "URL_EXTRA_HTML"
        private const val ARG_URL_BASE = "URL_EXTRA_BASE"
        private const val URL_SUFFIX_IMPULSE= "/?rst="
        private const val URL_SUFFIX_SET = "/?sw="

        const val PREFF_IMPULSE = "PREFF_IMPULSE"

        fun newInstance(urlJSon: String, urlHtml: String, urlBase: String): IOFragment {
            val fragment = IOFragment()
            val args = Bundle()
            args.putString(ARG_URL_JSON, urlJSon)
            args.putString(ARG_URL_HTML, urlHtml)
            args.putString(ARG_URL_BASE, urlBase)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
