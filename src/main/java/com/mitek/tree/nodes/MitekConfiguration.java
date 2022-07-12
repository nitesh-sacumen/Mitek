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
        String consentData();

        @Attribute(order = 200, requiredValue = true)
        String clientId();

        @Attribute(order = 300, requiredValue = true)
        String clientSecret();

        @Attribute(order = 400, requiredValue = true)
        String scope();

        @Attribute(order = 500, requiredValue = true)
        String grantType();

        @Attribute(order = 600, requiredValue = true)
        Integer retakeCount();

        @Attribute(order = 700, requiredValue = true)
        Integer retryCount();

        @Attribute(order = 800, requiredValue = true)
        Integer timeoutValue();

        @Attribute(order = 900, requiredValue = true)
        String APIUrl();

        @Attribute(order = 1000, requiredValue = true)
        String scriptFilePath();
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
        if (config.clientId() == null || config.clientSecret() == null ||
                config.scope() == null || config.grantType() == null
                || config.retakeCount() == null || config.retryCount() == null || config.timeoutValue() == null
                || config.APIUrl() == null || config.scriptFilePath() == null) {
            logger.error("Please configure apiUrl/clientId/clientSecret/scope/grantType/retakeCount/retryCount/timeoutValue/APIUrl/scriptFilePath to proceed");
            throw new NodeProcessException("Invalid credentials!!");
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
        sharedState.put(Constants.JS_URL, config.scriptFilePath());
        return goToNext().replaceSharedState(context.sharedState).build();
    }

}
