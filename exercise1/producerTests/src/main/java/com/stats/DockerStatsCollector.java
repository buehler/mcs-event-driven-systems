package com.stats;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.model.Statistics;

public class DockerStatsCollector {
    private final DockerClient dockerClient;

    // Fields to store the collected CPU and memory usage
    private volatile double cpuUsage;       // CPU percentage
    private volatile double memoryUsageMiB; // Memory usage (in MiB)

    public DockerStatsCollector() {
        this.dockerClient = DockerClientUtil.getDockerClient();
        this.cpuUsage = 0.0;       // Initialize default value
        this.memoryUsageMiB = 0.0; // Initialize default value
    }

    /**
     * Fetch stats for a given container
     */
    public void fetchStats(String containerId) {
        try (StatsCmd statsCmd = dockerClient.statsCmd(containerId)) {
            // Consume real-time container stats
            statsCmd.exec(new StatsCallback(stats -> {
                try {
                    // Extract CPU percentage and update the field
                    this.cpuUsage = calculateCpuUsage(stats);

                    // Extract memory usage (in MiB) and update the field
                    this.memoryUsageMiB = (stats.getMemoryStats() != null)
                            ? stats.getMemoryStats().getUsage() / (1024.0 * 1024.0)
                            : 0.0; // Default to 0 if memory stats are unavailable
                } catch (Exception e) {
                    System.err.printf("Error processing stats for container %s: %s%n", containerId, e.getMessage());
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to calculate CPU usage percentage (Handles null fields gracefully)
    private double calculateCpuUsage(Statistics stats) {
        try {
            if (stats == null || stats.getCpuStats() == null || stats.getPreCpuStats() == null) {
                // Return 0 by default if CPU stats are not available
                return 0.0;
            }

            Long cpuDelta = stats.getCpuStats().getCpuUsage() != null &&
                    stats.getCpuStats().getCpuUsage().getTotalUsage() != null &&
                    stats.getPreCpuStats().getCpuUsage() != null &&
                    stats.getPreCpuStats().getCpuUsage().getTotalUsage() != null
                    ? stats.getCpuStats().getCpuUsage().getTotalUsage()
                    - stats.getPreCpuStats().getCpuUsage().getTotalUsage()
                    : 0;

            Long systemDelta = stats.getCpuStats().getSystemCpuUsage() != null &&
                    stats.getPreCpuStats().getSystemCpuUsage() != null
                    ? stats.getCpuStats().getSystemCpuUsage()
                    - stats.getPreCpuStats().getSystemCpuUsage()
                    : 0;

            Long onlineCpus = stats.getCpuStats().getOnlineCpus() != null ?
                    stats.getCpuStats().getOnlineCpus() : null;

            if (systemDelta > 0 && onlineCpus != null && onlineCpus > 0) {
                return ((double) cpuDelta / systemDelta) * onlineCpus * 100.0;
            }
        } catch (Exception e) {
            System.err.println("Error calculating CPU usage: " + e.getMessage());
        }
        return 0.0; // Default value if calculation fails
    }

    /**
     * Get the latest CPU usage percentage
     */
    public double getCpuUsage() {
        return cpuUsage;
    }

    /**
     * Get the latest memory usage (in MiB)
     */
    public double getMemoryUsageMiB() {
        return memoryUsageMiB;
    }
}