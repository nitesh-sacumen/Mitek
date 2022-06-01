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
 * @author Sacumen (www.sacumen.com)
 * <p>
 * Setting and adding all the Prove Service URLs to the shared state.
 */
@Node.Metadata(outcomeProvider = SingleOutcomeNode.OutcomeProvider.class, configClass = MitekConfiguration.Config.class)
public class MitekConfiguration extends SingleOutcomeNode {
    private Logger logger = LoggerFactory.getLogger(MitekConfiguration.class);
    private final Config config;

    /**
     * Configuration for the node.
     */
    public interface Config {
        @Attribute(order = 100, requiredValue = true)
        String apiUrl();

        @Attribute(order = 200, requiredValue = true)
        String clientId();

        @Attribute(order = 300, requiredValue = true)
        String clientSecret();

        @Attribute(order = 400, requiredValue = true)
        String scope();

    }

    /**
     * Create the node.
     */
    @Inject
    public MitekConfiguration(@Assisted Config config) {
        this.config = config;
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        logger.info("Collecting and storing Prove Api input in shared state");
        JsonValue sharedState = context.sharedState;
        sharedState.put(Constants.API_URL, config.apiUrl());
        sharedState.put(Constants.CLIENT_ID, config.clientId());
        sharedState.put(Constants.CLIENT_SECRET, config.clientSecret());
        sharedState.put(Constants.SCOPE, config.scope());
        return goToNext().replaceSharedState(context.sharedState).build();
    }

}
