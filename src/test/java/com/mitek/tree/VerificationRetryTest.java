package com.mitek.tree;

import com.mitek.tree.config.Constants;
import com.mitek.tree.nodes.VerificationOutcome;
import com.mitek.tree.nodes.VerificationRetry;
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

public class VerificationRetryTest {
    @InjectMocks
    VerificationRetry verificationRetry;

    @BeforeMethod
    public void before() {
        initMocks(this);
    }


    @Test
    public void testVerificationRetryWithoutCallbacks() throws NodeProcessException {
        TreeContext treeContext = buildThreeContext(Collections.emptyList(),null);
        Action action = verificationRetry.process(treeContext);
        int callbacks  = action.callbacks.size();
        Assert.assertEquals(callbacks,8);
    }

    @Test
    public void testVerificationRetryWithReject() throws NodeProcessException {
        TreeContext treeContext = buildThreeContext(Collections.emptyList(),3);
        Action action = verificationRetry.process(treeContext);
        String outcome  = action.outcome;
        Assert.assertEquals(outcome,"REJECT");
    }

    @Test
    public void testVerificationRetryWithRetry() throws NodeProcessException {
        List<Callback> cbList = new ArrayList<>();
        String[] choices = {"Retry"};
        cbList.add(new ConfirmationCallback(0, choices, 0));
        TreeContext treeContext = buildThreeContext(cbList,2);
        Action action = verificationRetry.process(treeContext);
        String outcome  = action.outcome;
        Assert.assertEquals(outcome,"RETRY");
    }



    private TreeContext buildThreeContext(List<Callback> callbacks,Integer retryCount) {
        return new TreeContext(retrieveSharedState(retryCount), json(object()),
                new ExternalRequestContext.Builder().build(), callbacks
                , Optional.of("mockUserId"));
    }

    private JsonValue retrieveSharedState(Integer retryCount) {
        return json(object(field(USERNAME, "demo"),
                field(Constants.CLIENT_ID, "clientID"),
                field(Constants.CLIENT_SECRET, "secret"),
                field(Constants.GRANT_TYPE, "testGrant"),
                field(Constants.SCOPE, "test"),
                field(Constants.CONSENT_DATA, "terms and condition"),
                field(Constants.RETRY_COUNT, retryCount),
                field(Constants.MAX_RETRY_COUNT,3)));
    }
}
