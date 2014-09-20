/**
 * The actual logic that decides where to go next.
 *
 * PlayerState example:
   {"elevator":
        {"toRequests": [1, 3, 5, 5],
         "currentFloor": 1,
         "goingTo": 1,
         "state": "WAITING",
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
 */
var logic = {
  decideWhichFloorToGo : function(state) {
    console.log("PlayerState:");
    console.log(state);

    var currentFloor = state.elevator.currentFloor;
    var currentTarget = state.elevator.goingTo;
    var topFloor = state.floors;

    if (currentFloor !== currentTarget) {
      console.log("I want to go to " + currentTarget + "!");
      return currentTarget;
    } else {
      if (currentFloor === 1) {
        return topFloor;
      } else {
        var oneDown = currentFloor - 1;
        console.log("Going down (" + oneDown + ")!");
        return oneDown;
      }
    }
  }
}

module.exports = logic;