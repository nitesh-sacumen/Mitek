package com.mitek.tree;

import com.mitek.tree.config.Constants;
import com.mitek.tree.nodes.Review;
import com.mitek.tree.nodes.Selfie;
import com.mitek.tree.util.AccessToken;
import com.mitek.tree.util.VerifyDocument;
import com.sun.identity.authentication.callbacks.HiddenValueCallback;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.ExternalRequestContext;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.security.auth.callback.Callback;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.forgerock.json.JsonValue.*;
import static org.forgerock.openam.auth.node.api.SharedStateConstants.USERNAME;
import static org.mockito.MockitoAnnotations.initMocks;

public class ReviewTest {
    @InjectMocks
    Review review;

    @Mock
    AccessToken accessToken;

    @Mock
    VerifyDocument verifyDocument;

    @BeforeMethod
    public void before() {
        initMocks(this);
    }


    @Test
    public void testReviewWithCallbackIsNotPresent() throws NodeProcessException {
        TreeContext treeContext = buildThreeContext(Collections.emptyList());
        Action action = review.process(treeContext);
        int callbacks  = action.callbacks.size();
        Assert.assertEquals(callbacks,6);
    }

    @Test
    public void testReviewWithIsRetakeTrue() throws NodeProcessException {
        List<Callback> cbList = new ArrayList<>();
        HiddenValueCallback hcb = new HiddenValueCallback("isRetake");
        hcb.setValue("true");
        cbList.add(hcb);
        cbList.add(new HiddenValueCallback("front"));
        cbList.add(new HiddenValueCallback("selfie"));
        cbList.add(new HiddenValueCallback("passport"));
        cbList.add(new HiddenValueCallback("back"));
        TreeContext treeContext = buildThreeContext(cbList);
        Action action = review.process(treeContext);
        Assert.assertNotNull(action);
    }

    @Test
    public void testReviewWithIsRetakeFalse() throws NodeProcessException {
        List<Callback> cbList = new ArrayList<>();
        HiddenValueCallback hcb = new HiddenValueCallback("isRetake");
        hcb.setValue("false");
        cbList.add(hcb);
        cbList.add(new HiddenValueCallback("front"));
        cbList.add(new HiddenValueCallback("selfie"));
        cbList.add(new HiddenValueCallback("passport"));
        cbList.add(new HiddenValueCallback("back"));
        TreeContext treeContext = buildThreeContext(cbList);
        Action action = review.process(treeContext);
        Assert.assertNotNull(action);
    }


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
                field(Constants.CONSENT_DATA, "terms and condition")));
    }
}
