package ru.vegax.xavier.gateopener;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Switch;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    public static final int MY_REQUEST_ID = 1;
    public static final int NO_ELEMENTS = 6;
    public static  String _url_adress = "http://fasterpast.ru:8093/";
    public static  String _url_password = "password";
    public boolean _refreshTag;
    public boolean _pause = false;

//
//    public static  String _url_adress = "http://192.168.1.12/";
//    public static  String _url_password = "klmvts";

    public static  String url_main = "/?main=";
    public static String url_mainCmd;

    public Elements links;
    public Elements switchNames;
    public ArrayList<String> outputNames = new ArrayList<String>();
    public ArrayList<String> outStates = new ArrayList<String>();
    public ArrayList<String> urls = new ArrayList<String>();

    private ArrayList<SwData> arrayOfSwitches = new ArrayList<>(); // Construct the data source
    private SwAdapter adapter;
    private ListView lv;
    private Handler handler = new Handler(); // for cyclic refresh
    private boolean _connected; // last connection was successful

    private void rememberPrefs(){
        SharedPreferences preferences = this.getApplicationContext().getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("URL", _url_adress);
        editor.putString("PASS", _url_password);
        editor.putBoolean("REFRESH",_refreshTag);
        editor.commit();
    }
    private String joinStrings(String urlStr, String passStr, String endStr){
        String htmlStr = "http://";
        String slashStr = "/";
        String urlComplete = "";
        if(!urlStr.startsWith(htmlStr)){
            urlComplete = htmlStr;
        }
        urlComplete = urlComplete + urlStr;
        if(!urlStr.endsWith(slashStr)){
            urlComplete = urlComplete + slashStr;
        }
        return (urlComplete+passStr+endStr);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //todo: add possibility to set impulse time in ms
        //todo: add possibility to configure buttons to be impulse in ms
        // todo: add push messages for rise, fall or change in an input
        // todo: add possibility to reverse an input

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton refresh = (FloatingActionButton) findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rememberPrefs();
                url_mainCmd = joinStrings(_url_adress, _url_password,url_main);
                new NewThread(url_mainCmd).execute();
                Snackbar.make(view, "refreshing...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        lv = (ListView) findViewById(R.id.listView1);

        SharedPreferences preferences = this.getApplicationContext().getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
        _url_adress = preferences.getString("URL", "");
        _url_password = preferences.getString("PASS", "");
        _refreshTag = preferences.getBoolean("REFRESH",false);
        url_mainCmd = joinStrings(_url_adress, _url_password,url_main);
        new NewThread(url_mainCmd).execute();
        adapter = new SwAdapter(this,arrayOfSwitches);

        handler.post(runnableCode);

    }
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            // Do something here on the main thread
            if(_refreshTag && !_pause) {
                new NewThread(url_mainCmd).execute();
            }
            // Repeat this the same runnable code block again another 2 seconds
            handler.postDelayed(runnableCode, 900);
        }
    };

    public class SwData {
        public String name;
        public Boolean checked;
        public boolean inputOnly;

        public SwData(String name, Boolean checked, Boolean inputOnly) {
            this.inputOnly = inputOnly;
            this.name = name;
            this.checked = checked;
        }
    }
    public class SwAdapter extends ArrayAdapter<SwData> { // adapter for switches
        public SwAdapter(Context context, ArrayList<SwData> inSwitch) {
            super(context, 0, inSwitch);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position

            SwData swData = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
            }
            // Lookup view for data population
            Switch swItem = (Switch) convertView.findViewById(R.id.switch1);

            // Populate the data into the template view using the data object
            if(swData != null){
                swItem.setText(swData.name);
                swItem.setChecked(swData.checked);
                if(swData.inputOnly){
                    swItem.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_Large);
                }

            }


            // Return the completed view to render on screen
            return convertView;
        }
    }

    public class NewThread  extends AsyncTask<String,Void,String> {
        public String url;
        public NewThread(String url) {
            this.url = url;
            // arrayOfSwitches.clear();
            //  lv.setAdapter(null);
        }
        @Override
        protected String doInBackground(String... arg) {
            Document doc;

            try {
                //  url =  "http://xavv.ucoz.net/gateOpener2/"; // for tests
                doc = Jsoup.connect(url).get();
                _connected = true;
                links = doc.select("a[href]");
                switchNames = doc.select("font");
                outputNames.clear();
                outStates.clear();
                urls.clear();
                SwData newSwData;
                String [] hmlStrings = doc .text().split("\n");
                int j = 0;
                for (int i = 1; i <= NO_ELEMENTS ; i++){
                    if(hmlStrings[i].startsWith("turn")){
                        outputNames.add(switchNames.get(j).text());
                        outStates.add(links.get(j).text());
                        urls.add(links.get(j).attr("abs:href"));
                        Boolean checked = links.get(j).text().contains("OFF");
                        newSwData = new SwData(switchNames.get(j).text(),checked,true);
                        if(arrayOfSwitches.size() == NO_ELEMENTS){
                            SwData sw  = arrayOfSwitches.get(i-1);
                            sw.checked = newSwData.checked;
                            sw.inputOnly= newSwData.inputOnly;
                            sw.name = newSwData.name;
                            //arrayOfSwitches.set(i-1,newSwData) ;
                        }else{
                            arrayOfSwitches.add(newSwData);
                        }
                        j++;
                    }else{
                        urls.add("");
                        String inName = hmlStrings[i];
                        Boolean high = inName.contains("HIGH");
                        inName = inName.substring(inName.indexOf('[')+1);
                        inName = inName.substring(0,inName.indexOf(']'));
                        newSwData = new SwData(inName,high,false);
                        arrayOfSwitches.add(newSwData);
                    }
                }

            }catch (IOException e){
                if((_refreshTag && _connected)||!_refreshTag){
                    View view = findViewById(R.id.listView1);
                    Snackbar.make(view, "could not connect", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                _connected = false;
            }
            return null;
        }
        @Override
        protected void onPostExecute (String result){

            // arrayOfSwitches = arrayOfSwitchesBuff;
            // adapter = new SwAdapter(getApplicationContext(),arrayOfSwitches);
            if(!_connected){
                arrayOfSwitches.clear();
                lv.setAdapter(null);
            }else{
                lv.setAdapter(adapter);
            }

            lv.deferNotifyDataSetChanged();
            // lv.refreshDrawableState();
        }
    }
    public void setOutput(View view) {


        Switch switchID = (Switch) view;
        String swText = (String) switchID.getText();
        int index  = outputNames.indexOf(swText);
        if(index >= 0){
            String url_cmd = urls.get(index);
            if (url_cmd != ""){
                new NewThread(url_cmd).execute();
            }
        }else{
            switchID.setChecked(!switchID.isChecked());
        }


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gate_opener_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("URL_EXTRA", _url_adress);
            intent.putExtra("PASS_EXTRA", _url_password);
            intent.putExtra("REFRESH", _refreshTag);
            _pause = true;
            startActivityForResult(intent,MY_REQUEST_ID);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        _pause = false;
        if (requestCode == MY_REQUEST_ID) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                if(extras != null) {
                    _url_adress = data.getStringExtra("URL_EXTRA");
                    _url_password = data.getStringExtra("PASS_EXTRA");
                    _refreshTag = data.getBooleanExtra("REFRESH",false);
                    if (_url_password != null && _url_adress != null) {
                        url_mainCmd = joinStrings(_url_adress, _url_password,url_main);
                        rememberPrefs();
                        new NewThread(url_mainCmd).execute();
                    }
                }

            }
        }
    }
}
