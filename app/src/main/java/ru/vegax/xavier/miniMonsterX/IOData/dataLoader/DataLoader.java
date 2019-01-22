package ru.vegax.xavier.miniMonsterX.IOData.dataLoader;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import ru.vegax.xavier.miniMonsterX.IOData.IOFragment;
import ru.vegax.xavier.miniMonsterX.IOData.IOItem;

import static android.os.AsyncTask.Status.FINISHED;

interface CallBack {                   //declare an interface with the callback methods, so you can use on more than one class and just refer to the interface
    void notifyDataChanged();

    void notifyError(String e);

    void setDeviceId(String deviceId);
}

public abstract class DataLoader implements CallBack {
    public static final int PORT_NUMBER = 6;
    private static ArrayList<IOItem> _ioData;
    private static SharedPreferences _preferences;
    private static final String TAG = "DataLoader";
    private static String _urlJSon;
    private static String _urlHtml;
    private static String _urlBase;
    private static boolean _contCyclic; // is continuous cyclic
    private static long _lastRequest;

    private AsyncTask<String, Void, String> _htmDataLoader;
    private AsyncTask<String[], Void, Void> _jsonDataLoader;

    private Handler _handler;
    private Runnable _cyclicRefresh;
    private static int _datauploadRate = 100;

    protected DataLoader(ArrayList<IOItem> ioData, SharedPreferences preferences, String urlJSon, String urlHtml, String urlBase) {
        _urlJSon = urlJSon;
        _urlHtml = urlHtml;
        _urlBase = urlBase;
        _ioData = ioData;
        _preferences = preferences;
        _jsonDataLoader = new DataLoader.GetJSonDataAsync(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{_urlJSon, _urlHtml});
        // Create the Handler object (on the main thread by default)
        _handler = new Handler();
// Define the code block to be executed
        _cyclicRefresh = () -> {
            // Do something here on the main thread

            if (_contCyclic) {
                refreshData();

            }
            _handler.postDelayed(_cyclicRefresh, _datauploadRate); // Run the above code block on the main thread after 100 milli seconds
        };
        _handler.post(_cyclicRefresh);

    }

    public void stopUptading() {
        _contCyclic = false;
    }

    public void refreshData() {
        _contCyclic = true;
        if (_jsonDataLoader.getStatus() == FINISHED) {
            _jsonDataLoader = new DataLoader.GetJSonDataAsync(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{_urlJSon, _urlHtml});
        }
    }

    public void setOutputs(String suffix) {
        // _lastRequest = System.currentTimeMillis();
        _htmDataLoader = new SetHtmlDataAsync(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, _urlBase + suffix);
    }

    public void cancelTasks() {
        _handler.removeCallbacks(_cyclicRefresh);
        if (_jsonDataLoader != null) {
            _jsonDataLoader.cancel(true);
        }
        if (_htmDataLoader != null) {
            _htmDataLoader.cancel(true);
        }
    }

    public void stopTasks() {
        _contCyclic = false;
        if (_jsonDataLoader != null) {
            _jsonDataLoader.cancel(true);
        }
        if (_htmDataLoader != null) {
            _htmDataLoader.cancel(true);
        }
    }

    private static String getContent(String path, AsyncTask task) throws IOException {
        URL url = new URL(path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.connect();
        int timeout = 1;
        con.setConnectTimeout(timeout);// 0.1 seconds
        con.setRequestMethod("GET");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            StringBuilder result = new StringBuilder();
            String line;
//            int i = 0;
            while (((line = reader.readLine()) != null) && !task.isCancelled()) {
//                publishProgress(i);
//                i++;
                result.append(line);
            }
//            _progBarDownload.setProgress(100);
            return result.toString();
        }
    }

    public void setConnData(String urlJSon, String urlHtml, String urlBase) {
        _urlJSon = urlJSon;
        _urlHtml = urlHtml;
        _urlBase = urlBase;
    }


    private static class GetJSonDataAsync extends AsyncTask<String[], Void, Void> {
        private final DataLoader _dataLoader;
        private Exception _e = null;
        private String[] _portNames;
        private static boolean[] _masks;
        private boolean[] _states;


        GetJSonDataAsync(DataLoader dataLoader) {
            _dataLoader = dataLoader;
        }

        @Override
        protected Void doInBackground(String[]... urls) {

            String resultUrlJSon;
            String resultUrlHtml;
            try {
                resultUrlJSon = getContent(urls[0][0], this);

                resultUrlHtml = getContent(urls[0][1], this);
                jSonHandleNewData(resultUrlJSon, resultUrlHtml);
            } catch (Exception e) {

                _e = e;
            }

            return null;
        }

