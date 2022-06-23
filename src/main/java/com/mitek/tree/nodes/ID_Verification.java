package com.mitek.tree.nodes;

import com.google.common.collect.ImmutableList;
import com.sun.identity.authentication.client.AuthClientUtils;
import org.forgerock.openam.auth.node.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.ConfirmationCallback;
import javax.security.auth.callback.TextOutputCallback;
import java.util.ArrayList;
import java.util.List;

import static org.forgerock.openam.auth.node.api.Action.send;

@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass = ID_Verification.Config.class)
public class ID_Verification extends SingleOutcomeNode {
    private Logger logger = LoggerFactory.getLogger(AuthClientUtils.class);

    /**
     * Configuration for the node.
     */
    public interface Config {

    }

    @Inject
    public ID_Verification() {
    }

    List<Callback> cbList = new ArrayList<>();

    private Action collectRegField(TreeContext context) {
        try {
            logger.info("Collecting user consent for authentication");

            TextOutputCallback label1 = new TextOutputCallback(0, "Let verify your identity in 2 simple steps.");
            cbList.add(label1);

            TextOutputCallback label2 = new TextOutputCallback(0, "Select document type & country of issue.");
            cbList.add(label2);

            TextOutputCallback label3 = new TextOutputCallback(0, "Capture ID document");
            cbList.add(label3);


            TextOutputCallback label4 = new TextOutputCallback(0, "I agree to the terms of service.");
            cbList.add(label4);


            String[] choices = {"Next"};
            ConfirmationCallback confirmationCallback = new ConfirmationCallback(0, choices, 0);
            cbList.add(confirmationCallback);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


        return send(ImmutableList.copyOf(cbList)).build();
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        logger.debug("*********************ID Verification node********************");
        try {

            if ((!context.getCallback(ConfirmationCallback.class).isEmpty()) && context.getCallback(ConfirmationCallback.class).get().getSelectedIndex() == 0) {
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
}
