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
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AccessToken {
    private static Logger logger = LoggerFactory.getLogger(AuthClientUtils.class);
    public String getAccessToken(TreeContext context) {
        JsonValue sharedState = context.sharedState;
        String clientId = sharedState.get(Constants.CLIENT_ID).asString();
        String clientSecret = sharedState.get(Constants.CLIENT_SECRET).asString();
        String grantType = sharedState.get(Constants.GRANT_TYPE).asString();
        String scope = sharedState.get(Constants.SCOPE).asString();
        String accessToken = "";
        try (CloseableHttpClient httpclient = getHttpClient()) {
            HttpPost httpPost = createPostRequest(Constants.API_TOKEN_URL);
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
            CloseableHttpResponse response = httpclient.execute(httpPost);
            Integer responseCode = response.getStatusLine().getStatusCode();
            logger.debug("Access token response code: " + responseCode);
            HttpEntity entityResponse = response.getEntity();
            String result = EntityUtils.toString(entityResponse);
            if (responseCode != 200) {
                throw new NodeProcessException("Not able to retrieve access token: " + result);
            }
            JSONObject jsonResponse = new JSONObject(result);
            if (jsonResponse.has("access_token")) {
                accessToken = jsonResponse.getString("access_token");
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
        return accessToken;
    }

    public CloseableHttpClient getHttpClient() {
        return buildDefaultClient();
    }

    public CloseableHttpClient buildDefaultClient() {
        logger.debug("requesting http client connection client open");
        Integer timeout = 30;
        RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000).setConnectionRequestTimeout(timeout * 1000).setSocketTimeout(timeout * 1000).build();
        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        return clientBuilder.setDefaultRequestConfig(config).build();
    }

    public HttpPost createPostRequest(String url) {
        return new HttpPost(url);
    }
}
