package com.mitek.tree.util;

import com.mitek.tree.config.Constants;
import com.sun.identity.authentication.client.AuthClientUtils;
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
    private static Logger logger = LoggerFactory.getLogger(AuthClientUtils.class);

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
            if (backImageCode != "") {
                JSONObject backImageCodeObject = new JSONObject();
                JSONObject encodedDataObject = new JSONObject();
                encodedDataObject.put("PDF417", backImageCode);
                backImageCodeObject.put("encodedData", encodedDataObject);
                images.put(backImageCodeObject);
            }
            JSONObject obj = new JSONObject();
            obj.put("type", "IdDocument");
            obj.put("images", images);
            JSONArray evidence = new JSONArray();
            evidence.put(obj);
            JSONObject parentObj = new JSONObject();
            if (selfieData.startsWith(Constants.BASE64_STARTS_WITH)) {
                JSONObject selfieObject = new JSONObject();
                selfieObject.put("type", "Biometric");
                selfieObject.put("biometricType", "Selfie");
                String[] selfieImageData = selfieData.split(",");
                selfieObject.put("data", selfieImageData[1]);
                evidence.put(selfieObject);
                JSONObject verifications = new JSONObject();
                verifications.put("faceComparison", true);
                //verifications.put("faceLiveness", true);//confirm getting error face liveness not activated on this account
                JSONObject configuration = new JSONObject();
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
            System.out.println(jsonResponse.toString(4));
            JSONObject findings;
            String referenceId;
            for (Integer i = 1; i <= 30; i++) {
                if (jsonResponse.has("dossierMetadata")) {
                    JSONObject dossierMetadataObj = (JSONObject) jsonResponse.get("dossierMetadata");
                    referenceId = dossierMetadataObj.get("dossierId").toString();
                    sharedState.put(Constants.VERIFICATION_REFERENCE_ID, referenceId);
                    if (jsonResponse.has("findings")) {
                        findings = (JSONObject) jsonResponse.get("findings");
                        Boolean isAuthenticated = (Boolean) findings.get("authenticated");
                        if (isAuthenticated) {
                            sharedState.put(Constants.VERIFICATION_RESULT, Constants.VERIFICATION_SUCCESS);
                        } else {
                            sharedState.put(Constants.VERIFICATION_RESULT, Constants.VERIFICATION_FAILURE);
                        }
                        logger.debug("authentication status is:: " + isAuthenticated);
                        System.out.println("authentication status is:: " + isAuthenticated);
                        break;
                    }
                }
                //400 Bad Request/ 401 Unauthorized/ 403 Forbidden/ 408 Request Timeout/ 415 Unsupported Media Type/
                // 500 Internal Server Error/ 502 Bad Gateway/ 503 Service Unavailable/ 504 Gateway Timeout
                else if (responseCode == 400 || responseCode == 401 || responseCode == 403 || responseCode == 408 || responseCode == 415 || responseCode == 500 || responseCode == 502 || responseCode == 503 || responseCode == 504) {//system error/ retry scenario
                    logger.debug("authentication status is:: Retry");
                    System.out.println("authentication status is:: Retry");
                    sharedState.put(Constants.VERIFICATION_RESULT, Constants.VERIFICATION_RETRY);
                    break;
                }
                Thread.sleep(1000);
            }
        } catch (ConnectTimeoutException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
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
        Integer timeout = 30;
        RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000).setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();
        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        return clientBuilder.setDefaultRequestConfig(config).build();
    }
}
