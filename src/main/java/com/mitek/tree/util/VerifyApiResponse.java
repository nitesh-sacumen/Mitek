package com.mitek.tree.util;

import com.mitek.tree.config.Constants;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VerifyApiResponse {
    private static final Logger logger = LoggerFactory.getLogger(VerifyApiResponse.class);

    public static Boolean checkResponse(JSONObject jsonResponse, TreeContext context) {
        JsonValue sharedState = context.sharedState;
        String referenceId, processingStatus;
        JSONObject dossierMetadataObj, evidenceObject, imageObject, findings;
        JSONArray evidenceList, imagesList;
        Boolean flag, isAuthenticated;

        if (jsonResponse.has("dossierMetadata")) {
            dossierMetadataObj = (JSONObject) jsonResponse.get("dossierMetadata");
            referenceId = dossierMetadataObj.get("dossierId").toString();
            sharedState.put(Constants.VERIFICATION_REFERENCE_ID, referenceId);
            if (jsonResponse.has("evidence")) {
                evidenceList = jsonResponse.getJSONArray("evidence");
                flag = false;
                for (Integer j = 0; j < evidenceList.length(); j++) {
                    evidenceObject = evidenceList.getJSONObject(j);
                    if (evidenceObject.has("images")) {
                        imagesList = evidenceObject.getJSONArray("images");
                        for (Integer k = 0; k < imagesList.length(); k++) {
                            imageObject = imagesList.getJSONObject(k);
                            processingStatus = imageObject.getString("processingStatus");
                            if (processingStatus != null && processingStatus.equalsIgnoreCase("Failed")) {
                                flag = true;//case of failed image processing
                                break;
                            }
                        }
                    }
                    if (flag) {
                        break;
                    }
                }


                if (flag) {
                    sharedState.put(Constants.VERIFICATION_RESULT, Constants.VERIFICATION_RETRY);
                    logger.debug("one or more image processing failed");
                    return true;
                } else {
                    if (jsonResponse.has("findings")) {
                        findings = (JSONObject) jsonResponse.get("findings");
                        isAuthenticated = (Boolean) findings.get("authenticated");
                        if (isAuthenticated) {
                            sharedState.put(Constants.VERIFICATION_RESULT, Constants.VERIFICATION_SUCCESS);
                        } else {
                            sharedState.put(Constants.VERIFICATION_RESULT, Constants.VERIFICATION_FAILURE);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
