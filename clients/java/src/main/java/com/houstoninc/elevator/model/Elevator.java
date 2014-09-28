package com.houstoninc.elevator.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Elevator {
    @JsonProperty
    public List<Integer> toRequests;
    @JsonProperty
    public Integer currentFloor;
    @JsonProperty
    public Integer goingTo;
    @JsonProperty
    public State state;
    @JsonProperty
    public Integer capacity;

    public String toString() {
        return String.format(
                "\t{\"toRequests\": %s,\n" +
                "\t \"currentFloor\": %s,\n" +
                "\t \"goingTo\": %s,\n" +
                "\t \"state\": %s,\n" +
                "\t \"capacity\": %s}",
                toRequests,
                currentFloor,
                goingTo,
                state,
                capacity);
    }
}
