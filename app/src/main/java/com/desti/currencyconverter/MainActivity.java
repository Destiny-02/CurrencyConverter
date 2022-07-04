package com.desti.currencyconverter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import okhttp3.*;

public class MainActivity extends AppCompatActivity {
    private Spinner fromSpinner, toSpinner;
    private EditText valueEditText, feeEditText;
    private CheckBox feeCheckBox;
    private Button convertButton;
    private TextView resultTextView;

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
        convertButton = findViewById(R.id.convert_button);
        resultTextView = findViewById(R.id.result_text_view);

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
                    String url = String.format("https://api.apilayer.com/fixer/convert?to=%s&from=%s&amount=%.5f", fromCurr, toCurr, value);
                    Request request = new Request.Builder()
                            .url(url)
                            .addHeader("apikey", "7RwUti3ZWUIJAcFfCK0YLHYRuPQFJXUK")
                            .method("GET", null)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseString = response.body().string();
                    JSONParser parser = new JSONParser();
                    JSONObject json = (JSONObject) parser.parse(responseString);
                    value = (Double) json.get("result");
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
    }
}