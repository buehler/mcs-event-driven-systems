package com.stats;

import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.model.Statistics;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Consumer;

// Custom StatsCallback to process container statistics
public class StatsCallback extends ResultCallbackTemplate<StatsCallback, Statistics> implements Closeable {
    private final Consumer<Statistics> statsProcessor;

    public StatsCallback(Consumer<Statistics> statsProcessor) {
        this.statsProcessor = statsProcessor;
    }

    @Override
    public void onNext(Statistics stats) {
        // Process the statistics with the provided consumer function
        statsProcessor.accept(stats);
    }

    @Override
    public void close() throws IOException {
        // Optional cleanup
    }
}