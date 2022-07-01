package com.mitek.tree.nodes;

import com.google.common.collect.ImmutableList;
import com.mitek.tree.config.Constants;
import com.mitek.tree.util.CaptureBackScript;
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

@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass = CaptureBack.Config.class)
public class CaptureBack extends SingleOutcomeNode {
    CaptureBackScript captureBackScript=new CaptureBackScript();

    private static Logger logger = LoggerFactory.getLogger(AuthClientUtils.class);

    /**
     * Configuration for the node.
     */
    public interface Config {

    }

    @Inject
    public CaptureBack() {

    }

    @Override
    public Action process(TreeContext context) {
        logger.debug("*********************Capture back********************");
        JsonValue sharedState = context.sharedState;
        String verificationChoice = "PDF417_BARCODE";
        String url = "/mitek/p1.js";
        if (context.getCallback(HiddenValueCallback.class).isPresent()
                && context.getCallbacks(HiddenValueCallback.class).get(0).getValue().contains("*")) {
            String backImageCode = context.getCallbacks(HiddenValueCallback.class).get(0).getValue();
            sharedState.put(Constants.PDF_417_CODE, backImageCode);
            return goToNext().replaceSharedState(sharedState).build();
        } else if (context.getCallback(ConfirmationCallback.class).isPresent()) {
            return buildCallbacks(url, verificationChoice);
        } else {
            List<Callback> cbList = new ArrayList<>();
            ScriptTextOutputCallback scriptTextOutputCallback = new ScriptTextOutputCallback(captureBackScript.getremoveElements());
            cbList.add(scriptTextOutputCallback);
            TextOutputCallback textOutputCallback1 = new TextOutputCallback(0, "Capture Back of Document");
            cbList.add(textOutputCallback1);
            TextOutputCallback textOutputCallback2 = new TextOutputCallback(0, "* Use dark background");
            cbList.add(textOutputCallback2);
            TextOutputCallback textOutputCallback3 = new TextOutputCallback(0, "* Get all 4 corners of the bio-data page within the frame");
            cbList.add(textOutputCallback3);
            TextOutputCallback textOutputCallback4 = new TextOutputCallback(0, "* Make sure lighting is good");
            cbList.add(textOutputCallback4);
            String[] submitButton = {"Capture Back of Document"};
            ConfirmationCallback confirmationCallback = new ConfirmationCallback(0, submitButton, 0);
            cbList.add(confirmationCallback);
            return send(ImmutableList.copyOf(cbList)).build();
        }
    }
    private Action buildCallbacks(String url, String verificationChoice) {
        return send(new ArrayList<>() {{
            add(new TextOutputCallback(0, "Please wait after image back capture, it will be displayed shortly for preview."));
            add(new ScriptTextOutputCallback(captureBackScript.getCaptureBackScript(url, verificationChoice)));
            add(new HiddenValueCallback("captureBackResponse"));
            add(new HiddenValueCallback("captureBack"));
        }}).build();
    }
}
