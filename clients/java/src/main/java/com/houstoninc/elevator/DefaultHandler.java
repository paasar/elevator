package com.houstoninc.elevator;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("/")
public class DefaultHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHandler.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Logic logic = new Logic();

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "I'm a little elevator. Please POST state here to get where I want to go.";
    }

    @POST
    @Consumes(value = "application/json")
    @Produces(value = "application/json")
    public String whereToGo(String requestBody) {
        PlayerState state = jsonToPlayerState(requestBody);

        LOGGER.info("Server is asking where to go.");
        LOGGER.debug("State:\n{}", state);

        return "{\"go-to\": " + logic.decideWhichFloorToGo(state) + "}";
    }

    private PlayerState jsonToPlayerState(String requestBody) {
        PlayerState state = null;
        try {
            state = OBJECT_MAPPER.readValue(requestBody, PlayerState.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return state;
    }
}
