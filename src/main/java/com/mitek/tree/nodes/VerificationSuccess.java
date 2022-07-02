package com.mitek.tree.nodes;

import com.google.common.collect.ImmutableList;
import com.mitek.tree.util.VerificationSuccessScript;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
import com.sun.identity.authentication.client.AuthClientUtils;
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
 * @author Saucmen(www.sacumen.com) Verification Success node with
 * single outcome. This node will render Success message to user.
 */
@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass = VerificationSuccess.Config.class)
public class VerificationSuccess extends SingleOutcomeNode {

    VerificationSuccessScript verificationSuccessScript = new VerificationSuccessScript();
    private Logger logger = LoggerFactory.getLogger(AuthClientUtils.class);

    /**
     * Configuration for the node.
     */
    public interface Config {

    }

    List<Callback> cbList = new ArrayList<>();

    private Action collectRegField(TreeContext context) {
        cbList.add(getTextOutputCallbackObject("Verification Complete"));
        cbList.add(getTextOutputCallbackObject("Thank you!"));
        cbList.add(getTextOutputCallbackObject("Your verification is complete."));
        String[] choices = {"Next"};
        cbList.add(new ConfirmationCallback(0, choices, 0));
        cbList.add(new ScriptTextOutputCallback(verificationSuccessScript.getVerificationSuccessScript()));
        return send(ImmutableList.copyOf(cbList)).build();
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        logger.debug("*********************VerificationSuccess node********************");
        if ((context.hasCallbacks())) {
            return goToNext().build();
        } else {
            return collectRegField(context);
        }
    }

    private TextOutputCallback getTextOutputCallbackObject(String msg) {
        return new TextOutputCallback(0, msg);
    }
}