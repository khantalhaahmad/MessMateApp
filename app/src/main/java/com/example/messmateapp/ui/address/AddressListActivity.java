package com.example.messmateapp.ui.address;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmateapp.R;
import com.example.messmateapp.data.repository.AddressRepositoryImpl;

public class AddressListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AddressAdapter adapter;
    private AddressRepositoryImpl repository;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_list);

        recyclerView = findViewById(R.id.rvAddresses);
        progressBar = findViewById(R.id.progressBar);

        /* ✅ NEW: Context Passed */
        repository = new AddressRepositoryImpl(this);

        adapter = new AddressAdapter(this);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerView.setAdapter(adapter);

        /* ➕ ADD BUTTON */
        findViewById(R.id.btnAdd).setOnClickListener(v ->
                startActivity(
                        new Intent(
                                this,
                                AddAddressActivity.class
                        )
                )
        );

        loadAddresses();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 🔄 Refresh when coming back
        loadAddresses();
    }

    /* =================================================
       📍 LOAD ADDRESSES
       ================================================= */

    private void loadAddresses() {

        progressBar.setVisibility(View.VISIBLE);

        repository.getAddresses().observe(this, res -> {

            progressBar.setVisibility(View.GONE);

            if (res.isLoading()) {
                return;
            }

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
}
