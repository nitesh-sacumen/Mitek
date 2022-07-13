package com.mitek.tree;

import com.mitek.tree.config.Constants;
import com.mitek.tree.nodes.VerificationFailure;
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
import javax.security.auth.callback.ConfirmationCallback;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.forgerock.json.JsonValue.*;
import static org.forgerock.openam.auth.node.api.SharedStateConstants.USERNAME;
import static org.mockito.MockitoAnnotations.initMocks;

public class VerificationFailureTest {
    @InjectMocks
    VerificationFailure verificationFailure;

    @BeforeMethod
    public void before() {
        initMocks(this);
    }


    @Test
    public void testVerificationFailureCallbackWithCallbackIsNotPresent() throws NodeProcessException {
        TreeContext treeContext = buildThreeContext(Collections.emptyList());
        Action action = verificationFailure.process(treeContext);
        int callbacks  = action.callbacks.size();
        Assert.assertEquals(callbacks,6);
    }

    @Test
    public void testVerificationFailureWithCallbacks() throws NodeProcessException {
        List<Callback> cbList = new ArrayList<>();
        String[] choices = {"Call Support"};
        ConfirmationCallback ccb = new ConfirmationCallback(0, choices, 0);
        cbList.add(ccb);
        TreeContext treeContext = buildThreeContext(cbList);
        Action action = verificationFailure.process(treeContext);
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
