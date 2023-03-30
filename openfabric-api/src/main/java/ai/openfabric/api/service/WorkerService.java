package ai.openfabric.api.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import ai.openfabric.api.repository.WorkerRepository;

@Component
public class WorkerService {
    private DockerClient dockerClient = null;
    private DockerClientConfig config = null;
    private DockerHttpClient httpClient = null;

    @Autowired
    private WorkerRepository workerRepository;

    public WorkerService() {
        System.out.println("Initializing config..");
        config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();

        if (config != null) {
            System.out.println("Initializing httpClient..");
            httpClient = new ApacheDockerHttpClient.Builder()
                    .dockerHost(config.getDockerHost())
                    .sslConfig(config.getSSLConfig())
                    .maxConnections(100)
                    .connectionTimeout(Duration.ofSeconds(30))
                    .responseTimeout(Duration.ofSeconds(45))
                    .build();
        }
        if (httpClient != null) {
            System.out.println("Initializing dockerClient..");
            dockerClient = DockerClientImpl.getInstance(config, httpClient);
        }

        if (dockerClient != null) {
            System.out.println("Initialized WorkerService!");
        }

    }

    public List<Container> listAllContainers() {

        List<Container> list = dockerClient.listContainersCmd().withShowAll(true).exec();

        if (list == null) {
            System.out.println("List is empty!");
        }

        return list;
    }

    public List<Container> listRunningContainers() {
        List<String> statusList = new ArrayList<String>();
        statusList.add("running");
        List<Container> list = dockerClient.listContainersCmd().withStatusFilter(statusList).exec();
        System.out.println("List is listtt!");
        return list;
    }

    public void startContainer(String containerID) {
        dockerClient.startContainerCmd(containerID).exec();
    }

    public void stopContainer(String containerID) {
        dockerClient.stopContainerCmd(containerID).exec();
    }

    public InspectContainerResponse containerInformation(String containerID) {
        return dockerClient.inspectContainerCmd(containerID).exec();
    }

    public CompletableFuture<Statistics> containerStats(String containerID) {
        StatsCmd cmd = dockerClient.statsCmd(containerID);
        CompletableFuture<Statistics> future = new CompletableFuture<>();

        ResultCallback.Adapter<Statistics> callback = new ResultCallback.Adapter<Statistics>() {
            @Override
            public void onNext(Statistics stats) {
                // Process the stats object here
                System.out.println(stats);

                try {
                    close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Complete the future with the stats object
                future.complete(stats);
            }
        };

        cmd.exec(callback);

        // Return the CompletableFuture to the caller
        return future;
    }

}
