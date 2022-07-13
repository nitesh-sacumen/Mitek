package com.mitek.tree;

import com.mitek.tree.config.Constants;
import com.mitek.tree.nodes.MitekConfiguration;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.ExternalRequestContext;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.security.auth.callback.Callback;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.forgerock.json.JsonValue.*;
import static org.forgerock.json.JsonValue.field;
import static org.forgerock.openam.auth.node.api.SharedStateConstants.USERNAME;
import static org.mockito.MockitoAnnotations.initMocks;

public class MitekConfigurationTest {
    @InjectMocks
    MitekConfiguration mitekConfiguration;

    @Mock
    MitekConfiguration.Config config;

    @BeforeMethod
    public void before() {
        initMocks(this);
    }

    @Test
    public void testMitekConfigurationWithFailure(){
        TreeContext treeContext = buildThreeContext(Collections.emptyList());

        Exception exception = Assert.expectThrows(NodeProcessException.class, () -> {
            mitekConfiguration.process(treeContext);
        });

        String expectedMessage = "Invalid credentials!!";
        String actualMessage = exception.getMessage();
        Assert.assertEquals(actualMessage,expectedMessage);
    }

    @Test
    public void testMitekConfigurationWithSuccess() throws NodeProcessException {
        mitekConfiguration = new MitekConfiguration(config);
        Mockito.when(config.APIUrl()).thenReturn("www.test.com");
        Mockito.when(config.clientId()).thenReturn("clientID");
        Mockito.when(config.clientSecret()).thenReturn("secret");
        Mockito.when(config.grantType()).thenReturn("testGrant");
        Mockito.when(config.retakeCount()).thenReturn(3);
        Mockito.when(config.retryCount()).thenReturn(3);
        Mockito.when(config.scope()).thenReturn("test");
        Mockito.when(config.timeoutValue()).thenReturn(3000);
        Mockito.when(config.consentData()).thenReturn("consent data");

        TreeContext treeContext = buildThreeContext(Collections.emptyList());

        Action action = mitekConfiguration.process(treeContext);

        JsonValue jsonValue = treeContext.sharedState;
        Assert.assertEquals(jsonValue.get(Constants.CLIENT_ID).asString(),"clientID");
        Assert.assertEquals(jsonValue.get(Constants.API_URL).asString(),"www.test.com");
        Assert.assertEquals(jsonValue.get(Constants.CLIENT_SECRET).asString(),"secret");
        Assert.assertEquals(jsonValue.get(Constants.MAX_RETAKE_COUNT).asInteger(),3);
        Assert.assertEquals(jsonValue.get(Constants.MAX_RETRY_COUNT).asInteger(),3);
        Assert.assertEquals(jsonValue.get(Constants.SCOPE).asString(),"test");
        Assert.assertEquals(jsonValue.get(Constants.TIMEOUT_VALUE).asInteger(),3000);
        Assert.assertEquals(jsonValue.get(Constants.CONSENT_DATA).asString(),"consent data");

        Assert.assertNotNull(action);
    }



    private TreeContext buildThreeContext(List<Callback> callbacks) {
        return new TreeContext(retrieveSharedState(), json(object()),
                new ExternalRequestContext.Builder().build(), callbacks
                , Optional.of("mockUserId"));
    }

    private JsonValue retrieveSharedState() {
        return json(object(field(USERNAME, "demo")));
    }
}
