package com.example.messmateapp.ui.cart;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmateapp.R;
import com.example.messmateapp.data.model.AddressDto;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;


public class AddressBottomSheetAdapter
        extends RecyclerView.Adapter<AddressBottomSheetAdapter.Holder> {


    private final List<AddressDto> list;
    private final OnAddressSelectListener listener;
    private final BottomSheetDialog dialog;

    private int selectedPosition = -1;


    /* ================= Interface ================= */

    public interface OnAddressSelectListener {
        void onAddressSelected(AddressDto address);
    }


    /* ================= Constructor ================= */

    public AddressBottomSheetAdapter(List<AddressDto> list,
                                     OnAddressSelectListener listener,
                                     BottomSheetDialog dialog) {

        this.list = list;
        this.listener = listener;
        this.dialog = dialog;

        // ✅ Prefer default, else first item
        if (list != null && !list.isEmpty()) {

            for (int i = 0; i < list.size(); i++) {

                if (list.get(i).isDefault) {
                    selectedPosition = i;
                    return;
                }
            }

            // fallback
            selectedPosition = 0;
        }
    }


    /* ================= ViewHolder ================= */

    static class Holder extends RecyclerView.ViewHolder {

        TextView tvAddress;
        TextView tvLabel;

        public Holder(@NonNull View itemView) {

            super(itemView);

            tvAddress = itemView.findViewById(R.id.tvAddressItem);
            tvLabel = itemView.findViewById(R.id.tvLabel);
        }
    }


    /* ================= Adapter ================= */

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_address_bottomsheet, parent, false);

        return new Holder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {

        AddressDto address = list.get(position);


        /* ---------- Build Full Address ---------- */

        String fullAddress =
                safe(address.house) + ", " +
                        safe(address.area) + ", " +
                        safe(address.city) + " - " +
                        safe(address.pincode);

        holder.tvAddress.setText(fullAddress);


        /* ---------- Label ---------- */

        if (holder.tvLabel != null) {

            holder.tvLabel.setText(
                    address.label != null && !address.label.isEmpty()
                            ? address.label
                            : "Address"
            );
        }


        /* ---------- Highlight Selected ---------- */

        boolean selected = position == selectedPosition;

        holder.itemView.setBackgroundResource(
                selected
                        ? R.drawable.bg_address_selected
                        : R.drawable.bg_address_normal
        );

        holder.tvAddress.setTypeface(
                null,
                selected ? Typeface.BOLD : Typeface.NORMAL
        );


        /* ---------- Click ---------- */

        holder.itemView.setOnClickListener(v -> {

            if (position == selectedPosition) {
                dialog.dismiss();
                return;
            }

            // 🔥 Do NOT change UI yet, wait for backend
            if (listener != null) {
                listener.onAddressSelected(address);
            }

            // Close sheet (Checkout will refresh UI)
            v.postDelayed(() -> dialog.dismiss(), 200);
        });
    }


    @Override
    public int getItemCount() {

        return list == null ? 0 : list.size();
    }


    /* ================= Utils ================= */

    private String safe(String s) {

        return s == null ? "" : s.trim();
    }
}
