package com.automation.engine.http.publisher;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    @GetMapping("/get/{id}")
    public ResponseEntity<String> getWithParams(@PathVariable String id, @RequestParam(required = false) String query) {
        String response = "GET response with id: " + id + (query != null ? ", query: " + query : "");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/post/header")
    public ResponseEntity<Map<String, String>> postEndpoint(@RequestBody Map<String, String> body, @RequestHeader HttpHeaders headers) {
        Map<String, String> response = new HashMap<>(body);
        response.put("Content-Type", Objects.requireNonNull(headers.getContentType()).toString());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/put/{id}")
    public ResponseEntity<String> putEndpoint(@PathVariable String id, @RequestBody String body) {
        return ResponseEntity.ok("Updated ID: " + id + " with body: " + body);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteEndpoint(@PathVariable String id) {
        return ResponseEntity.ok("Deleted ID: " + id);
    }
}