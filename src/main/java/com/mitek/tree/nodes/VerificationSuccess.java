package com.mitek.tree.nodes;

import com.google.common.collect.ImmutableList;
import com.mitek.tree.util.VerificationSuccessScript;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
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
 * @author Sacumen(www.sacumen.com) Verification Success node with
 * single outcome. This node will render Success message to user.
 */
@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass = VerificationSuccess.Config.class)
public class VerificationSuccess extends SingleOutcomeNode {

    private static final Logger logger = LoggerFactory.getLogger(VerificationSuccess.class);

    /**
     * Configuration for the node.
     */
    public interface Config {

    }


    /**
     * @return Action, Which will redirect to next action.
     * Display text to user and collect user choice for success.
     */
    private Action collectRegField() {
        List<Callback> cbList = new ArrayList<>();
        cbList.add(getTextOutputCallbackObject("Verification Complete"));
        cbList.add(getTextOutputCallbackObject("Thank you!"));
        cbList.add(getTextOutputCallbackObject("Your verification is complete."));
        String[] choices = {"Next"};
        cbList.add(new ConfirmationCallback(0, choices, 0));
        cbList.add(new ScriptTextOutputCallback(VerificationSuccessScript.getVerificationSuccessScript()));
        return send(ImmutableList.copyOf(cbList)).build();
    }

    /**
     * @param context
     * @return Action, Which will redirect to next action.
     */
    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        logger.debug("*********************VerificationSuccess node********************");
        if ((context.hasCallbacks())) {
            return goToNext().build();
        } else {
            return collectRegField();
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