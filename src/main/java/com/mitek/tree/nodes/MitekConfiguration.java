package com.mitek.tree.nodes;

import com.google.inject.assistedinject.Assisted;
import com.mitek.tree.config.Constants;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * @author Sacumen(www.sacumen.com) Mitek configuration node with
 * single outcome. This node will get mitek configuration and put it thorugh shared context.
 */
@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass = MitekConfiguration.Config.class)
public class MitekConfiguration extends SingleOutcomeNode {
    private static final Logger logger = LoggerFactory.getLogger(MitekConfiguration.class);
    private final Config config;

    /**
     * Configuration for the node.
     */
    public interface Config {

        @Attribute(order = 100)
        default String consentData() {
            return "";
        }

        @Attribute(order = 200, requiredValue = true)
        default String clientId() {
            return "";
        }

        @Attribute(order = 300, requiredValue = true)
        default String clientSecret() {
            return "";
        }

        @Attribute(order = 400, requiredValue = true)
        default String scope() {
            return "";
        }

        @Attribute(order = 500, requiredValue = true)
        default String grantType() {
            return "";
        }

        @Attribute(order = 600, requiredValue = true)
        default Integer retakeCount() {
            return 0;
        }

        @Attribute(order = 700, requiredValue = true)
        default Integer retryCount() {
            return 0;
        }

        @Attribute(order = 800, requiredValue = true)
        default Integer timeoutValue() {
            return 0;
        }

        @Attribute(order = 900, requiredValue = true)
        default String APIUrl() {
            return "";
        }

        @Attribute(order = 1000, requiredValue = true)
        default String scriptFolderPath() {
            return "";
        }
    }

    /**
     * Create the node.
     */
    @Inject
    public MitekConfiguration(@Assisted Config config) {
        this.config = config;
    }

    /**
     * @param context
     * @return Action, Which will redirect to next action.
     */
    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        JsonValue sharedState = context.sharedState;
        if (config.clientId().equals("") || config.clientSecret().equals("") ||
                config.scope().equals("") || config.grantType().equals("")
                || config.APIUrl().equals("") || config.scriptFolderPath().equals("")) {
            logger.error("Please configure apiUrl/clientId/clientSecret/scope/grantType/APIUrl/scriptFolderPath to proceed");
            throw new NodeProcessException("Invalid/Missing credentials!!");
        }
        sharedState.put(Constants.CLIENT_ID, config.clientId());
        sharedState.put(Constants.CLIENT_SECRET, config.clientSecret());
        sharedState.put(Constants.SCOPE, config.scope());
        sharedState.put(Constants.GRANT_TYPE, config.grantType());
        sharedState.put(Constants.CONSENT_DATA, config.consentData());
        sharedState.put(Constants.MAX_RETAKE_COUNT, config.retakeCount());
        sharedState.put(Constants.MAX_RETRY_COUNT, config.retryCount());
        sharedState.put(Constants.TIMEOUT_VALUE, config.timeoutValue());
        sharedState.put(Constants.API_URL, config.APIUrl());
        sharedState.put(Constants.MITEK_FOLDER_URL, config.scriptFolderPath());
        return goToNext().replaceSharedState(context.sharedState).build();
    }

}
