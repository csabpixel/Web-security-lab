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

    private static final String PIN_SESSION_KEY = "burpPin";
    private static final String PIN_FAILS_KEY = "burpPinFails";
    private static final String PIN_LOCKOUT_KEY = "burpPinLockoutUntil";

    private static final int DELAY_START_AT = 3;
    private static final int LOCKOUT_THRESHOLD = 8;
    private static final int LOCKOUT_SECONDS = 5;

    private int getOrCreatePin(jakarta.servlet.http.HttpSession session) {
        Integer pin = (Integer) session.getAttribute(PIN_SESSION_KEY);
        if (pin == null) {
            pin = java.util.concurrent.ThreadLocalRandom.current().nextInt(1, 51);
            session.setAttribute(PIN_SESSION_KEY, pin);
        }
        return pin;
    }

    @GetMapping("/pin")
    public Map<String, Object> pin(
            @RequestParam(value = "code", defaultValue = "") String code,
            jakarta.servlet.http.HttpSession session) {
        Map<String, Object> result = new LinkedHashMap<>();

        Long lockoutUntil = (Long) session.getAttribute(PIN_LOCKOUT_KEY);
        if (lockoutUntil != null) {
            if (System.currentTimeMillis() < lockoutUntil) {
                long remaining = (lockoutUntil - System.currentTimeMillis()) / 1000 + 1;
                result.put("success", false);
                result.put("locked", true);
                result.put("message", "Túl sok sikertelen próbálkozás. Várj még " + remaining + " másodpercet.");
                return result;
            } else {
                session.removeAttribute(PIN_LOCKOUT_KEY);
                session.removeAttribute(PIN_FAILS_KEY);
            }
        }

        Integer fails = (Integer) session.getAttribute(PIN_FAILS_KEY);
        if (fails == null) fails = 0;
        if (fails >= DELAY_START_AT) {
            long delayMs = (fails - DELAY_START_AT + 1) * 500L;
            try {
                Thread.sleep(delayMs);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        int correct = getOrCreatePin(session);
        try {
            int guessed = Integer.parseInt(code.trim());
            if (guessed == correct) {
                session.removeAttribute(PIN_FAILS_KEY);
                session.removeAttribute(PIN_LOCKOUT_KEY);
                result.put("success", true);
                result.put("message", "Helyes szám! Belépve az admin felületre.");
                result.put("flag", "BURP-PIN-" + correct);
                return result;
            }
        } catch (NumberFormatException e) {
        }

        fails++;
        session.setAttribute(PIN_FAILS_KEY, fails);

        if (fails >= LOCKOUT_THRESHOLD) {
            session.setAttribute(PIN_LOCKOUT_KEY,
                    System.currentTimeMillis() + LOCKOUT_SECONDS * 1000L);
            result.put("success", false);
            result.put("locked", true);
            result.put("message", "Túl sok sikertelen próbálkozás. " + LOCKOUT_SECONDS + " másodperces zárolás.");
            return result;
        }

        result.put("success", false);
        result.put("attempts", fails);
        result.put("message", "Rossz kód. (" + fails + "/" + LOCKOUT_THRESHOLD + " próbálkozás)");
        return result;
    }

    @PostMapping("/pin/reset")
    public Map<String, Object> pinReset(jakarta.servlet.http.HttpSession session) {
        int newPin = java.util.concurrent.ThreadLocalRandom.current().nextInt(1, 51);
        session.setAttribute(PIN_SESSION_KEY, newPin);
        session.removeAttribute(PIN_FAILS_KEY);
        session.removeAttribute(PIN_LOCKOUT_KEY);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", "Új szám generálva, védelem újraindítva.");
        return result;
    }
}
