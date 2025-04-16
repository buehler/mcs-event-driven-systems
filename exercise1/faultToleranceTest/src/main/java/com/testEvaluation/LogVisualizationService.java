package com.testEvaluation;

import com.aggregator.LogAggregator; // Import LogAggregator

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LogVisualizationService {

    private static final String LOG_DIRECTORY_PATH = "./exercise1/faultToleranceTest/logs/aggregated";
    private static final double AGGREGATION_INTERVAL_SECONDS = 0.5; // Configurable aggregation interval

    public static void main(String[] args) {
        System.out.println("Starting Log Visualization Service...");

        // Step 1: Call LogAggregator to generate aggregated logs
        try {
            LogAggregator aggregator = new LogAggregator();
            aggregator.aggregateLogFile(findNewestLogFile(), AGGREGATION_INTERVAL_SECONDS);
        } catch (IOException e) {
            System.err.println("Error during log aggregation: " + e.getMessage());
            return;
        }

        // Step 2: Parse producer and consumer logs
        TimeSeries producerSeries = parseLogFile("producer.log", "Producers");
        TimeSeries consumerSeries = parseLogFile("consumer.log", "Consumers");
        List<EventMarker> events = parseEventFile("events.log");

        if (producerSeries == null || consumerSeries == null) {
            System.out.println("Could not read one or more log files.");
            return;
        }

        // Step 3: Plot both series in the same chart and include events
        displayChart(producerSeries, consumerSeries, events);

        System.out.println("Charts generated successfully!");
    }

    // Method to find the newest available log file dynamically
    private static String findNewestLogFile() {
        try {
            return java.nio.file.Files.list(java.nio.file.Paths.get("./exercise1/faultToleranceTest/logs"))
                    .filter(java.nio.file.Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".log"))
                    .max(java.util.Comparator.comparingLong(f -> f.toFile().lastModified()))
                    .map(path -> path.getFileName().toString())
                    .orElse(null);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static TimeSeries parseLogFile(String fileName, String seriesName) {
        Path filePath = FileSystems.getDefault().getPath(LOG_DIRECTORY_PATH, fileName);
        TimeSeries timeSeries = new TimeSeries(seriesName);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line;
            // Skip header
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length < 3) {
                    System.err.println("Skipping malformed line: " + line);
                    continue;
                }

                // Parse timestamp
                LocalDateTime timestamp = LocalDateTime.parse(parts[0], formatter);
                int value = Integer.parseInt(parts[2]); // Read the "value" field

                // Add data point to the series with millisecond-level precision
                timeSeries.addOrUpdate(
                        new Millisecond(Date.from(timestamp.atZone(ZoneOffset.systemDefault()).toInstant())),
                        value
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return timeSeries;
    }

    private static List<EventMarker> parseEventFile(String fileName) {
        Path filePath = FileSystems.getDefault().getPath(LOG_DIRECTORY_PATH, fileName);
        List<EventMarker> events = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line;
            // Skip header
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                LocalDateTime timestamp = LocalDateTime.parse(parts[0], formatter);
                String eventDescription = parts[1]; // Read the "event" field

                // Add the event marker to the list
                events.add(new EventMarker(timestamp, eventDescription));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return events;
        }

        return events;
    }

    private static void displayChart(TimeSeries producerSeries, TimeSeries consumerSeries, List<EventMarker> events) {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(producerSeries);
        dataset.addSeries(consumerSeries);

        // Create the chart
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Producer and Consumer Performance (Interval: " + AGGREGATION_INTERVAL_SECONDS + "s)",
                "Time",
                "Messages Count",
                dataset,
                true,
                true,
                false
        );

        // Customize the chart appearance
        XYPlot plot = chart.getXYPlot();
        DateAxis xAxis = new DateAxis("Time");
        xAxis.setDateFormatOverride(new SimpleDateFormat("HH:mm:ss.SSS")); // Include milliseconds in the X-Axis
        plot.setDomainAxis(xAxis);

        // Add event markers to the plot
        for (EventMarker event : events) {
            addEventMarker(plot, event);
        }

        // Display the chart in an application frame
        ApplicationFrame frame = new ApplicationFrame(
                "Producer and Consumer Messages with Events at " + AGGREGATION_INTERVAL_SECONDS + "s Interval");
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(1200, 600));
        frame.setContentPane(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }

    private static void addEventMarker(XYPlot plot, EventMarker event) {
        // Add a vertical line (domain marker) for the event
        Marker marker = new ValueMarker(
                Date.from(event.timestamp.atZone(ZoneOffset.systemDefault()).toInstant()).getTime());
        marker.setPaint(Color.RED);
        marker.setStroke(new BasicStroke(2.0f));
        marker.setLabel(event.description);
        marker.setLabelAnchor(org.jfree.chart.ui.RectangleAnchor.TOP_LEFT);
        marker.setLabelTextAnchor(org.jfree.chart.ui.TextAnchor.BOTTOM_LEFT);
        marker.setLabelOffset(new RectangleInsets(10, 5, 5, 5));
        plot.addDomainMarker(marker);
    }

    // A helper class to store event details
    private static class EventMarker {
        LocalDateTime timestamp;
        String description;

        EventMarker(LocalDateTime timestamp, String description) {
            this.timestamp = timestamp;
            this.description = description;
        }
    }
}