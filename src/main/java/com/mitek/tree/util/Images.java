package com.mitek.tree.util;

import org.json.JSONObject;

public class Images {
    public static JSONObject getBackImageObject(String backImageCode) {
        JSONObject backImageCodeObject = new JSONObject();
        JSONObject encodedDataObject = new JSONObject();
        encodedDataObject.put("PDF417", backImageCode);
        backImageCodeObject.put("encodedData", encodedDataObject);
        return backImageCodeObject;
    }

    public static JSONObject getSelfieConfigurationObject() {
        JSONObject configuration = new JSONObject();
        JSONObject verifications = new JSONObject();
        verifications.put("faceComparison", true);
       // verifications.put("faceLiveness", true);
        configuration.put("verifications", verifications);
        return configuration;
    }

    public static JSONObject getSelfieObject(String selfieData) {
        JSONObject selfieObject = new JSONObject();
        selfieObject.put("type", "Biometric");
        selfieObject.put("biometricType", "Selfie");
        String[] selfieImageData = selfieData.split(",");
        selfieObject.put("data", selfieImageData[1]);
        return selfieObject;
    }
}
