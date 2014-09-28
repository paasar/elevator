package com.houstoninc.elevator.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Tally {
    @JsonProperty
    public Integer happy;
    @JsonProperty
    public Integer unhappy;

    public String toString() {
        return String.format(
                "\t{\"happy\": %s,\n" +
                "\t \"unhappy\": %s}",
                happy,
                unhappy);
    }
}
