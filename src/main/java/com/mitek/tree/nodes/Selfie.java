package com.mitek.tree.nodes;

import com.mitek.tree.config.Constants;
import com.mitek.tree.util.SelfieScript;
import com.sun.identity.authentication.callbacks.HiddenValueCallback;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
import com.sun.identity.authentication.client.AuthClientUtils;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.SingleOutcomeNode;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.security.auth.callback.TextOutputCallback;
import java.util.ArrayList;

import static org.forgerock.openam.auth.node.api.Action.send;

/**
 * @author Saucmen(www.sacumen.com) Selfie node with
 * single outcome. This node will capture selfie image.
 */
@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass = Selfie.Config.class)
public class Selfie extends SingleOutcomeNode {
    SelfieScript selfieScript = new SelfieScript();

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
    public Action process(TreeContext context) {
        logger.debug("*********************Selfie node********************");
        JsonValue sharedState = context.sharedState;
        if (context.getCallback(HiddenValueCallback.class).isPresent() && context.getCallback(HiddenValueCallback.class).get().getValue().startsWith(Constants.BASE64_STARTS_WITH)) {
            return goToNext().replaceSharedState(sharedState).build();
        }
        return buildCallbacks(Constants.JS_URL, Constants.SELFIE_VERIFICATION_OPTION);
    }

    /**
     * @param url                A path for javascript file.
     * @param verificationChoice Type of verification eg: Passport/selfie/DL/ID
     * @return Action, Which will redirect to next action.
     */
    private Action buildCallbacks(String url, String verificationChoice) {
        return send(new ArrayList<>() {{
            add(new TextOutputCallback(0, "Please wait after selfie image capture, it will be displayed shortly for preview."));
            add(new ScriptTextOutputCallback(selfieScript.getSelfieScript(url, verificationChoice)));
            add(new HiddenValueCallback("captureSelfieResponse"));
        }}).build();
    }
}