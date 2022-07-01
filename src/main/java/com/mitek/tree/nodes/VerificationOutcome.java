package com.mitek.tree.nodes;

import com.google.common.collect.ImmutableList;
import com.mitek.tree.config.Constants;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
import com.sun.identity.authentication.client.AuthClientUtils;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.*;
import org.forgerock.util.i18n.PreferredLocales;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.forgerock.openam.auth.node.api.Action.send;

@Node.Metadata(outcomeProvider = VerificationOutcome.MitekOutcomeProvider.class, configClass = VerificationOutcome.Config.class)
public class VerificationOutcome implements Node {

    /**
     * Configuration for the node.
     */
    private static Logger logger = LoggerFactory.getLogger(AuthClientUtils.class);
    private static final String BUNDLE = "com/mitek/tree/nodes/VerificationOutcome";

    public interface Config {
    }

    @Inject
    public VerificationOutcome() {
    }


    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        logger.debug("*********************VerificationOutcome node********************");
        System.out.println("*********************VerificationOutcome node********************");
        try {
            JsonValue sharedState = context.sharedState;
            String verification_result = sharedState.get(Constants.VERIFICATION_RESULT).asString();
            logger.info("Verification result" + verification_result);
            System.out.println("Verification result" + verification_result);
                if (verification_result == Constants.VERIFICATION_SUCCESS) {
                    return goTo(MitekOutcome.SUCCESS).replaceSharedState(sharedState).build();
                } else if (verification_result == Constants.VERIFICATION_FAILURE) {
                    return goTo(MitekOutcome.FAILURE).replaceSharedState(sharedState).build();
                } else {
                    return goTo(MitekOutcome.RETRY).replaceSharedState(sharedState).build();
                }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            throw new NodeProcessException("Exception is: " + e);
        }
    }
    private Action.ActionBuilder goTo(MitekOutcome outcome) {
        return Action.goTo(outcome.name());
    }

    /**
     * The possible outcomes for the VerificationOutcome.
     */
    private enum MitekOutcome {
        /**
         * selection for FAILURE.
         */
        FAILURE,
        /**
         * selection for SUCCESS.
         */
        SUCCESS,
        /**
         * selection for RETRY.
         */
        RETRY,

    }

    /**
     * Defines the possible outcomes from this SymantecOutcomeProvider node.
     */
    public static class MitekOutcomeProvider implements OutcomeProvider {
        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
            ResourceBundle bundle = locales.getBundleInPreferredLocale(VerificationOutcome.BUNDLE,
                    MitekOutcomeProvider.class.getClassLoader());
            return ImmutableList.of(new Outcome(MitekOutcome.SUCCESS.name(), bundle.getString("successOutcome")),
                    new Outcome(MitekOutcome.FAILURE.name(), bundle.getString("failureOutcome")),
                    new Outcome(MitekOutcome.RETRY.name(), bundle.getString("retryOutcome")));
        }
    }
}