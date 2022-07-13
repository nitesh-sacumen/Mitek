/**
 * @author Sacumen(www.sacumen.com)
 * This class will send the captured images data to the mitek api for verification
 * and will check for whether the verification was successful/failed,timeout
 */

package com.mitek.tree.util;

import com.mitek.tree.config.Constants;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketTimeoutException;

public class VerifyDocument {
    private static final Logger logger = LoggerFactory.getLogger(VerifyDocument.class);

    public void verify(String accessToken, String frontData, String selfieData, String passportData, String backImageCode, TreeContext context) throws NodeProcessException {
        JsonValue sharedState = context.sharedState;
        JSONObject jsonResponse;
        Integer responseCode;
        if (frontData.startsWith(Constants.BASE64_STARTS_WITH) || passportData.startsWith(Constants.BASE64_STARTS_WITH)) {
            Images images = new Images();
            JSONObject parentObj = images.createParentObject(passportData, frontData, backImageCode, selfieData);
            jsonResponse = verify(context, parentObj, accessToken);
            responseCode = sharedState.get(Constants.VERIFY_RESPONSE_CODE).asInteger();
            if (responseCode == 400 || responseCode == 401 || responseCode == 403 || responseCode == 408
                    || responseCode == 415 || responseCode == 500 || responseCode == 502 || responseCode == 503
                    || responseCode == 504) {
                sharedState.put(Constants.VERIFICATION_RESULT, Constants.VERIFICATION_RETRY);
            } else {
                checkResponse(jsonResponse, context);
            }
        }
    }

    private void checkResponse(JSONObject jsonResponse, TreeContext context) {
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
                } else {
                    if (jsonResponse.has("findings")) {
                        findings = (JSONObject) jsonResponse.get("findings");
                        isAuthenticated = (Boolean) findings.get("authenticated");
                        if (isAuthenticated) {
                            sharedState.put(Constants.VERIFICATION_RESULT, Constants.VERIFICATION_SUCCESS);
                        } else {
                            sharedState.put(Constants.VERIFICATION_RESULT, Constants.VERIFICATION_FAILURE);
                        }
                    }
                }
            }
        }
    }

    private JSONObject verify(TreeContext context, JSONObject parentObj, String accessToken) throws NodeProcessException {
        JSONObject jsonResponse = null;
        JsonValue sharedState = context.sharedState;
        HttpConnectionClient httpConnectionClient = new HttpConnectionClient();
        HttpPost httpPost = httpConnectionClient.createPostRequest(sharedState.get(Constants.API_URL).asString() + Constants.VERIFY_DOCUMENT_API_URL);
        httpPost.addHeader("Accept", "application/json");
        httpPost.addHeader("Content-Type", "application/json");
        httpPost.addHeader("Authorization", "Bearer " + accessToken);
        try (CloseableHttpClient httpclient = httpConnectionClient.getHttpClient(context)) {
            StringEntity stringEntity = new StringEntity(parentObj.toString());
            httpPost.setEntity(stringEntity);
            CloseableHttpResponse response = httpclient.execute(httpPost);
            Integer responseCode = response.getStatusLine().getStatusCode();
            logger.debug("verify document api response code: " + responseCode);
            sharedState.put(Constants.VERIFY_RESPONSE_CODE, responseCode);
            HttpEntity entityResponse = response.getEntity();
            String result = EntityUtils.toString(entityResponse);
            jsonResponse = new JSONObject(result);
        } catch (ConnectTimeoutException | SocketTimeoutException e) {
            logger.error(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new NodeProcessException("Exception is: " + e);
        }
        return jsonResponse;
    }
}
