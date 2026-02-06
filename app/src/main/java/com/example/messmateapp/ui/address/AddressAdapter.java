package com.example.messmateapp.ui.address;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmateapp.R;
import com.example.messmateapp.data.model.AddressDto;
import com.example.messmateapp.data.model.DeleteAddressResponse;
import com.example.messmateapp.data.remote.ApiClient;
import com.example.messmateapp.data.remote.ApiService;
import com.example.messmateapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddressAdapter
        extends RecyclerView.Adapter<AddressAdapter.Holder> {

    private final Context context;
    private final List<AddressDto> list = new ArrayList<>();


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
            int position
    ) {

        AddressDto a = list.get(position);


        String full =
                (a.getHouse() != null ? a.getHouse() : "") + ", " +
                        (a.getArea() != null ? a.getArea() : "") + ", " +
                        (a.getCity() != null ? a.getCity() : "") + " - " +
                        (a.getPincode() != null ? a.getPincode() : "");


        h.txt.setText(full);


        /* ================= DEFAULT STYLE ================= */

        if (a.isDefault()) {

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


        /* ================= DELETE (3 DOTS) ================= */

        h.btnMore.setOnClickListener(v -> {

            new AlertDialog.Builder(context)
                    .setTitle("Delete Address")
                    .setMessage("Are you sure you want to delete this address?")
                    .setPositiveButton("Delete", (dialog, which) -> {

                        deleteAddress(a.getId(), position);

                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }


    /* ================= API CALL ================= */

    private void deleteAddress(String addressId, int position) {

        // ✅ Correct SessionManager usage
        SessionManager session = new SessionManager(context);

        String token = "Bearer " + session.getToken();


        ApiService api = ApiClient
                .getClient()
                .create(ApiService.class);


        api.deleteAddress(addressId, token)
                .enqueue(new Callback<DeleteAddressResponse>() {

                    @Override
                    public void onResponse(
                            Call<DeleteAddressResponse> call,
                            Response<DeleteAddressResponse> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()) {

                            // Remove locally
                            list.remove(position);

                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, list.size());

                            Toast.makeText(context,
                                    "Address deleted",
                                    Toast.LENGTH_SHORT).show();

                        } else {

                            Toast.makeText(context,
                                    "Delete failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }


                    @Override
                    public void onFailure(
                            Call<DeleteAddressResponse> call,
                            Throwable t
                    ) {

                        Toast.makeText(context,
                                "Server error",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    /* ================= VIEW HOLDER ================= */

    static class Holder extends RecyclerView.ViewHolder {

        TextView txt;
        ImageView btnMore;


        Holder(View v) {
            super(v);

            txt = v.findViewById(R.id.txtAddress);
            btnMore = v.findViewById(R.id.btnMore); // 3 dots
        }
    }


    @Override
    public int getItemCount() {
        return list.size();
    }
}
