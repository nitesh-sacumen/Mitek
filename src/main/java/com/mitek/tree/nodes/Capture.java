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

@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass = Capture.Config.class)
public class Capture extends SingleOutcomeNode {


    private static Logger logger = LoggerFactory.getLogger(AuthClientUtils.class);

    /**
     * Configuration for the node.
     */
    public interface Config {

    }

    @Inject
    public Capture() {

    }

    @Override
    public Action process(TreeContext context) {
        logger.debug("*********************Capture node********************");
        JsonValue sharedState = context.sharedState;
        Boolean isCaptureRefresh = false;
        if (sharedState.get(Constants.IS_CAPTURE_REFRESH).isNotNull()) {
            isCaptureRefresh = sharedState.get(Constants.IS_CAPTURE_REFRESH).asBoolean();
            if (isCaptureRefresh == true) {
                sharedState.put(Constants.IS_CAPTURE_REFRESH, false);
            }
        }

        String verificationChoice = sharedState.get(Constants.VERIFICATION_CHOICE).asString();
        String url = "/mitek/p1.js";
        System.out.println("into capture");

        if (context.getCallback(HiddenValueCallback.class).isPresent()) {
            System.out.println("into capture has callback");
            String imageData = context.getCallback(HiddenValueCallback.class).get().getValue();
            logger.debug("*********imageData**********" + imageData);
            System.out.println("image data is" + imageData);
            sharedState.put(Constants.CAPTURE_RESULT, imageData);
            return goToNext().replaceSharedState(sharedState).build();
        }
        System.out.println("into capture no callback");
        System.out.println("verification is:");
        return buildCallbacks(url, verificationChoice, isCaptureRefresh);
    }

    private Action buildCallbacks(String url, String verificationChoice, Boolean isCaptureRefresh) {
        return send(new ArrayList<>() {{
            add(new TextOutputCallback(0, "Please wait after image capture, it will be displayed shortly for preview."));
            add(new ScriptTextOutputCallback(getAuthDataScript(url, verificationChoice, isCaptureRefresh)));
            add(new HiddenValueCallback("captureResponse"));
        }}).build();

    }

    private String getAuthDataScript(String scriptURL, String verificationChoice, Boolean isCaptureRefresh) {
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
                "setTimeout(function(){\n" +
                "var imageData = document.getElementById('capturedImage').src;\n" +
                "var result = imageData.startsWith('" + Constants.BASE64_STARTS_WITH + "');\n" +
                "var imageBase64=document.getElementById('captureResponse').value;\n" +
                "if(result===true){\n" +
                "document.getElementById('captureResponse').value=imageData;\n" +
                "}\n" +
                "else{\n" +
                "document.getElementById('captureResponse').value='';\n" +
                "}\n" +
                "document.getElementById('loginButton_0').click();" +
                "}, 15000);\r\n" +
                "var yourCodeToBeCalled = function(){\r\n" +
                "}\r\n" + "loadJS(" + "\"" + scriptURL + "\"" + ", yourCodeToBeCalled, document.body);";

    }
}