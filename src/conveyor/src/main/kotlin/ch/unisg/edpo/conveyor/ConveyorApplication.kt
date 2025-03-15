package ch.unisg.edpo.conveyor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.kafka.annotation.EnableKafka

@SpringBootApplication
@EnableKafka
class ConveyorApplication

fun main(args: Array<String>) {
    runApplication<ConveyorApplication>(*args)
}
