package com.example.messmateapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class CountryAdapter extends ArrayAdapter<CountryModel> {

    public CountryAdapter(@NonNull Context context, @NonNull List<CountryModel> list) {
        super(context, 0, list);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // 👉 TOP selected view
        return createView(position, convertView, parent, true);
    }

    @NonNull
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // 👉 Dropdown list items
        return createView(position, convertView, parent, false);
    }

    private View createView(int position, View convertView, ViewGroup parent, boolean isSelected) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_country, parent, false);
        }

        ImageView flag = convertView.findViewById(R.id.imgFlag);
        TextView code = convertView.findViewById(R.id.tvCode);
        ImageView arrow = convertView.findViewById(R.id.imgArrow);

        CountryModel model = getItem(position);

        if (model != null) {
            flag.setImageResource(model.getFlag());

            if (isSelected) {
                // ✅ CLOSED spinner (TOP)
                code.setVisibility(View.GONE);
                arrow.setVisibility(View.VISIBLE);
            } else {
                // ✅ DROPDOWN list
                code.setVisibility(View.VISIBLE);
                code.setText(model.getCode());
                arrow.setVisibility(View.GONE);
            }
        }

        return convertView;
    }
}
