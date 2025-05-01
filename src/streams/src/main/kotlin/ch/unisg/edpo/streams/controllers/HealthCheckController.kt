package ch.unisg.edpo.streams.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthCheckController {
    @GetMapping("/healthz")
    fun healthCheck() = ResponseEntity.ok().build<Unit>()
}
