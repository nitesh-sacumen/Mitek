package com.mitek.tree.nodes;

import com.google.common.collect.ImmutableList;
import com.mitek.tree.config.Constants;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
import com.sun.identity.authentication.client.AuthClientUtils;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.ChoiceCallback;
import javax.security.auth.callback.ConfirmationCallback;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.forgerock.openam.auth.node.api.Action.send;

@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass = DocumentType.Config.class)
public class DocumentType extends SingleOutcomeNode{
    private static Logger logger = LoggerFactory.getLogger(AuthClientUtils.class);

    /**
     * Configuration for the node.
     */
    public interface Config {

    }

    @Inject
    public DocumentType() {
    }


    private Action collectRegField(TreeContext context, Boolean isVerificationOptionsRefresh) {
        logger.debug("Collecting Verification Options");
        List<Callback> cbList = new ArrayList<>();
        try {
            String[] choices = {"Identification Card", "Passport", "Driving Licence"};
            ChoiceCallback verificationOptions = new ChoiceCallback("Which type of document would you like to submit?", choices, 0, false);
            cbList.add(verificationOptions);

            String[] choices2 = {"Usa or Canada", "Rest of the world"};
            ChoiceCallback verificationOptions2 = new ChoiceCallback("Select country of Issue", choices2, 0, false);
            cbList.add(verificationOptions2);

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
            logger.debug("*********************Document type********************");
            JsonValue sharedState = context.sharedState;
            Boolean isVerificationOptionsRefresh = false;

            if (sharedState.get(Constants.IS_VERIFICATION_REFRESH).isNotNull() && sharedState.get(Constants.IS_VERIFICATION_REFRESH).asBoolean() == true) {
                isVerificationOptionsRefresh = sharedState.get(Constants.IS_VERIFICATION_REFRESH).asBoolean();
                if (isVerificationOptionsRefresh == true) {
                    sharedState.put(Constants.IS_VERIFICATION_REFRESH, false);
                    return buildCallbacks();
                }
            }


            if (!context.getCallbacks(ChoiceCallback.class).isEmpty()) {
                Integer selectedIDChoice = Arrays.stream(context.getCallbacks(ChoiceCallback.class).get(0).getSelectedIndexes()).findFirst().getAsInt();
                Integer selectedCountryChoice = Arrays.stream(context.getCallbacks(ChoiceCallback.class).get(0).getSelectedIndexes()).findFirst().getAsInt();

                logger.debug("selectedIDChoice index: "+selectedIDChoice);
                logger.debug("selectedCountryChoice index: "+selectedCountryChoice);


                String selectedIDValue;
                String selectedCountry;
                switch (selectedIDChoice) {
                    case 0:
                        selectedIDValue = "DOCUMENT";
                        break;
                    case 1:
                        selectedIDValue = "PASSPORT";
                        break;
                    case 2:
                        selectedIDValue = "DL FRONT";
                        break;
                    default:
                        logger.debug("No id option selected/Invalid option. Please try again.");
                        System.out.println("No id option selected/Invalid option. Please try again.");
                        selectedIDValue = "Invalid Choice";
                }

                switch (selectedCountryChoice) {
                    case 0:
                        selectedCountry = "USA OR CANADA";
                        break;
                    case 1:
                        selectedCountry = "REST OF THE WORLD";
                        break;
                    default:
                        logger.debug("No option selected/Invalid option. Please try again.");
                        System.out.println("No country option selected/Invalid option. Please try again.");
                        selectedCountry = "Invalid Country";
                }
                sharedState.put(Constants.IDENTITY_CHOICE, selectedIDValue);
                sharedState.put(Constants.COUNTRY_CHOICE, selectedCountry);
                return goToNext().build();
            } else {
                return collectRegField(context, isVerificationOptionsRefresh);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            throw new NodeProcessException("Exception is: " + e);
        }
    }

    private Action buildCallbacks() {
        return send(new ArrayList<>() {{
            add(new ScriptTextOutputCallback(getAuthDataScript()));
        }}).build();

    }

    private String getAuthDataScript() {
        return "document.getElementById('integratorDocTypeInput').remove();\n" +
                "document.getElementById('mitekScript').remove();\n" +
                "document.getElementById('capturedTimeout').remove();\n" +
                "document.getElementById('uiContainer').remove();\n" +
                "document.getElementById('mitekMediaContainer').remove();\n" +
                "if (document.contains(document.getElementById('parentDiv'))) {\n" +
                "document.getElementById('parentDiv').remove();\n" +
                "}\n" +
                "if (document.contains(document.getElementById('integratorAutoCaptureButton'))) {\n" +
                "document.getElementById('integratorAutoCaptureButton').remove();\n" +
                "}\n" +
                "if (document.contains(document.getElementById('integratorManualCaptureButton'))) {\n" +
                "document.getElementById('integratorManualCaptureButton').remove();\n" +
                "}\n" +
                "document.getElementById('loginButton_0').click();";
    }

}
