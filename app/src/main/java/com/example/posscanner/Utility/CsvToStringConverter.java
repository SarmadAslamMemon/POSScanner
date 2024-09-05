package com.example.posscanner.Utility;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.opencsv.CSVReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



public class CsvToStringConverter {

    public static String convertCsvToJson(InputStream inputStream) throws IOException {
        // Initialize a CSVReader to read the CSV content
        CSVReader reader = new CSVReader(new InputStreamReader(inputStream));

        // Read all lines from the CSV file
        List<String[]> csvData = reader.readAll();

        // Close the CSVReader
        reader.close();

        // Convert the CSV data into a list of JSON objects
        List<JsonObject> jsonData = new ArrayList<>();
        String[] headers = csvData.get(0); // Assuming the first row contains headers

        for (int i = 1; i < csvData.size(); i++) { // Skip header row
            String[] row = csvData.get(i);
            JsonObject jsonObject = new JsonObject();
            for (int j = 0; j < headers.length; j++) {
                jsonObject.addProperty(headers[j], row[j]);
            }
            jsonData.add(jsonObject);
        }


        Gson gson = new Gson();
        return gson.toJson(jsonData);
    }
}
