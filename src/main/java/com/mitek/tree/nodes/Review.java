package com.mitek.tree.nodes;

import com.google.common.collect.ImmutableList;
import com.mitek.tree.config.Constants;
import com.mitek.tree.util.AccessToken;
import com.mitek.tree.util.ReviewScript;
import com.mitek.tree.util.VerifyDocument;
import com.sun.identity.authentication.callbacks.HiddenValueCallback;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.util.i18n.PreferredLocales;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.forgerock.openam.auth.node.api.Action.send;

/**
 * @author Sacumen(www.sacumen.com) Review node with
 * two outcome. This node will verify captured images for passport, selfie and DL/ID.
 * This node contains two outcome - Retake and Wait.
 * Retake will again force user to retake image
 * Wait will connect to next node after verification result.
 */
@Node.Metadata(outcomeProvider = Review.ReviewOutcomeProvider.class, configClass = Review.Config.class)
public class Review implements Node {

    private static final String BUNDLE = "com/mitek/tree/nodes/Review";
    private static final Logger logger = LoggerFactory.getLogger(Review.class);
    AccessToken accessToken;
    VerifyDocument verifyDocument;

    /**
     * Configuration for the node.
     */
    public interface Config {
    }

    @Inject
    public Review(AccessToken accessToken, VerifyDocument verifyDocument) {
        this.accessToken = accessToken;
        this.verifyDocument = verifyDocument;
    }

    /**
     * @param context
     * @return Action, Which will redirect to next action.
     */
    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        logger.debug("*********************Review node********************");
        JsonValue sharedState = context.sharedState;

        Integer retakeCount;
        if (sharedState.get(Constants.RETAKE_COUNT).isNull()) {
            sharedState.put(Constants.RETAKE_COUNT, 0);
        }
        retakeCount = sharedState.get(Constants.RETAKE_COUNT).asInteger();

        if (!context.getCallback(HiddenValueCallback.class).isEmpty()) {
            String isRetake = context.getCallbacks(HiddenValueCallback.class).get(0).getValue();
            sharedState.put(Constants.IS_RETAKE, isRetake);

            if (isRetake.equalsIgnoreCase("true")) {
                logger.info("Retaking image.......");
                retakeCount++;
                sharedState.put(Constants.RETAKE_COUNT, retakeCount);
                return goTo(ReviewOutcome.Retake).replaceSharedState(sharedState).build();
            } else {
                logger.info("Submitting image.....");
                String frontData = "";
                if (context.getCallbacks(HiddenValueCallback.class).get(1).getValue().startsWith(Constants.BASE64_STARTS_WITH)) {
                    frontData = context.getCallbacks(HiddenValueCallback.class).get(1).getValue();
                }
                String selfieData = "";
                if (context.getCallbacks(HiddenValueCallback.class).get(2).getValue().startsWith(Constants.BASE64_STARTS_WITH)) {
                    selfieData = context.getCallbacks(HiddenValueCallback.class).get(2).getValue();
                }
                String passportData = "";
                if (context.getCallbacks(HiddenValueCallback.class).get(3).getValue().startsWith(Constants.BASE64_STARTS_WITH)) {
                    passportData = context.getCallbacks(HiddenValueCallback.class).get(3).getValue();
                }
                String backData = "";
                if (context.getCallbacks(HiddenValueCallback.class).get(4).getValue().startsWith(Constants.BASE64_STARTS_WITH)) {
                    backData = context.getCallbacks(HiddenValueCallback.class).get(4).getValue();
                }
                String backImageCode = "";
                if (sharedState.get(Constants.PDF_417_CODE).isNotNull()) {
                    backImageCode = sharedState.get(Constants.PDF_417_CODE).asString();
                }
                String accessTokenResult = accessToken.getAccessToken(context);
                if (accessTokenResult != null) {
                    verifyDocument.verify(accessTokenResult, frontData, selfieData, passportData, backImageCode, backData, context);
                }
                return goTo(ReviewOutcome.Wait).replaceSharedState(sharedState).build();
            }
        }
        Integer maxRetakeCount = sharedState.get(Constants.MAX_RETAKE_COUNT).asInteger();
        String styleFilePath = sharedState.get(Constants.MITEK_FOLDER_URL).asString() + Constants.STYLE_FILE_URL;
        return buildCallbacks(retakeCount, maxRetakeCount, styleFilePath);
    }

    /**
     * @param retakeCount Number of retakes for image capture
     * @return Action, Which will redirect to next action
     */
    private Action buildCallbacks(Integer retakeCount, Integer maxRetakeCount, String styleFilePath) {
        return send(new ArrayList<>() {{
            add(new ScriptTextOutputCallback(ReviewScript.getReviewScript(retakeCount, maxRetakeCount, styleFilePath)));
            add(new HiddenValueCallback("isRetake"));
            add(new HiddenValueCallback("front"));
            add(new HiddenValueCallback("selfie"));
            add(new HiddenValueCallback("passport"));
            add(new HiddenValueCallback("back"));
        }}).build();

    }

    /**
     * @param outcome Node outcome
     * @return Next node
     */
    private Action.ActionBuilder goTo(Review.ReviewOutcome outcome) {
        return Action.goTo(outcome.name());
    }

    /**
     * The possible outcomes for the Review.
     */
    public enum ReviewOutcome {
        /**
         * selection of Retake.
         */
        Retake,
        /**
         * selection for Wait.
         */
        Wait
    }


    /**
     * This class will create customized outcome for the node.
     */
    public static class ReviewOutcomeProvider implements org.forgerock.openam.auth.node.api.OutcomeProvider {

        /**
         * @param locales        Local property file for configuration.
         * @param nodeAttributes Node attributes for outcomes
         * @return List of possible outcomes.
         */
        @Override
        public List<Outcome> getOutcomes(PreferredLocales locales, JsonValue nodeAttributes) {
            ResourceBundle bundle = locales.getBundleInPreferredLocale(Review.BUNDLE, Review.ReviewOutcomeProvider.class.getClassLoader());
            return ImmutableList.of(new Outcome(ReviewOutcome.Retake.name(), bundle.getString("retake")), new Outcome(ReviewOutcome.Wait.name(), bundle.getString("wait")));
        }
    }
}