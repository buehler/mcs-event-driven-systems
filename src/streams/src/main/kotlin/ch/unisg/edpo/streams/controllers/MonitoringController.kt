package ch.unisg.edpo.streams.controllers

import ch.unisg.edpo.proto.models.v1.BlockColor
import ch.unisg.edpo.streams.AvgAggregator
import org.apache.kafka.streams.StoreQueryParameters
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class MonitoringController(private val sbBean: StreamsBuilderFactoryBean) {

    @GetMapping("/monitor")
    fun monitor(model: Model): String {
        val blocksStore = sbBean.kafkaStreams?.store(
            StoreQueryParameters.fromNameAndType(
                "windowed-blocks-handled-table",
                QueryableStoreTypes.windowStore<BlockColor, Int>()
            )
        )
        if (blocksStore == null) {
            model.addAttribute("error", "Blocks Store not found")
            return "monitor"
        }

        val conveyorStore = sbBean.kafkaStreams?.store(
            StoreQueryParameters.fromNameAndType(
                "avg-conveyor-moving-table",
                QueryableStoreTypes.keyValueStore<String, AvgAggregator>()
            )
        )
        if (conveyorStore == null) {
            model.addAttribute("error", "conveyor Store not found")
            return "monitor"
        }

        val windowedData = mutableListOf<Map<String, Any>>()
        blocksStore.all()
            .asSequence()
            .groupBy { it.key.key() }
            .filter { it.value.count() > 0 }
            .forEach { group ->
                val color = group.key.toString()
                val lastWindow = group.value.last()
                val window = lastWindow.key.window()
                val count = lastWindow.value
                windowedData.add(
                    mapOf(
                        "color" to color,
                        "windowStart" to window.startTime().toString(),
                        "windowEnd" to window.endTime().toString(),
                        "count" to count
                    )
                )
                print("Window: ${window.startTime()} - ${window.endTime()}, Color: $color, Count: $count\n")
            }

        model.addAttribute("windowedData", windowedData)

        conveyorStore.get("avg-conveyor-moving")?.let { avg ->
            model.addAttribute("avgConveyorSpeed", "${avg.get()}ms -- ${avg.count} blocks moved")
        } ?: run {
            model.addAttribute("error", "Conveyor Store not found")
        }

        return "monitor"
    }
}
