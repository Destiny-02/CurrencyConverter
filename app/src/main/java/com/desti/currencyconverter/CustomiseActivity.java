package com.desti.currencyconverter;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CustomiseActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CurrencyListAdapter currencyListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customise);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        View v = findViewById(android.R.id.content).getRootView();
        recyclerView = v.findViewById(R.id.currency_recycler_view);
        currencyListAdapter = new CurrencyListAdapter(getCurrencyList());
        recyclerView.setAdapter(currencyListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private String[] getCurrencyList() {
        return new String[] {"apple", "chicken", "penguin"};
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
