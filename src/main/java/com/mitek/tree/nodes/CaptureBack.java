package com.mitek.tree.nodes;

import com.google.common.collect.ImmutableList;
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
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.callback.TextOutputCallback;
import java.util.ArrayList;
import java.util.List;

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
            ScriptTextOutputCallback scriptTextOutputCallback = new ScriptTextOutputCallback(removeElements());
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
            add(new ScriptTextOutputCallback(getAuthDataScript(url, verificationChoice)));
            add(new HiddenValueCallback("captureBackResponse"));
            add(new HiddenValueCallback("captureBack"));
        }}).build();

    }

    private String removeElements() {
        return "if (document.contains(document.getElementById('parentDiv'))) {\n" +
                "document.getElementById('parentDiv').remove();\n" +
                "}\n" +
                "if (document.contains(document.getElementById('integratorDocTypeInput'))) {\n" +
                "document.getElementById('integratorDocTypeInput').remove();\n" +
                "}\n" +
                "if (document.contains(document.getElementById('capturedTimeout'))) {\n" +
                "document.getElementById('capturedTimeout').remove();\n" +
                "}\n" +
                "if (document.contains(document.getElementById('mitekScript'))) {\n" +
                "document.getElementById('mitekScript').remove();\n" +
                "}\n" +
                "if (document.contains(document.getElementById('integratorAutoCaptureButton'))) {\n" +
                "document.getElementById('integratorAutoCaptureButton').remove();\n" +
                "}\n" +
                "if (document.contains(document.getElementById('integratorManualCaptureButton'))) {\n" +
                "document.getElementById('integratorManualCaptureButton').remove();\n" +
                "}\n" +
                "if (document.contains(document.getElementById('uiContainer'))) {\n" +
                "document.getElementById('uiContainer').remove();\n" +
                "}\n" +
                "if (document.contains(document.getElementById('mitekMediaContainer'))) {\n" +
                "document.getElementById('mitekMediaContainer').remove();\n" +
                "}\n" +
                "if (document.contains(document.getElementById('capturedImage'))) {\n" +
                "document.getElementById('capturedImage').remove();\n" + "}\n" +
                "if (document.contains(document.getElementById('captureFrontResponse'))) {\n" +
                "document.getElementById('captureFrontResponse').remove();\n" + "}\n";
    }

    private String getAuthDataScript(String scriptURL, String verificationChoice) {
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
                "input.setAttribute('value','" + verificationChoice + "');\r\n" +
                "document.body.appendChild(input);\r\n" +

                "var capturedTimeout = document.createElement('input');\n" +
                "capturedTimeout.id = 'capturedTimeout';\n" +
                "capturedTimeout.type = 'hidden';\n" +
                "capturedTimeout.value = '';\n" +
                "document.body.appendChild(capturedTimeout);\n" +

                "var interval = setInterval(function () {\n" +
                "var codeData = document.getElementById('capturedBackImageCode').value;\n" +
                "var result = codeData.includes('*');\n" +
                "if (result === true) {\n" +
                "document.getElementById('captureBackResponse').value = codeData;\n" +
                "var backData = document.getElementById('capturedBackImage').src;\n" +
                "document.getElementById('captureBack').value = backData;\n" +

                "if (document.contains(document.getElementById('capturedImageContainer'))) {\n" +
                "var capturedImageContainer=document.getElementById('capturedImageContainer');\n" +
                "var backImageData = document.createElement('input');\n" +
                "backImageData.id = 'backImageData';\n" +
                "backImageData.type = 'hidden';\n" +
                "backImageData.value = backData;\n" +
                "capturedImageContainer.appendChild(backImageData);\n" +
                "f2();\n" +
                "}\n" + "}\n" +
                "else if(document.getElementById('capturedTimeout').value=='timeout') {\n" +
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
