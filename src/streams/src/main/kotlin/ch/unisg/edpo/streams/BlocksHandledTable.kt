package ch.unisg.edpo.streams

import ch.unisg.edpo.proto.events.machines.v1.BlockSorted
import ch.unisg.edpo.proto.models.v1.BlockColor
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Grouped
import org.apache.kafka.streams.kstream.KTable
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.state.KeyValueStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class BlocksHandledTable {
    @Value("\${topics.events}")
    private val eventsTopic = ""

    @Autowired
    fun blocksHandledTable(sb: StreamsBuilder): KTable<BlockColor, Int> {
        val table = sb
            .stream(eventsTopic, Consumed.with(Serdes.String(), GeneratedMessageSerdes()))
            .filter { _, v -> v is BlockSorted }
            .groupBy(
                { _, v -> (v as BlockSorted).color },
                Grouped.with(EnumSerde(BlockColor::class.java), GeneratedMessageSerdes())
            )
            .aggregate(
                { -> 0 },
                { _, _, agg -> agg + 1 },
                Materialized
                    .`as`<BlockColor, Int, KeyValueStore<Bytes, ByteArray>>("blocks-handled-table")
                    .withKeySerde(EnumSerde(BlockColor::class.java))
                    .withValueSerde(Serdes.Integer()),
            )

        return table
    }
}