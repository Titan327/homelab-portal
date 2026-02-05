package com.hosting_portal.api.controller;

import com.hosting_portal.api.service.SshService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/health")
public class HealthController {

    private final SshService sshService;

    public HealthController(SshService sshService) {
        this.sshService = sshService;
    }

    @GetMapping("/")
    public String health() {
        return "ok";
    }

    @GetMapping("/{node}")
    public String healthOfNode(@PathVariable String node) {
        String result = sshService.executeCommand("sudo pvesh get /nodes/"+node+"/services --output-format json");
        log.info(result);
        return result;
    }

    // TODO remplacer par juste un get pour savoir si tel raid est online avec des infos sur chaque disque du style n rais: online {Disk1:online, Disk2:online}
    @GetMapping("/{node}/raid/{raid}")
    public String healthOfRaid(@PathVariable String node,@PathVariable String raid) {
        String result = sshService.executeCommand("sudo pvesh get /nodes/"+node+"/disks/zfs/"+raid+" --output-format json");
        log.info(result);
        return result;
    }

}
