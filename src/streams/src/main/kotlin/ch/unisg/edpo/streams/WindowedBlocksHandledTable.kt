package ch.unisg.edpo.streams

import ch.unisg.edpo.proto.events.machines.v1.BlockSorted
import ch.unisg.edpo.proto.models.v1.BlockColor
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.state.WindowStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class WindowedBlocksHandledTable {
    @Value("\${topics.events}")
    private val eventsTopic = ""

    @Autowired
    fun windowedBlocksHandledTable(sb: StreamsBuilder): KTable<Windowed<BlockColor>, Int> = sb
        .stream(eventsTopic, Consumed.with(Serdes.String(), GeneratedMessageSerdes()))
        .filter { _, v -> v is BlockSorted }
        .groupBy(
            { _, v -> (v as BlockSorted).color },
            Grouped.with(EnumSerde(BlockColor::class.java), GeneratedMessageSerdes())
        )
        .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(30)))
        .aggregate(
            { -> 0 },
            { _, _, agg -> agg + 1 },
            Materialized
                .`as`<BlockColor, Int, WindowStore<Bytes, ByteArray>>("windowed-blocks-handled-table")
                .withKeySerde(EnumSerde(BlockColor::class.java))
                .withValueSerde(Serdes.Integer()),
        )
}