package com.mitek.tree.nodes;

import com.google.common.collect.ImmutableList;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
import com.sun.identity.authentication.client.AuthClientUtils;
import org.forgerock.openam.auth.node.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.callback.TextOutputCallback;
import java.util.ArrayList;
import java.util.List;

import static org.forgerock.openam.auth.node.api.Action.send;

@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass = VerificationSuccess.Config.class)
public class VerificationSuccess extends SingleOutcomeNode {


    private Logger logger = LoggerFactory.getLogger(AuthClientUtils.class);

    /**
     * Configuration for the node.
     */
    public interface Config {

    }

    List<Callback> cbList = new ArrayList<>();

    private Action collectRegField(TreeContext context) {
        try {

            TextOutputCallback label1 = new TextOutputCallback(0, "Verification Complete");
            cbList.add(label1);

            TextOutputCallback label2 = new TextOutputCallback(0, "Thank you!");
            cbList.add(label2);

            TextOutputCallback label3 = new TextOutputCallback(0, "Your verification is complete.");
            cbList.add(label3);

            String[] choices = {"Next"};
            ConfirmationCallback confirmationCallback = new ConfirmationCallback(0, choices, 0);
            cbList.add(confirmationCallback);

            ScriptTextOutputCallback scriptTextOutputCallback = new ScriptTextOutputCallback(getAuthDataScript());
            cbList.add(scriptTextOutputCallback);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


        return send(ImmutableList.copyOf(cbList)).build();
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        logger.debug("*********************VerificationSuccess node********************");
        try {

            if ((context.hasCallbacks())) {
                return goToNext().build();
            } else {
                return collectRegField(context);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            throw new NodeProcessException("Exception is: " + e);
        }
    }

    private String getAuthDataScript() {

        return "if (document.contains(document.getElementById('footer'))) {\n" +
                "document.getElementById('footer').style.marginBottom='0px';\n" +
                "}\n" +

                "if (document.contains(document.getElementById('parentDiv'))) {\n" +
                "document.getElementById('parentDiv').remove();\n" +
                "}\n" +


                "if (document.contains(document.getElementById('mitekMediaContainer'))) {\n" +
                "document.getElementById('mitekMediaContainer').remove();\n" +
                "}\n" +


                "if (document.contains(document.getElementById('uiContainer'))) {\n" +
                "document.getElementById('uiContainer').remove();\n" +
                "}\n" +

                "if (document.contains(document.getElementById('mitekScript'))) {\n" +
                "document.getElementById('mitekScript').remove();\n" +
                "}\n" +

                "if (document.contains(document.getElementById('capturedTimeout'))) {\n" +
                "document.getElementById('capturedTimeout').remove();\n" +
                "}\n" +

                "if (document.contains(document.getElementById('hidden'))) {\n" +
                "document.getElementById('hidden').remove();\n" +
                "}\n" +
                "if (document.contains(document.getElementById('capturedImageContainer'))) {\n" +
                "document.getElementById('capturedImageContainer').remove();\n" +
                "}\n";
    }
}