package com.automation.engine.http.publisher;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/get")
    public ResponseEntity<String> getEndpoint() {
        return ResponseEntity.ok("GET response");
    }

    @PostMapping("/post")
    public ResponseEntity<Map<String, String>> postEndpoint(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(body);
    }

    @PutMapping("/put")
    public ResponseEntity<String> putEndpoint(@RequestBody String body) {
        return ResponseEntity.ok("Received: " + body);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteEndpoint() {
        return ResponseEntity.noContent().build();
    }
}