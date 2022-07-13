package com.mitek.tree.nodes;

import com.google.common.collect.ImmutableList;
import com.mitek.tree.config.Constants;
import com.mitek.tree.util.PassportScript;
import com.sun.identity.authentication.callbacks.HiddenValueCallback;
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
 * @author Sacumen(www.sacumen.com) Passport node with
 * single outcome. This node will capture image of passport document.
 */
@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass = Passport.Config.class)
public class Passport extends SingleOutcomeNode {

    private static final Logger logger = LoggerFactory.getLogger(Passport.class);

    /**
     * Configuration for the node.
     */
    public interface Config {

    }

    @Inject
    public Passport() {

    }

    /**
     * @param context
     * @return Action, Which will redirect to next action.
     */
    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        logger.debug("*********************Passport node********************");
        JsonValue sharedState = context.sharedState;
        if (context.getCallback(HiddenValueCallback.class).isPresent() && context.getCallback(HiddenValueCallback.class).get().getValue().startsWith(Constants.BASE64_STARTS_WITH)) {
            return goToNext().replaceSharedState(sharedState).build();
        } else if (context.getCallback(ConfirmationCallback.class).isPresent()) {
            String scriptFilePath = sharedState.get(Constants.JS_URL).asString();
            return buildCallbacks(scriptFilePath, Constants.PASSPORT_VERIFICATION_OPTION);
        } else {
            Boolean isVerificationRefresh = false;
            if (sharedState.get(Constants.IS_VERIFICATION_REFRESH).isNotNull() && sharedState.get(Constants.IS_VERIFICATION_REFRESH).asBoolean()) {
                isVerificationRefresh = true;
                sharedState.put(Constants.IS_VERIFICATION_REFRESH, false);
            }
            List<Callback> cbList = new ArrayList<>();
            cbList.add(new ScriptTextOutputCallback(PassportScript.getScript(isVerificationRefresh)));
            cbList.add(getTextOutputCallbackObject("Capture Passport"));
            cbList.add(getTextOutputCallbackObject("* Use dark background"));
            cbList.add(getTextOutputCallbackObject("* Get all 4 corners of the bio-data page within the frame"));
            cbList.add(getTextOutputCallbackObject("* Make sure lighting is good"));
            String[] submitButton = {"Capture Passport"};
            cbList.add(new ConfirmationCallback(0, submitButton, 0));
            return send(ImmutableList.copyOf(cbList)).build();
        }
    }

    /**
     * @param url                A path for javascript file.
     * @param verificationChoice Type of verification eg: Passport/selfie/DL/ID
     * @return Action, Which will redirect to next action.
     */
    private Action buildCallbacks(String url, String verificationChoice) {
        return send(new ArrayList<>() {{
            add(new TextOutputCallback(0, "Please wait after passport image capture, it will be displayed shortly for preview."));
            add(new ScriptTextOutputCallback(PassportScript.getPassportScript(url, verificationChoice)));
            add(new HiddenValueCallback("capturePassportResponse"));
        }}).build();
    }

    /**
     * @param msg Message that needs to be rendered to the user.
     * @return Text output callback
     */
    private TextOutputCallback getTextOutputCallbackObject(String msg) {
        return new TextOutputCallback(0, msg);
    }
}