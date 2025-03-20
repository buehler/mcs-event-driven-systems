package ch.unisg.edpo.manager.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HealthCheckController {
    @GetMapping("/healthz")
    public ResponseEntity<Void> healthCheck() {
        return ResponseEntity.ok().build();
    }
}
