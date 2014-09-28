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
var logic = {
  decideWhichFloorToGo : function(playerState) {
    console.log("PlayerState:");
    console.log(playerState);

    var elevator = playerState.elevator;
    var state = elevator.state;
    var currentFloor = elevator.currentFloor;
    var currentTarget = elevator.goingTo;
    var topFloor = playerState.floors;

    if (currentFloor === currentTarget && state === "EMBARKING") {
      if (currentFloor === 1) {
        return topFloor;
      } else {
        var oneDown = currentFloor - 1;
        console.log("Going down (" + oneDown + ")!");
        return oneDown;
      }
    } else {
      console.log("I want to go to " + currentTarget + "!");
      return currentTarget;
    }
  }
}

module.exports = logic;