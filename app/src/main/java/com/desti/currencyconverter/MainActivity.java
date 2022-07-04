package com.desti.currencyconverter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

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
                String valueString = valueEditText.getText().toString();
                if (valueString.equals("")) valueString = "0";
                double value = Double.parseDouble(valueString);

                // convert
                value++;

                // add % fee
                if (feeCheckBox.isChecked()) {
                    String feeString = feeEditText.getText().toString();
                    if (feeString.equals("")) feeString = "0";
                    double feePercent = Double.parseDouble(feeString);
                    value *= (1+feePercent/100);
                }

                value = (double) Math.round(value*100)/100;
                valueString = Double.toString(value);
                if (valueString.endsWith(".0")) valueString = valueString.substring(0, valueString.length()-2);

                resultTextView.setText(valueString);
            }
        });
    }
}