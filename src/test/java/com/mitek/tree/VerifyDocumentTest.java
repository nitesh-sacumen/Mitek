package com.mitek.tree;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.mitek.tree.config.Constants;
import com.mitek.tree.util.AccessToken;
import com.mitek.tree.util.VerifyDocument;
import com.sleepycat.je.tree.Tree;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.ExternalRequestContext;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.junit.After;
import org.junit.Rule;
import org.mockito.InjectMocks;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.security.auth.callback.Callback;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.forgerock.json.JsonValue.*;
import static org.forgerock.json.JsonValue.field;
import static org.forgerock.openam.auth.node.api.SharedStateConstants.USERNAME;
import static org.mockito.MockitoAnnotations.initMocks;

public class VerifyDocumentTest {
    @InjectMocks
    private VerifyDocument verifyDocument;

    @Rule
    public WireMockRule wireMockRule;

    String wireMockPort;


    private TreeContext context;

    @BeforeMethod
    public void before() {
        wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());
        wireMockRule.start();
        wireMockPort = String.valueOf(wireMockRule.port());
        initMocks(this);
    }

//    @Test
//    public void testVerifyWithSucccess() throws NodeProcessException {
//        TreeContext treeContext = buildThreeContext(Collections.emptyList());
//        wireMockRule.stubFor(post(WireMock.urlPathMatching("/api/verify/v2/dossier"))
//                .willReturn(aResponse()
//                        .withStatus(200).withHeader("Content-Type", "application/x-www-form-urlencoded")));
//        verifyDocument.verify("test123", "data:front,Image", "data:selfie,Image", "data:passport,Image","data:back,Image",treeContext);
//        JsonValue jsonValue = treeContext.sharedState;
//        Assert.assertEquals(jsonValue.get(Constants.VERIFICATION_RESULT),"test123");
//    }

//    @Test
//    public void testGetAccessTokenWithFailure() throws NodeProcessException {
//        wireMockRule.stubFor(post(WireMock.urlPathMatching("/connect/token"))
//                .willReturn(aResponse()
//                        .withStatus(400)));
//        Exception exception = Assert.expectThrows(NodeProcessException.class, () -> {
//            accessToken.getAccessToken(buildThreeContext(Collections.emptyList()));
//        });
//
//        String expectedMessage = "Caught exception while generating access token, responseCode : 400";
//        String actualMessage = exception.getMessage();
//        Assert.assertEquals(actualMessage,expectedMessage);
//    }


    private TreeContext buildThreeContext(List<Callback> callbacks) {
        return new TreeContext(retrieveSharedState(), json(object()),
                new ExternalRequestContext.Builder().build(), callbacks
                , Optional.of("mockUserId"));
    }

    private JsonValue retrieveSharedState() {
        return json(object(field(USERNAME, "demo"),
                field(Constants.CLIENT_ID, "clientID"),
                field(Constants.CLIENT_SECRET, "secret"),
                field(Constants.GRANT_TYPE, "testGrant"),
                field(Constants.SCOPE, "test"),
                field(Constants.API_URL,"http://localhost:"+wireMockPort)));
    }

    @After
    public void tearDown() {
        wireMockRule.stop();
    }
}
