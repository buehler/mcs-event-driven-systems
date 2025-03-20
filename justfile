@default:
    just --list

@compose +ARGS:
    docker compose {{ARGS}}

@compose-dev +ARGS:
    docker compose -f docker-compose.yml -f docker-compose.dev.yml {{ARGS}}

@send-rotary-data value:
    #!/bin/bash
    timestamp=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
    result="{\"sensor_id\": \"abc\", \"location\": \"HumanWS\", \"timestamp\": \"$timestamp\", \"rotary\": {\"position\": {{value}} } }"
    echo "send $result to mqtt sensor topic"
    docker compose exec mqtt mosquitto_pub -t Tinkerforge/HumanWS/rotary_abc -m "$result"

@send-add-to-inventory-command color:
    #!/bin/bash
    result="{\"color\": \"BLOCK_COLOR_{{uppercase(color)}}\"}"
    echo "send $result"
    just produce "commands" "AddToInventory" "$result"

@send-move-block-on-conveyor-command:
    #!/bin/bash
    result="{}"
    echo "send $result"
    just produce "commands" "ConveyorMoveBlock" "$result"

@send-block-to-nfc-command position="top_left":
    #!/bin/bash
    result="{\"position\": \"PICKUP_POSITION_{{uppercase(position)}}\"}"
    echo "send $result"
    just produce "commands" "MoveBlockFromShipmentToNfc" "$result"

@send-block-to-conveyor-command:
    #!/bin/bash
    result="{}"
    echo "send $result"
    just produce "commands" "MoveBlockFromNfcToConveyor" "$result"

@send-sort-block-command color:
    #!/bin/bash
    result="{\"color\": \"BLOCK_COLOR_{{uppercase(color)}}\"}"
    echo "send $result"
    just produce "commands" "SortBlock" "$result"

@create-topic name partitions='1' replication_factor='1':
    docker compose exec kafka \
        /opt/bitnami/kafka/bin/kafka-topics.sh \
        --create --bootstrap-server 'kafka:9092' \
        --replication-factor {{replication_factor}} \
        --partitions {{partitions}} \
        --topic '{{name}}'

@list-topics:
    docker compose exec kafka \
    /opt/bitnami/kafka/bin/kafka-topics.sh \
    --bootstrap-server 'kafka:9092' \
    --list 

@delete-topic name:
    docker compose exec kafka \
    /opt/bitnami/kafka/bin/kafka-topics.sh \
    --bootstrap-server 'kafka:9092' \
    --delete --topic '{{name}}'

@consume-topic name:
    docker compose exec kafka \
    /opt/bitnami/kafka/bin/kafka-console-consumer.sh \
    --bootstrap-server 'kafka:9092' \
    --topic '{{name}}' \
    --from-beginning

[private]
[working-directory: 'src/test-kafka-publisher']
@produce topic eventname data:
    go run main.go '{{topic}}' '{{eventname}}' '{{data}}'
