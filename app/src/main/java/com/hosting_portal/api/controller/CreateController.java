package com.hosting_portal.api.controller;

import com.hosting_portal.api.service.SshService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/create")
public class CreateController {

    private final SshService sshService;

    public CreateController(SshService sshService) {
        this.sshService = sshService;
    }

    @GetMapping("/test")
    public Map<String, Object> test() {
        Map<String, String> variables = new HashMap<>();
        variables.put("CTID", "300");
        variables.put("MEMORY", "4096");

        String result1 = sshService.executeResourceScript("test.sh", variables);
        String result2 = sshService.executeResourceScript("ssh.sh", variables);
        String result3 = sshService.executeResourceScript("gitlab.sh", variables);
        log.info(result);

        return Map.of(
                "success", true,
                "output", result,
                "executedAt", LocalDateTime.now().toString()
        );
    }

}
