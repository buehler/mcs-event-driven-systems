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
    result=$(echo "$result" | \
            buf convert \
                src/protobuf/commands/inventory/v1/inventory.proto \
                --type commands.inventory.v1.AddToInventory \
                --from -#format=json --to -#format=binpb)
    just cli-produce commands "$result"

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

@cli-produce name value:
    docker compose exec kafka \
    sh -c "echo '{{value}}' | /opt/bitnami/kafka/bin/kafka-console-producer.sh \
    --bootstrap-server 'kafka:9092' \
    --topic '{{name}}'"
