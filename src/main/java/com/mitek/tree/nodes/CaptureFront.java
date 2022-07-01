package com.mitek.tree.nodes;

import com.google.common.collect.ImmutableList;
import com.mitek.tree.config.Constants;
import com.mitek.tree.util.CaptureFrontScript;
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
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.callback.TextOutputCallback;
import java.util.ArrayList;
import java.util.List;

import static org.forgerock.openam.auth.node.api.Action.send;

@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass = CaptureFront.Config.class)
public class CaptureFront extends SingleOutcomeNode {
    CaptureFrontScript captureFrontScript = new CaptureFrontScript();

    private static Logger logger = LoggerFactory.getLogger(AuthClientUtils.class);

    /**
     * Configuration for the node.
     */
    public interface Config {

    }

    @Inject
    public CaptureFront() {

    }

    @Override
    public Action process(TreeContext context) {
        logger.debug("*********************Capture front********************");
        JsonValue sharedState = context.sharedState;
        String verificationChoice = "DOCUMENT";
        String url = "/mitek/p1.js";
        if (context.getCallback(HiddenValueCallback.class).isPresent()
                && context.getCallback(HiddenValueCallback.class).get().getValue().startsWith(Constants.BASE64_STARTS_WITH)) {
            return goToNext().replaceSharedState(sharedState).build();
        } else if (context.getCallback(ConfirmationCallback.class).isPresent()) {
            return buildCallbacks(url, verificationChoice);
        } else {
            Boolean isVerificationRefresh = false;
            if (sharedState.get(Constants.IS_VERIFICATION_REFRESH).isNotNull() && sharedState.get(Constants.IS_VERIFICATION_REFRESH).asBoolean() == true) {
                isVerificationRefresh = true;
                sharedState.put(Constants.IS_VERIFICATION_REFRESH, false);
            }
            List<Callback> cbList = new ArrayList<>();
            ScriptTextOutputCallback scriptTextOutputCallback = new ScriptTextOutputCallback(captureFrontScript.getRemoveElements(isVerificationRefresh));
            cbList.add(scriptTextOutputCallback);
            TextOutputCallback textOutputCallback1 = new TextOutputCallback(0, "Capture Front of Document");
            cbList.add(textOutputCallback1);
            TextOutputCallback textOutputCallback2 = new TextOutputCallback(0, "* Use dark background");
            cbList.add(textOutputCallback2);
            TextOutputCallback textOutputCallback3 = new TextOutputCallback(0, "* Get all 4 corners of the bio-data page within the frame");
            cbList.add(textOutputCallback3);
            TextOutputCallback textOutputCallback4 = new TextOutputCallback(0, "* Make sure lighting is good");
            cbList.add(textOutputCallback4);
            String[] submitButton = {"Capture Front of Document"};
            ConfirmationCallback confirmationCallback = new ConfirmationCallback(0, submitButton, 0);
            cbList.add(confirmationCallback);
            return send(ImmutableList.copyOf(cbList)).build();
        }
    }

    private Action buildCallbacks(String url, String identityChoice) {
        return send(new ArrayList<>() {{
            add(new TextOutputCallback(0, "Please wait after image front capture, it will be displayed shortly for preview."));
            add(new ScriptTextOutputCallback(captureFrontScript.getCaptureFrontScript(url, identityChoice)));
            add(new HiddenValueCallback("captureFrontResponse"));
        }}).build();

    }

}
