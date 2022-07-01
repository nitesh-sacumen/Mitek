package com.mitek.tree.nodes;

import com.google.common.collect.ImmutableList;
import com.mitek.tree.config.Constants;
import com.mitek.tree.util.ConsentScript;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
import com.sun.identity.authentication.client.AuthClientUtils;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.callback.TextOutputCallback;
import java.util.ArrayList;
import java.util.List;

import static org.forgerock.openam.auth.node.api.Action.send;

@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass = Consent.Config.class)
public class Consent extends SingleOutcomeNode {


    private Logger logger = LoggerFactory.getLogger(AuthClientUtils.class);

    /**
     * Configuration for the node.
     */
    public interface Config {

    }

    @Inject
    public Consent() {
    }

    List<Callback> cbList = new ArrayList<>();

    private Action collectRegField(String[] consentLines) {
        try {
            logger.debug("*********************Consent node********************");
            ConsentScript consentScript = new ConsentScript();
            ScriptTextOutputCallback scriptTextOutputCallback = new ScriptTextOutputCallback(consentScript.getConsentScript());
            cbList.add(scriptTextOutputCallback);
            TextOutputCallback textOutputCallback;
            for (Integer i = 0; i < consentLines.length; i++) {
                textOutputCallback = new TextOutputCallback(0, consentLines[i]);
                cbList.add(textOutputCallback);
            }
            String[] choices = {"Next"};
            ConfirmationCallback confirmationCallback = new ConfirmationCallback(0, choices, 0);
            cbList.add(confirmationCallback);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return send(ImmutableList.copyOf(cbList)).build();
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        logger.debug("*********************Capture node********************");
        try {
            JsonValue sharedState = context.sharedState;
            if (sharedState.get(Constants.CONSENT_DATA).isNull()) {
                logger.debug("skipping consent node as no data provided");
                return goToNext().build();
            } else if ((!context.getCallback(ConfirmationCallback.class).isEmpty()) && context.getCallback(ConfirmationCallback.class).get().getSelectedIndex() == 0) {
                return goToNext().build();
            } else {
                String consentData = sharedState.get(Constants.CONSENT_DATA).asString();
                String consentLines[] = consentData.split("\\\\n");
                return collectRegField(consentLines);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            throw new NodeProcessException("Exception is: " + e);
        }
    }
}