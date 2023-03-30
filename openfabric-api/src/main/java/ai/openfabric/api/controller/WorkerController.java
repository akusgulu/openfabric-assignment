package ai.openfabric.api.controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Statistics;

import ai.openfabric.api.service.WorkerService;

@RestController
@RequestMapping("${node.api.path}/worker")
public class WorkerController {
    @Autowired
    private WorkerService workerService;

    @PostMapping(path = "/hello")
    public @ResponseBody String hello(@RequestBody String name) {
        return "Hello!" + name;
    }

    @GetMapping(path = "/listContainers")
    public @ResponseBody List<Container> listContainers() {
        return workerService.listAllContainers();

    }

    @GetMapping(path = "/listRunningContainers")
    public @ResponseBody List<Container> listRunningContainers() {
        return workerService.listRunningContainers();

    }

    @PostMapping(path = "/startContainer")
    public @ResponseBody void startContainer(@RequestBody String containerID) {
        workerService.startContainer(containerID);

    }

    @PostMapping(path = "/stopContainer")
    public @ResponseBody void stopContainer(@RequestBody String containerID) {
        workerService.stopContainer(containerID);

    }

    @GetMapping(path = "/containerInformation")
    public @ResponseBody InspectContainerResponse containerInformation(@RequestParam String containerID) {
        return workerService.containerInformation(containerID);

    }

    @GetMapping(path = "/containerStats")
    public @ResponseBody Statistics containerStats(@RequestParam String containerID) {
        CompletableFuture<Statistics> futureStats = workerService.containerStats(containerID);
        try {
            Statistics stats = futureStats.get();
            return stats;
        } catch (Exception e) {
            System.out.print(e);
        }
        return null;
    }

}
