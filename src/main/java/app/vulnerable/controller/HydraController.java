package app.vulnerable.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/hydra")
@CrossOrigin
public class HydraController {

    private static final List<String> USER_POOL = List.of(
            "admin", "alex", "zoli", "peter", "eva",
            "bob", "charlie", "david", "frank", "george",
            "henry", "ivan", "john", "kate", "leo",
            "mary", "nate", "olivia", "quinn", "rachel",
            "sam", "tom", "victor", "wendy", "chris"
    );

    private static final List<String> WORDLIST = List.of(
            "password", "123456", "qwerty", "letmein", "welcome",
            "admin", "monkey", "dragon", "sunshine", "master",
            "trustnoone", "shadow", "superman", "batman", "iloveyou",
            "starwars", "hello123", "football", "matrix", "hunter",
            "change", "coffee", "summer2024", "winter", "pizza",
            "abc123", "passwords1", "admin123", "root", "toor"
    );

    private static volatile String difficulty = "easy";
    private static final Map<String, String> passwords = new ConcurrentHashMap<>();
    private static volatile String flagUser;

    static { rebuild(); }

    private static void rebuild() {
        java.util.concurrent.ThreadLocalRandom r = java.util.concurrent.ThreadLocalRandom.current();
        passwords.clear();
        if ("hard".equals(difficulty)) {
            List<String> shuffled = new ArrayList<>(USER_POOL);
            Collections.shuffle(shuffled);
            List<String> picked = shuffled.subList(0, 5);
            for (String u : picked) {
                passwords.put(u, WORDLIST.get(r.nextInt(WORDLIST.size())));
            }
            flagUser = picked.get(r.nextInt(picked.size()));
        } else {
            passwords.put("admin", WORDLIST.get(r.nextInt(WORDLIST.size())));
            flagUser = "admin";
        }
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Map<String, Object> login(@RequestParam("username") String username,
                                     @RequestParam("password") String password) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (!passwords.containsKey(username)) {
            result.put("success", false);
            result.put("error", "Ismeretlen felhasználó");
            return result;
        }
        if (!passwords.get(username).equals(password)) {
            result.put("success", false);
            result.put("error", "Hibás jelszó");
            return result;
        }

        if (!username.equals(flagUser)) {
            result.put("success", false);
            result.put("error", "Ez a fiók zárolva van — csak egyetlen fiók aktív a rendszeren, keresd meg melyik!");
            return result;
        }

        result.put("success", true);
        result.put("username", username);
        result.put("message", "Sikeres belépés mint " + username + "! Te vagy az aktív fiók.");
        result.put("flag", "HYDRA-FLAG-" + password.toUpperCase());
        return result;
    }

    @GetMapping(value = "/wordlist", produces = MediaType.TEXT_PLAIN_VALUE)
    public String wordlist() {
        return String.join("\n", WORDLIST) + "\n";
    }

    @GetMapping(value = "/users", produces = MediaType.TEXT_PLAIN_VALUE)
    public String userList() {
        return String.join("\n", passwords.keySet()) + "\n";
    }

    @GetMapping("/mode")
    public Map<String, Object> getMode() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("mode", difficulty);
        return result;
    }

    @PostMapping("/mode")
    public Map<String, Object> setMode(@RequestBody Map<String, String> req) {
        String requested = req.getOrDefault("mode", "easy");
        difficulty = "hard".equals(requested) ? "hard" : "easy";
        rebuild();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("mode", difficulty);
        result.put("message", "hard".equals(difficulty)
                ? "Nehéz mód aktív — 5 user, mind random jelszóval, flag-tulajdonos is random."
                : "Sima mód aktív — csak admin fiók, random jelszóval.");
        return result;
    }

    @PostMapping("/reset")
    public Map<String, Object> reset() {
        rebuild();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", "hard".equals(difficulty)
                ? "Új 5 felhasználó, új jelszavak, új aktív fiók."
                : "Új jelszó az admin fiókhoz.");
        return result;
    }
}
