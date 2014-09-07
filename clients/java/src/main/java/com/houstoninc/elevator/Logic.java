package com.houstoninc.elevator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The actual logic that decides where to go next.
 *
 * Stateless approach is recommended.
 *
 * PlayerState example:
   {"elevator":
        {"toRequests": [1, 3, 5, 5],
         "currentFloor": 1,
         "goingTo": 1,
         "state": WAITING,
         "capacity": 1},
   "floors": 5,
   "fromRequests":
     [  {"floor": 1,
         "impatient": true,
         "direction": UP}
     ,  {"floor": 2,
         "impatient": false,
         "direction": DOWN}
     ],
   "tally":
        {"happy": 0,
         "unhappy": 0},
   "tick": 3}
 */
public class Logic {

    private static final Logger LOGGER = LoggerFactory.getLogger(Logic.class);

    public int decideWhichFloorToGo(PlayerState state) {
        int currentFloor = state.elevator.currentFloor;
        int topFloor = state.floors;
        int currentTarget = state.elevator.goingTo;

        if (currentFloor != currentTarget) {
            LOGGER.info("I want to go to {}!", currentTarget);
            return currentTarget;
        } else {
            if (currentFloor == 1) {
                return topFloor;
            } else {
                int oneDown = currentFloor - 1;
                LOGGER.info("Going down ({})!", oneDown);
                return oneDown;
            }
        }
    }
}
