package com.desti.currencyconverter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import okhttp3.*;

public class MainActivity extends AppCompatActivity {
    private Spinner fromSpinner, toSpinner;
    private EditText valueEditText, feeEditText;
    private CheckBox feeCheckBox;
    private CheckBox monthCheckBox;
    private Button convertButton;
    private TextView resultTextView;
    private String[] dropdownOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        fromSpinner = findViewById(R.id.from_spinner);
        toSpinner = findViewById(R.id.to_spinner);
        valueEditText = findViewById(R.id.value_edit_text);
        feeEditText = findViewById(R.id.fee_text_view);
        feeCheckBox = findViewById(R.id.fee_checkbox);
        monthCheckBox = findViewById(R.id.month_checkbox);
        convertButton = findViewById(R.id.convert_button);
        resultTextView = findViewById(R.id.result_text_view);
        setSpinners();

        feeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    feeEditText.setVisibility(View.VISIBLE);
                } else {
                    feeEditText.setVisibility(View.INVISIBLE);
                }
            }
        });

        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // collect information required for conversion
                String valueString = valueEditText.getText().toString();
                if (valueString.equals("")) valueString = "0";
                double value = Double.parseDouble(valueString);

                String fromCurr = fromSpinner.getSelectedItem().toString();
                String toCurr = toSpinner.getSelectedItem().toString();

                // convert to desired currency
                try {
                    OkHttpClient client = new OkHttpClient().newBuilder().build();
                    String url;

                    if (monthCheckBox.isChecked()){
                        Calendar calendar = Calendar.getInstance();
                        Date todayDate = calendar.getTime();
                        calendar.add(Calendar.MONTH, -1);
                        Date oneMonthAgoDate = calendar.getTime();
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        String today = format.format(todayDate);
                        String oneMonthAgo = format.format(oneMonthAgoDate);
                        url = String.format("https://api.exchangerate.host/timeseries?start_date=%s&end_date=%s&base=%s&symbols=%s&amount=%.2f", oneMonthAgo, today, fromCurr, toCurr, value);
                    } else {
                        url = String.format("https://api.exchangerate.host/convert?from=%s&to=%s&amount=%.2f", fromCurr, toCurr, value);
                    }

                    Request request = new Request.Builder()
                            .url(url)
                            .get()
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseString = response.body().string();
                    JSONObject json = new JSONObject(responseString);
                    if (monthCheckBox.isChecked()) {
                        Double sum = 0.0;
                        int count = 0;
                        JSONObject jsonRates = json.getJSONObject("rates");
                        String key;
                        Double rate = 0.0;
                        for (Iterator<String> it = jsonRates.keys(); it.hasNext(); ) {
                            key = it.next();
                            if (jsonRates.getJSONObject(key).has(toCurr) && jsonRates.getJSONObject(key).get(toCurr) != JSONObject.NULL) {
                                rate = (Double) jsonRates.getJSONObject(key).get(toCurr);
                                sum += (Double) rate;
                                count++;
                            }
                        }
                        if (count != 0) {
                            value = (sum / count);
                        } else {
                            value = 0;
                        }

                    } else {
                        Object rate = json.get("result");
                        if (JSONObject.NULL.equals(rate)) {
                            value = 0;
                        } else {
                            value = (double) rate;
                        }
                    }

                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    return;
                }

                // add % fee
                if (feeCheckBox.isChecked()) {
                    String feeString = feeEditText.getText().toString();
                    if (feeString.equals("")) feeString = "0";
                    double feePercent = Double.parseDouble(feeString);
                    value *= (1+feePercent/100);
                }

                // format and display
                value = (double) Math.round(value*100)/100;
                valueString = Double.toString(value);
                if (valueString.endsWith(".0")) valueString = valueString.substring(0, valueString.length()-2);
                resultTextView.setText(valueString);
            }
        });

        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(fromSpinner.getContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("com.desti.currencyconverter.from", fromSpinner.getSelectedItem().toString());
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(toSpinner.getContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("com.desti.currencyconverter.to", toSpinner.getSelectedItem().toString());
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.customise:
                Intent intent = new Intent(MainActivity.this ,
                        CustomiseActivity.class);
                intent.putExtra("dropdown", dropdownOptions);
                startActivityForResult(intent, 1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // TODO: fix resultCode != RESULT_OK (0)
        if (requestCode == 1) {
            setSpinners();
        }
    }

    private void setSpinners() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String prefsString = prefs.getString("com.desti.currencyconverter.dropdownoptions", "empty");
        if (prefsString.equals("empty")) {
            dropdownOptions = getResources().getStringArray(R.array.default_currencies);
        } else {
            dropdownOptions = stringToArray(prefsString);
        }

        ArrayAdapter<String> fromAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dropdownOptions);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);

        ArrayAdapter<String> toAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dropdownOptions);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);

        String fromPref = prefs.getString("com.desti.currencyconverter.from", "empty");
        String toPref = prefs.getString("com.desti.currencyconverter.to", "empty");

        if (dropdownOptions.length > 1 && (fromPref.equals("empty") && toPref.equals("empty"))) {
            toSpinner.setSelection(1);
        } else {
            List<String> dropdownList = Arrays.asList(dropdownOptions);
            int fromIndex = dropdownList.lastIndexOf(fromPref);
            int toIndex = dropdownList.lastIndexOf(toPref);

            if ( fromIndex != -1) {
                fromSpinner.setSelection(fromIndex);
            } else {
                fromSpinner.setSelection(0);
            }

            if ( toIndex != -1) {
                toSpinner.setSelection(toIndex);
            } else {
                toSpinner.setSelection(0);
            }
        }
    }

    public String[] stringToArray(String s) {
        return s.split(",");
    }

    public static String arrayToString(String[] sa) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sa.length; i++) {
            sb.append(sa[i]).append(",");
        }
        return sb.toString();
    }
}