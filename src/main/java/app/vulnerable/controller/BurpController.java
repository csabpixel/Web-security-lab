package app.vulnerable.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/burp")
@CrossOrigin
public class BurpController {

    private static final String SECRET_FLAG = "Itt a kincs";

    @GetMapping("/task")
    public Map<String, Object> task(HttpServletRequest request) {
        String debugHeader = request.getHeader("X-Debug-Mode");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Egy szokványos válasz.");
        body.put("timestamp", System.currentTimeMillis());
        body.put("orderId", 42);

        if ("true".equalsIgnoreCase(debugHeader)) {
            body.put("secret", SECRET_FLAG);
            body.put("hint", "Debug mód aktiválva! A titkos flag látszik.");
        } else {
            body.put("hint", "Ha lenne debug módod, láthatnál még valamit (X-Debug-Mode header?)");
        }
        return body;
    }

    @PostMapping("/check")
    public Map<String, Object> check(@RequestBody Map<String, String> req) {
        String flag = req.getOrDefault("flag", "").trim();
        Map<String, Object> result = new LinkedHashMap<>();
        boolean ok = SECRET_FLAG.equals(flag);
        result.put("success", ok);
        result.put("message", ok
                ? "Sikerült! Megtaláltad a rejtett flaget."
                : "Nem stimmel — próbáld újra Burp Suite-tal.");
        return result;
    }

    private static final String CORRECT_PIN = "37";

    @GetMapping("/pin")
    public Map<String, Object> pin(@RequestParam(value = "code", defaultValue = "") String code) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (CORRECT_PIN.equals(code)) {
            result.put("success", true);
            result.put("message", "Helyes PIN! Belépve az admin felületre.");
            result.put("flag", "BURP-PIN-" + CORRECT_PIN);
        } else {
            result.put("success", false);
            result.put("message", "Rossz kód.");
        }
        return result;
    }
}
