package com.houstoninc.elevator.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class PlayerState {
    @JsonProperty
    public Elevator elevator;
    @JsonProperty
    public Integer floors;
    @JsonProperty
    public List<FromRequest> fromRequests;
    @JsonProperty
    public Tally tally;
    @JsonProperty
    public Integer tick;

    public String toString() {
        return String.format(
                "{\"elevator\":\n%s,\n" +
                "\"floors\": %s,\n" +
                "\"fromRequests\":\n\t%s,\n" +
                "\"tally\":\n%s,\n" +
                "\"tick\": %s}",
                elevator,
                floors,
                fromRequests,
                tally,
                tick);
    }
}
