package com.houstoninc.elevator;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class DefaultHandlerTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(DefaultHandler.class);
    }

    /**
     * Test to see that the message "Got it!" is sent in the response.
     */
    @Test
    public void testGetIt() {
        final String responseMsg = target().path("/").request().get(String.class);

        assertEquals("I'm a little elevator. Please POST state here to get where I want to go.", responseMsg);
    }
}
