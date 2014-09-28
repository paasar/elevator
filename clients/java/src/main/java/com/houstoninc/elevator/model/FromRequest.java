package com.houstoninc.elevator.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FromRequest {
    @JsonProperty
    public Integer floor;
    @JsonProperty
    public boolean impatient;
    @JsonProperty
    public Direction direction;

    public String toString() {
        return String.format(
                "\t{\"floor\": %s, \"impatient\": %s, \"direction\": %s}\n\t",
                floor,
                impatient,
                direction);
    }
}
