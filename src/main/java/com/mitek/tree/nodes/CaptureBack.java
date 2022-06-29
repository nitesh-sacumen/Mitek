package com.mitek.tree.nodes;

import com.mitek.tree.config.Constants;
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

@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass = CaptureBack.Config.class)
public class CaptureBack extends SingleOutcomeNode {


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
        Boolean isCaptureRefresh = false;
        if (sharedState.get(Constants.IS_CAPTURE_REFRESH).isNotNull()) {
            isCaptureRefresh = sharedState.get(Constants.IS_CAPTURE_REFRESH).asBoolean();
            if (isCaptureRefresh == true) {
                sharedState.put(Constants.IS_CAPTURE_REFRESH, false);
            }
        }

        String backReference = "PDF417_BARCODE";
        String url = "/mitek/p1.js";


        if (context.getCallback(HiddenValueCallback.class).isPresent()) {
            String isBackCaptureImage = context.getCallback(HiddenValueCallback.class).get().getValue();
            logger.debug("isBackCaptureImage: "+isBackCaptureImage);
            if (isBackCaptureImage.equalsIgnoreCase("true")) {
                return buildCallbacks(url, backReference,isCaptureRefresh);
            }
            return goToNext().replaceSharedState(sharedState).build();

        } else {
            return buildCallbacksForCaptureMessage();
        }
    }


    private Action buildCallbacks(String url, String backReference, Boolean isCaptureRefresh) {
        return send(new ArrayList<>() {{
            add(new TextOutputCallback(0, "Please wait after image capture, it will be displayed shortly for preview."));
            add(new ScriptTextOutputCallback(getAuthDataScript(url, backReference,isCaptureRefresh)));
            add(new HiddenValueCallback("captureBackResponse"));
        }}).build();

    }

    private Action buildCallbacksForCaptureMessage() {
        logger.debug("buildCallbacksForCaptureMessage............");
        return send(new ArrayList<>() {{
            add(new TextOutputCallback(0, "Capture back of DL/ID"));
            add(new ScriptTextOutputCallback(getAuthDataScriptForCaptureMessage()));
            add(new HiddenValueCallback("diplayBackMessage"));
        }}).build();

    }

    private String getAuthDataScriptForCaptureMessage() {
        return "document.getElementById('loginButton_0').value = 'Capture Front Of DL/ID';\n" +
                "var button = document.getElementById('loginButton_0');\n" +
                "button.onclick = function() {\n" +
                "document.getElementById('diplayBackMessage').value = 'true';\n" +
                "};\n";
    }

    private String getAuthDataScript(String scriptURL, String backReference, Boolean isCaptureRefresh) {
        return "var loadJS = function(url, implementationCode, location){\r\n" +
                "var scriptTag = document.createElement('script');\r\n" +
                "scriptTag.id='mitekScript';\n" +
                "scriptTag.src = url;\r\n" +
                "var link = document.createElement('link');\r\n" +
                "link.rel = 'stylesheet';\r\n" +
                "link.type = 'text/css';\r\n" +
                "link.href = '/mitek/style.css';\r\n" +
                "document.getElementById('loginButton_0').style.display = 'none';\n" +
                "scriptTag.appendChild(link);\r\n" +
                "location.appendChild(scriptTag);\r\n" + "};\r\n" +
                "var input = document.createElement('input');\r\n" + "input.setAttribute('type', 'hidden');\r\n" +
                "input.setAttribute('id', 'integratorDocTypeInput');\r\n" +
                "input.setAttribute('value','" + backReference + "');\r\n" +
                "document.body.appendChild(input);\r\n" +

                "var capturedTimeout = document.createElement('input');\n" +
                "capturedTimeout.id = 'capturedTimeout';\n" +
                "capturedTimeout.type = 'hidden';\n" +
                "capturedTimeout.value = '';\n" +
                "document.body.appendChild(capturedTimeout);\n" +

                "var interval = setInterval(function () {\n" +
                "var codeData = document.getElementById('capturedBackImage').value;\n" +
                "document.getElementById('captureBackResponse').value = codeData;\n" +
                "if(document.getElementById('capturedTimeout').value=='timeout') {\n" +
                "document.getElementById('captureBackResponse').value = '';\n" +
                "f2();\n" +
                "}\n" +
                "}, 500);\n" +
                "function f2() {\n" +
                "clearInterval(interval);\n" +
                "document.getElementById('loginButton_0').click();\n" +
                "}\n" +
                "var yourCodeToBeCalled = function(){\r\n" +
                "}\r\n" + "loadJS(" + "\"" + scriptURL + "\"" + ", yourCodeToBeCalled, document.body);";

    }
}
