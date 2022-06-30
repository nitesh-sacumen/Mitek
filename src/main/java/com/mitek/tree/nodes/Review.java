package com.mitek.tree.nodes;

import com.google.common.collect.ImmutableList;
import com.mitek.tree.config.Constants;
import com.sun.identity.authentication.callbacks.HiddenValueCallback;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
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
import java.net.SocketTimeoutException;
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
        System.out.println("*********************Review node********************");
        JsonValue sharedState = context.sharedState;
        Integer retakeCount;
        if (sharedState.get(Constants.RETAKE_COUNT).isNull()) {
            sharedState.put(Constants.RETAKE_COUNT, 0);
        }
        retakeCount = sharedState.get(Constants.RETAKE_COUNT).asInteger();
        if (!context.getCallback(HiddenValueCallback.class).isEmpty()) {
            String isRetake = context.getCallbacks(HiddenValueCallback.class).get(0).getValue();
            sharedState.put("isRetake", isRetake);
            if (isRetake.equalsIgnoreCase("true")) {
                sharedState.put(Constants.IS_VERIFICATION_REFRESH, true);
                logger.debug("Retaking image.......");
                retakeCount++;
                sharedState.put(Constants.RETAKE_COUNT, retakeCount);
                System.out.println(sharedState.get(Constants.RETAKE_COUNT).asInteger());
                return goTo(ReviewOutcome.Retake).replaceSharedState(sharedState).build();
            } else {
                logger.debug("Submitting image.....");
                System.out.println("Submitting image.....");
                String frontData = context.getCallbacks(HiddenValueCallback.class).get(1).getValue();

                String selfieData = context.getCallbacks(HiddenValueCallback.class).get(2).getValue();
                String passportData = context.getCallbacks(HiddenValueCallback.class).get(3).getValue();
                String backImageCode = sharedState.get(Constants.PDF_417_CODE).asString();
                System.out.println("back image code is:");
                System.out.println(backImageCode);

                String clientId = sharedState.get(Constants.CLIENT_ID).asString();
                String clientSecret = sharedState.get(Constants.CLIENT_SECRET).asString();
                String grantType = sharedState.get(Constants.GRANT_TYPE).asString();
                String scope = sharedState.get(Constants.SCOPE).asString();
                try (CloseableHttpClient httpclient = getHttpClient()) {
                    HttpPost httpPost = createPostRequest(Constants.API_TOKEN_URL);
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
                    System.out.println("Access token response code: " + responseCode);
                    HttpEntity entityResponse = response.getEntity();
                    String result = EntityUtils.toString(entityResponse);
                    if (responseCode != 200) {
                        throw new NodeProcessException("Not able to retrieve access token: " + result);
                    }
                    JSONObject jsonResponse = new JSONObject(result);

                    String accessToken = "";
                    if (jsonResponse.has("access_token")) {
                        accessToken = jsonResponse.getString("access_token");
                    }


                    if (accessToken != "") {
                        httpPost = createPostRequest(Constants.VERIFY_DOCUMENT_API_URL);
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

                        JSONObject obj = new JSONObject();
                        obj.put("type", "IdDocument");
                        obj.put("images", images);


                        JSONArray evidence = new JSONArray();
                        evidence.put(obj);
                        JSONObject passportObj = new JSONObject();

                        if (selfieData.startsWith(Constants.BASE64_STARTS_WITH)) {
                            JSONObject selfieObject = new JSONObject();
                            selfieObject.put("type", "Biometric");
                            selfieObject.put("biometricType", "Selfie");
                            String[] selfieImageData = selfieData.split(",");
                            selfieObject.put("data", selfieImageData[1]);
                            evidence.put(selfieObject);

                            JSONObject verifications = new JSONObject();
                            verifications.put("faceComparison", true);
                            //verifications.put("faceLiveness", true);//confirm getting error face liveness account not activated on this account

                            JSONObject configuration = new JSONObject();
                            configuration.put("verifications", verifications);
                            passportObj.put("configuration", configuration);

                        }
                        passportObj.put("evidence", evidence);
                        httpPost.addHeader("Accept", "application/json");
                        httpPost.addHeader("Content-Type", "application/json");
                        httpPost.addHeader("Authorization", "Bearer " + accessToken);
                        stringEntity = new StringEntity(passportObj.toString());
                        httpPost.setEntity(stringEntity);

                        response = httpclient.execute(httpPost);
                        responseCode = response.getStatusLine().getStatusCode();
                        logger.debug("verify document api response code: " + responseCode);
                        System.out.println("verify document api response code: " + responseCode);

                        entityResponse = response.getEntity();
                        result = EntityUtils.toString(entityResponse);
                        jsonResponse = new JSONObject(result);

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
                            else if (responseCode == 400 || responseCode == 401 || responseCode == 403 ||
                                    responseCode == 408 || responseCode == 415 ||
                                    responseCode == 500 || responseCode == 502 ||
                                    responseCode == 503 || responseCode == 504) {//system error/ retry scenario
                                logger.debug("authentication status is:: Retry");
                                System.out.println("authentication status is:: Retry");
                                sharedState.put(Constants.VERIFICATION_RESULT, Constants.VERIFICATION_RETRY);
                                break;
                            }

                            Thread.sleep(1000);
                        }
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
                    throw new NodeProcessException("Exception is: " + e);
                }

                return goTo(ReviewOutcome.Wait).replaceSharedState(sharedState).build();
            }

        }

        return buildCallbacks(retakeCount);
    }

    private Action buildCallbacks(Integer retakeCount) {
        return send(new ArrayList<>() {{
            add(new ScriptTextOutputCallback(getAuthDataScript(retakeCount)));
            add(new HiddenValueCallback("isRetake"));
            add(new HiddenValueCallback("front"));
            add(new HiddenValueCallback("selfie"));
            add(new HiddenValueCallback("passport"));
            add(new HiddenValueCallback("back"));
        }}).build();

    }

    private String getAuthDataScript(Integer retakeCount) {
        return "document.getElementById('loginButton_0').style.display='none';\n" +
                "if (document.contains(document.getElementById('integratorAutoCaptureButton'))) {\n" +
                "document.getElementById('integratorAutoCaptureButton').remove();\n" +
                "}\n" +
                "if (document.contains(document.getElementById('integratorManualCaptureButton'))) {\n" +
                "document.getElementById('integratorManualCaptureButton').remove();\n" +
                "}\n" +
                "if (document.contains(document.getElementById('frontImage')) || document.contains(document.getElementById('passportImage'))) {\n" +
                "var parentDiv=document.createElement('div');\n" +
                "parentDiv.id='parentDiv';\n" +
                "parentDiv.className='float-container';\n" +
                "var div=document.createElement('div');\n" +
                "div.id='imageContainer';\n" +

                "if (document.contains(document.getElementById('frontImage'))) {\n" +
                "var frontImage = document.getElementById('frontImage').value;\n" +
                "document.getElementById('front').value = frontImage;\n" +
                "var img = document.createElement('img');\n" +
                "img.id='frontImg';\n" +
                "img.style.width='100%';\n" +
                "img.style.height='auto';\n" +
                "img.src = frontImage;\n" +
                "img.className='float-child-image';\n" +
                "div.appendChild(img);" +
                "}\n" +
                "else{\n" +
                "document.getElementById('front').value = '';\n" +
                "}\n" +

                "if (document.contains(document.getElementById('backImageData'))) {\n" +
                "var backImageData = document.getElementById('backImageData').value;\n" +
                "document.getElementById('back').value = backImageData;\n" +
                "var img = document.createElement('img');\n" +
                "img.id='backImg';\n" +
                "img.style.width='100%';\n" +
                "img.style.height='auto';\n" +
                "img.src = backImageData;\n" +
                "img.className='float-child-image';\n" +
                "div.appendChild(img);" +
                "}\n" +
                "else{\n" +
                "document.getElementById('back').value = '';\n" +
                "}\n" +

                "if (document.contains(document.getElementById('passportImage'))) {\n" +
                "var passportImage = document.getElementById('passportImage').value;\n" +
                "document.getElementById('passport').value = passportImage;\n" +
                "var img = document.createElement('img');\n" +
                "img.id='passportImg';\n" +
                "img.style.width='100%';\n" +
                "img.style.height='auto';\n" +
                "img.src = passportImage;\n" +
                "img.className='float-child-image';\n" +
                "div.appendChild(img);" +
                "}\n" +
                "else{\n" +
                "document.getElementById('passport').value = '';\n" +
                "}\n" +
                "if (document.contains(document.getElementById('selfieImage'))) {\n" +
                "var selfieImage = document.getElementById('selfieImage').value;\n" +
                "document.getElementById('selfie').value = selfieImage;\n" +
                "var img1 = document.createElement('img');\n" +
                "img1.id='selfieImg';\n" +
                "img1.style.width='100%';\n" +
                "img1.style.height='auto';\n" +
                "img1.src = selfieImage;\n" +
                "img1.className='float-child-image';\n" +
                "div.appendChild(img1);" +
                "}\n" +
                "else{\n" +
                "document.getElementById('selfie').value = '';\n" +
                "}\n" +
                "var buttonDiv=document.createElement('div');\n" +
                "buttonDiv.id='buttonContainer';\n" +
                "var button = document.createElement('button');\n" +
                "button.id = 'captureRetake';\n" +
                "button.innerHTML = 'Retake'\n" +
                "button.className = 'btn btn-block btn-primary';\n" +
                "if(" + retakeCount + "===3){\n" +
                "button.disabled = true;\n" +
                "};\n" +
                "button.onclick = function() {\n" +
                "document.getElementById('isRetake').value = 'true'\n" +
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
                "buttonDiv.appendChild(button)\n;" +
                "buttonDiv.appendChild(button1)\n;" +
                "parentDiv.appendChild(div);\n" +
                "parentDiv.appendChild(buttonDiv);\n" +
                "document.body.appendChild(parentDiv);\n" +
                "}\n";
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
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000).build();
        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        return clientBuilder.setDefaultRequestConfig(config).build();
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