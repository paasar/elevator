# Manually controlled client for the elevator competition

## Prerequisites

You will need [Leiningen][1] 1.7.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein ring server-headless


Runnable (uber)jar can be found in `dist/`

Run it with `dist/run.sh` or `dist/run.bat` or
    java -jar elevator-control.jar

After modifying source you can create a new uberjar with
    lein ring uberjar

## License

Created by Ari Paasonen, ari (dot) paasonen (at) raah (dot) fi

CC BY-NC
