# Sensors

This application is responsible for "forwarding" the sensor read data from MQTT to the
warehouse Kafka system. It is written in Rust to provide the fastest possible implementation
of the sensor data forwarding. It reads the MQTT events, transforms them accordingly
and forwards them onto Kafka with the protobuf encoding that is found in the
[protobuf](../protobuf) directory.
