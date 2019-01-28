package ru.vegax.xavier.minimonsterx.iodata.dataLoader

import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Handler

import org.json.JSONException
import org.json.JSONObject

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.ArrayList

import ru.vegax.xavier.minimonsterx.iodata.IOFragment
import ru.vegax.xavier.minimonsterx.iodata.IOItem

import android.os.AsyncTask.Status.FINISHED

internal interface CallBack {                   //declare an interface with the callback methods, so you can use on more than one class and just refer to the interface
    fun notifyDataChanged()

    fun notifyError(e: String)

    fun setDeviceId(deviceId: String)
}

abstract class DataLoader protected constructor(ioData: ArrayList<IOItem>, preferences: SharedPreferences, urlJSon: String, urlHtml: String, urlBase: String) : CallBack {

    private var mHtmDataLoader: AsyncTask<String, Void, String>? = null
    private var mJsonDataLoader: AsyncTask<Array<String>, Void, Void>? = null


    private val mHandler = Handler()
    private val mCyclicRefresh : Runnable
    init {
        mUrlJSon = urlJSon
        mUrlHtml = urlHtml
        mUrlBase = urlBase
        mIoData = ioData
        mPreferences = preferences
        mJsonDataLoader = DataLoader.GetJSonDataAsync(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, arrayOf(mUrlJSon, mUrlHtml))
        // Create the Handler object (on the main thread by default)
        // Define the code block to be executed


        mCyclicRefresh = Runnable{
            if (mContCyclic) {
                refreshData()
            }
            recursiveCall()
        }

        mHandler.post(mCyclicRefresh)

    }
    private fun recursiveCall() {
        mHandler.postDelayed(mCyclicRefresh, mDatauploadRate.toLong()) // Run the above code block on the main thread after 100 milli seconds.
    }

    fun stopUptading() {
        mContCyclic = false
    }

    fun refreshData() {
        mContCyclic = true
        if (mJsonDataLoader!!.status == FINISHED) {
            mJsonDataLoader = DataLoader.GetJSonDataAsync(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, arrayOf(mUrlJSon, mUrlHtml))
        }
    }

    fun setOutputs(suffix: String) {
        mHtmDataLoader = SetHtmlDataAsync(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mUrlBase + suffix)
    }

    fun cancelTasks() {
        mHandler.removeCallbacks(mCyclicRefresh)
        if (mJsonDataLoader != null) {
            mJsonDataLoader!!.cancel(true)
        }
        if (mHtmDataLoader != null) {
            mHtmDataLoader!!.cancel(true)
        }
    }

    fun stopTasks() {
        mContCyclic = false
        if (mJsonDataLoader != null) {
            mJsonDataLoader!!.cancel(true)
        }
        if (mHtmDataLoader != null) {
            mHtmDataLoader!!.cancel(true)
        }
    }

    fun setConnData(urlJSon: String, urlHtml: String, urlBase: String) {
        mUrlJSon = urlJSon
        mUrlHtml = urlHtml
        mUrlBase = urlBase
    }


    private class GetJSonDataAsync internal constructor(private val mDataLoader: DataLoader) : AsyncTask<Array<String>, Void, Void>() {
        private var mE: Exception? = null
        private var mPortNames: Array<String>? = null
        private var mStates: BooleanArray? = null

        override fun doInBackground(vararg urls: Array<String>): Void? {

            val resultUrlJSon: String
            val resultUrlHtml: String
            try {
                resultUrlJSon = getContent(urls[0][0], this)

                resultUrlHtml = getContent(urls[0][1], this)
                jSonHandleNewData(resultUrlJSon, resultUrlHtml)
            } catch (e: Exception) {

                mE = e
            }
            return null
        }

        private fun jSonHandleNewData(strJSon: String, strHtml: String) {
            var strMyJSon = strJSon

            mMasks = BooleanArray(PORT_NUMBER)
            mStates = BooleanArray(PORT_NUMBER)

            try {
                if (!strMyJSon.contains("}")) {
                    strMyJSon += "}"
                }
                val jsonObj = JSONObject(strMyJSon)

                // Getting JSON Array node
                val id = jsonObj.getString("id")
                val statesJs = jsonObj.getJSONArray("prt")
                val maskJS = jsonObj.getJSONArray("pst")
                mDataLoader.setDeviceId(id)
                //                        JSONArray temp = jsonObj.getJSONArray("t");
                //                        JSONArray watchdog = jsonObj.getJSONArray("wdr");
                //                        String pwm1 = jsonObj.getString("pwm1");
                //                        String pwm2 = jsonObj.getString("pwm2");
                //                        String pwmt = jsonObj.getString("pwmt");
                for (i in 0 until statesJs.length()) {
                    mMasks[i] = maskJS.getString(i) == "1"
                    mStates!![i] = statesJs.getString(i) == "1"
                }
                mPortNames = getPortNames(strHtml) // take names between [...]

            } catch (e: JSONException) {
                mDataLoader.notifyError(e.message!!)
            }

        }