        private void jSonHandleNewData(String strJSon, String strHtml) {

            _masks = new boolean[PORT_NUMBER];
            _states = new boolean[PORT_NUMBER];

            try {
                if (!strJSon.contains("}")) {
                    strJSon += "}";
                }
                JSONObject jsonObj = new JSONObject(strJSon);

                // Getting JSON Array node
                String id = jsonObj.getString("id");
                JSONArray statesJs = jsonObj.getJSONArray("prt");
                JSONArray maskJS = jsonObj.getJSONArray("pst");
                _dataLoader.setDeviceId(id);
//                        JSONArray temp = jsonObj.getJSONArray("t");
//                        JSONArray watchdog = jsonObj.getJSONArray("wdr");
//                        String pwm1 = jsonObj.getString("pwm1");
//                        String pwm2 = jsonObj.getString("pwm2");
//                        String pwmt = jsonObj.getString("pwmt");
                for (int i = 0; i < statesJs.length(); i++) {
                    _masks[i] = maskJS.getString(i).equals("1");
                    _states[i] = statesJs.getString(i).equals("1");
                }
                _portNames = getPortNames(strHtml); // take names between [...]

            } catch (final JSONException e) {
                _dataLoader.notifyError(e.getMessage());
            }
        }

        private static String[] getPortNames(String strHtml) {
            String[] portNames = new String[]{"port1", "port2", "port3", "port4", "port5", "port6"};
            int lastStart = 0;
            int lastEnd = 0;
            if (strHtml != null && strHtml.contains("<pre>Manual switch")) {
                for (int i = 0; i < PORT_NUMBER; i++) {
                    int start = strHtml.indexOf('[', lastStart + 1);
                    lastStart = start;

                    int end = strHtml.indexOf(']', lastEnd + 1);
                    lastEnd = end;
                    if (lastStart > 0 && lastEnd > 0) {
                        String helpString = strHtml.substring(start + 1, end);

                        if (_masks != null) {
                            if (_masks[i]) {
                                int braceIndexStart = helpString.indexOf('>');
                                int braceIndexEnd = helpString.indexOf('<', braceIndexStart);
                                portNames[i] = helpString.substring(braceIndexStart + 1, braceIndexEnd);
                            } else {
                                portNames[i] = helpString;
                            }
                        }
                    }
                }
            }

            return portNames;
        }

        @Override
        protected void onPostExecute(Void voids) {
            if (_e == null) {
                populateRecyclerView(_portNames, _masks, _states); // populate reciclerView
                _contCyclic = true;
            } else {
                _contCyclic = false;
                _dataLoader.notifyError(_e.getMessage());
            }
        }

        private void populateRecyclerView(String[] portNames, boolean[] maskJS, boolean[] states) {
            //   _ioData.clear();
            //    if(System.currentTimeMillis()- _lastRequest> _datauploadRate*7){
            for (int i = 0; i < portNames.length; i++) {
                IOItem curItem = null;
                if (_ioData.size() > i) {
                    curItem = _ioData.get(i);
                }
                if (curItem != null) {
                    if (!curItem.isChanging()) {
                        IOItem newItem = new IOItem(portNames[i], maskJS[i], states[i], curItem.isImpulse());
                        _ioData.set(i, newItem);
                    }
                } else {
                    boolean isImpulse = _preferences.getBoolean(IOFragment.PREFF_IMPULSE + _urlBase + i, false);
                    _ioData.add(new IOItem(portNames[i], maskJS[i], states[i], isImpulse));
                }
            }
            _dataLoader.notifyDataChanged();
            //   }
        }
    }

    // sets Html Data but doesn't require response
    private static class SetHtmlDataAsync extends AsyncTask<String, Void, String> {
        private final DataLoader _dataLoader;
        private Exception _e = null;

        SetHtmlDataAsync(DataLoader dataLoader) {
            _dataLoader = dataLoader;
        }

        @Override
        protected String doInBackground(String... urls) {

            String resultUrl = null;
            try {
                resultUrl = getContent(urls[0], this);


            } catch (Exception e) {

                _e = e;
            }
            return resultUrl;
        }

        @Override
        protected void onPostExecute(String strHtml) {
            if (_e != null) {
                _dataLoader.notifyError(_e.getMessage());
            } else {
                _dataLoader.refreshData();

            }
        }

    }
}
