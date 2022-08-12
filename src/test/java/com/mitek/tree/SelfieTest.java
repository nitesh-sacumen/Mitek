package com.mitek.tree;

import com.mitek.tree.config.Constants;
import com.mitek.tree.nodes.Selfie;
import com.sun.identity.authentication.callbacks.HiddenValueCallback;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.ExternalRequestContext;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.mockito.InjectMocks;
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

public class SelfieTest {
    @InjectMocks
    Selfie selfie;

    @BeforeMethod
    public void before() {
        initMocks(this);
    }


    @Test
    public void testSelfieWithCallbackIsNotPresent() throws NodeProcessException {
        TreeContext treeContext = buildThreeContext(Collections.emptyList());
        Action action = selfie.process(treeContext);
        int callbacks  = action.callbacks.size();
        Assert.assertEquals(callbacks,3);
    }

    @Test
    public void testSelfieWithCallbacks() throws NodeProcessException {
        List<Callback> cbList = new ArrayList<>();
        HiddenValueCallback hcb = new HiddenValueCallback("captureSelfieResponse");
        hcb.setValue("imageData");
        cbList.add(hcb);
        TreeContext treeContext = buildThreeContext(cbList);
        Action action = selfie.process(treeContext);
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
