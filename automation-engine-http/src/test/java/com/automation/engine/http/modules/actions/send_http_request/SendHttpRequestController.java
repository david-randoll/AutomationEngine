package com.automation.engine.http.modules.actions.send_http_request;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sendHttpRequest")
public class SendHttpRequestController {
    @PostMapping(value = "/echo", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> echoJson(@RequestBody Map<String, Object> body) {
        return body;
    }

    @PostMapping(value = "/echo", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> echoForm(@RequestParam MultiValueMap<String, String> form) {
        return new HashMap<>(form.toSingleValueMap());
    }

    @PostMapping(value = "/echo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> echoMultipart(@RequestParam MultiValueMap<String, Object> form) {
        Map<String, Object> result = new HashMap<>();
        for (var entry : form.entrySet()) {
            if (entry.getValue().size() == 1) {
                result.put(entry.getKey(), entry.getValue().getFirst());
            } else {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    @GetMapping("/not-found")
    public ResponseEntity<String> notFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    @GetMapping("/error")
    public ResponseEntity<String> error() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error");
    }
}
