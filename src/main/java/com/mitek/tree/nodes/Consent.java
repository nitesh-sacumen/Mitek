package com.mitek.tree.nodes;

import com.google.common.collect.ImmutableList;
import com.mitek.tree.config.Constants;
import com.mitek.tree.util.ConsentScript;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
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

/**
 * @author Saucmen(www.sacumen.com) Consent text node with
 * single outcome. This node will present consent to user and this will be same for all the flows.
 */
@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass = Consent.Config.class)
public class Consent extends SingleOutcomeNode {


    private static final Logger logger = LoggerFactory.getLogger(Consent.class);

    /**
     * Configuration for the node.
     */
    public interface Config {

    }

    @Inject
    public Consent() {
    }


    /**
     * @param consentLines Consent text
     * @return Action, Which will redirect to next action.
     */
    private Action collectRegField(String[] consentLines) {
        List<Callback> cbList = new ArrayList<>();
        ScriptTextOutputCallback scriptTextOutputCallback = new ScriptTextOutputCallback(ConsentScript.getConsentScript());
        cbList.add(scriptTextOutputCallback);
        TextOutputCallback textOutputCallback;
        for (String consentLine : consentLines) {
            textOutputCallback = new TextOutputCallback(0, consentLine);
            cbList.add(textOutputCallback);
        }
        String[] choices = {"Next"};
        cbList.add(new ConfirmationCallback(0, choices, 0));
        return send(ImmutableList.copyOf(cbList)).build();
    }

    /**
     * @param context
     * @return Action, Which will redirect to next action.
     */
    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        logger.debug("*********************Consent node********************");
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
    }
}