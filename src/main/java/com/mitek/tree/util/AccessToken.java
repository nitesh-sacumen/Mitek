package com.mitek.tree.util;

import com.mitek.tree.config.Constants;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Sacumen(www.sacumen.com)
 * This class will generate access token and put it in shared context.
 */
public class AccessToken {
    private static final Logger logger = LoggerFactory.getLogger(AccessToken.class);

    HttpConnectionClient httpConnectionClient;

    @Inject
    public AccessToken(HttpConnectionClient httpConnectionClient) {
        this.httpConnectionClient = httpConnectionClient;
    }

    /**
     * @param context TreeContext object
     * @return Access token
     * @throws NodeProcessException
     */
    public String getAccessToken(TreeContext context) throws NodeProcessException {
        String accessToken;
        JsonValue sharedState = context.sharedState;
        try (CloseableHttpClient httpclient = httpConnectionClient.getHttpClient(context)) {
            HttpPost httpPost = httpConnectionClient.createPostRequest(sharedState.get(Constants.API_URL).asString() + Constants.API_TOKEN_URL);
            addParamsToPostRequest(context, httpPost);
            CloseableHttpResponse response = httpclient.execute(httpPost);
            Integer responseCode = response.getStatusLine().getStatusCode();
            if (responseCode != 200) {
                logger.error("Error while retrieving access token: " + "response code : " + responseCode);
                throw new NodeProcessException("responseCode : " + responseCode);
            }
            HttpEntity entityResponse = response.getEntity();
            String result = EntityUtils.toString(entityResponse);
            JSONObject jsonResponse = new JSONObject(result);
            if (jsonResponse.has("access_token")) {
                accessToken = jsonResponse.getString("access_token");
                return accessToken;
            }else{
                logger.error("Invalid response from get access token API!!");
                throw new NodeProcessException("Invalid response from get access token API!");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new NodeProcessException("Caught exception while generating access token, " + e.getLocalizedMessage());
        }
    }

    /**
     * Adding header parameter to Http post request
     *
     * @param context  TreeContext forgerock SDK Object
     * @param httpPost Http post object
     * @throws UnsupportedEncodingException
     */
    private void addParamsToPostRequest(TreeContext context, HttpPost httpPost) throws UnsupportedEncodingException {
        JsonValue sharedState = context.sharedState;
        String clientId = sharedState.get(Constants.CLIENT_ID).asString();
        String clientSecret = sharedState.get(Constants.CLIENT_SECRET).asString();
        String grantType = sharedState.get(Constants.GRANT_TYPE).asString();
        String scope = sharedState.get(Constants.SCOPE).asString();

        httpPost.addHeader("Accept", "*/*");
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");

        Map<String, String> parameters = new HashMap<>();
        parameters.put("grant_type", grantType);
        parameters.put("scope", scope);
        parameters.put("client_id", clientId);
        parameters.put("client_secret", clientSecret);
        String form = parameters.entrySet().stream().map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8)).collect(Collectors.joining("&"));
        StringEntity stringEntity = new StringEntity(form);
        httpPost.setEntity(stringEntity);
    }
}
