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

@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass = VerificationFailure.Config.class)
public class VerificationFailure extends SingleOutcomeNode {
    VerificationFailureScript verificationFailureScript = new VerificationFailureScript();
    private Logger logger = LoggerFactory.getLogger(AuthClientUtils.class);

    /**
     * Configuration for the node.
     */
    public interface Config {

    }

    List<Callback> cbList = new ArrayList<>();

    private Action collectRegField(TreeContext context) {
        try {
            TextOutputCallback label1 = new TextOutputCallback(0, "Verification Pending");
            cbList.add(label1);
            TextOutputCallback label2 = new TextOutputCallback(0, "More details are needed to complete your verification");
            cbList.add(label2);
            JsonValue sharedState = context.sharedState;
            if (sharedState.get(Constants.VERIFICATION_REFERENCE_ID).isNotNull()) {
                String referenceId = "";
                referenceId = "Reference Id # " + sharedState.get(Constants.VERIFICATION_REFERENCE_ID).asString();
                TextOutputCallback label3 = new TextOutputCallback(0, referenceId);
                cbList.add(label3);
            }
            TextOutputCallback label4 = new TextOutputCallback(0, "Please contact our customer support team.");
            cbList.add(label4);
            TextOutputCallback label5 = new TextOutputCallback(0, "(BDO-123-4567)");
            cbList.add(label5);
            String[] choices = {"Call Support"};
            ConfirmationCallback confirmationCallback = new ConfirmationCallback(0, choices, 0);
            cbList.add(confirmationCallback);
            ScriptTextOutputCallback scriptTextOutputCallback = new ScriptTextOutputCallback(verificationFailureScript.getVerificationFailureScript());
            cbList.add(scriptTextOutputCallback);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return send(ImmutableList.copyOf(cbList)).build();
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        logger.debug("*********************VerificationFailure node********************");
        try {
            if ((context.hasCallbacks())) {
                return goToNext().build();
            } else {
                return collectRegField(context);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            throw new NodeProcessException("Exception is: " + e);
        }
    }
}