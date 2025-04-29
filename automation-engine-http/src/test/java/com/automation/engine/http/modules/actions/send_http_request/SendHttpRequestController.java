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
    public Map<String, Object> echoForm(@RequestParam Map<String, String> form) {
        return new HashMap<>(form);
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
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Not found");
    }

    @GetMapping("/error")
    public ResponseEntity<String> error() {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal error");
    }

    @GetMapping("/basic")
    public Map<String, Object> basicGet() {
        return Map.of("message", "Basic GET success");
    }

    @GetMapping("/query")
    public Map<String, Object> getWithQuery(@RequestParam Map<String, String> params) {
        return Map.of("query", params);
    }

    @GetMapping("/path/{id}")
    public Map<String, Object> getWithPath(@PathVariable("id") int id) {
        return Map.of("id", id);
    }

    @GetMapping("/headers")
    public Map<String, Object> getWithHeaders(@RequestHeader Map<String, String> headers) {
        return Map.of("headers", headers);
    }

    @GetMapping("/body")
    public Map<String, Object> getWithBody(@RequestBody(required = false) Map<String, Object> body) {
        // GET with body is rare, but we allow it for testing
        return Map.of("body", body != null ? body : Map.of());
    }

    @GetMapping("/emptyQuery")
    public Map<String, String> handleEmptyQueryParams(@RequestParam Map<String, String> params) {
        return params;
    }

    @GetMapping("/special/{value}")
    public Map<String, String> handleSpecialChars(@PathVariable String value, @RequestParam String q) {
        return Map.of("path", value, "query", q);
    }

    @GetMapping("/largeQuery")
    public Map<String, Object> handleLargeQuery(@RequestParam String q) {
        return Map.of("length", q.length());
    }

    @GetMapping("/contentTypeOnly")
    public Map<String, Object> handleContentTypeOnly(@RequestHeader("Content-Type") String contentType) {
        return Map.of("contentType", contentType);
    }

    @PostMapping("/post/json")
    public Map<String, Object> postJson(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        Integer age = (Integer) body.get("age");
        return Map.of("message", "Received " + name + " aged " + age);
    }

    @PostMapping("/post/form")
    public Map<String, Object> postForm(@RequestParam Map<String, String> params) {
        return Map.of("status", "Form received");
    }

    @PostMapping("/post/multipart")
    public Map<String, Object> postMultipart(@RequestParam Map<String, String> params) {
        return Map.of("result", "Multipart received");
    }

    @PostMapping("/post/missing")
    public ResponseEntity<Map<String, Object>> postMissing(@RequestBody(required = false) Map<String, Object> body) {
        if (body == null) {
            return ResponseEntity.ok(Map.of("error", "Missing body"));
        }
        return ResponseEntity.ok(Map.of("received", body));
    }

    @PostMapping("/post/headers")
    public Map<String, Object> postHeaders(@RequestHeader("X-Custom-Header") String customHeader,
                                           @RequestHeader("Authorization") String authorization) {
        return Map.of(
                "customHeader", customHeader,
                "authorization", authorization
        );
    }

    @PostMapping("/post/large")
    public Map<String, Object> postLarge(@RequestBody Map<String, Object> body) {
        String largeText = (String) body.get("largeText");
        return Map.of("receivedLength", largeText.length());
    }

    @PostMapping("/post/wrongContentType")
    public ResponseEntity<Map<String, Object>> postWrongContentType(@RequestBody(required = false) String body,
                                                                    @RequestHeader(value = "Content-Type", required = false) String contentType) {
        if (!"application/json".equals(contentType)) {
            return ResponseEntity.ok(Map.of("error", "Unsupported content type"));
        }
        return ResponseEntity.ok(Map.of("received", body));
    }

    @PostMapping("/post/emptyJson")
    public Map<String, Object> postEmptyJson(@RequestBody Map<String, Object> body) {
        if (body.isEmpty()) {
            return Map.of("status", "Empty JSON received");
        }
        return Map.of("received", body);
    }

    @PostMapping("/post/invalidJson")
    public ResponseEntity<Map<String, Object>> postInvalidJson() {
        return ResponseEntity.ok(Map.of("error", "Invalid JSON"));
    }

    @PostMapping("/post/extraFields")
    public Map<String, Object> postExtraFields(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        Integer age = (Integer) body.get("age");
        return Map.of("message", "Received " + name + " aged " + age);
    }

    @PostMapping("/post/largeMultipart")
    public Map<String, Object> postLargeMultipart(@RequestParam("fileContent") String fileContent) {
        return Map.of("receivedLength", fileContent.length());
    }

    @PostMapping("/post/noContentType")
    public Map<String, Object> postNoContentType(@RequestBody Map<String, Object> body) {
        return Map.of("status", "Default content-type handled");
    }

    @PostMapping("/post/headersNoBody")
    public Map<String, Object> postHeadersNoBody(@RequestHeader("X-Special-Header") String header) {
        return Map.of("header", header);
    }

    @PutMapping("/put/simple")
    public Map<String, Object> putSimple(@RequestBody Map<String, Object> body) {
        return Map.of("message", "Updated " + body.get("name") + " as " + body.get("role"));
    }

    @PutMapping("/put/user/{id}")
    public Map<String, Object> putWithPathVariable(@PathVariable int id, @RequestBody Map<String, Object> body) {
        return Map.of("userId", id, "email", body.get("email"));
    }

    @PutMapping("/put/query")
    public Map<String, Object> putWithQuery(@RequestParam boolean active, @RequestBody Map<String, Object> body) {
        return Map.of("active", active, "user", body.get("username"));
    }

    @PutMapping("/put/headers")
    public Map<String, Object> putWithHeaders(@RequestHeader("X-Update-Mode") String mode, @RequestBody Map<String, Object> body) {
        return Map.of("mode", mode);
    }

    @PutMapping("/put/emptyBody")
    public Map<String, Object> putEmptyBody(@RequestBody Map<String, Object> body) {
        if (body.isEmpty()) {
            return Map.of("status", "Empty body received");
        }
        return Map.of("body", body);
    }

    @PutMapping("/put/invalidJson")
    public Map<String, Object> putInvalidJson() {
        return Map.of("error", "Invalid JSON");
    }

    @PutMapping("/put/large")
    public Map<String, Object> putLargePayload(@RequestBody Map<String, Object> body) {
        String text = (String) body.get("bigText");
        return Map.of("length", text.length());
    }

    @PutMapping("/put/multipart")
    public Map<String, Object> putMultipart(@RequestParam("description") String description) {
        return Map.of("description", description);
    }

    @PutMapping("/put/noContentType")
    public Map<String, Object> putNoContentType(@RequestBody Map<String, Object> body) {
        return Map.of("result", "Handled without content-type");
    }

    @PutMapping("/putWithoutId")
    public ResponseEntity<String> putWithoutId() {
        return ResponseEntity.badRequest().body("Missing id");
    }

    @PutMapping("/putWithQuery")
    public Map<String, Object> putWithQuery(@RequestParam String id) {
        return Map.of("id", id);
    }

    @PutMapping("/putLargeBody")
    public Map<String, Object> putLargeBody(@RequestBody Map<String, Object> body) {
        return Map.of("status", "received large body");
    }

    @PutMapping("/putTimeout")
    public String putTimeout() throws InterruptedException {
        Thread.sleep(5000); // simulate slow server
        return "Request timeout";
    }

    @PutMapping("/putMalformedHeaders")
    public String putMalformedHeaders() {
        return "Bad Request";
    }

    @PutMapping("/putNoBody")
    public String putNoBody() {
        return "No body provided";
    }

    @PutMapping("/putOverwrite")
    public Map<String, Object> putOverwrite(@RequestParam String id, @RequestBody Map<String, Object> body) {
        return body;
    }

    @PutMapping("/putPlainText")
    public String putPlainText(@RequestBody String body) {
        return body;
    }

    @PutMapping("/putUnicode")
    public Map<String, String> putUnicode(@RequestBody Map<String, String> body) {
        return body;
    }

    @PutMapping("/putNestedJson")
    public Map<String, Object> putNestedJson(@RequestBody Map<String, Object> body) {
        return body;
    }

    @PatchMapping("/patchMalformedHeaders")
    public ResponseEntity<String> patchMalformedHeaders(@RequestHeader(value = "X-Custom-Header", required = false) String header) {
        if (header == null || header.contains("\u0000")) {
            return ResponseEntity.badRequest().body("Bad Request: Invalid Header");
        }
        return ResponseEntity.ok("Header OK");
    }

    @PatchMapping("/patch/emptyPath")
    public ResponseEntity<String> patchEmptyPath() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found");
    }

    @PatchMapping("/patchNoBody")
    public Map<String, String> patchNoBody(@RequestBody(required = false) Map<String, Object> body) {
        if (body == null || body.isEmpty()) {
            return Map.of("status", "no body received");
        }
        return Map.of("status", "body received");
    }

    @PatchMapping("/patchPartial")
    public ResponseEntity<Map<String, String>> patchPartial(@RequestBody Map<String, String> body) {
        var response = new HashMap<String, String>();
        response.put("name", body.getOrDefault("name", "default-name"));
        response.put("otherField", "default");
        return ResponseEntity.ok(response);
    }

    @PatchMapping(value = "/patchText", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> patchText(@RequestBody String body) {
        return ResponseEntity.ok("Received " + body);
    }

    @PatchMapping("/patchUnicode")
    public Map<String, String> patchUnicode(@RequestBody Map<String, String> body) {
        return Map.of("echo", body.get("message"));
    }

    @PatchMapping("/patchDeepJson")
    public Map<String, Object> patchDeepJson(@RequestBody Map<String, Object> body) {
        return body;
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteWithPathVariable(@PathVariable String id) {
        return ResponseEntity.ok("Item " + id + " deleted");
    }

    @DeleteMapping("/deleteNoBody")
    public ResponseEntity<String> deleteNoBody() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found");
    }

    @DeleteMapping("/deleteWithQuery")
    public ResponseEntity<String> deleteWithQuery(@RequestParam("id") String id) {
        return ResponseEntity.ok("Query param id " + id + " deleted");
    }

    @DeleteMapping("/deleteWithInvalidQueryParam")
    public ResponseEntity<String> deleteWithInvalidQuery(@RequestParam(value = "param", required = false) String param) {
        if ("invalid".equals(param)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid query parameter");
        }
        return ResponseEntity.status(HttpStatus.OK).body("Item deleted with param: " + param);
    }

    @DeleteMapping("/deleteWithHeaders")
    public ResponseEntity<String> deleteWithHeaders(@RequestHeader("X-Custom-Header") String header) {
        if ("some-header-value".equals(header)) {
            return ResponseEntity.ok("Headers received");
        }
        return ResponseEntity.badRequest().body("Bad Header");
    }

    @DeleteMapping("/deleteWithInvalidHeaders")
    public ResponseEntity<String> deleteWithInvalidHeaders(@RequestHeader(value = "X-Invalid-Header", required = false) String invalidHeader) {
        if (invalidHeader != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Header");
        }
        return ResponseEntity.status(HttpStatus.OK).body("Item deleted with valid headers");
    }

    @DeleteMapping("/deleteInvalid")
    public ResponseEntity<String> deleteInvalid() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found");
    }

    @DeleteMapping("/deleteWithBody")
    public ResponseEntity<String> deleteWithBody(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok("Item " + body.get("id") + " deleted with body");
    }


    @DeleteMapping("/deleteNoPath")
    public ResponseEntity<String> deleteNoPath() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found");
    }

    @DeleteMapping("/deleteInvalidPath")
    public ResponseEntity<String> deleteInvalidPath() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not Found");
    }

    @DeleteMapping("/deleteWithInvalidPathVariable/{id}")
    public ResponseEntity<String> deleteWithInvalidPathVariable(@PathVariable("id") String id) {
        if ("invalid-id".equals(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Item invalid-id not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body("Item " + id + " deleted");
    }

    @DeleteMapping("/deleteMalformedJson")
    public ResponseEntity<String> deleteMalformedJson(@RequestBody String malformedJson) {
        if (malformedJson.contains("abc")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Malformed JSON");
        }
        return ResponseEntity.status(HttpStatus.OK).body("Item deleted with valid JSON");
    }

    @DeleteMapping("/deleteWithExtraQuery")
    public ResponseEntity<String> deleteWithExtraQuery(@RequestParam(value = "extra", required = false) String extra,
                                                       @RequestParam(value = "param", required = false) String param) {
        if (extra != null && param != null) {
            return ResponseEntity.status(HttpStatus.OK).body("Item " + param + " deleted with extra params");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing query parameters");
    }

    @DeleteMapping("/deleteEmptyBody")
    public ResponseEntity<String> deleteEmptyBody(@RequestBody Map<String, Object> body) {
        if (body.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body("Empty body received");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Body is not empty");
    }
}