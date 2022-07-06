package com.desti.currencyconverter;


import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CurrencyListAdapter extends RecyclerView.Adapter<CurrencyListAdapter.ViewHolder> {

    // TODO: check if static is ok
    private static List<CurrencyModel> currencyModelList;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox checkBox;

        public ViewHolder(View view) {
            super(view);
            checkBox = (CheckBox) view.findViewById(R.id.currency_checkbox);
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CurrencyModel m = currencyModelList.get(getAdapterPosition());
                    m.setChecked(checkBox.isChecked());
                    currencyModelList.set(getAdapterPosition(), m);
                }
            });
        }

        public CheckBox getCheckBox() {
            return checkBox;
        }
    }

    public CurrencyListAdapter(List<CurrencyModel> dataSet) {
        currencyModelList = dataSet;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.currency_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        final CurrencyModel item = currencyModelList.get(position);
        viewHolder.getCheckBox().setText(item.getCurrency());
        viewHolder.getCheckBox().setChecked(item.isChecked());
    }

    @Override
    public int getItemCount() {
        return currencyModelList.size();
    }
}
