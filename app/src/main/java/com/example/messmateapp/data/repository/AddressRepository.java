package com.example.messmateapp.data.repository;

import androidx.lifecycle.LiveData;

import com.example.messmateapp.data.model.AddressDto;
import com.example.messmateapp.utils.Resource;

import java.util.List;

/**
 * ✅ Zomato Style Address Repository
 */
public interface AddressRepository {

    /* ================= GET ================= */

    // Get all addresses
    LiveData<Resource<List<AddressDto>>> getAddresses();

    // Get active address (Bill Page)
    LiveData<Resource<AddressDto>> getActiveAddress();


    /* ================= ADD ================= */

    // Add new address (Auto select)
    LiveData<Resource<List<AddressDto>>> addAddress(AddressDto address);


    /* ================= DELETE ================= */

    // Delete address (returns updated list)
    LiveData<Resource<List<AddressDto>>> deleteAddress(String id);


    /* ================= SELECT ================= */

    // Select / Set active address (returns selected)
    LiveData<Resource<AddressDto>> selectAddress(String id);
}
