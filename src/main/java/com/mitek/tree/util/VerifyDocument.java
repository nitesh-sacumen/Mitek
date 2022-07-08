package com.mitek.tree.util;

import com.mitek.tree.config.Constants;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketTimeoutException;

public class VerifyDocument {
    private static final Logger logger = LoggerFactory.getLogger(VerifyDocument.class);

    public void verify(String accessToken, String frontData, String selfieData, String passportData, String backImageCode, TreeContext context) {
        JsonValue sharedState = context.sharedState;
        try (CloseableHttpClient httpclient = getHttpClient()) {
            HttpPost httpPost = createPostRequest(Constants.VERIFY_DOCUMENT_API_URL);
            String[] imageData = null;
            JSONObject data = new JSONObject();
            if (frontData.startsWith(Constants.BASE64_STARTS_WITH)) {
                imageData = frontData.split(",");
            } else if (passportData.startsWith(Constants.BASE64_STARTS_WITH)) {
                imageData = passportData.split(",");
            }
            data.put("data", imageData[1]);
            JSONArray images = new JSONArray();
            images.put(data);
            JSONArray evidence = new JSONArray();
            JSONObject parentObj = new JSONObject();
            JSONObject obj = new JSONObject();
            obj.put("type", "IdDocument");
            if (backImageCode != null) {
                JSONObject backImageCodeObject = new JSONObject();
                JSONObject encodedDataObject = new JSONObject();
                encodedDataObject.put("PDF417", backImageCode);
                backImageCodeObject.put("encodedData", encodedDataObject);
                images.put(backImageCodeObject);
            }
            obj.put("images", images);
            evidence.put(obj);
            JSONObject configuration = new JSONObject();
            if (selfieData.startsWith(Constants.BASE64_STARTS_WITH)) {
                JSONObject selfieObject = new JSONObject();
                selfieObject.put("type", "Biometric");
                selfieObject.put("biometricType", "Selfie");
                String[] selfieImageData = selfieData.split(",");
                selfieObject.put("data", selfieImageData[1]);
                evidence.put(selfieObject);
                JSONObject verifications = new JSONObject();
                verifications.put("faceComparison", true);
                configuration.put("verifications", verifications);
                parentObj.put("configuration", configuration);
            }
            parentObj.put("evidence", evidence);
            httpPost.addHeader("Accept", "application/json");
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.addHeader("Authorization", "Bearer " + accessToken);
            StringEntity stringEntity = new StringEntity(parentObj.toString());
            httpPost.setEntity(stringEntity);
            CloseableHttpResponse response = httpclient.execute(httpPost);
            Integer responseCode = response.getStatusLine().getStatusCode();
            logger.debug("verify document api response code: " + responseCode);
            HttpEntity entityResponse = response.getEntity();
            String result = EntityUtils.toString(entityResponse);
            JSONObject jsonResponse = new JSONObject(result);
            JSONObject findings;
            String referenceId;
            Integer timeoutValue = sharedState.get(Constants.TIMEOUT_VALUE).asInteger();
            for (Integer i = 1; i <= timeoutValue; i++) {
                if (jsonResponse.has("dossierMetadata")) {
                    JSONObject dossierMetadataObj = (JSONObject) jsonResponse.get("dossierMetadata");
                    referenceId = dossierMetadataObj.get("dossierId").toString();
                    sharedState.put(Constants.VERIFICATION_REFERENCE_ID, referenceId);
                    if (jsonResponse.has("evidence")) {
                        try {
                            JSONArray evidenceList = jsonResponse.getJSONArray("evidence");
                            JSONObject evidenceObject;
                            JSONArray imagesList;
                            JSONObject imageObject;
                            String processingStatus;
                            Boolean flag = false;
                            for (Integer j = 0; j < evidenceList.length(); j++) {
                                evidenceObject = evidenceList.getJSONObject(j);
                                if (evidenceObject.has("images")) {
                                    imagesList = evidenceObject.getJSONArray("images");
                                    for (Integer k = 0; k < imagesList.length(); k++) {
                                        imageObject = imagesList.getJSONObject(k);
                                        processingStatus = imageObject.getString("processingStatus");
                                        if (processingStatus != null) {
                                            if (processingStatus.equalsIgnoreCase("Successful")) {
                                                continue;
                                            } else if (processingStatus.equalsIgnoreCase("Failed")) {
                                                flag = true;//case of failed image processing
                                                break;
                                            }
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
                                System.out.println("one or more image processing failed");
                                return;
                            } else {
                                if (jsonResponse.has("findings")) {
                                    findings = (JSONObject) jsonResponse.get("findings");
                                    Boolean isAuthenticated = (Boolean) findings.get("authenticated");
                                    if (isAuthenticated) {
                                        sharedState.put(Constants.VERIFICATION_RESULT, Constants.VERIFICATION_SUCCESS);
                                    } else {
                                        sharedState.put(Constants.VERIFICATION_RESULT, Constants.VERIFICATION_FAILURE);
                                    }
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
                //400 Bad Request/ 401 Unauthorized/ 403 Forbidden/ 408 Request Timeout/ 415 Unsupported Media Type/
                // 500 Internal Server Error/ 502 Bad Gateway/ 503 Service Unavailable/ 504 Gateway Timeout
                else if (responseCode == 400 || responseCode == 401 || responseCode == 403 || responseCode == 408 || responseCode == 415 || responseCode == 500 || responseCode == 502 || responseCode == 503 || responseCode == 504) {//system error/ retry scenario
                    logger.debug("error response code is:: " + responseCode);
                    System.out.println("error response code is:: " + responseCode);
                    sharedState.put(Constants.VERIFICATION_RESULT, Constants.VERIFICATION_RETRY);
                    break;
                }
                Thread.sleep(1000);
            }
        } catch (ConnectTimeoutException | SocketTimeoutException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public CloseableHttpClient getHttpClient() {
        return buildDefaultClient();
    }

    public HttpPost createPostRequest(String url) {
        return new HttpPost(url);
    }

    public CloseableHttpClient buildDefaultClient() {
        logger.debug("requesting http client connection client open");
        Integer timeout = Constants.REQUEST_TIMEOUT;
        RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000).setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();
        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        return clientBuilder.setDefaultRequestConfig(config).build();
    }
}
