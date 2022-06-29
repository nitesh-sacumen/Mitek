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
import java.util.Locale;

import static org.forgerock.openam.auth.node.api.Action.send;

@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass = CaptureFront.Config.class)
public class CaptureFront extends SingleOutcomeNode {


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
        Boolean isCaptureRefresh;
        if (sharedState.get(Constants.IS_CAPTURE_REFRESH).isNotNull()) {
            isCaptureRefresh = sharedState.get(Constants.IS_CAPTURE_REFRESH).asBoolean();
            if (isCaptureRefresh == true) {
                sharedState.put(Constants.IS_CAPTURE_REFRESH, false);
            }
        }

        String verificationChoice = sharedState.get(Constants.VERIFICATION_CHOICE).asString();

        logger.debug("verificationChoice: " + verificationChoice);
        String url = "/mitek/p1.js";

        if (context.getCallback(HiddenValueCallback.class).isPresent()) {
            String isCaptureImage = context.getCallback(HiddenValueCallback.class).get().getValue();
            logger.debug("isCaptureImage: "+isCaptureImage);
            if (isCaptureImage.equalsIgnoreCase("true")) {
                return buildCallbacks(url, verificationChoice);
            }
            return goToNext().replaceSharedState(sharedState).build();

        } else {
            return buildCallbacksForCaptureMessage(verificationChoice);
        }
    }


    private Action buildCallbacks(String url, String identityChoice) {
        logger.debug("buildCallbacks............");
        return send(new ArrayList<>() {{
            add(new TextOutputCallback(0, "Please wait after image capture, it will be displayed shortly for preview."));
            add(new ScriptTextOutputCallback(getAuthDataScript(url, identityChoice)));
            add(new HiddenValueCallback("captureFrontResponse"));
        }}).build();

    }

    private Action buildCallbacksForCaptureMessage(String identityChoice) {
        logger.debug("buildCallbacksForCaptureMessage............");
        return send(new ArrayList<>() {{
            add(new TextOutputCallback(0, "Capture front of " + identityChoice.toLowerCase()));
            add(new ScriptTextOutputCallback(getAuthDataScriptForCaptureMessage(identityChoice)));
            add(new HiddenValueCallback("diplayFrontMessage"));
        }}).build();

    }

    private String getAuthDataScriptForCaptureMessage(String identityChoice) {
        return "document.getElementById('loginButton_0').value = 'Capture Front Of " + identityChoice.toLowerCase() + "';\n" +
                "var button = document.getElementById('loginButton_0');\n" +
                "button.onclick = function() {\n" +
                "document.getElementById('diplayFrontMessage').value = 'true';\n" +
                "};\n";
    }

    private String getAuthDataScript(String scriptURL, String identityChoice) {
        return "if (document.contains(document.getElementById('uiContainer'))) {\n" +
                "document.getElementById('uiContainer').remove();\n" + "}\n" +
                "if (document.contains(document.getElementById('mitekMediaContainer'))) {\n" +
                "document.getElementById('mitekMediaContainer').remove();\n" + "}\n" +
                "if (document.contains(document.getElementById('integratorDocTypeInput'))) {\n" +
                "document.getElementById('integratorDocTypeInput').remove();\n" + "}\n" +
                "if (document.contains(document.getElementById('capturedTimeout'))) {\n" +
                "document.getElementById('capturedTimeout').remove();\n" + "}\n" +
                "if (document.contains(document.getElementById('mitekScript'))) {\n" +
                "document.getElementById('mitekScript').remove();\n" + "}\n" +
                "if (document.contains(document.getElementById('integratorAutoCaptureButton'))) {\n" +
                "document.getElementById('integratorAutoCaptureButton').remove();\n" + "}\n" +
                "if (document.contains(document.getElementById('integratorManualCaptureButton'))) {\n" +
                "document.getElementById('integratorManualCaptureButton').remove();\n" + "}\n" +
                "if (document.contains(document.getElementById('capturedImage'))) {\n" +
                "document.getElementById('capturedImage').remove();\n" + "}\n" +
                "if (document.contains(document.getElementById('captureSelfieResponse'))) {\n" +
                "document.getElementById('captureSelfieResponse').remove();\n" + "}\n" +


                "var loadJS = function(url, implementationCode, location){\r\n" +
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
                "input.setAttribute('value','" + identityChoice + "');\r\n" +
                "document.body.appendChild(input);\r\n" +

                "var capturedTimeout = document.createElement('input');\n" +
                "capturedTimeout.id = 'capturedTimeout';\n" +
                "capturedTimeout.type = 'hidden';\n" +
                "capturedTimeout.value = '';\n" +
                "document.body.appendChild(capturedTimeout);\n" +


                "var interval = setInterval(function () {\n" +
                "var imageData = document.getElementById('capturedImage').src;\n" +
                "var result = imageData.startsWith('" + Constants.BASE64_STARTS_WITH + "');\n" +
                "if (result === true) {\n" +
                "document.getElementById('captureFrontResponse').value = imageData;\n" +

                "var frontImage = document.createElement('input');\n" +
                "frontImage.id = 'frontImage';\n" +
                "frontImage.type = 'hidden';\n" +
                "frontImage.value = imageData;\n" +
                "document.body.appendChild(frontImage);\n" +

                "f2();\n" +
                "}\n" +
                "else if(document.getElementById('capturedTimeout').value=='timeout') {\n" +
                "document.getElementById('captureFrontResponse').value = '';\n" +
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
