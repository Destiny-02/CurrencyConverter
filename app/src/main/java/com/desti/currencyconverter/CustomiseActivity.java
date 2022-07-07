package com.desti.currencyconverter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CustomiseActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CurrencyListAdapter currencyListAdapter;
    private List<CurrencyModel> currencyModelList;
    private List<String> dropdownOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            dropdownOptions = Arrays.asList(extras.getStringArray("dropdown"));
        } else {
            dropdownOptions = new ArrayList<>();
        }
        initCurrencyList();

        setContentView(R.layout.activity_customise);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        View v = findViewById(android.R.id.content).getRootView();
        recyclerView = v.findViewById(R.id.currency_recycler_view);
        currencyListAdapter = new CurrencyListAdapter(currencyModelList);
        recyclerView.setAdapter(currencyListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
    }

    private void initCurrencyList() {
        currencyModelList = new ArrayList<>();
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
            String key;
            for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                key = it.next();
                boolean isChecked = false;
                String description = json.getJSONObject(key).getString("description");
                if (dropdownOptions.contains(key)) {
                    isChecked = true;
                }
                currencyModelList.add(new CurrencyModel(key, description, isChecked));
            }
        } catch (UnknownHostException e) {
            Toast.makeText(CustomiseActivity.this, R.string.no_wifi_message, Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            Toast.makeText(CustomiseActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            return;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public String[] currencyModelsToStrings(List<CurrencyModel> currencyModels) {
        List<String> currencyStringList = new ArrayList<>();
        for (CurrencyModel cm : currencyModels) {
            if (cm.isChecked()) {
                currencyStringList.add(cm.getCurrency());
            }
        }

        String[] currencyStrings = currencyStringList.toArray(new String[currencyStringList.size()]);
        return currencyStrings;
    }

    @Override
    public void onBackPressed() {
        savePreferences();
        super.onBackPressed();
    }

    private void savePreferences() {
        String[] selectedStringArray = currencyModelsToStrings(currencyModelList);
        String selectedStrings = MainActivity.arrayToString(selectedStringArray);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("com.desti.currencyconverter.dropdownoptions", selectedStrings);
        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.customise_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.search_bar);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currencyListAdapter.filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deselect:
                deselectAll();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void deselectAll() {
        for (int i=0; i<currencyModelList.size(); i++) {
            if (currencyModelList.get(i).isChecked()) {
                CurrencyModel updated = currencyModelList.get(i);
                updated.setChecked(false);
                currencyModelList.set(i, updated);
            }
        }
        currencyListAdapter.notifyDataSetChanged();
    }
}
