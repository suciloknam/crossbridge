package com.crossbridge.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;

@RestController
public class ApiController {

    // Injected from Kubernetes ConfigMap or ECS environment variable
    // This is the cloud-native config pattern — no hardcoded values
    @Value("${app.environment:local}")
    private String environment;

    @Value("${app.version:1.0.0}")
    private String version;

    // Liveness probe — Kubernetes and ECS both hit this
    // If this returns non-200, container is restarted
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
            "status",      "UP",
            "environment", environment,
            "version",     version,
            "timestamp",   Instant.now().toString()
        );
    }

    // Shows which cloud and pod/task is serving the request
    // HOSTNAME = pod name in Kubernetes, task ID in ECS
    // Demonstrates load balancing when replicas > 1
    @GetMapping("/api/info")
    public Map<String, String> info() {
        return Map.of(
            "service",     "crossbridge",
            "environment", environment,
            "version",     version,
            "host",        System.getenv().getOrDefault("HOSTNAME", "unknown")
        );
    }

    // Simple echo endpoint — useful for smoke tests in pipeline
    @GetMapping("/api/echo/{message}")
    public Map<String, String> echo(@PathVariable String message) {
        return Map.of(
            "message",     message,
            "environment", environment,
            "timestamp",   Instant.now().toString()
        );
    }
}
