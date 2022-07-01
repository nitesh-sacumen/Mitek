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

@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass = Selfie.Config.class)
public class Selfie extends SingleOutcomeNode {
SelfieScript selfieScript=new SelfieScript();

    private static Logger logger = LoggerFactory.getLogger(AuthClientUtils.class);

    /**
     * Configuration for the node.
     */
    public interface Config {

    }

    @Inject
    public Selfie() {

    }

    @Override
    public Action process(TreeContext context) {
        logger.debug("*********************Selfie node********************");
        JsonValue sharedState = context.sharedState;
        String verificationChoice = "SELFIE";
        String url = "/mitek/p1.js";
        if (context.getCallback(HiddenValueCallback.class).isPresent() && context.getCallback(HiddenValueCallback.class).get().getValue().startsWith(Constants.BASE64_STARTS_WITH)) {
            return goToNext().replaceSharedState(sharedState).build();
        }
        return buildCallbacks(url, verificationChoice);
    }

    private Action buildCallbacks(String url, String verificationChoice) {
        return send(new ArrayList<>() {{
            add(new TextOutputCallback(0, "Please wait after selfie image capture, it will be displayed shortly for preview."));
            add(new ScriptTextOutputCallback(selfieScript.getSelfieScript(url, verificationChoice)));
            add(new HiddenValueCallback("captureSelfieResponse"));
        }}).build();
    }
}