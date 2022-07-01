package com.mitek.tree.nodes;

import com.google.common.collect.ImmutableList;
import com.mitek.tree.config.Constants;
import com.mitek.tree.util.VerificationOptionsScript;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
import com.sun.identity.authentication.client.AuthClientUtils;
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

@Node.Metadata(outcomeProvider = VerificationOptions.VerificationOptionsOutcomeProvider.class, configClass = VerificationOptions.Config.class)
public class VerificationOptions implements Node {
    VerificationOptionsScript verificationOptionsScript = new VerificationOptionsScript();

    private static Logger logger = LoggerFactory.getLogger(AuthClientUtils.class);
    private static final String BUNDLE = "com/mitek/tree/nodes/VerificationOptions";

    /**
     * Configuration for the node.
     */
    public interface Config {

    }

    @Inject
    public VerificationOptions() {
    }

    private Action collectRegField() {
        logger.debug("Collecting Verification Options");
        List<Callback> cbList = new ArrayList<>();
        try {
            String[] choices = {"Passport", "DL/ID"};
            ChoiceCallback verificationOptions = new ChoiceCallback("Which type of document would you like to submit?", choices, 0, false);
            cbList.add(verificationOptions);
            String[] submitButton = {"Next"};
            ConfirmationCallback confirmationCallback = new ConfirmationCallback(0, submitButton, 0);
            cbList.add(confirmationCallback);
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return send(ImmutableList.copyOf(cbList)).build();
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        try {
            logger.debug("*********************Verification Options node********************");
            JsonValue sharedState = context.sharedState;
            Boolean isVerificationOptionsRefresh;
            if (sharedState.get(Constants.IS_VERIFICATION_REFRESH).isNotNull() && sharedState.get(Constants.IS_VERIFICATION_REFRESH).asBoolean() == true) {
                isVerificationOptionsRefresh = sharedState.get(Constants.IS_VERIFICATION_REFRESH).asBoolean();
                if (isVerificationOptionsRefresh == true) {
                    sharedState.put(Constants.IS_VERIFICATION_REFRESH, false);
                    return buildCallbacks();
                }
            }
            if (!context.getCallback(ChoiceCallback.class).isEmpty()) {
                Integer selectedIndex = Arrays.stream(context.getCallback(ChoiceCallback.class).get().getSelectedIndexes()).findFirst().getAsInt();
                String selectedValue;
                switch (selectedIndex) {
                    case 0:
                        selectedValue = "PASSPORT";
                        sharedState.put(Constants.VERIFICATION_CHOICE, selectedValue);
                        return goTo(VerificationOptionsOutcome.PASSPORT).replaceSharedState(sharedState).build();
                    case 1:
                        selectedValue = "DOCUMENT";
                        sharedState.put(Constants.VERIFICATION_CHOICE, selectedValue);
                        return goTo(VerificationOptionsOutcome.IDDL).replaceSharedState(sharedState).build();
                    default:
                        logger.debug("No option selected/Invalid option. Please try again.");
                        return null;
                }
            } else {
                return collectRegField();
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            throw new NodeProcessException("Exception is: " + e);
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
            add(new ScriptTextOutputCallback(verificationOptionsScript.getVerificationOptionsScript()));
        }}).build();

    }


    public static class VerificationOptionsOutcomeProvider implements OutcomeProvider {
        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
            ResourceBundle bundle = locales.getBundleInPreferredLocale(VerificationOptions.BUNDLE,
                    VerificationOptions.VerificationOptionsOutcomeProvider.class.getClassLoader());
            return ImmutableList.of(new Outcome(VerificationOptionsOutcome.IDDL.name(), bundle.getString("idDl")),
                    new Outcome(VerificationOptionsOutcome.PASSPORT.name(), bundle.getString("passport")));
        }
    }

}