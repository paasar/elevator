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

constants.clj - general constants like maximum number of floors.
core.clj - main logic of manipulating game state and the like.
elevator_state.clj - elevator state manipulation functions
handler.clj - URL-mappings
request_generator.clj - logic for creating elevator requests in floors
rest_client.clj - client polling functionality
scheduler.clj - scheduled tasks for advancing game state and polling clients
util.clj - some helper functions
view.clj - internal state data transformations into form used by the game view page

## Running

To start a web server for the application, run:

    lein ring server-headless

Then you can access following URLs:
http://localhost:3000/      - Player signup form
http://localhost:3000/game  - Game view
http://localhost:3000/admin - Admin console

## License

Created by Ari Paasonen, ari (dot) paasonen (at) raah (dot) fi

CC BY-NC
