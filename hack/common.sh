#/bin/bash

function sendEvent() {
    topic=$1
    event=$2
    payload=$3

    echo "Send event '$event' to topic '$topic' with payload '$payload'."
    docker compose run -d --rm test-publisher "$topic" "$event" "$payload" 2> /dev/null
}
