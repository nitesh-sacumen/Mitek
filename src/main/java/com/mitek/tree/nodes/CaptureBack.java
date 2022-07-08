package com.mitek.tree.nodes;

import com.google.common.collect.ImmutableList;
import com.mitek.tree.config.Constants;
import com.mitek.tree.util.CaptureBackScript;
import com.mitek.tree.util.RemoveElements;
import com.sun.identity.authentication.callbacks.HiddenValueCallback;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
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


/**
 * @author Saucmen(www.sacumen.com) Capture Back node with
 * single outcome. This node will capture back image of ID/DL document.
 */
@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass = CaptureBack.Config.class)
public class CaptureBack extends SingleOutcomeNode {

    private static final Logger logger = LoggerFactory.getLogger(CaptureBack.class);

    /**
     * Configuration for the node.
     */
    public interface Config {
    }

    @Inject
    public CaptureBack() {

    }

    /**
     * @param context
     * @return Action, Which will redirect to next action.
     */
    @Override
    public Action process(TreeContext context) {
        logger.debug("*********************Capture back********************");
        JsonValue sharedState = context.sharedState;
        if (context.getCallback(HiddenValueCallback.class).isPresent() && context.getCallbacks(HiddenValueCallback.class).get(0).getValue().contains("*")) {
            String backImageCode = context.getCallbacks(HiddenValueCallback.class).get(0).getValue();
            sharedState.put(Constants.PDF_417_CODE, backImageCode);
            return goToNext().replaceSharedState(sharedState).build();
        } else if (context.getCallback(ConfirmationCallback.class).isPresent()) {
            return buildCallbacks(Constants.JS_URL, Constants.BACK_VERIFICATION_OPTION);
        } else {
            List<Callback> cbList = new ArrayList<>();
            cbList.add(new ScriptTextOutputCallback(RemoveElements.removeElements()));
            cbList.add(getTextOutputCallbackObject("Capture Back of Document"));
            cbList.add(getTextOutputCallbackObject("* Use dark background"));
            cbList.add(getTextOutputCallbackObject("** Get all 4 corners of the bio-data page within the frame"));
            cbList.add(getTextOutputCallbackObject("* Make sure lighting is good"));
            String[] submitButton = {"Capture Back of Document"};
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
            add(new TextOutputCallback(0, "Please wait after image back capture, it will be displayed shortly for preview."));
            add(new ScriptTextOutputCallback(CaptureBackScript.getCaptureBackScript(url, verificationChoice)));
            add(new HiddenValueCallback("captureBackResponse"));
            add(new HiddenValueCallback("captureBack"));
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
