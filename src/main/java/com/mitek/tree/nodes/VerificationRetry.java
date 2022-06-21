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

@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass = VerificationRetry.Config.class)
public class VerificationRetry extends SingleOutcomeNode {


    private Logger logger = LoggerFactory.getLogger(AuthClientUtils.class);

    /**
     * Configuration for the node.
     */
    public interface Config {

    }

    List<Callback> cbList = new ArrayList<>();

    private Action collectRegField(TreeContext context) {
        try {

            TextOutputCallback label1 = new TextOutputCallback(0, "Verification Error");
            cbList.add(label1);

            TextOutputCallback label2 = new TextOutputCallback(0, "Oops! There was an issue on the captured image.");
            cbList.add(label2);

            TextOutputCallback label3 = new TextOutputCallback(0, "We would like you to retry.");
            cbList.add(label3);

            TextOutputCallback label4 = new TextOutputCallback(0, "Tips for retake.");
            cbList.add(label4);

            TextOutputCallback label5 = new TextOutputCallback(0, "Make sure the lighting is proper.");
            cbList.add(label5);

            TextOutputCallback label6 = new TextOutputCallback(0, "Use dark background.");
            cbList.add(label6);

            String[] choices = {"Retry"};
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
        logger.debug("*********************VerificationRetry node********************");
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

        return "if (document.contains(document.getElementById('parentDiv'))) {\n" +
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
                "}\n";
    }
}