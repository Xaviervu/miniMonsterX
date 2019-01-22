package ru.vegax.xavier.miniMonsterX.selectDevice;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Objects;
import java.util.Set;

import ru.vegax.xavier.miniMonsterX.R;

import static ru.vegax.xavier.miniMonsterX.MainActivity.MY_PREFS;
import static ru.vegax.xavier.miniMonsterX.MainActivity.PREFF_DEV_NAME;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DeviceSelectFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class DeviceSelectFragment extends AppCompatDialogFragment {

    private static final String EXTRA_DEVICE_NAME = "DEVICE_NAME_EXTRA";
    private OnFragmentInteractionListener _listener;

    public DeviceSelectFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        if (window == null) return;
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = getResources().getDimensionPixelSize(R.dimen.popup_width);
        window.setAttributes(params);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.fragment_select_device, null);
        builder.setView(view);

        //Initialize the RecyclerView
        RecyclerView recVListOfDevices = view.findViewById(R.id.recVListOfDevices);

        //Set the Layout Manager
        recVListOfDevices.setLayoutManager(new LinearLayoutManager(view.getContext()));

        //Initialize the ArrayLIst that will contain the data
        SharedPreferences preferences = view.getContext().getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE);
        Set<String> set = preferences.getStringSet(PREFF_DEV_NAME, null);
        if (set != null && set.size() > 0) {
            assert getArguments() != null;
            String currDevice = Objects.requireNonNull(getArguments()).getString(EXTRA_DEVICE_NAME);
            DeviceSelectAdapter adapter = new DeviceSelectAdapter(view.getContext(), set, currDevice) {
                @Override
                public void onClick(View v) {
                    if (v instanceof Button) {
                        // delete item
                        onDeleteItem(((String) (v).getTag()));
                    } else {
                        onButtonPressed(((TextView) v).getText().toString());
                    }
                    getDialog().dismiss();
                }
            };


            //Initialize the adapter and set it ot the RecyclerView
            recVListOfDevices.setAdapter(adapter);
        }
        return builder.create();
    }

    public void onButtonPressed(String deviceName) {
        if (_listener != null) {
            _listener.onFragmentResult(deviceName);
        }
    }

    public void onDeleteItem(String deviceName) {
        if (_listener != null) {
            _listener.onDeleteItem(deviceName);
        }
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            _listener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        _listener = null;
    }

    public DeviceSelectFragment newInstance(String currentDevice) {
        DeviceSelectFragment fragment = new DeviceSelectFragment();

        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_DEVICE_NAME, currentDevice);
        fragment.setArguments(bundle);

        return fragment;
    }


    public interface OnFragmentInteractionListener {
        void onFragmentResult(String deviceName);

        void onDeleteItem(String deviceName);
    }
}
