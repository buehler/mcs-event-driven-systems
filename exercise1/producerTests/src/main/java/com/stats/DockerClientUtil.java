package com.stats;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.okhttp.OkHttpDockerCmdExecFactory;

public class DockerClientUtil {

    public static DockerClient getDockerClient() {
        // Docker client configuration (Unix socket for communication)
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("unix:///var/run/docker.sock") // Path to Docker daemon socket
                .build();

        return DockerClientBuilder.getInstance(config)
                .withDockerCmdExecFactory(new OkHttpDockerCmdExecFactory())
                .build();
    }
}