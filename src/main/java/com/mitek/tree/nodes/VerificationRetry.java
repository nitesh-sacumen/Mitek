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

@Node.Metadata(outcomeProvider = VerificationRetry.OutcomeProvider.class, configClass = VerificationRetry.Config.class)
public class VerificationRetry implements Node {
    VerificationRetryScript verificationRetryScript = new VerificationRetryScript();
    private static final String BUNDLE = "com/mitek/tree/nodes/VerificationRetry";
    private Logger logger = LoggerFactory.getLogger(AuthClientUtils.class);

    /**
     * Configuration for the node.
     */
    public interface Config {

    }

    List<Callback> cbList = new ArrayList<>();

    private Action collectRegField(TreeContext context) {
        try {
            TextOutputCallback label1 = new TextOutputCallback(0, "Verification Error");
            cbList.add(label1);
            TextOutputCallback label2 = new TextOutputCallback(0, "Oops! There was an issue on the captured image.");
            cbList.add(label2);
            TextOutputCallback label3 = new TextOutputCallback(0, "We would like you to retry.");
            cbList.add(label3);
            TextOutputCallback label4 = new TextOutputCallback(0, "Tips for retake.");
            cbList.add(label4);
            TextOutputCallback label5 = new TextOutputCallback(0, "Make sure the lighting is proper.");
            cbList.add(label5);
            TextOutputCallback label6 = new TextOutputCallback(0, "Use dark background.");
            cbList.add(label6);
            String[] choices = {"Retry"};
            ConfirmationCallback confirmationCallback = new ConfirmationCallback(0, choices, 0);
            cbList.add(confirmationCallback);
            ScriptTextOutputCallback scriptTextOutputCallback = new ScriptTextOutputCallback(verificationRetryScript.getVerificationRetryScript());
            cbList.add(scriptTextOutputCallback);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return send(ImmutableList.copyOf(cbList)).build();
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        logger.debug("*********************VerificationRetry node********************");
        try {
            JsonValue sharedState = context.sharedState;
            if (sharedState.get(Constants.RETRY_COUNT).isNull()) {
                sharedState.put(Constants.RETRY_COUNT, 0);
            } else if (sharedState.get(Constants.RETRY_COUNT).asInteger() == 3) {
                return goTo(VerificationRetryOutcome.Reject).replaceSharedState(sharedState).build();
            }

            if ((context.hasCallbacks())) {
                Integer retryCount = sharedState.get(Constants.RETRY_COUNT).asInteger();
                retryCount++;
                sharedState.put(Constants.RETRY_COUNT, retryCount);
                sharedState.put(Constants.RETAKE_COUNT, 0);
                return goTo(VerificationRetryOutcome.Retry).replaceSharedState(sharedState).build();
            } else {
                return collectRegField(context);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            throw new NodeProcessException("Exception is: " + e);
        }
    }


    private Action.ActionBuilder goTo(VerificationRetry.VerificationRetryOutcome outcome) {
        return Action.goTo(outcome.name());
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

    public static class OutcomeProvider implements org.forgerock.openam.auth.node.api.OutcomeProvider {
        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
            ResourceBundle bundle = locales.getBundleInPreferredLocale(VerificationRetry.BUNDLE, VerificationRetry.OutcomeProvider.class.getClassLoader());
            return ImmutableList.of(new Outcome(VerificationRetryOutcome.Retry.name(), bundle.getString("retry")),
                    new Outcome(VerificationRetryOutcome.Reject.name(), bundle.getString("reject")));
        }
    }
}