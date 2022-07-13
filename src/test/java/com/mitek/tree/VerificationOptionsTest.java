package com.mitek.tree;

import com.mitek.tree.config.Constants;
import com.mitek.tree.nodes.VerificationOptions;
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
import javax.security.auth.callback.ChoiceCallback;
import javax.security.auth.callback.ConfirmationCallback;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.forgerock.json.JsonValue.*;
import static org.forgerock.openam.auth.node.api.SharedStateConstants.USERNAME;
import static org.mockito.MockitoAnnotations.initMocks;

public class VerificationOptionsTest {
    @InjectMocks
    VerificationOptions verificationOptions;

    @BeforeMethod
    public void before() {
        initMocks(this);
    }


    @Test
    public void testVerificationOptionsCallbackWithCallbackIsNotPresent() throws NodeProcessException {
        TreeContext treeContext = buildThreeContext(Collections.emptyList(),false);
        Action action = verificationOptions.process(treeContext);
        int callbacks  = action.callbacks.size();
        Assert.assertEquals(callbacks,2);
    }

    @Test
    public void testVerificationOptionsWithRefershTrue() throws NodeProcessException {
        TreeContext treeContext = buildThreeContext(Collections.emptyList(),true);
        Action action = verificationOptions.process(treeContext);
        int callbacks  = action.callbacks.size();
        Assert.assertEquals(callbacks,1);
    }

    @Test
    public void testVerificationOptionsCallbackWithPassportChoice() throws NodeProcessException {
        List<Callback> cbList = new ArrayList<>();
        String[] choices = {"Passport", "DL/ID"};
        ChoiceCallback ccb = new ChoiceCallback("Which type of document would you like to submit?", choices, 0, false);
        ccb.setSelectedIndex(0);
        cbList.add(ccb);
        String[] submitButton = {"Next"};
        cbList.add(new ConfirmationCallback(0, submitButton, 0));
        TreeContext treeContext = buildThreeContext(cbList,false);
        Action action = verificationOptions.process(treeContext);
        Assert.assertEquals(treeContext.sharedState.get(Constants.VERIFICATION_CHOICE).asString(),Constants.PASSPORT_VERIFICATION_OPTION);
        Assert.assertNotNull(action);
    }

    @Test
    public void testVerificationOptionsCallbackWithDLChoice() throws NodeProcessException {
        List<Callback> cbList = new ArrayList<>();
        String[] choices = {"Passport", "DL/ID"};
        ChoiceCallback ccb = new ChoiceCallback("Which type of document would you like to submit?", choices, 0, false);
        ccb.setSelectedIndex(1);
        cbList.add(ccb);
        String[] submitButton = {"Next"};
        cbList.add(new ConfirmationCallback(0, submitButton, 0));
        TreeContext treeContext = buildThreeContext(cbList,false);
        Action action = verificationOptions.process(treeContext);
        Assert.assertEquals(treeContext.sharedState.get(Constants.VERIFICATION_CHOICE).asString(),Constants.DOCUMENT_VERIFICATION_OPTION);
        Assert.assertNotNull(action);
    }

    @Test
    public void testVerificationOptionsCallbackWithDefaultChoice() throws NodeProcessException {
        List<Callback> cbList = new ArrayList<>();
        String[] choices = {"Passport", "DL/ID"};
        ChoiceCallback ccb = new ChoiceCallback("Which type of document would you like to submit?", choices, 0, false);
        ccb.setSelectedIndex(2);
        cbList.add(ccb);
        String[] submitButton = {"Next"};
        cbList.add(new ConfirmationCallback(0, submitButton, 0));
        TreeContext treeContext = buildThreeContext(cbList,false);
        Exception exception = Assert.expectThrows(NodeProcessException.class, () -> {
            verificationOptions.process(treeContext);
        });

        String expectedMessage = "Exception is: No option selected!!";
        String actualMessage = exception.getMessage();
        Assert.assertEquals(actualMessage,expectedMessage);
    }

    private TreeContext buildThreeContext(List<Callback> callbacks,boolean isRefereshVerification) {
        return new TreeContext(retrieveSharedState(isRefereshVerification), json(object()),
                new ExternalRequestContext.Builder().build(), callbacks
                , Optional.of("mockUserId"));
    }


    private JsonValue retrieveSharedState(boolean isRefereshVerification) {
        return json(object(field(USERNAME, "demo"),
                field(Constants.CLIENT_ID, "clientID"),
                field(Constants.CLIENT_SECRET, "secret"),
                field(Constants.GRANT_TYPE, "testGrant"),
                field(Constants.SCOPE, "test"),
                field(Constants.CONSENT_DATA, "terms and condition"),
                field(Constants.IS_VERIFICATION_REFRESH,isRefereshVerification)));
    }
}
