package com.houstoninc.elevator;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class PlayerState {
    @JsonProperty
    Elevator elevator;
    @JsonProperty
    Integer floors;
    @JsonProperty
    List<FromRequest> fromRequests;
    @JsonProperty
    Tally tally;
    @JsonProperty
    Integer tick;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Elevator {
        @JsonProperty
        List<Integer> toRequests;
        @JsonProperty
        Integer currentFloor;
        @JsonProperty
        Integer goingTo;
        @JsonProperty
        State state;
        @JsonProperty
        Integer capacity;

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

    public enum State {
        WAITING,
        EMBARKING,
        DISEMBARKING,
        ASCENDING,
        DESCENDING
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FromRequest {
        @JsonProperty
        Integer floor;
        @JsonProperty
        boolean impatient;
        @JsonProperty
        Direction direction;

        public String toString() {
            return String.format(
                    "\t{\"floor\": %s, \"impatient\": %s, \"direction\": %s}\n\t",
                    floor,
                    impatient,
                    direction);
        }
    }

    public enum Direction {
        UP,
        DOWN
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Tally {
        @JsonProperty
        Integer happy;
        @JsonProperty
        Integer unhappy;

        public String toString() {
            return String.format(
                    "\t{\"happy\": %s,\n" +
                    "\t \"unhappy\": %s}",
                    happy,
                    unhappy);
        }
    }

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
