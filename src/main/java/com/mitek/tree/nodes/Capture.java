package com.mitek.tree.nodes;

import com.google.common.collect.ImmutableList;
import com.mitek.tree.config.Constants;
import com.sun.identity.authentication.callbacks.HiddenValueCallback;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
import com.sun.identity.authentication.client.AuthClientUtils;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.security.auth.callback.Callback;
import java.util.ArrayList;
import java.util.List;

import static org.forgerock.openam.auth.node.api.Action.send;

@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass = Capture.Config.class)
public class Capture extends SingleOutcomeNode {

    private static Logger utilDebug = LoggerFactory.getLogger(AuthClientUtils.class);

    /**
     * Configuration for the node.
     */
    public interface Config {

    }

    @Inject
    public Capture() {
    }


    private Action buildCallbacks(TreeContext context) {
        NodeState sharedState = context.getStateFor(this);
        String verificationChoice = sharedState.get(Constants.VERIFICATION_CHOICE).asString();
        return send(new ArrayList<Callback>() {{
            add(new ScriptTextOutputCallback(getAuthDataScript("/mitek/p1.js", verificationChoice)));
            add(new HiddenValueCallback("imageURL"));
        }}).build();
    }

    @Override
    public Action process(TreeContext context) {
        JsonValue sharedState = context.sharedState;
        if (context.hasCallbacks()) {
            String imageData = context.getCallback(HiddenValueCallback.class).get().getValue();
            utilDebug.debug("*********imageData**********" + imageData);
            sharedState.add("imageRef",imageData);
            return goToNext().replaceSharedState(sharedState).build();
        }

        return buildCallbacks(context);

    }

    private String getAuthDataScript(String scriptURL, String verificationChoice) {
        return "var loadJS = function(url, implementationCode, location){\r\n" +
                "    var scriptTag = document.createElement('script');\r\n" +
                "    scriptTag.src = url;\r\n" +
                " var link = document.createElement('link');\r\n" +
                "link.rel = 'stylesheet';\r\n" +
                "link.type = 'text/css';\r\n" +
                "link.href = '/mitek/style.css';\r\n" +
                "scriptTag.appendChild(link);\r\n" +
                "location.appendChild(scriptTag);\r\n" + "};\r\n" +
                "var input = document.createElement('input');\r\n" + "input.setAttribute('type', 'hidden');\r\n" +

                "input.setAttribute('id', 'integratorDocTypeInput');\r\n" +

                "input.setAttribute('value','" + verificationChoice + "');\r\n" +

                "document.body.appendChild(input);\r\n" +
                "setTimeout(function(){\n" +
                "var imageData = document.getElementById('capturedImage').src;\n" +
                "console.log('-----------------image',imageData);\n" +
                "document.getElementById('imageURL').value=imageData;\n" +
                "}, 25000);\r\n" +
                "var yourCodeToBeCalled = function(){\r\n" +
                "var imageData = document.getElementById('capturedImage').src;\n" +
                "console.log('-----------------image',imageData);\n" +
                "document.getElementById('imageURL').value=imageData;\n" +
                "}\r\n" + "loadJS(" + "\"" + scriptURL + "\"" + ", yourCodeToBeCalled, document.body);";

    }


}