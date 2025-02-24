
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

@produce-topic name message:
    docker compose exec kafka \
    /opt/bitnami/kafka/bin/kafka-console-producer.sh \
    --bootstrap-server 'kafka:9092' \
    --topic '{{name}}'
