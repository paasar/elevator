"""
 The actual logic that decides where to go next.

 PlayerState example:
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
"""

def decide_which_floor_to_go(state):
  elevator = state['elevator']
  current_floor = elevator['currentFloor']
  current_target = elevator['goingTo']
  top_floor = state['floors']

  if (current_floor != current_target):
    print "I want to go to " + str(current_target) + "!"
    return current_target
  else:
    if (current_floor == 1):
      return top_floor
    else:
      one_down = current_floor - 1
      print "Going down (" + str(one_down) + ")!"
      return one_down
