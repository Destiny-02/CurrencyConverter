package com.desti.currencyconverter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CurrencyListAdapter extends RecyclerView.Adapter<CurrencyListAdapter.ViewHolder> {

    private List<CurrencyModel> currencyModelList;
    private List<CurrencyModel> filteredList;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox checkBox;

        public ViewHolder(View view) {
            super(view);
            checkBox = (CheckBox) view.findViewById(R.id.currency_checkbox);
            checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    // CurrencyModel updatedCM = new CurrencyModel(compoundButton.getText().toString(), isChecked);
                    // filteredList.set(getAdapterPosition(), updatedCM);
                    for (CurrencyModel cm : currencyModelList) {
                        if (cm.getCurrency().equals(compoundButton.getText().toString())) {
                            cm.setChecked(isChecked);
                        }
                    }
                }
            });
        }

        public CheckBox getCheckBox() {
            return checkBox;
        }
    }

    public CurrencyListAdapter(List<CurrencyModel> dataSet) {
        currencyModelList = dataSet;
        filteredList = currencyModelList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.currency_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        final CurrencyModel item = filteredList.get(position);
        viewHolder.getCheckBox().setText(item.getCurrency());
        viewHolder.getCheckBox().setChecked(item.isChecked());
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String text) {
        filteredList = new ArrayList<>();

        for (CurrencyModel cm : currencyModelList) {
            if (cm.getCurrency().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(cm);
            }
        }
        notifyDataSetChanged();
    }
}
