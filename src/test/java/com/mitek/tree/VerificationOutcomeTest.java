package com.mitek.tree;

import com.mitek.tree.config.Constants;
import com.mitek.tree.nodes.VerificationOutcome;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.forgerock.json.JsonValue.*;
import static org.forgerock.openam.auth.node.api.SharedStateConstants.USERNAME;
import static org.mockito.MockitoAnnotations.initMocks;

public class VerificationOutcomeTest {
    @InjectMocks
    VerificationOutcome verificationOutcome;

    @BeforeMethod
    public void before() {
        initMocks(this);
    }


    @Test
    public void testVerificationOutcomeWithSuccessResult() throws NodeProcessException {
        TreeContext treeContext = buildThreeContext(Collections.emptyList(),Constants.VERIFICATION_SUCCESS);
        Action action = verificationOutcome.process(treeContext);
        String outcome  = action.outcome;
        Assert.assertEquals(outcome,"SUCCESS");
    }

    @Test
    public void testVerificationOutcomeWithFailureResult() throws NodeProcessException {
        TreeContext treeContext = buildThreeContext(Collections.emptyList(),Constants.VERIFICATION_FAILURE);
        Action action = verificationOutcome.process(treeContext);
        String outcome  = action.outcome;
        Assert.assertEquals(outcome,"FAILURE");
    }

    @Test
    public void testVerificationOutcomeWithRetryResult() throws NodeProcessException {
        TreeContext treeContext = buildThreeContext(Collections.emptyList(),Constants.VERIFICATION_RETRY);
        Action action = verificationOutcome.process(treeContext);
        String outcome  = action.outcome;
        Assert.assertEquals(outcome,"RETRY");
    }



    private TreeContext buildThreeContext(List<Callback> callbacks, String verificationResult) {
        return new TreeContext(retrieveSharedState(verificationResult), json(object()),
                new ExternalRequestContext.Builder().build(), callbacks
                , Optional.of("mockUserId"));
    }

    private JsonValue retrieveSharedState(String verificationResult) {
        return json(object(field(USERNAME, "demo"),
                field(Constants.CLIENT_ID, "clientID"),
                field(Constants.CLIENT_SECRET, "secret"),
                field(Constants.GRANT_TYPE, "testGrant"),
                field(Constants.SCOPE, "test"),
                field(Constants.CONSENT_DATA, "terms and condition"),
                field(Constants.VERIFICATION_RESULT,verificationResult)));
    }
}
