package com.houstoninc.elevator;

import com.houstoninc.elevator.model.Elevator;
import com.houstoninc.elevator.model.PlayerState;
import com.houstoninc.elevator.model.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The actual logic that decides where to go next.
 *
 * PlayerState example:
   {"elevator":
        {"toRequests": [1, 3, 5, 5],
         "currentFloor": 1,
         "goingTo": 1,
         "state": "EMBARKING",
         "capacity": 6},
   "floors": 5,
   "fromRequests":
     [  {"floor": 1, "impatient": true, "direction": "UP"}
     ,  {"floor": 2, "impatient": false, "direction": "DOWN"}
     ],
   "tally":
        {"happy": 0,
         "unhappy": 0},
   "tick": 3}

 * Elevator state can be: EMBARKING, DISEMBARKING, ASCENDING or DESCENDING
 * EMBARKING can be considered also as idle.
 */
public class Logic {

    private static final Logger LOGGER = LoggerFactory.getLogger(Logic.class);

    public int decideWhichFloorToGo(PlayerState playerState) {
        Elevator elevator = playerState.elevator;
        State state = elevator.state;
        int currentFloor = elevator.currentFloor;
        int currentTarget = elevator.goingTo;
        int topFloor = playerState.floors;

        if (currentFloor == currentTarget && state == State.EMBARKING) {
            if (currentFloor == 1) {
                return topFloor;
            } else {
                int oneDown = currentFloor - 1;
                LOGGER.info("Going down ({})!", oneDown);
                return oneDown;
            }
        } else {
            LOGGER.info("I want to go to {}!", currentTarget);
            return currentTarget;
        }
    }
}
