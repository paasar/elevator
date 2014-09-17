# Elevator competition client template made with Clojure

## Prerequisites

You will need [Leiningen][1] 1.7.0 or above installed.

[1]: https://github.com/technomancy/leiningen

Actual logic is in `./src/elevator/logic.clj`

## Running

To start a web server for the application, run:

    lein ring server-headless

Default port is `3333` and it is set in `./project.clj`

To use different port from command line, run:

    lein ring server-headless 3334

## License

Created by Ari Paasonen, ari (dot) paasonen (at) raah (dot) fi

CC BY-NC
