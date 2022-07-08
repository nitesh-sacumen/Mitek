package com.mitek.tree.nodes;

import com.google.common.collect.ImmutableList;
import com.mitek.tree.config.Constants;
import com.mitek.tree.util.VerificationFailureScript;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
import com.sun.identity.authentication.client.AuthClientUtils;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.callback.TextOutputCallback;
import java.util.ArrayList;
import java.util.List;

import static org.forgerock.openam.auth.node.api.Action.send;

/**
 * @author Saucmen(www.sacumen.com) Verification Failure node with
 * single outcome. This node will render failure message to user.
 */
@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass = VerificationFailure.Config.class)
public class VerificationFailure extends SingleOutcomeNode {
    VerificationFailureScript verificationFailureScript = new VerificationFailureScript();
    private static final Logger logger = LoggerFactory.getLogger(VerificationFailure.class);

    /**
     * Configuration for the node.
     */
    public interface Config {
    }


    /**
     * @param context
     * @return Action, Which will redirect to next action.
     * Display text to user and collect user choice for customer support or call support.
     */
    private Action collectRegField(TreeContext context) {
        List<Callback> cbList = new ArrayList<>();
        cbList.add(getTextOutputCallbackObject("Verification Pending"));
        cbList.add(getTextOutputCallbackObject("More details are needed to complete your verification"));
        JsonValue sharedState = context.sharedState;
        if (sharedState.get(Constants.VERIFICATION_REFERENCE_ID).isNotNull()) {
            String referenceId = "";
            referenceId = "Reference Id # " + sharedState.get(Constants.VERIFICATION_REFERENCE_ID).asString();
            cbList.add(getTextOutputCallbackObject(referenceId));
        }
        cbList.add(getTextOutputCallbackObject("Please contact our customer support team."));
        cbList.add(getTextOutputCallbackObject("(BDO-123-4567)"));
        String[] choices = {"Call Support"};
        cbList.add(new ConfirmationCallback(0, choices, 0));
        cbList.add(new ScriptTextOutputCallback(verificationFailureScript.getVerificationFailureScript()));
        return send(ImmutableList.copyOf(cbList)).build();
    }

    /**
     * @param context
     * @return Action, Which will redirect to next action.
     */
    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        logger.debug("*********************VerificationFailure node********************");
        if ((context.hasCallbacks())) {
            return goToNext().build();
        } else {
            return collectRegField(context);
        }
    }

    /**
     * @param msg Message that needs to be rendered to the user.
     * @return Text output callback
     */
    private TextOutputCallback getTextOutputCallbackObject(String msg) {
        return new TextOutputCallback(0, msg);
    }
}