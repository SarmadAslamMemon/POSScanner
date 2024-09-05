package com.example.posscanner.Model;


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ScannerModel {
    private String barcode;
    private String name;
    private int price;
    private int quantity;

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String toString() {
        return getName() + "               " + getPrice();
    }

    public static List<ScannerModel> parseJsonArray(String jsonArray) {
        List<ScannerModel> modelList = new ArrayList<>();
        try {
            JSONArray jsArray = new JSONArray(jsonArray);
            for (int i = 0; i < jsArray.length(); i++) {
                JSONObject jsonObject = jsArray.getJSONObject(i);
                ScannerModel model = new ScannerModel();
                model.setBarcode(jsonObject.getString("Code"));
                model.setName(jsonObject.getString("Name"));
                model.setPrice(jsonObject.getInt("Price"));
                modelList.add(model);
            }
        } catch (JSONException e) {
            Log.e("ModelScanner", "Error parsing JSON array: " + e.getMessage());
        }
        return modelList;
    }
}

