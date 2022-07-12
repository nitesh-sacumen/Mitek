package com.mitek.tree.nodes;

import com.google.common.collect.ImmutableList;
import com.mitek.tree.config.Constants;
import com.mitek.tree.util.VerificationOptionsScript;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.*;
import org.forgerock.util.i18n.PreferredLocales;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.ChoiceCallback;
import javax.security.auth.callback.ConfirmationCallback;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static org.forgerock.openam.auth.node.api.Action.send;

/**
 * @author Sacumen(www.sacumen.com) Verification option node with
 * two outcome DL/ID and Passport. This node will get document type from user.
 * DL/ID - This will redirect to DL/ID flow.
 * Passport - This will redirect to passport flow.
 */
@Node.Metadata(outcomeProvider = VerificationOptions.VerificationOptionsOutcomeProvider.class, configClass = VerificationOptions.Config.class)
public class VerificationOptions implements Node {

    private static final Logger logger = LoggerFactory.getLogger(VerificationOptions.class);
    private static final String BUNDLE = "com/mitek/tree/nodes/VerificationOptions";

    /**
     * Configuration for the node.
     */
    public interface Config {

    }

    @Inject
    public VerificationOptions() {
    }

    /**
     * Get document selection choice from user.
     *
     * @return Action, Which will redirect to next action.
     */
    private Action collectRegField() {
        logger.debug("Collecting Verification Options");
        List<Callback> cbList = new ArrayList<>();
        String[] choices = {"Passport", "DL/ID"};
        cbList.add(new ChoiceCallback("Which type of document would you like to submit?", choices, 0, false));
        String[] submitButton = {"Next"};
        cbList.add(new ConfirmationCallback(0, submitButton, 0));
        return send(ImmutableList.copyOf(cbList)).build();
    }

    /**
     * @param context
     * @return Action, Which will redirect to next action.
     */
    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        try {
            logger.debug("*********************Verification Options node********************");
            JsonValue sharedState = context.sharedState;
            Boolean isVerificationOptionsRefresh;
            if (sharedState.get(Constants.IS_VERIFICATION_REFRESH).isNotNull() && sharedState.get(Constants.IS_VERIFICATION_REFRESH).asBoolean()) {
                isVerificationOptionsRefresh = sharedState.get(Constants.IS_VERIFICATION_REFRESH).asBoolean();
                if (isVerificationOptionsRefresh) {
                    sharedState.put(Constants.IS_VERIFICATION_REFRESH, false);
                    return buildCallbacks();
                }
            }
            if (!context.getCallback(ChoiceCallback.class).isEmpty()) {
                String existingVerificationChoice = null;
                if (sharedState.get(Constants.VERIFICATION_CHOICE).isNotNull()) {
                    existingVerificationChoice = sharedState.get(Constants.VERIFICATION_CHOICE).asString();
                }
                Integer selectedIndex = Arrays.stream(context.getCallback(ChoiceCallback.class).get().getSelectedIndexes()).findFirst().getAsInt();
                String selectedValue;
                switch (selectedIndex) {
                    case 0:
                        selectedValue = Constants.PASSPORT_VERIFICATION_OPTION;
                        sharedState.put(Constants.VERIFICATION_CHOICE, selectedValue);
                        if (existingVerificationChoice != null) {
                            f1(existingVerificationChoice, selectedValue, context);
                        }
                        return goTo(VerificationOptionsOutcome.PASSPORT).replaceSharedState(sharedState).build();
                    case 1:
                        selectedValue = Constants.DOCUMENT_VERIFICATION_OPTION;
                        sharedState.put(Constants.VERIFICATION_CHOICE, selectedValue);
                        if (existingVerificationChoice != null) {
                            f1(existingVerificationChoice, selectedValue, context);
                        }
                        return goTo(VerificationOptionsOutcome.IDDL).replaceSharedState(sharedState).build();
                    default:
                        logger.error("No option selected/Invalid option. Please try again.");
                        throw new NodeProcessException("No option selected!!");
                }
            } else {
                return collectRegField();
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new NodeProcessException("Exception is: " + e);
        }
    }

    void f1(String existingVerificationChoice, String selectedValue, TreeContext context) {
        if (!existingVerificationChoice.equalsIgnoreCase(selectedValue)) {
            JsonValue sharedState = context.sharedState;
            sharedState.put(Constants.RETAKE_COUNT, 0);
            sharedState.put(Constants.RETRY_COUNT, 0);
        }
    }

    private Action.ActionBuilder goTo(VerificationOptions.VerificationOptionsOutcome outcome) {
        return Action.goTo(outcome.name());
    }

    /**
     * The possible outcomes for the VerificationOptions.
     */
    private enum VerificationOptionsOutcome {
        /**
         * selection for ID/DL.
         */
        IDDL,
        /**
         * selection for PASSPORT.
         */
        PASSPORT
    }

    private Action buildCallbacks() {
        return send(new ArrayList<>() {{
            add(new ScriptTextOutputCallback(VerificationOptionsScript.getVerificationOptionsScript()));
        }}).build();

    }


    /**
     * This class will create customized outcome for the node.
     */
    public static class VerificationOptionsOutcomeProvider implements OutcomeProvider {
        /**
         * @param locales        Local property file for configuration.
         * @param nodeAttributes Node attributes for outcomes
         * @return List of possible outcomes.
         */
        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
            ResourceBundle bundle = locales.getBundleInPreferredLocale(VerificationOptions.BUNDLE,
                    VerificationOptions.VerificationOptionsOutcomeProvider.class.getClassLoader());
            return ImmutableList.of(new Outcome(VerificationOptionsOutcome.IDDL.name(), bundle.getString("idDl")),
                    new Outcome(VerificationOptionsOutcome.PASSPORT.name(), bundle.getString("passport")));
        }
    }

}