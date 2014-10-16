# Extreme startup coding competition - Elevator logic

Have you ever waited too long for an elevator to come?
Now is your chance to prove that you can program an elevator that is always in the correct place.

This project offers a game server programmed in Clojure and a few client templates in different programming languages.

Clients are basic REST servers. They are polled by the server for their next desired action or target floor.
Communication data format is JSON.

See server and client readme files for more detailed information.

## Prerequisites

You will need Leiningen to run the server.

For client programs following programs are needed:
* Clojure: Leiningen
* Java: maven
* JavaScript: node.js
* Python: web.py

There is also a Swing GUI based client for manual elevator guidance.

## Known issues
* A firewall can prevent communication between the server and a client. Make sure firewall is properly configured before starting.

## License

Created by Ari Paasonen, ari (dot) paasonen (at) raah (dot) fi

CC BY-NC
