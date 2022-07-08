package com.mitek.tree.nodes;

import com.google.common.collect.ImmutableList;
import com.mitek.tree.config.Constants;
import com.mitek.tree.util.VerificationRetryScript;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
import com.sun.identity.authentication.client.AuthClientUtils;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.util.i18n.PreferredLocales;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.callback.TextOutputCallback;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.forgerock.openam.auth.node.api.Action.send;

/**
 * @author Saucmen(www.sacumen.com) Verification Retry node with
 * two outcome - Retry and Retake. This node will render retry message to user.
 * Retry - Retry will force user to retry entire flow.
 * Retake - Retake will force user to retake image again.
 */
@Node.Metadata(outcomeProvider = VerificationRetry.OutcomeProvider.class, configClass = VerificationRetry.Config.class)
public class VerificationRetry implements Node {
    VerificationRetryScript verificationRetryScript = new VerificationRetryScript();
    private static final String BUNDLE = "com/mitek/tree/nodes/VerificationRetry";
    private static final Logger logger = LoggerFactory.getLogger(VerificationRetry.class);

    /**
     * Configuration for the node.
     */
    public interface Config {

    }


    /**
     * @param context
     * @return Action, Which will redirect to next action.
     * Display text to user and collect user choice for retry.
     */
    private Action collectRegField(TreeContext context) {
        List<Callback> cbList = new ArrayList<>();
        cbList.add(getTextOutputCallbackObject("Verification Error"));
        cbList.add(getTextOutputCallbackObject("Oops! There was an issue on the captured image."));
        cbList.add(getTextOutputCallbackObject("We would like you to retry."));
        cbList.add(getTextOutputCallbackObject("Tips for retake."));
        cbList.add(getTextOutputCallbackObject("Make sure the lighting is proper."));
        cbList.add(getTextOutputCallbackObject("Use dark background."));
        String[] choices = {"Retry"};
        cbList.add(new ConfirmationCallback(0, choices, 0));
        cbList.add(new ScriptTextOutputCallback(verificationRetryScript.getVerificationRetryScript()));
        return send(ImmutableList.copyOf(cbList)).build();
    }

    /**
     * @param context
     * @return Action, Which will redirect to next action.
     */
    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        logger.debug("*********************VerificationRetry node********************");
        JsonValue sharedState = context.sharedState;
        if (sharedState.get(Constants.RETRY_COUNT).isNull()) {
            sharedState.put(Constants.RETRY_COUNT, 0);
        } else if (sharedState.get(Constants.RETRY_COUNT).asInteger() == Constants.RETRY_COUNT_VALUE) {
            return goTo(VerificationRetryOutcome.Reject).replaceSharedState(sharedState).build();
        }
        if ((context.hasCallbacks())) {
            Integer retryCount = sharedState.get(Constants.RETRY_COUNT).asInteger();
            retryCount++;
            System.out.println("retry"+retryCount);
            sharedState.put(Constants.RETRY_COUNT, retryCount);
            sharedState.put(Constants.RETAKE_COUNT, 0);
            return goTo(VerificationRetryOutcome.Retry).replaceSharedState(sharedState).build();
        } else {
            return collectRegField(context);
        }
    }


    /**
     * @param outcome Node outcome
     * @return Next node
     */
    private Action.ActionBuilder goTo(VerificationRetry.VerificationRetryOutcome outcome) {
        return Action.goTo(outcome.name());
    }

    /**
     * @param msg Message that needs to be rendered to the user.
     * @return Text output callback
     */
    private TextOutputCallback getTextOutputCallbackObject(String msg) {
        return new TextOutputCallback(0, msg);
    }


    /**
     * The possible outcomes for the VerificationRetry.
     */
    public enum VerificationRetryOutcome {
        /**
         * selection of Retry.
         */
        Retry,
        /**
         * selection for Reject.
         */
        Reject
    }


    /**
     * This class will create customized outcome for the node.
     */
    public static class OutcomeProvider implements org.forgerock.openam.auth.node.api.OutcomeProvider {
        /**
         * @param locales        Local property file for configuration.
         * @param nodeAttributes Node attributes for outcomes
         * @return List of possible outcomes.
         */
        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
            ResourceBundle bundle = locales.getBundleInPreferredLocale(VerificationRetry.BUNDLE, VerificationRetry.OutcomeProvider.class.getClassLoader());
            return ImmutableList.of(new Outcome(VerificationRetryOutcome.Retry.name(), bundle.getString("retry")),
                    new Outcome(VerificationRetryOutcome.Reject.name(), bundle.getString("reject")));
        }
    }
}