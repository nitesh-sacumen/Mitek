package com.mitek.tree.nodes;

import com.google.common.collect.ImmutableList;
import com.mitek.tree.config.Constants;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
import org.forgerock.openam.auth.node.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.TextOutputCallback;
import java.util.ArrayList;
import java.util.List;

import static org.forgerock.openam.auth.node.api.Action.send;

@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass = Capture.Config.class)
public class Capture extends SingleOutcomeNode {


    private Logger logger = LoggerFactory.getLogger(Capture.class);

    /**
     * Configuration for the node.
     */
    public interface Config {

    }

    @Inject
    public Capture() {
    }

    List<Callback> cbList = new ArrayList<>();

    private Action collectRegField(TreeContext context) {
        try {
            logger.info("Capturing verification details.");
            NodeState sharedState = context.getStateFor(this);
            String verificationChoice = sharedState.get(Constants.VERIFICATION_CHOICE).asString();

            TextOutputCallback textOutputCallback=new TextOutputCallback(0,"You have selected "+ verificationChoice+" as your verification method. Please click on auto capture button");
            cbList.add(textOutputCallback);
            ScriptTextOutputCallback scb = new ScriptTextOutputCallback(getAuthDataScript("/mitek/p1.js", verificationChoice));
            cbList.add(scb);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


        return send(ImmutableList.copyOf(cbList)).build();
    }

    @Override
    public Action process(TreeContext context) {

        if (!context.getCallback(ScriptTextOutputCallback.class).isEmpty()) {
            return goToNext().build();
        } else {
            return collectRegField(context);
        }
    }

    private String getAuthDataScript(String scriptURL, String verificationChoice) {
        return "var loadJS = function(url, implementationCode, location){\r\n" +
                "    var scriptTag = document.createElement('script');\r\n" + "    scriptTag.src = url;\r\n" +
                "    scriptTag.onload = implementationCode;\r\n" +
                "    scriptTag.onreadystatechange = implementationCode;\r\n" +
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
                "var yourCodeToBeCalled = function(){\r\n" +
                "}\r\n" + "loadJS(" + "\"" + scriptURL + "\"" + ", yourCodeToBeCalled, document.body);";
    }
}