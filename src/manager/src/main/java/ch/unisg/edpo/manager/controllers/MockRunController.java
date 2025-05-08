package ch.unisg.edpo.manager.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class MockRunController {
    @GetMapping("/mock/happy-path")
    public ResponseEntity<Void> healthCheck() {
        // Insert Code Here.
        return ResponseEntity.ok().build();
    }
}
