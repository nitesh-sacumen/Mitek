package com.mitek.tree.nodes;

import com.google.common.collect.ImmutableList;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.iplanet.sso.SSOToken;
import com.mitek.tree.config.Constants;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.SingleOutcomeNode;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.openam.identity.idm.IdentityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.ChoiceCallback;
import javax.security.auth.callback.TextOutputCallback;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.forgerock.openam.auth.node.api.Action.send;

@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass = VerificationOptions.Config.class)
public class VerificationOptions extends SingleOutcomeNode {


    private Logger logger = LoggerFactory.getLogger(VerificationOptions.class);

    /**
     * Configuration for the node.
     */
    public interface Config {

    }

    @Inject
    public VerificationOptions() {
    }

    List<Callback> cbList = new ArrayList<>();

    private Action collectRegField(TreeContext context) {
        try {
            logger.info("Collecting Verification Options");
            String[] choices = {"Selfie", "Passport", "Driving Licence"};
            ChoiceCallback verificationOptions = new ChoiceCallback("Please select one verification option", choices, 0, false);
            cbList.add(verificationOptions);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


        return send(ImmutableList.copyOf(cbList)).build();
    }

    @Override
    public Action process(TreeContext context) {
        try {
            if (!context.getCallback(ChoiceCallback.class).isEmpty()) {
                Integer selectedIndex = Arrays.stream(context.getCallback(ChoiceCallback.class).get().getSelectedIndexes()).findFirst().getAsInt();

                String selectedValue;
                switch (selectedIndex) {
                    case 0:
                        selectedValue = "SELFIE";
                        break;
                    case 1:
                        selectedValue = "PASSPORT";
                        break;
                    case 2:
                        selectedValue = "DRIVING LICENCE";
                        break;

                    default:
                        selectedValue = "Invalid";
                }
                JsonValue sharedState = context.sharedState;
                sharedState.put(Constants.VERIFICATION_CHOICE, selectedValue);
                return goToNext().build();
            } else {
                return collectRegField(context);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

}