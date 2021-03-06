package com.desti.currencyconverter;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import okhttp3.*;

public class MainActivity extends AppCompatActivity {
    private Spinner fromSpinner, toSpinner;
    private EditText valueEditText, addFeeEditText, deductFeeEditText, customRateEditText;
    private CheckBox addFeeCheckBox, deductFeeCheckbox, monthCheckBox;
    private Button convertButton;
    private TextView resultTextView;
    private CheckBox customRateCheckbox;
    private LinearLayout currencyLayout, resultLayout;
    private String[] dropdownOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        findWidgets();
        setWidgets();

        customRateCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                resultLayout.setVisibility(View.INVISIBLE);
                if (isChecked) {
                    currencyLayout.setVisibility(View.GONE);
                    customRateEditText.setVisibility(View.VISIBLE);
                    monthCheckBox.setVisibility(View.GONE);
                    convertButton.setEnabled(true);
                } else {
                    currencyLayout.setVisibility(View.VISIBLE);
                    customRateEditText.setVisibility(View.GONE);
                    monthCheckBox.setVisibility(View.VISIBLE);
                    convertButton.setEnabled(!checkEmptyDropdownOptions());
                }
            }
        });

        monthCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                resultLayout.setVisibility(View.INVISIBLE);
            }
        });

        addFeeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                resultLayout.setVisibility(View.INVISIBLE);
                if (isChecked) {
                    addFeeEditText.setVisibility(View.VISIBLE);
                } else {
                    addFeeEditText.setVisibility(View.INVISIBLE);
                }
            }
        });

        deductFeeCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                resultLayout.setVisibility(View.INVISIBLE);
                if (isChecked) {
                    deductFeeEditText.setVisibility(View.VISIBLE);
                } else {
                    deductFeeEditText.setVisibility(View.INVISIBLE);
                }
            }
        });

        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissKeyboard(MainActivity.this);
                double value = getValue();

                // handle custom conversion
                if (customRateCheckbox.isChecked()) {
                    String rateString = customRateEditText.getText().toString();
                    if (rateString.equals("")) rateString = "0";
                    value *= Double.parseDouble(rateString);
                    saveCustomRatePreferences(rateString);
                } else {
                    // convert to desired currency
                    try {
                        // conduct request
                        OkHttpClient client = new OkHttpClient().newBuilder().build();
                        String url = buildConversionUrl(value);
                        Request request = new Request.Builder()
                                .url(url)
                                .get()
                                .build();
                        Response response = client.newCall(request).execute();
                        String responseString = response.body().string();
                        JSONObject json = new JSONObject(responseString);

                        if (monthCheckBox.isChecked()) {
                            value = getMonthlyAverage(json);
                        } else {
                            Double result = jsonObjectToDouble(json, "result");
                            if (result == null) {
                                value = 0;
                            } else {
                                value = result;
                            }
                        }
                    } catch (UnknownHostException e) {
                        Toast.makeText(MainActivity.this, R.string.no_wifi_message, Toast.LENGTH_SHORT).show();
                        resultLayout.setVisibility(View.INVISIBLE);
                        return;
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if (addFeeCheckBox.isChecked()) value = addFee(value);
                if (deductFeeCheckbox.isChecked()) value = deductFee(value);

                // format and display
                value = (double) Math.round(value*100)/100;
                displayResult(value);
            }
        });

        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                resultLayout.setVisibility(View.INVISIBLE);
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
                resultLayout.setVisibility(View.INVISIBLE);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(toSpinner.getContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("com.desti.currencyconverter.to", toSpinner.getSelectedItem().toString());
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        valueEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                resultLayout.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

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
            reset();
            setSpinners();
        }
    }

    private void findWidgets() {
        fromSpinner = findViewById(R.id.from_spinner);
        toSpinner = findViewById(R.id.to_spinner);
        valueEditText = findViewById(R.id.value_edit_text);
        addFeeEditText = findViewById(R.id.add_fee_text_view);
        deductFeeEditText = findViewById(R.id.deduct_fee_text_view);
        customRateEditText = findViewById(R.id.custom_rate_edit_text);
        addFeeCheckBox = findViewById(R.id.add_fee_checkbox);
        deductFeeCheckbox = findViewById(R.id.deduct_fee_checkbox);
        monthCheckBox = findViewById(R.id.month_checkbox);
        convertButton = findViewById(R.id.convert_button);
        resultTextView = findViewById(R.id.result_text_view);
        customRateCheckbox = findViewById(R.id.custom_rate_checkbox);
        currencyLayout = findViewById(R.id.currency_layout);
        resultLayout =  findViewById(R.id.result_layout);
    }

    private void setWidgets() {
        setSpinners();
        setFee();
        setCustomRate();
        customRateEditText.setVisibility(View.GONE);
        resultLayout.setVisibility(View.INVISIBLE);
    }

    private void setSpinners() {
        setDropdownOptions();

        convertButton.setEnabled(!checkEmptyDropdownOptions() || customRateCheckbox.isChecked());

        // set spinner adapters
        ArrayAdapter<String> fromAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dropdownOptions);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);
        ArrayAdapter<String> toAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dropdownOptions);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);

        setDropdownPreferences();
    }

    private void setFee() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        addFeeEditText.setText(prefs.getString("com.desti.currencyconverter.addfee", "0"));
        deductFeeEditText.setText(prefs.getString("com.desti.currencyconverter.deductfee", "0"));
    }

    private void setCustomRate() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        customRateEditText.setText(prefs.getString("com.desti.currencyconverter.customrate", ""));
    }

    private void setDropdownOptions() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String prefsString = prefs.getString("com.desti.currencyconverter.dropdownoptions", "empty");
        if (prefsString.equals("empty")) {
            dropdownOptions = getResources().getStringArray(R.array.default_currencies);
        } else {
            dropdownOptions = stringToArray(prefsString);
        }
    }

    private boolean checkEmptyDropdownOptions() {
        if (dropdownOptions.length == 0) {
            Toast.makeText(MainActivity.this, R.string.no_options, Toast.LENGTH_LONG).show();
            return true;
        }   else {
            return false;
        }
    }

    private void setDropdownPreferences() {
        // get spinner default selected preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String fromPref = prefs.getString("com.desti.currencyconverter.from", "empty");
        String toPref = prefs.getString("com.desti.currencyconverter.to", "empty");

        if (dropdownOptions.length == 0) return;

        // no preferences --> defaults to pos 0 and pos 1 respectively
        if (dropdownOptions.length > 1 && (fromPref.equals("empty") && toPref.equals("empty"))) {
            toSpinner.setSelection(1);
        } else {
            List<String> dropdownList = Arrays.asList(dropdownOptions);
            int fromIndex = dropdownList.lastIndexOf(fromPref);
            int toIndex = dropdownList.lastIndexOf(toPref);

            // set spinner positions if possible, otherwise, defaults to pos 0
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

    private double getValue() {
        String valueString = valueEditText.getText().toString();
        if (valueString.equals("")) valueString = "0";
        return Double.parseDouble(valueString);
    }

    private String getTodayString() {
        Calendar calendar = Calendar.getInstance();
        Date todayDate = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(todayDate);
    }

    private String getOneMonthAgoDateString() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        Date oneMonthAgoDate = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(oneMonthAgoDate);
    }

    private String buildConversionUrl(double value) {
        String fromCurr = fromSpinner.getSelectedItem().toString();
        String toCurr = toSpinner.getSelectedItem().toString();
        if (monthCheckBox.isChecked()){
            return String.format("https://api.exchangerate.host/timeseries?start_date=%s&end_date=%s&base=%s&symbols=%s&amount=%.2f", getOneMonthAgoDateString(), getTodayString(), fromCurr, toCurr, value);
        } else {
            return String.format("https://api.exchangerate.host/convert?from=%s&to=%s&amount=%.2f", fromCurr, toCurr, value);
        }
    }

    private double getMonthlyAverage(JSONObject json) throws Exception {
        Double sum = 0.0;
        int count = 0;
        JSONObject jsonRates = json.getJSONObject("rates");
        String key;
        Double amount = 0.0;
        for (Iterator<String> it = jsonRates.keys(); it.hasNext(); ) {
            key = it.next();
            amount = jsonObjectToDouble(jsonRates.getJSONObject(key), toSpinner.getSelectedItem().toString());
            if (amount != null) {
                sum += amount;
                count++;
            }
        }
        if (count != 0) {
            return sum / count;
        } else {
            return 0;
        }
    }

    private double addFee(double value) {
        String feeString = addFeeEditText.getText().toString();
        if (feeString.equals("")) feeString = "0";

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(addFeeEditText.getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("com.desti.currencyconverter.addfee", feeString);
        editor.apply();

        double feePercent = Double.parseDouble(feeString);
        value *= (1+feePercent/100);
        return value;
    }

    private double deductFee(double value) {
        String feeString = deductFeeEditText.getText().toString();
        if (feeString.equals("")) feeString = "0";

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(deductFeeEditText.getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("com.desti.currencyconverter.deductfee", feeString);
        editor.apply();

        double feePercent = Double.parseDouble(feeString);
        value *= (1-feePercent/100);
        return value;
    }

    private void saveCustomRatePreferences(String rateString) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(customRateCheckbox.getContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("com.desti.currencyconverter.customrate", rateString);
        editor.apply();
    }

    private void displayResult(double value) {
        String valueString = Double.toString(value);
        if (valueString.endsWith(".0")) valueString = valueString.substring(0, valueString.length()-2);
        resultTextView.setText(valueString);
        resultLayout.setVisibility(View.VISIBLE);
    }

    private void reset() {
        valueEditText.setText("");
    }

    private String[] stringToArray(String s) {
        if (s.equals("")) return new String[]{};
        return s.split(",");
    }

    public static String arrayToString(String[] sa) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sa.length; i++) {
            sb.append(sa[i]).append(",");
        }
        return sb.toString();
    }

    private Double jsonObjectToDouble(JSONObject obj, String key) {
        try {
            if (obj.has(key) && obj.get(key) != JSONObject.NULL) {
                Object rate = obj.get(key);
                Double rateDouble;

                if (rate instanceof Integer) {
                    rateDouble = (double) ((Integer) rate);
                } else {
                    rateDouble = (double) rate;
                }

                return rateDouble;
            } else {
                return null;
            }
        } catch (JSONException e) {
            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void dismissKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != activity.getCurrentFocus())
            imm.hideSoftInputFromWindow(activity.getCurrentFocus()
                    .getApplicationWindowToken(), 0);
    }
}