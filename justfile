@default:
    just --list

@compose +ARGS:
    docker compose {{ARGS}}

@compose-dev +ARGS:
    docker compose -f docker-compose.yml -f docker-compose.dev.yml {{ARGS}}

@compose-prod +ARGS:
    docker compose -f docker-compose.yml -f docker-compose.prod.yml {{ARGS}}

@restart-camunda env="dev":
    just compose-{{env}} down manager inventory
    just compose-{{env}} up -d

@sensor-button-press:
    #!/bin/bash
    echo "send MQTT sensor button press"
    just mqtt "HumanWS" "rgb_button" "24dY" '"state": "PRESSED!"'
    sleep 1
    just mqtt "HumanWS" "rgb_button" "24dY" '"state": "RELEASED!"'

@sensor-left-distance-value value:
    #!/bin/bash
    echo "send MQTT left distance sensor value {{value}}"
    just mqtt "Conveyor" "distance_IR_short" "TFu" '"distance": {{value}}'

@sensor-right-distance-value value:
    #!/bin/bash
    echo "send MQTT right distance sensor value {{value}}"
    just mqtt "HumanWS" "distance_IR_short" "TG2" '"distance": {{value}}'


@sensor-rotary-value value:
    #!/bin/bash
    echo "send MQTT rotary sensor value {{value}}"
    just mqtt "HumanWS" "rotary" "KVx" '"position": {{value}}'

@sensor-nfc-value hasTag:
    #!/bin/bash
    echo 'send MQTT NFC sensor value {{ if hasTag == "yes" { "with tag" } else { "without tag" } }}'
    just mqtt "Conveyor" "nfc" "22Mp" '{{ if hasTag == "yes" { "\"ID\": \"foobar\"" } else { " \"foo\": 1 " } }}'

@send-sensor-right-object-detected-event:
    #!/bin/bash
    result="{}"
    echo "send $result"
    just produce "events" "RightObjectDetected" "$result"

@send-sensor-nfc-object-detected-event:
    #!/bin/bash
    result="{}"
    echo "send $result"
    just produce "events" "NFCDistDetected" "$result"

@send-sensor-nfc-object-removed-event:
    #!/bin/bash
    result="{}"
    echo "send $result"
    just produce "events" "NFCDistRemoved" "$result"

@send-block-sorted-event color:
    #!/bin/bash
    result='{"color": "BLOCK_COLOR_{{uppercase(color)}}"}'
    echo "send $result"
    just produce "events" "BlockSorted" "$result"

@send-right-object-detected-event:
    #!/bin/bash
    result="{}"
    echo "send $result"
    just produce "events" "RightObjectDetected" "$result"

@send-right-object-removed-event:
    #!/bin/bash
    result="{}"
    echo "send $result"
    just produce "events" "RightObjectRemoved" "$result"

@send-left-object-detected-event:
    #!/bin/bash
    result="{}"
    echo "send $result"
    just produce "events" "LeftObjectDetected" "$result"

@send-left-object-removed-event:
    #!/bin/bash
    result="{}"
    echo "send $result"
    just produce "events" "LeftObjectRemoved" "$result"

@send-move-block-on-conveyor-command:
    #!/bin/bash
    result="{}"
    echo "send $result"
    just produce "commands" "ConveyorMoveBlock" "$result"

@send-block-positioned-on-nfc-event:
    #!/bin/bash
    result="{}"
    echo "send $result"
    just produce "events" "BlockPositionedOnNfc" "$result"

@send-block-positioned-on-conveyor-event:
    #!/bin/bash
    result="{}"
    echo "send $result"
    just produce "events" "BlockPositionedOnConveyor" "$result"

@send-block-to-nfc-command position="top_left":
    #!/bin/bash
    result='{"position": "PICKUP_POSITION_{{uppercase(position)}}"}'
    echo "send $result"
    just produce "commands" "MoveBlockFromShipmentToNfc" "$result"

@send-block-to-conveyor-command:
    #!/bin/bash
    result="{}"
    echo "send $result"
    just produce "commands" "MoveBlockFromNfcToConveyor" "$result"

@send-block-to-color-command:
    #!/bin/bash
    result="{}"
    echo "send $result"
    just produce "commands" "MoveBlockFromConveyorToColorDetector" "$result"

@send-sort-block-command color:
    #!/bin/bash
    result='{"color": "BLOCK_COLOR_{{uppercase(color)}}"}'
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

[private]
@mqtt loc type uid data:
    docker compose exec mqtt mosquitto_pub -t Tinkerforge/{{loc}}/{{type}}_{{uid}} -m '{ "UID": "{{uid}}", "type": "{{type}}", "location": "{{loc}}", {{data}} }'
