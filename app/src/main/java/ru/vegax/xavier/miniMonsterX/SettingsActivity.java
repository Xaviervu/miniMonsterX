package ru.vegax.xavier.miniMonsterX;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Set;

import static ru.vegax.xavier.miniMonsterX.MainActivity.EXTRA_FOR_CREATION;
import static ru.vegax.xavier.miniMonsterX.MainActivity.EXTRA_NAME;
import static ru.vegax.xavier.miniMonsterX.MainActivity.EXTRA_PASS;
import static ru.vegax.xavier.miniMonsterX.MainActivity.EXTRA_URL;
import static ru.vegax.xavier.miniMonsterX.MainActivity.MY_PREFS;
import static ru.vegax.xavier.miniMonsterX.MainActivity.PREFF_DEV_NAME;

public class SettingsActivity extends AppCompatActivity {

    // UI references.
    private AutoCompleteTextView _txtVurl;
    private EditText _txtVpassword;
    private TextView _txtVdeviceName;
    private boolean _forCreation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // Set up the login form.
        _txtVurl = findViewById(R.id.url);
        _txtVdeviceName = findViewById(R.id.deviceName);
        _txtVpassword = findViewById(R.id.password);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            _txtVdeviceName.setText(extras.getString(EXTRA_NAME));
            _txtVurl.setText(extras.getString(EXTRA_URL));
            _txtVpassword.setText(extras.getString(EXTRA_PASS));
            _forCreation = extras.getBoolean(EXTRA_FOR_CREATION);
        }
        TextView txtVdialogTitle = findViewById(R.id.dialogTitle);
        if (_forCreation) {
            txtVdialogTitle.setText(getString(R.string.add_new_device));
            _txtVdeviceName.setEnabled(!_forCreation);
        }
        _txtVdeviceName.setEnabled(_forCreation);
        _txtVpassword.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == R.id.password || id == EditorInfo.IME_NULL) {

                setLoginData();
                return true;

            }
            return false;
        });

        Button btnOk = findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(view -> setLoginData());

    }


    private void setLoginData() {


        // Reset errors.
        _txtVurl.setError(null);
        _txtVpassword.setError(null);

        // Store values at the time of the login attempt.
        String devName = _txtVdeviceName.getText().toString();
        String address = _txtVurl.getText().toString();
        String password = _txtVpassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a device name, valid password, if the user entered one.
        if (TextUtils.isEmpty(devName)) {
            _txtVdeviceName.setError(getString(R.string.dev_name_required));
            cancel = true;
        } else {
            if (_forCreation) {
                SharedPreferences preferences = this.getApplicationContext().getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE);
                //Retrieve the values
                Set<String> set = preferences.getStringSet(PREFF_DEV_NAME, null);
                if (set != null && set.contains(devName)) {
                    focusView = _txtVdeviceName;
                    _txtVdeviceName.setError(getString(R.string.dev_name_already_exists));
                    cancel = true;
                }

            }
        }

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            _txtVpassword.setError(getString(R.string.error_invalid_password));
            focusView = _txtVpassword;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(address)) {
            _txtVurl.setError(getString(R.string.error_field_required));
            focusView = _txtVurl;
            cancel = true;
        } else if (!isURLValid(address)) {
            _txtVurl.setError(getString(R.string.error_invalid_address));
            focusView = _txtVurl;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            assert focusView != null;
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            Intent resultData = new Intent();
            resultData.putExtra(EXTRA_NAME, _txtVdeviceName.getText().toString());
            resultData.putExtra(EXTRA_URL, _txtVurl.getText().toString());
            resultData.putExtra(EXTRA_PASS, _txtVpassword.getText().toString());
            resultData.putExtra(EXTRA_FOR_CREATION, _forCreation);
            setResult(Activity.RESULT_OK, resultData);
            finish();
        }
    }

    private boolean isURLValid(String url) {
        return url.length() > 1;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 1;
    }
}
