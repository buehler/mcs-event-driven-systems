package ch.unisg.edpo.conveyor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ConveyorApplication

fun main(args: Array<String>) {
    runApplication<ConveyorApplication>(*args)
}
