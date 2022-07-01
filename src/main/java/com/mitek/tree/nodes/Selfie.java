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

@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass = Selfie.Config.class)
public class Selfie extends SingleOutcomeNode {


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
            add(new ScriptTextOutputCallback(getAuthDataScript(url, verificationChoice)));
            add(new HiddenValueCallback("captureSelfieResponse"));
        }}).build();

    }

    private String getAuthDataScript(String scriptURL, String verificationChoice) {
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
                "if (document.contains(document.getElementById('captureFrontResponse'))) {\n" +
                "document.getElementById('captureFrontResponse').remove();\n" + "}\n" +
                "if (document.contains(document.getElementById('captureBackResponse'))) {\n" +
                "document.getElementById('captureBackResponse').remove();\n" + "}\n" +
                "if (document.contains(document.getElementById('capturePassportResponse'))) {\n" +
                "document.getElementById('capturePassportResponse').remove();\n" + "}\n" +
                "if (document.contains(document.getElementById('selfieImage'))) {\n" +
                "document.getElementById('selfieImage').remove();\n" + "}\n" +

                "var loadJS = function(url, implementationCode, location){\r\n" +
                "var scriptTag = document.createElement('script');\r\n" +
                "scriptTag.id='mitekScript';\n" +
                "scriptTag.src = url;\r\n" +
                "var link = document.createElement('link');\r\n" +
                "link.rel = 'stylesheet';\r\n" +
                "link.type = 'text/css';\r\n" +
                "link.href = '/mitek/style.css';\r\n" +
                "document.getElementById('loginButton_0').style.display = 'none';\n" + "scriptTag.appendChild(link);\r\n" + "location.appendChild(scriptTag);\r\n" + "};\r\n" + "var input = document.createElement('input');\r\n" + "input.setAttribute('type', 'hidden');\r\n" + "input.setAttribute('id', 'integratorDocTypeInput');\r\n" + "input.setAttribute('value','" + verificationChoice + "');\r\n" + "document.body.appendChild(input);\r\n" +

                "var capturedTimeout = document.createElement('input');\n" + "capturedTimeout.id = 'capturedTimeout';\n" + "capturedTimeout.type = 'hidden';\n" + "capturedTimeout.value = '';\n" + "document.body.appendChild(capturedTimeout);\n" +
                "var interval = setInterval(function () {\n" + "var imageData = document.getElementById('capturedImage').src;\n" +
                "var result = imageData.startsWith('" + Constants.BASE64_STARTS_WITH + "');\n" +
                "if (result === true) " + "{\n" +
                "document.getElementById('captureSelfieResponse').value = imageData;\n" +
                "if (document.contains(document.getElementById('capturedImageContainer'))) {\n" +
                "var capturedImageContainer=document.getElementById('capturedImageContainer');\n" +
                "var selfieImage = document.createElement('input');\n" +
                "selfieImage.id = 'selfieImage';\n" +
                "selfieImage.type = 'hidden';\n" +
                "selfieImage.value = imageData;\n" +
                "capturedImageContainer.appendChild(selfieImage);\n" +
                "f2();\n" + "}\n" +"}\n" +
                "else if(document.getElementById('capturedTimeout').value=='timeout') {\n" +
                "document.getElementById('captureSelfieResponse').value = '';\n" +
                "f2();\n" + "}\n" + "}, 500);\n" +
                "function f2() {\n" + "clearInterval(interval);\n" + "document.getElementById('loginButton_0').click();\n" +
                "}\n" +
                "var yourCodeToBeCalled = function(){\r\n" + "}\r\n" + "loadJS(" + "\"" + scriptURL + "\"" + ", yourCodeToBeCalled, document.body);";

    }
}