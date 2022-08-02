package com.mitek.tree.util;

import org.json.JSONArray;
import org.json.JSONObject;

public class Images {
    private JSONObject getBackImageObject(String backImageCode) {
        JSONObject backImageCodeObject = new JSONObject();
        JSONObject encodedDataObject = new JSONObject();
        encodedDataObject.put("PDF417", backImageCode);
        backImageCodeObject.put("encodedData", encodedDataObject);
        return backImageCodeObject;
    }

    private JSONObject getSelfieConfigurationObject() {
        JSONObject configuration = new JSONObject();
        JSONObject verifications = new JSONObject();
        verifications.put("faceComparison", true);
        verifications.put("faceLiveness", true);
        configuration.put("verifications", verifications);
        return configuration;
    }

    private JSONObject getSelfieObject(String selfieData) {
        JSONObject selfieObject = new JSONObject();
        selfieObject.put("type", "Biometric");
        selfieObject.put("biometricType", "Selfie");
        String[] selfieImageData = selfieData.split(",");
        selfieObject.put("data", selfieImageData[1]);
        return selfieObject;
    }

    public JSONObject createParentObject(String passportData, String frontData, String backImageCode, String backData, String selfieData) {
        JSONObject data, parentObj, obj, backDataObject;
        JSONArray images, evidence;
        String[] backImageArray;
        String[] imageData = !passportData.equals("") ? passportData.split(",") : frontData.split(",");
        data = new JSONObject();
        data.put("data", imageData[1]);
        images = new JSONArray();
        images.put(data);
        evidence = new JSONArray();
        parentObj = new JSONObject();
        obj = new JSONObject();
        obj.put("type", "IdDocument");
        if (!backImageCode.equals("") && !backImageCode.startsWith("*")) {
            images.put(getBackImageObject(backImageCode));
        } else if (!backData.equals("")) {
            backImageArray = backData.split(",");
            backDataObject = new JSONObject();
            backDataObject.put("data", backImageArray[1]);
            images.put(backDataObject);
        }
        obj.put("images", images);
        evidence.put(obj);
        if (!selfieData.equals("")) {
            evidence.put(getSelfieObject(selfieData));
            parentObj.put("configuration", getSelfieConfigurationObject());
        }
        parentObj.put("evidence", evidence);
        return parentObj;
    }

}
