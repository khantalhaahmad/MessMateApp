package com.example.messmateapp.ui.address;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmateapp.R;
import com.example.messmateapp.data.model.AddressDto;

import java.util.ArrayList;
import java.util.List;

public class AddressAdapter
        extends RecyclerView.Adapter<AddressAdapter.Holder> {

    private final Context context;
    private List<AddressDto> list = new ArrayList<>();


    public AddressAdapter(Context context) {
        this.context = context;
    }


    public void setData(List<AddressDto> data) {

        list.clear();

        if (data != null) {
            list.addAll(data);
        }

        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public Holder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_address, parent, false);

        return new Holder(v);
    }


    @Override
    public void onBindViewHolder(
            @NonNull Holder h,
            int i
    ) {

        AddressDto a = list.get(i);


        String full =
                a.house + ", " +
                        a.area + ", " +
                        a.city + " - " +
                        a.pincode;


        h.txt.setText(full);


        if (a.isDefault) {

            // ⭐ Highlight Default Address

            h.txt.setTypeface(null, Typeface.BOLD);

            h.txt.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_home,
                    0,
                    0,
                    0
            );

            h.txt.setCompoundDrawablePadding(8);

        } else {

            h.txt.setTypeface(null, Typeface.NORMAL);

            h.txt.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, 0, 0
            );
        }
    }


    @Override
    public int getItemCount() {
        return list.size();
    }


    static class Holder extends RecyclerView.ViewHolder {

        TextView txt;

        Holder(View v) {
            super(v);

            txt = v.findViewById(R.id.txtAddress);
        }
    }
}
