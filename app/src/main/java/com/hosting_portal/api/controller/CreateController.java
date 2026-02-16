package com.hosting_portal.api.controller;

import com.hosting_portal.api.service.SshService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/create")
public class CreateController {

    private final SshService sshService;

    public CreateController(SshService sshService) {
        this.sshService = sshService;
    }

    @PostMapping("/LXC")
    public Map<String, Object> createLXC(@RequestBody String[] addOn) {
        Map<String, String> variables = new HashMap<>();
        variables.put("CTID", "300");
        variables.put("MEMORY", "4096");

        List<String> result = new
                ArrayList<>();

        for (String script : addOn) {
            result.add(sshService.executeResourceScript(script+".sh", variables));
        }


        return Map.of(
                "success", true,
                "output", result,
                "executedAt", LocalDateTime.now().toString()
        );
    }

    @DeleteMapping("/LXC")
    public Map<String, Object> deleteLXC() {
        Map<String, String> variables = new HashMap<>();
        variables.put("CTID", "300");

        String result = sshService.executeResourceScript("deleteLXC.sh", variables);
        log.info(result);

        return Map.of(
                "success", true,
                "output", result,
                "executedAt", LocalDateTime.now().toString()
        );
    }

}
