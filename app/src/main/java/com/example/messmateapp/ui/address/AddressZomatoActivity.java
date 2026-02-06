package com.example.messmateapp.ui.address;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmateapp.R;
import com.example.messmateapp.data.repository.AddressRepositoryImpl;

public class AddressZomatoActivity extends AppCompatActivity {

    private ImageView btnBack;
    private LinearLayout btnAdd;

    private RecyclerView rv;
    private ProgressBar progressBar;

    private AddressAdapter adapter;
    private AddressRepositoryImpl repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_address_zomato);

        initViews();

        repository = new AddressRepositoryImpl(this);

        adapter = new AddressAdapter(this);

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        loadAddresses();

        // 🔙 Back
        btnBack.setOnClickListener(v -> finish());

        // ➕ Add Address → Open Map
        btnAdd.setOnClickListener(v ->
                startActivity(
                        new Intent(
                                this,
                                AddAddressActivity.class
                        )
                )
        );
    }


    private void initViews() {

        btnBack = findViewById(R.id.btnBack);
        btnAdd = findViewById(R.id.layoutAddAddress);

        rv = findViewById(R.id.rvAddresses);
        progressBar = findViewById(R.id.progressBar);
    }


    // 📍 LOAD ADDRESSES (Same as AddressListActivity)

    private void loadAddresses() {

        progressBar.setVisibility(View.VISIBLE);

        repository.getAddresses().observe(this, res -> {

            progressBar.setVisibility(View.GONE);

            if (res.isLoading()) return;

            if (res.isSuccess()) {

                adapter.setData(res.getData());

            } else {

                Toast.makeText(
                        this,
                        res.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        loadAddresses(); // refresh
    }
}
