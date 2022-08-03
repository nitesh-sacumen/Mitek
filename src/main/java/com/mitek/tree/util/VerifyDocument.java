/**
 * @author Sacumen(www.sacumen.com)
 * This class will send the captured images data to the mitek api for verification
 * and will check for whether the verification was successful/failed,timeout
 */

package com.mitek.tree.util;

import com.mitek.tree.config.Constants;
import com.sun.identity.idm.AMIdentity;
import org.apache.commons.lang.exception.ExceptionUtils;
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
import org.forgerock.openam.core.CoreWrapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.SocketTimeoutException;
import java.util.*;

import static org.forgerock.openam.auth.node.api.SharedStateConstants.REALM;

public class VerifyDocument {
    private static final Logger logger = LoggerFactory.getLogger(VerifyDocument.class);
    HttpConnectionClient httpConnectionClient;
    Images images;
    CoreWrapper coreWrapper;


    @Inject
    public VerifyDocument(HttpConnectionClient httpConnectionClient, Images images, CoreWrapper coreWrapper) {
        this.httpConnectionClient = httpConnectionClient;
        this.images = images;
        this.coreWrapper = coreWrapper;
    }

    public void verify(String accessToken, String frontData, String selfieData, String passportData, String backImageCode, String backData, TreeContext context) throws NodeProcessException {
        JsonValue sharedState = context.sharedState;
        JSONObject jsonResponse;
        Integer responseCode;
        if (!frontData.equals("") || !passportData.equals("")) {
            JSONObject parentObj = images.createParentObject(passportData, frontData, backImageCode, backData, selfieData);
            jsonResponse = verify(context, parentObj, accessToken);
            String username = sharedState.get("username").asString();
            if (username != null) {
                try {
                    AMIdentity userIdentity = coreWrapper.getIdentity(username, context.sharedState.get(REALM).asString());
                    if (userIdentity != null) {
                        List<String> list = List.of(jsonResponse.toString());
                        Set<String> verifyResponse = new HashSet<>(list);
                        String response = sharedState.get(Constants.RESPONSE).asString();
                        Map<String, Object> attributesMap = new HashMap<>();
                        attributesMap.put(response, verifyResponse);
                        userIdentity.setAttributes(attributesMap);
                        userIdentity.store();
                    }
                } catch (Exception e) {
                    logger.error(ExceptionUtils.getStackTrace(e));
                }
            }
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