        override fun onPostExecute(voids: Void?) {
            if (mE == null) {
                populateRecyclerView(mPortNames!!, mMasks, mStates) // populate reciclerView
                mContCyclic = true
            } else {
                mContCyclic = false
                mDataLoader.notifyError(mE!!.message!!)
            }
        }

        private fun populateRecyclerView(portNames: Array<String>, maskJS: BooleanArray?, states: BooleanArray?) {
            //   mIoData.clear();
            //    if(System.currentTimeMillis()- _lastRequest> mDatauploadRate*7){
            for (i in portNames.indices) {
                var curItem: IOItem? = null
                if (mIoData.size > i) {
                    curItem = mIoData[i]
                }
                if (curItem != null) {
                    if (!curItem.isChanging) {
                        val newItem = IOItem(portNames[i], maskJS!![i], states!![i], curItem.isImpulse, false)
                        mIoData[i] = newItem
                    }
                } else {
                    val isImpulse = mPreferences.getBoolean(IOFragment.PREFF_IMPULSE + mUrlBase + i, false)
                    mIoData.add(IOItem(portNames[i], maskJS!![i], states!![i], isImpulse, false))
                }
            }
            mDataLoader.notifyDataChanged()
            //   }
        }

        companion object {
            private lateinit var mMasks: BooleanArray

            private fun getPortNames(strHtml: String?): Array<String> {
                val portNames = arrayOf("port1", "port2", "port3", "port4", "port5", "port6")
                var lastStart = 0
                var lastEnd = 0
                if (strHtml != null && strHtml.contains("<pre>Manual switch")) {
                    for (i in 0 until PORT_NUMBER) {
                        val start = strHtml.indexOf('[', lastStart + 1)
                        lastStart = start

                        val end = strHtml.indexOf(']', lastEnd + 1)
                        lastEnd = end
                        if (lastStart > 0 && lastEnd > 0) {
                            val helpString = strHtml.substring(start + 1, end)

                            if (mMasks[i]) {
                                val braceIndexStart = helpString.indexOf('>')
                                val braceIndexEnd = helpString.indexOf('<', braceIndexStart)
                                portNames[i] = helpString.substring(braceIndexStart + 1, braceIndexEnd)
                            } else {
                                portNames[i] = helpString
                            }
                        }
                    }
                }

                return portNames
            }
        }
    }

    // sets Html Data but doesn't require response
    private class SetHtmlDataAsync internal constructor(private val mDataLoader: DataLoader) : AsyncTask<String, Void, String>() {
        private var mE: Exception? = null

        override fun doInBackground(vararg urls: String): String? {

            var resultUrl: String? = null
            try {
                resultUrl = getContent(urls[0], this)


            } catch (e: Exception) {

                mE = e
            }

            return resultUrl
        }

        override fun onPostExecute(strHtml: String) {
            if (mE != null) {
                mDataLoader.notifyError(mE!!.message!!)
            } else {
                mDataLoader.refreshData()

            }
        }

    }

    companion object {
        const val PORT_NUMBER = 6
        private lateinit var mIoData: ArrayList<IOItem>
        private lateinit var mPreferences: SharedPreferences
        private var mUrlJSon: String = ""
        private var mUrlHtml: String = ""
        private var mUrlBase: String = ""
        private var mContCyclic: Boolean = false // is continuous cyclic
        private val mDatauploadRate = 100

        @Throws(IOException::class)
        private fun getContent(path: String, task: AsyncTask<*, *, *>): String {
            val url = URL(path)
            val con = url.openConnection() as HttpURLConnection
            con.connect()
            val timeout = 1
            con.connectTimeout = timeout// 0.1 seconds
            con.requestMethod = "GET"
            BufferedReader(InputStreamReader(con.inputStream)).use { reader ->
                val result = StringBuilder()
                var line: String?
                line = reader.readLine()
                //            int i = 0;
                while (!task.isCancelled && line != null) {
                    result.append(line)
                    line = reader.readLine()
                }
                return result.toString()
            }
        }
    }
}
