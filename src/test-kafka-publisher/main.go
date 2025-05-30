package main

import (
	"fmt"
	"log"
	"os"
	"reflect"

	inventoryCmds "github.com/buehler/mcs-event-driven-systems/test-kafka-publisher/gen/commands/inventory/v1"
	machineCmds "github.com/buehler/mcs-event-driven-systems/test-kafka-publisher/gen/commands/machines/v1"
	inventoryEvts "github.com/buehler/mcs-event-driven-systems/test-kafka-publisher/gen/events/inventory/v1"
	machineEvts "github.com/buehler/mcs-event-driven-systems/test-kafka-publisher/gen/events/machines/v1"
	sensorEvts "github.com/buehler/mcs-event-driven-systems/test-kafka-publisher/gen/events/sensors/v1"
	"github.com/kelseyhightower/envconfig"

	"github.com/confluentinc/confluent-kafka-go/kafka"
	"google.golang.org/protobuf/encoding/protojson"
	"google.golang.org/protobuf/proto"
)

type AppConfig struct {
	KafkaHost string `envconfig:"KAFKA_HOST" default:"localhost"`
	KafkaPort uint16 `envconfig:"KAFKA_PORT" default:"9092"`
}

func encode[T proto.Message](jsonData string) []byte {
	msg := reflect.New(reflect.TypeOf((*T)(nil)).Elem().Elem()).Interface().(T)
	if err := protojson.Unmarshal([]byte(jsonData), msg); err != nil {
		log.Fatalf("failed to unmarshal JSON: %v", err)
	}
	msgData, _ := proto.Marshal(msg)
	return msgData
}

func main() {
	var config AppConfig
	envconfig.Process("sensors", &config)

	args := os.Args[1:]

	if len(args) < 3 {
		panic("Usage: go run main.go <topic> <event type> <data>")
	}

	topic := args[0]
	eventType := args[1]
	jsonData := args[2]

	cfg := kafka.ConfigMap{
		"bootstrap.servers": fmt.Sprintf("%s:%d", config.KafkaHost, config.KafkaPort),
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
	case "RightObjectDetected":
		msgData = encode[*sensorEvts.RightObjectDetected](jsonData)
	case "RightObjectRemoved":
		msgData = encode[*sensorEvts.RightObjectRemoved](jsonData)
	case "LeftObjectDetected":
		msgData = encode[*sensorEvts.LeftObjectDetected](jsonData)
	case "LeftObjectRemoved":
		msgData = encode[*sensorEvts.LeftObjectRemoved](jsonData)
	case "NFCDistDetected":
		msgData = encode[*sensorEvts.NFCDistDetected](jsonData)
	case "NFCDistRemoved":
		msgData = encode[*sensorEvts.NFCDistRemoved](jsonData)
	case "NFCObjectDetected":
		msgData = encode[*sensorEvts.NFCObjectDetected](jsonData)
	case "NFCObjectRemoved":
		msgData = encode[*sensorEvts.NFCObjectRemoved](jsonData)
	case "BlockPositionedOnNfc":
		msgData = encode[*machineEvts.BlockPositionedOnNfc](jsonData)
	case "BlockPositionedOnConveyor":
		msgData = encode[*machineEvts.BlockPositionedOnConveyor](jsonData)
	case "BlockPositionedOnColorDetector":
		msgData = encode[*machineEvts.BlockPositionedOnColorDetector](jsonData)
	case "ConveyorBlockMoved":
		msgData = encode[*machineEvts.ConveyorBlockMoved](jsonData)
	case "AreaClearButtonPressed":
		msgData = encode[*sensorEvts.AreaClearButtonPressed](jsonData)
	case "ColorDetected":
		msgData = encode[*sensorEvts.ColorDetected](jsonData)
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
