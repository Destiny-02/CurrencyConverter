package com.desti.currencyconverter;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
    }

    private String[] getCurrencyList() {
        String[] symbolsArray = new String[] {};
        try {
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            String url = "https://api.exchangerate.host/symbols";
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            Response response = client.newCall(request).execute();
            String responseString = response.body().string();
            JSONObject json = new JSONObject(responseString).getJSONObject("symbols");
            ArrayList<String> keys = new ArrayList<>();
            String key;
            for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                key = it.next();
                keys.add(key);
            }
            symbolsArray = keys.toArray(new String[0]);
        } catch (Exception e) {
            Toast.makeText(CustomiseActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            return new String[] {};
        }
        return symbolsArray;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
