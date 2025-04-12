package com.automation.engine.http.publisher;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Validated
@RestController
@RequestMapping("/test/reactive")
public class ReactiveTestController {

    @GetMapping("/get")
    public Mono<ResponseEntity<String>> getEndpoint() {
        return Mono.just(ResponseEntity.ok("GET response"));
    }

    @PostMapping("/post")
    public Mono<ResponseEntity<Map<String, String>>> postEndpoint(@RequestBody Mono<Map<String, String>> bodyMono) {
        return bodyMono.map(ResponseEntity::ok);
    }

    @PutMapping("/put")
    public Mono<ResponseEntity<String>> putEndpoint(@RequestBody Mono<String> bodyMono) {
        return bodyMono.map(body -> ResponseEntity.ok("Received: " + body));
    }

    @DeleteMapping("/delete")
    public Mono<ResponseEntity<Void>> deleteEndpoint() {
        return Mono.just(ResponseEntity.noContent().build());
    }

    @GetMapping("/get-with-params")
    public Mono<ResponseEntity<String>> getWithParams(@RequestParam Map<String, String> queryParams,
                                                      @RequestHeader(value = "Custom-Header", required = false) String customHeader) {
        String response = "Query Params: " + queryParams;
        return Mono.just(ResponseEntity.ok(response));
    }

    @GetMapping("/get-with-path/{id}")
    public Mono<ResponseEntity<String>> getWithPath(@PathVariable String id,
                                                    @RequestHeader(value = "Another-Header", required = false) String anotherHeader) {
        String response = "Path Variable: " + id;
        return Mono.just(ResponseEntity.ok(response));
    }

    @PostMapping("/post-with-params/{id}")
    public Mono<ResponseEntity<Map<String, Object>>> postWithParams(@PathVariable String id,
                                                                    @RequestParam Map<String, String> queryParams,
                                                                    @RequestBody Mono<Map<String, String>> bodyMono,
                                                                    @RequestHeader(value = "Post-Header", required = false) String postHeader) {
        return bodyMono.map(body -> {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("pathId", id);
            response.put("queryParams", queryParams);
            response.put("body", body);
            if (postHeader != null) {
                response.put("Post-Header", postHeader);
            }
            return ResponseEntity.ok(response);
        });
    }

    @GetMapping("/long-process")
    public Mono<ResponseEntity<String>> longProcessEndpoint() {
        return Mono.delay(Duration.ofMillis(100))
                .map(ignore -> ResponseEntity.ok("Long process completed"));
    }

    @GetMapping("/query")
    public Mono<ResponseEntity<Map<String, String>>> handleQueryParams(@RequestParam Map<String, String> params) {
        return Mono.just(ResponseEntity.ok(params));
    }

    @GetMapping("/error-500")
    public Mono<ResponseEntity<String>> handle500Error() {
        return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred"));
    }

    @GetMapping("/error-400")
    public Mono<ResponseEntity<String>> handle400Error() {
        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unexpected error occurred"));
    }

    @PostMapping("/error-validation")
    public Mono<Void> handleValidationError(@RequestBody @Valid Mono<TestRequest> bodyMono) {
        return bodyMono.then();
    }

    @GetMapping("/slow-response")
    public Mono<ResponseEntity<String>> handleSlowResponse() {
        return Mono.delay(Duration.ofSeconds(5))
                .map(ignore -> ResponseEntity.ok("Processed after delay"));
    }

    public record TestRequest(@NotEmpty String name, String value) {
    }
}
