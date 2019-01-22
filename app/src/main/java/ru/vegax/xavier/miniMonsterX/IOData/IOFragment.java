package ru.vegax.xavier.miniMonsterX.IOData;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;

import ru.vegax.xavier.miniMonsterX.IOData.dataLoader.DataLoader;
import ru.vegax.xavier.miniMonsterX.R;

import static ru.vegax.xavier.miniMonsterX.MainActivity.MY_PREFS;


public class IOFragment extends Fragment {
    private static final String TAG = "IOFragment";
    private static final String ARG_URL_JSON = "URL_EXTRA_JSON";
    private static final String ARG_URL_HTML = "URL_EXTRA_HTML";
    private static final String ARG_URL_BASE = "URL_EXTRA_BASE";
    private static final String _urlSufixImpulse = "/?rst=";
    private static final String _urlSufixSet = "/?sw=";

    public static final String PREFF_IMPULSE = "PREFF_IMPULSE";


    private String _urlJSon;
    private String _urlHtml;
    private String _urlBase;

    private ProgressBar _progBarDownload;
    private ArrayList<IOItem> _ioData;
    private IOAdapter _adapter;
    private TextView _txtVdeviceName;
    private DataLoader _dataLoader;
    RecyclerView _recyclerView;

    public IOFragment() {
        // Required empty public constructor
    }

    public static IOFragment newInstance(String urlJSon, String urlHtml, String urlBase) {
        IOFragment fragment = new IOFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL_JSON, urlJSon);
        args.putString(ARG_URL_HTML, urlHtml);
        args.putString(ARG_URL_BASE, urlBase);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            _urlJSon = getArguments().getString(ARG_URL_JSON);
            _urlHtml = getArguments().getString(ARG_URL_HTML);
            _urlBase = getArguments().getString(ARG_URL_BASE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.io_data_fragment, container, false);
        _progBarDownload = v.findViewById(R.id.progBarDownloadJS);

        _progBarDownload.setProgress(100);


        _txtVdeviceName = v.findViewById(R.id.txtVdeviceName);

        //Initialize the RecyclerView
        _recyclerView = v.findViewById(R.id.recyclerView);

        //Set the Layout Manager
        _recyclerView.setLayoutManager(new LinearLayoutManager(v.getContext()));

        //Initialize the ArrayLIst that will contain the data
        _ioData = new ArrayList<>();

        //Initialize the adapter and set it ot the RecyclerView

        _adapter = new IOAdapter(v.getContext(), _ioData) {
            @Override
            public boolean onLongClick(View v) {
                impulseOutput(v);
                return true;
            }

            @Override
            public void onClick(View v) {
                setOutput(v);
            }

        };


        _recyclerView.setAdapter(_adapter);
        SharedPreferences sharedPreferences = v.getContext().getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE);

        _dataLoader = new DataLoader(_ioData, sharedPreferences, _urlJSon, _urlHtml, _urlBase) {
            @Override
            public void notifyDataChanged() {
                boolean isButtonChanging = false;
                for (int i = 0; i < DataLoader.PORT_NUMBER; i++) {

                    isButtonChanging |= _ioData.get(i).isChanging(); // if at least one element is being edited

                }
                if (!isButtonChanging) {
                    _adapter.notifyDataSetChanged();
                }
                _progBarDownload.setIndeterminate(false);
                _recyclerView.setVisibility(View.VISIBLE);


            }

            @Override
            public void notifyError(String e) {
                Toast.makeText(v.getContext(), getString(R.string.no_conn), Toast.LENGTH_SHORT).show();
                _progBarDownload.setIndeterminate(false);
                _recyclerView.setVisibility(View.INVISIBLE);
                _txtVdeviceName.setText(getString(R.string.no_conn));
            }

            @Override
            public void setDeviceId(String deviceId) {
                v.post(() -> {
                    _txtVdeviceName.setText(deviceId);//send to a UI thread!
                });

            }

        };
        refreshData();

        return v;
    }


    @Override
    public void onDestroy() {
        _dataLoader.cancelTasks();

        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public void setConnData(String urlJSon, String urlHtml, String urlBase) {
        _urlJSon = urlJSon;
        _urlHtml = urlHtml;
        _urlBase = urlBase;
        _dataLoader.setConnData(urlJSon, urlHtml, urlBase);
    }

    //handle click from item

    public void setOutput(View view) {
        int curPos = (int) view.getTag();
        IOItem currentItem = _ioData.get(curPos);
        String suffix;
        if (currentItem.isImpulse()) {
            // set the suffix string for pressing turning the output on or off for a period of time
            suffix = _urlSufixImpulse + (curPos + 1); //
            currentItem.setChanging(false);
            _dataLoader.setOutputs(suffix);
            ((Switch) view).setChecked(currentItem.isOn());
        } else {
            ((Switch) view).setChecked(currentItem.isOn());
            // set the suffix string for pressing turning the output on or off
            suffix = _urlSufixSet + (curPos + 1) + "-" + (currentItem.isOn() ? "0" : "1"); //"/?sw=i-1" for turning on i - port number starting from 1
            currentItem.setChanging(false);
            _dataLoader.setOutputs(suffix);

        }

    }
    //handle long click from item

    public void impulseOutput(View view) {
        int curPos = (int) view.getTag();
        IOItem currentItem = _ioData.get(curPos);

        currentItem.setImpulse(!currentItem.isImpulse());
        SharedPreferences preferences = Objects.requireNonNull(getContext()).getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PREFF_IMPULSE + _urlBase + curPos, currentItem.isImpulse());
        editor.apply();
        _adapter.notifyItemChanged(curPos);
        currentItem.setChanging(false);
    }

    public void stopUpdating() {
        _progBarDownload.setIndeterminate(false);
        _dataLoader.stopUptading();
    }

    public void refreshData() {
        if (!_urlHtml.equals("")) {
            _progBarDownload.setIndeterminate(true);

            _dataLoader.refreshData();
        } else {
            _ioData.clear();
            _recyclerView.notify();
        }
    }

    public void clearData() {
        _ioData.clear();
        synchronized (_recyclerView) {
            _recyclerView.notify();
        }
    }

    public void cancelTasks() {
        _dataLoader.stopTasks();
        _progBarDownload.setIndeterminate(false);
    }
}
