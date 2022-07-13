package com.mitek.tree.util;

import com.mitek.tree.config.Constants;
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
        // verifications.put("faceLiveness", true);
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

    public JSONObject createParentObject(String passportData, String frontData, String backImageCode, String selfieData) {
        JSONObject data, parentObj, obj;
        JSONArray images, evidence;
        String[] imageData = passportData.startsWith(Constants.BASE64_STARTS_WITH) ? passportData.split(",") : frontData.split(",");
        data = new JSONObject();
        data.put("data", imageData[1]);
        images = new JSONArray();
        images.put(data);
        evidence = new JSONArray();
        parentObj = new JSONObject();
        obj = new JSONObject();
        obj.put("type", "IdDocument");
        if (backImageCode != null) {
            images.put(getBackImageObject(backImageCode));
        }
        obj.put("images", images);
        evidence.put(obj);
        if (selfieData.startsWith(Constants.BASE64_STARTS_WITH)) {
            evidence.put(getSelfieObject(selfieData));
            parentObj.put("configuration", getSelfieConfigurationObject());
        }
        parentObj.put("evidence", evidence);
        return parentObj;
    }

}
