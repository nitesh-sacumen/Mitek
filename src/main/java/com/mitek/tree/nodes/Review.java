package com.mitek.tree.nodes;

import com.google.common.collect.ImmutableList;
import com.mitek.tree.config.Constants;
import com.sun.identity.authentication.callbacks.HiddenValueCallback;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
import com.sun.identity.authentication.client.AuthClientUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.util.i18n.PreferredLocales;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.forgerock.openam.auth.node.api.Action.send;

@Node.Metadata(outcomeProvider = Review.ReviewOutcomeProvider.class, configClass = Review.Config.class)
public class Review implements Node {

    private static final String BUNDLE = "com/mitek/tree/nodes/Review";
    private static Logger logger = LoggerFactory.getLogger(AuthClientUtils.class);

    /**
     * Configuration for the node.
     */
    public interface Config {

    }

    @Inject
    public Review() {
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        logger.debug("*********************Review node********************");
        JsonValue sharedState = context.sharedState;
        if (!context.getCallback(HiddenValueCallback.class).isEmpty()) {
            String isRetake = context.getCallbacks(HiddenValueCallback.class).get(0).getValue();
            sharedState.put("isRetake", isRetake);

            if (isRetake.equalsIgnoreCase("true")) {
                sharedState.put(Constants.IS_VERIFICATION_REFRESH, true);
                logger.debug("Retaking image.......");
                return goTo(ReviewOutcome.Retake).replaceSharedState(sharedState).build();
            } else {
                logger.debug("Submitting image.....");
                System.out.println("Submitting image.....");
                String verificationChoice = sharedState.get(Constants.VERIFICATION_CHOICE).asString();
                String frontData = context.getCallbacks(HiddenValueCallback.class).get(1).getValue();
                String selfieData = context.getCallbacks(HiddenValueCallback.class).get(2).getValue();
                String apiUrl = sharedState.get(Constants.API_URL).asString();
                String clientId = sharedState.get(Constants.CLIENT_ID).asString();
                String clientSecret = sharedState.get(Constants.CLIENT_SECRET).asString();
                String grantType = sharedState.get(Constants.GRANT_TYPE).asString();
                String scope = sharedState.get(Constants.SCOPE).asString();

                try (CloseableHttpClient httpclient = getHttpClient()) {
                    HttpPost httpPost = createPostRequest(apiUrl + Constants.API_TOKEN_URL);
                    httpPost.addHeader("Accept", "*/*");
                    httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");

                    Map<String, String> parameters = new HashMap<>();
                    parameters.put("grant_type", grantType);
                    parameters.put("scope", scope);
                    parameters.put("client_id", clientId);
                    parameters.put("client_secret", clientSecret);
                    String form = parameters.entrySet()
                            .stream()
                            .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                            .collect(Collectors.joining("&"));

                    StringEntity stringEntity = new StringEntity(form);
                    httpPost.setEntity(stringEntity);

                    CloseableHttpResponse response = httpclient.execute(httpPost);

                    Integer responseCode = response.getStatusLine().getStatusCode();
                    logger.debug("Access token response code: " + responseCode);

                    HttpEntity entityResponse = response.getEntity();
                    String result = EntityUtils.toString(entityResponse);
                    if (response.getStatusLine().getStatusCode() != 200) {
                        throw new NodeProcessException("Not able to retrieve access token: " + result);
                    }
                    JSONObject jsonResponse = new JSONObject(result);

                    String accessToken = "";
                    if (jsonResponse.has("access_token")) {
                        accessToken = jsonResponse.getString("access_token");
                    }

                    if (accessToken != "") {
                        //code for making api call to verify passport details via base 64 input
                        String[] passportData;
                        logger.debug("setting data......");
                        passportData = frontData.split(",");
                        httpPost = createPostRequest(apiUrl + Constants.API_PASSPORT_URL);

                        JSONObject data = new JSONObject();
                        data.put("data", passportData[1]);

                        JSONArray images = new JSONArray();
                        images.put(data);

                        JSONObject obj = new JSONObject();
                        obj.put("type", "IdDocument");
                        obj.put("images", images);

                        JSONArray evidence = new JSONArray();
                        evidence.put(obj);

                        JSONObject passportObj = new JSONObject();
                        passportObj.put("evidence", evidence);
                        httpPost.addHeader("Accept", "application/json");
                        httpPost.addHeader("Content-Type", "application/json");

                        httpPost.addHeader("Authorization", "Bearer " + accessToken);
                        stringEntity = new StringEntity(passportObj.toString());
                        httpPost.setEntity(stringEntity);

                        response = httpclient.execute(httpPost);
                        Thread.sleep(30);//delay to handle timeout scenario
                        responseCode = response.getStatusLine().getStatusCode();
                        logger.debug("verify api token response code: " + responseCode);

                        entityResponse = response.getEntity();
                        result = EntityUtils.toString(entityResponse);
                        jsonResponse = new JSONObject(result);

                        System.out.println(jsonResponse.toString(4));

                        if (jsonResponse.has("dossierMetadata")) {
                            JSONObject dossierMetadataObj = (JSONObject) jsonResponse.get("dossierMetadata");
                            String referenceId = dossierMetadataObj.get("dossierId").toString();
                            sharedState.put(Constants.VERIFICATION_REFERENCE_ID, referenceId);
                        }

                        if (jsonResponse.has("findings")) {
                            JSONObject findings = (JSONObject) jsonResponse.get("findings");

                            if (findings.has("authenticated")) {//authenticated true/false scenario
                                Boolean isAuthenticated = (Boolean) findings.get("authenticated");
                                if (isAuthenticated) {
                                    sharedState.put(Constants.VERIFICATION_RESULT, Constants.VERIFICATION_SUCCESS);
                                } else {
                                    sharedState.put(Constants.VERIFICATION_RESULT, Constants.VERIFICATION_FAILURE);
                                }
                                logger.debug("passport authentication status is:: " + isAuthenticated);
                                System.out.println("passport authentication status is:: " + isAuthenticated);
                            }
                        }
                        //400 Bad Request/ 401 Unauthorized/ 403 Forbidden/ 408 Request Timeout/ 415 Unsupported Media Type/
                        // 500 Internal Server Error/ 502 Bad Gateway/ 503 Service Unavailable/ 504 Gateway Timeout
                        else if (responseCode == 400 || responseCode == 401 || responseCode == 403 ||
                                responseCode == 408 || responseCode == 415 ||
                                responseCode == 500 || responseCode == 502 ||
                                responseCode == 503 || responseCode == 504) {//system error/ retry scenario
                            logger.debug("passport authentication status is:: Retry");
                            System.out.println("passport authentication status is:: Retry");
                            sharedState.put(Constants.VERIFICATION_RESULT, Constants.VERIFICATION_RETRY);
                        } else {//timeout scenario
                            logger.debug("passport authentication status is:: timeout");
                            System.out.println("passport authentication status is:: timeout");
                            sharedState.put(Constants.VERIFICATION_RESULT, Constants.VERIFICATION_TIMEOUT);
                        }


                    }

                } catch (Exception e) {
                    logger.error(e.getMessage());
                    e.printStackTrace();
                    throw new NodeProcessException("Exception is: " + e);
                }

                return goTo(ReviewOutcome.Wait).replaceSharedState(sharedState).build();
            }

        }

        return buildCallbacks();
    }

