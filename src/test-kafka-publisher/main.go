package main

import (
	"log"
	"os"
	"reflect"

	inventoryCmds "github.com/buehler/mcs-event-driven-systems/test-kafka-publisher/gen/commands/inventory/v1"
	machineCmds "github.com/buehler/mcs-event-driven-systems/test-kafka-publisher/gen/commands/machines/v1"
	inventoryEvts "github.com/buehler/mcs-event-driven-systems/test-kafka-publisher/gen/events/inventory/v1"
	machineEvts "github.com/buehler/mcs-event-driven-systems/test-kafka-publisher/gen/events/machines/v1"
	"github.com/confluentinc/confluent-kafka-go/kafka"
	"google.golang.org/protobuf/encoding/protojson"
	"google.golang.org/protobuf/proto"
)

func encode[T proto.Message](jsonData string) []byte {
	msg := reflect.New(reflect.TypeOf((*T)(nil)).Elem().Elem()).Interface().(T)
	if err := protojson.Unmarshal([]byte(jsonData), msg); err != nil {
		log.Fatalf("failed to unmarshal JSON: %v", err)
	}
	msgData, _ := proto.Marshal(msg)
	return msgData
}

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
	case "BlockSorted":
		msgData = encode[*machineEvts.BlockSorted](jsonData)
	case "ConveyorMoveBlock":
		msgData = encode[*machineCmds.ConveyorMoveBlock](jsonData)
	case "MoveBlockFromShipmentToNfc":
		msgData = encode[*machineCmds.MoveBlockFromShipmentToNfc](jsonData)
	case "MoveBlockFromNfcToConveyor":
		msgData = encode[*machineCmds.MoveBlockFromNfcToConveyor](jsonData)
	case "SortBlock":
		msgData = encode[*machineCmds.SortBlock](jsonData)
	case "MoveBlockFromConveyorToColorDetector":
		msgData = encode[*machineCmds.MoveBlockFromConveyorToColorDetector](jsonData)
	case "ProcessNewShipment":
		msgData = encode[*inventoryCmds.ProcessNewShipment](jsonData)
	case "ShipmentProcessed":
		msgData = encode[*inventoryEvts.ShipmentProcessed](jsonData)
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

	_ = p.Produce(&msg, nil)
	p.Flush(1000)
	log.Println("Successfully sent Kafka message")
}
