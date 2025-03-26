package com.automation.engine.http.publisher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
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

    @GetMapping("/get-with-params")
    public ResponseEntity<String> getWithParams(@RequestParam Map<String, String> queryParams,
                                                @RequestHeader(value = "Custom-Header", required = false) String customHeader) {
        String response = "Query Params: " + queryParams;
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-with-path/{id}")
    public ResponseEntity<String> getWithPath(@PathVariable String id,
                                              @RequestHeader(value = "Another-Header", required = false) String anotherHeader) {
        String response = "Path Variable: " + id;
        return ResponseEntity.ok(response);
    }

    @PostMapping("/post-with-params/{id}")
    public ResponseEntity<Map<String, Object>> postWithParams(@PathVariable String id,
                                                              @RequestParam Map<String, String> queryParams,
                                                              @RequestBody Map<String, String> body,
                                                              @RequestHeader(value = "Post-Header", required = false) String postHeader) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("pathId", id);
        response.put("queryParams", queryParams);
        response.put("body", body);
        if (postHeader != null) {
            response.put("Post-Header", postHeader);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/long-process")
    public ResponseEntity<String> longProcessEndpoint() throws InterruptedException {
        Thread.sleep(100); // Simulating a long process
        return ResponseEntity.ok("Long process completed");
    }
}