    private Action buildCallbacks() {
        return send(new ArrayList<>() {{
            add(new ScriptTextOutputCallback(getAuthDataScript()));
            add(new HiddenValueCallback("isRetake"));
            add(new HiddenValueCallback("front"));
            add(new HiddenValueCallback("selfie"));
        }}).build();

    }

    private String getAuthDataScript() {
        return "document.getElementById('loginButton_0').style.display='none';\n" +
                "document.getElementById('integratorAutoCaptureButton').remove();\n" +
                "document.getElementById('integratorManualCaptureButton').remove();\n" +

                "var parentDiv=document.createElement('div');\n" +
                "parentDiv.id='parentDiv';\n" +
                "parentDiv.className='float-container';\n" +
                "parentDiv.style.marginTop='-20%';\n" +
                "parentDiv.style.overflow='hidden';\n" +
                "var div=document.createElement('div');\n" +
                "div.id='imageContainer';\n" +
                "div.className='float-child-left';\n" +
                "var buttonDiv=document.createElement('div');\n" +
                "buttonDiv.id='buttonContainer';\n" +
                "buttonDiv.className='float-child-right';\n" +

                "var frontImage = document.getElementById('frontImage').value;\n" +
                "document.getElementById('front').value = frontImage;\n" +
                "var img = document.createElement('img');\n" +
                "img.id='frontImg';\n" +
                "img.style.width='100%';\n" +
                "img.style.height='auto';\n" +
                "img.src = frontImage;\n" +

                "var selfieImage = document.getElementById('selfieImage').value;\n" +
                "document.getElementById('selfie').value = selfieImage;\n" +
                "var img1 = document.createElement('img');\n" +
                "img1.id='selfieImg';\n" +
                "img1.style.width='100%';\n" +
                "img1.style.height='auto';\n" +
                "img1.src = selfieImage;\n" +

                "var button = document.createElement('button');\n" +
                "button.id = 'captureRetake';\n" +
                "button.innerHTML = 'Retake'\n" +
                "button.className = 'btn btn-block btn-primary';\n" +
                "button.onclick = function() {\n" +
                "document.getElementById('isRetake').value = 'true'\n" +
                "document.getElementById('loginButton_0').style.display = 'none';\n" +
                "document.getElementById('loginButton_0').click();\n" +
                "};\n" +
                "var button1 = document.createElement('button');\n" +
                "button1.id = 'captureSubmit';\n" +
                "button1.innerHTML = 'Submit'\n" +
                "button1.className = 'btn btn-block btn-primary';\n" +
                "button1.onclick = function() {\n" +
                "document.getElementById('isRetake').value = 'false'\n" +
                "document.getElementById('loginButton_0').click();\n" +
                "};\n" +
                "div.appendChild(img);" +
                "div.appendChild(img1);" +
                "buttonDiv.appendChild(button)\n;" +
                "buttonDiv.appendChild(button1)\n;" +
                "parentDiv.appendChild(div);\n" +
                "parentDiv.appendChild(buttonDiv);\n" +
                "document.body.appendChild(parentDiv);\n";
    }

    public CloseableHttpClient getHttpClient() {
        return buildDefaultClient();
    }

    public HttpPost createPostRequest(String url) {
        return new HttpPost(url);
    }

    public CloseableHttpClient buildDefaultClient() {
        logger.debug("requesting http client connection client open");

        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        return clientBuilder.build();
    }

    private Action.ActionBuilder goTo(Review.ReviewOutcome outcome) {
        return Action.goTo(outcome.name());
    }

    /**
     * The possible outcomes for the Review.
     */
    public enum ReviewOutcome {
        /**
         * selection of Retake.
         */
        Retake,
        /**
         * selection for Wait.
         */
        Wait
    }


    public static class ReviewOutcomeProvider implements org.forgerock.openam.auth.node.api.OutcomeProvider {
        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
            ResourceBundle bundle = locales.getBundleInPreferredLocale(Review.BUNDLE, Review.ReviewOutcomeProvider.class.getClassLoader());
            return ImmutableList.of(new Outcome(ReviewOutcome.Retake.name(), bundle.getString("retake")),
                    new Outcome(ReviewOutcome.Wait.name(), bundle.getString("wait")));
        }
    }
}