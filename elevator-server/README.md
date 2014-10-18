# Elevator competition server made with Clojure

## Prerequisites

You will need [Leiningen][1] 1.7.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Parts

### Tests

Test can be found under ./test/elevator_server/

And are run from elevator-server root directory with command
    lein test

### Implementation

Implementation can be found under ./src/elevator_server/
HTML, properties and json templates/examples can be found under ./resources

* constants.clj - general constants like maximum number of floors.
* core.clj - main logic of manipulating game state and the like.
* elevator_state.clj - elevator state manipulation functions
* handler.clj - URL-mappings
* request_generator.clj - logic for creating elevator requests in floors
* rest_client.clj - client polling functionality
* scheduler.clj - scheduled tasks for advancing game state and polling clients
* util.clj - some helper functions
* view.clj - internal state data transformations into form used by the game view page

## Running

To start a web server for the application, run:

    lein ring server-headless

Then you can access following URLs:
http://localhost:3000/      - Player signup form
http://localhost:3000/game  - Game view
http://localhost:3000/admin - Admin console

## Ideas for improvement
* Test connection to players in admin page. Do a get request by pushing a button.
* Better game state logging. In current format the log is pretty hard to parse afterwards.
* People in floors respects the direction of the elevator. How to do this as elevator can go either way after embarking?
* Run simulations to find better request generation algorithm and/or other game parameters for more fun experience. Currently there are too long pauses and maybe a bit too big request waves from time to time.
* More than one elevator per player?

## License

Created by Ari Paasonen, ari (dot) paasonen (at) raah (dot) fi

CC BY-NC
