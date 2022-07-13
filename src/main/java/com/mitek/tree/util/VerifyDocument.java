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
        JSONObject data, parentObj, obj, jsonResponse;
        JSONArray images, evidence;
        Integer responseCode;
        if (frontData.startsWith(Constants.BASE64_STARTS_WITH) || passportData.startsWith(Constants.BASE64_STARTS_WITH)) {
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
                images.put(Images.getBackImageObject(backImageCode));
            }
            obj.put("images", images);
            evidence.put(obj);
            if (selfieData.startsWith(Constants.BASE64_STARTS_WITH)) {
                evidence.put(Images.getSelfieObject(selfieData));
                parentObj.put("configuration", Images.getSelfieConfigurationObject());
            }
            parentObj.put("evidence", evidence);
            HttpConnectionClient httpConnectionClient = new HttpConnectionClient();
            HttpPost httpPost = httpConnectionClient.createPostRequest(sharedState.get(Constants.API_URL).asString() + Constants.VERIFY_DOCUMENT_API_URL);
            httpPost.addHeader("Accept", "application/json");
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.addHeader("Authorization", "Bearer " + accessToken);
            try (CloseableHttpClient httpclient = httpConnectionClient.getHttpClient()) {
                StringEntity stringEntity = new StringEntity(parentObj.toString());
                httpPost.setEntity(stringEntity);
                CloseableHttpResponse response = httpclient.execute(httpPost);
                responseCode = response.getStatusLine().getStatusCode();
                logger.debug("verify document api response code: " + responseCode);
                HttpEntity entityResponse = response.getEntity();
                String result = EntityUtils.toString(entityResponse);
                jsonResponse = new JSONObject(result);
            } catch (ConnectTimeoutException | SocketTimeoutException e) {
                logger.error(e.getMessage());
                throw new NodeProcessException("Exception is: " + e);
            } catch (Exception e) {
                logger.error(e.getMessage());
                throw new NodeProcessException("Exception is: " + e);
            }


            Integer timeoutValue = sharedState.get(Constants.TIMEOUT_VALUE).asInteger();
            Boolean flag;
            for (Integer i = 1; i <= timeoutValue; i++) {
                //400 Bad Request/ 401 Unauthorized/ 403 Forbidden/ 408 Request Timeout/ 415 Unsupported Media Type/
                // 500 Internal Server Error/ 502 Bad Gateway/ 503 Service Unavailable/ 504 Gateway Timeout
                if (responseCode == 400 || responseCode == 401 || responseCode == 403 || responseCode == 408
                        || responseCode == 415 || responseCode == 500 || responseCode == 502 || responseCode == 503
                        || responseCode == 504) {
                    logger.debug("error response code is:: " + responseCode);
                    sharedState.put(Constants.VERIFICATION_RESULT, Constants.VERIFICATION_RETRY);
                    break;
                } else {
                    flag = VerifyApiResponse.checkResponse(jsonResponse, context);
                    if (flag) {
                        break;
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    throw new NodeProcessException("Exception is: " + e);
                }
            }
        }
    }
}
