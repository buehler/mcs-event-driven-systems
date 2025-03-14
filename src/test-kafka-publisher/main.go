package main

import (
	"log"
	"os"

	inventoryCmds "github.com/buehler/mcs-event-driven-systems/test-kafka-publisher/gen/commands/inventory/v1"
	"github.com/confluentinc/confluent-kafka-go/kafka"
	"google.golang.org/protobuf/encoding/protojson"
	"google.golang.org/protobuf/proto"
)

func main() {
	args := os.Args[1:]

	if len(args) < 3 {
		panic("Usage: go run main.go <topic> <event type> <data>")
	}

	topic := args[0]
	eventType := args[1]
	jsonData := args[2]

	cfg := kafka.ConfigMap{
		"bootstrap.servers": "localhost:9092",
	}

	p, _ := kafka.NewProducer(&cfg)
	defer p.Close()

	var msgData []byte
	switch eventType {
	case "AddToInventory":
		var msg inventoryCmds.AddToInventory
		if err := protojson.Unmarshal([]byte(jsonData), &msg); err != nil {
			log.Fatalf("failed to unmarshal JSON: %v", err)
		}
		msgData, _ = proto.Marshal(&msg)
	default:
		panic("Invalid event type")
	}

	msg := kafka.Message{
		TopicPartition: kafka.TopicPartition{
			Topic:     &topic,
			Partition: kafka.PartitionAny,
		},
		Value: msgData,
		Headers: []kafka.Header{
			{Key: "messageType", Value: []byte(eventType)},
		},
	}

	p.Produce(&msg, nil)
	p.Flush(1000)
	log.Println("Successfully sent Kafka message")
}
