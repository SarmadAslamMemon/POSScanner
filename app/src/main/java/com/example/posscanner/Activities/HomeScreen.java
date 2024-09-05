package com.example.posscanner.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.posscanner.Model.ScannerModel;
import com.example.posscanner.R;
import com.example.posscanner.Utility.CsvToStringConverter;
import com.example.posscanner.Utility.SessionManager;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeScreen extends AppCompatActivity {
    public static int totalAmt = 0;
    private static String jSonData;
    private static int amount = 0;
    View separator;
    AutoCompleteTextView autoCompleteTextView_searchable;
    boolean check = false;
    SessionManager sm;
    LinearLayout linearDesData, headerLinear;
    TextView totalAmountDisplay;
    Button clearBtn;
    List<ScannerModel> modelList;
    private ActivityResultLauncher<String> mGetContent;
    private Map<String, ScannerModel> scannedItemsMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        sm = new SessionManager(this);
        autoCompleteTextView_searchable = findViewById(R.id.autoCompleteTextView_searchable);
        linearDesData = findViewById(R.id.linearAddData);
        totalAmountDisplay = findViewById(R.id.totalAmount);

        separator = findViewById(R.id.horiSeparator);
        clearBtn = findViewById(R.id.buttonClear);
        clearBtn.setOnClickListener(v -> {
            ClearViews();
        });

        jSonData = sm.getJSonString();
        if (jSonData != null && !jSonData.isEmpty()) {
            Log.e("simson", "if json string is not empty means 2nd:" + jSonData);
            setupDropdowns();
        }
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            if (result != null) {
                showToast("File Uploaded Successfully");
                if (!sm.getJSonString().isEmpty()) {
                    resetUploaded();
                    modelList.clear();

                    Log.e("simson", "size here " + modelList.size());

                }
                handleSelectedFile(result);

            }
        });
    }

    public void scanMe(View view) {
        scanMe();
    }
    public void scanMe() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setOrientationLocked(true); // Allow rotation
        integrator.setPrompt("Scan a barcode");
        integrator.initiateScan();
    }
    // Method to handle the result of the barcode scan
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            if (scanResult.getContents() != null) {
                linearDesData.setVisibility(View.VISIBLE);
                ScannerModel model = showData(scanResult.getContents());
                if (model.getName() != null) {
                    if (!scannedItemsMap.containsKey(model.getBarcode())) {
                        // Item does not exist, add it to the map and create a new view
                        model.setQuantity(1); // Initial quantity is 1
                        addView(model);
                        Log.e("simson", "check inside addview :" + model.getBarcode() + "name" + model.getName());
                        scannedItemsMap.put(model.getBarcode(), model);
                    } else {
                        ScannerModel existingItem = scannedItemsMap.get(model.getBarcode());
                        existingItem.setQuantity(existingItem.getQuantity() + 1);
                        updateItemView(existingItem);
                        Log.e("simson", "check inside updateview  :" + model.getBarcode());

                    }
                    autoCompleteTextView_searchable.setText("");
                } else {
                    showToast("Invalid Bar Code");
                }
            } else {
                showToast("No scan ");
            }
        } else {
            Log.e("simson", "scan result is null");
        }

    }

    private ScannerModel showData(String code) {
        ScannerModel obj = new ScannerModel();
        if (jSonData != null) {

            try {
                JSONArray jsArray = new JSONArray(jSonData);
                if (jsArray != null) {
                    for (int i = 0; i < jsArray.length(); i++) {
                        JSONObject jsObj = jsArray.getJSONObject(i);
                        if (jsObj.getString("Code").equals(code)) {
                            obj.setBarcode(jsObj.getString("Code"));
                            obj.setName(jsObj.getString("Name"));
                            obj.setPrice(jsObj.getInt("Price"));
                            check = true;
                            break; // No need to continue looping if the code is found
                        }
                    }
                    if (!check) {
                        showToast("Data not Found");
                    } else {
                        showToast("Data Found");
                        check = false;
                    }
                } else {
                    showToast("No Particular Data for Code");
                }
            } catch (JSONException e) {
                Log.d("Simson", "JSON exception:" + e.getMessage());
            }
        } else {
            Log.d("Simson", "NO File");
        }
        return obj;
    }


    private void setupDropdowns() {
        Log.e("simson", "string dropdown:" + jSonData);
        modelList = ScannerModel.parseJsonArray(sm.getJSonString());
        ArrayAdapter<ScannerModel> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, modelList);
        autoCompleteTextView_searchable.setAdapter(adapter);
        autoCompleteTextView_searchable.setOnItemClickListener((parent, view, position, id) -> {
            ScannerModel selectedModel = (ScannerModel) parent.getItemAtPosition(position);
            if (scannedItemsMap.containsKey(selectedModel.getBarcode())) {
                ScannerModel existingItem = scannedItemsMap.get(selectedModel.getBarcode());
                existingItem.setQuantity(existingItem.getQuantity() + 1);
                updateItemView(existingItem); // Update the existing view
            } else {
                selectedModel.setQuantity(1); // Initial quantity is 1
                scannedItemsMap.put(selectedModel.getBarcode(), selectedModel);
                addView(selectedModel); // Add a new view
            }

            autoCompleteTextView_searchable.setText("");
        });
    }

    private void pickFile() {
        mGetContent.launch("text/comma-separated-values");
    }


    private void handleSelectedFile(Uri fileUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            String mimeType = getContentResolver().getType(fileUri);
            if (mimeType.equals("text/comma-separated-values")) {
                jSonData = CsvToStringConverter.convertCsvToJson(inputStream);
                if (jSonData != null) {
                    sm.saveJsonString(jSonData);
                    setupDropdowns();
                    showToast("updated the session");
                } else {
                    showToast("Unable to parse JSon");
                }
            } else {
                showToast("file other than CSV");
            }
        } catch (IOException e) {
            Log.e("Simson", e.getMessage());
        }
    }

    public void uploadFile(View view) {
        pickFile();
    }

    void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void addView(ScannerModel item) {

        scannedItemsMap.put(item.getBarcode(), item);

        Log.e("Simson", "check Add View:" + item.getQuantity());
        LinearLayout.LayoutParams layoutParamsProductName = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);
        LinearLayout.LayoutParams layoutParamsProductQuantity = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        LinearLayout.LayoutParams layoutParamsProductPrice = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        LinearLayout.LayoutParams layoutParamsProductTotal = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);

        LinearLayout childLinear = new LinearLayout(this);
        childLinear.setOrientation(LinearLayout.HORIZONTAL);
        childLinear.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        TextView productName = new TextView(this);
        productName.setLayoutParams(layoutParamsProductName);
        productName.setText(item.getName());
        productName.setTextSize(12);
        productName.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        childLinear.addView(productName);

        TextView productQuantity = new TextView(this);
        productQuantity.setLayoutParams(layoutParamsProductQuantity);
        productQuantity.setText(String.valueOf(item.getQuantity()));
        productQuantity.setTextSize(12);
        productQuantity.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        childLinear.addView(productQuantity);

        TextView productPrice = new TextView(this);
        productPrice.setLayoutParams(layoutParamsProductPrice);
        productPrice.setText(String.valueOf(item.getPrice()));
        productPrice.setTextSize(12);
        productPrice.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        childLinear.addView(productPrice);

        TextView productTotal = new TextView(this);
        productTotal.setLayoutParams(layoutParamsProductTotal);
        productTotal.setText(String.valueOf(item.getQuantity() * item.getPrice()));
        productTotal.setTextSize(12);
        productTotal.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        childLinear.addView(productTotal);
        Log.d("simson", "price is" + item.getName() + "price is " + item.getPrice() + "quantity is  " + item.getQuantity());


        linearDesData.addView(childLinear);
        linearDesData.addView(new View(this) {{
            setBackgroundColor(getResources().getColor(R.color.black));
            setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1
            ));
        }});
        updateTotalAmount();
    }
    private void updateItemView(ScannerModel item) {
        for (int i = 0; i < linearDesData.getChildCount(); i++) {
            View childView = linearDesData.getChildAt(i);
            if (childView instanceof LinearLayout) {
                LinearLayout linearLayout = (LinearLayout) childView;
                if (linearLayout.getChildAt(0) instanceof TextView) {
                    TextView productNameTextView = (TextView) linearLayout.getChildAt(0);
                    if (productNameTextView.getText().toString().equals(item.getName())) {
                        TextView productQuantityTextView = (TextView) linearLayout.getChildAt(1);
                        productQuantityTextView.setText(String.valueOf(item.getQuantity()));
                        TextView totalAmountTextView = (TextView) linearLayout.getChildAt(3);
                        totalAmountTextView.setText(String.valueOf(item.getQuantity() * item.getPrice()));
                        Log.d("simson", "IN update quantity is" + item.getName() + "price is " + item.getPrice() + "quantity is  " + item.getQuantity());
                        updateTotalAmount();
                        break;
                    }
                }
            }
        }
    }
    private void updateTotalAmount() {
        int totalAmount = 0;
        for (Map.Entry<String, ScannerModel> entry : scannedItemsMap.entrySet()) {
            ScannerModel item = entry.getValue();
            totalAmount += (item.getPrice() * item.getQuantity());
        }
        totalAmt = totalAmount;
        totalAmountDisplay.setText(String.valueOf(totalAmt));
    }
    private void ClearViews() {
        clearViewExceptHeader();
        showToast("Data Cleared");


    }
    private void resetUploaded() {
        sm.clearJsonString();
        clearViewExceptHeader();


    }
    void clearViewExceptHeader() {
        linearDesData.removeAllViews();
//        int childCount = linearDesData.getChildCount();
//        for (int i = 1; i < childCount; i++) {
//            View childView = linearDesData.getChildAt(i);
//            if (childView != headerLinear) {
//                linearDesData.removeView(childView);
//            }
//            Log.e("simson","childs are:"+linearDesData.getChildCount());
//        }
        totalAmountDisplay.setText(" ");
        autoCompleteTextView_searchable.setHint("Search Product Here");
        autoCompleteTextView_searchable.setFocusable(true);
        totalAmt = 0;
        amount = 0;
        scannedItemsMap.clear();

    }

}














