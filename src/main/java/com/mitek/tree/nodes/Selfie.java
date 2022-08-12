package com.mitek.tree.nodes;

import com.mitek.tree.config.Constants;
import com.mitek.tree.util.SelfieScript;
import com.sun.identity.authentication.callbacks.HiddenValueCallback;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.security.auth.callback.TextOutputCallback;
import java.util.ArrayList;

import static org.forgerock.openam.auth.node.api.Action.send;

/**
 * @author Sacumen(www.sacumen.com) Selfie node with
 * single outcome. This node will capture selfie image.
 */
@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass = Selfie.Config.class)
public class Selfie extends SingleOutcomeNode {

    private static final Logger logger = LoggerFactory.getLogger(Selfie.class);

    /**
     * Configuration for the node.
     */
    public interface Config {
    }

    @Inject
    public Selfie() {
    }

    /**
     * @param context
     * @return Action, Which will redirect to next action.
     */
    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        logger.debug("*********************Selfie node********************");
        JsonValue sharedState = context.sharedState;
        if (context.getCallback(HiddenValueCallback.class).isPresent() && context.getCallback(HiddenValueCallback.class).get().getValue().startsWith(Constants.BASE64_STARTS_WITH)) {
            return goToNext().replaceSharedState(sharedState).build();
        }
        String scriptFilePath = sharedState.get(Constants.MITEK_FOLDER_URL).asString() + Constants.SCRIPT_FILE_URL;
        String styleFilePath = sharedState.get(Constants.MITEK_FOLDER_URL).asString() + Constants.STYLE_FILE_URL;
        return buildCallbacks(scriptFilePath, Constants.SELFIE_VERIFICATION_OPTION, styleFilePath);
    }

    /**
     * @param url                A path for javascript file.
     * @param verificationChoice Type of verification eg: Passport/selfie/DL/ID
     * @return Action, Which will redirect to next action.
     */
    private Action buildCallbacks(String url, String verificationChoice, String styleFilePath) {
        return send(new ArrayList<>() {{
            add(new TextOutputCallback(0, "Please wait after selfie image capture, it will be displayed shortly for preview."));
            add(new ScriptTextOutputCallback(SelfieScript.getSelfieScript(url, verificationChoice, styleFilePath)));
            add(new HiddenValueCallback("captureSelfieResponse"));
        }}).build();
    }
}