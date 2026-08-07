package app.vulnerable.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/hydra")
@CrossOrigin
public class HydraController {

    private static final String USERNAME = "admin";

    private static final List<String> WORDLIST = List.of(
            "password", "123456", "qwerty", "letmein", "welcome",
            "admin", "monkey", "dragon", "sunshine", "master",
            "trustnoone", "shadow", "superman", "batman", "iloveyou",
            "starwars", "hello123", "football", "matrix", "hunter",
            "change", "coffee", "summer2024", "winter", "pizza",
            "abc123", "passwords1", "admin123", "root", "toor"
    );

    private static volatile String currentPassword = pickRandom();

    private static String pickRandom() {
        return WORDLIST.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(WORDLIST.size()));
    }

    @PostMapping(value = "/login",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Map<String, Object> login(@RequestParam("username") String username,
                                     @RequestParam("password") String password) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (USERNAME.equals(username) && currentPassword.equals(password)) {
            result.put("success", true);
            result.put("message", "Sikeres belépés! Üdv, " + username + ".");
            result.put("flag", "HYDRA-FLAG-" + currentPassword.toUpperCase());
            return result;
        }
        result.put("success", false);
        result.put("message", "Hibás felhasználónév vagy jelszó.");
        return result;
    }

    @GetMapping(value = "/wordlist", produces = MediaType.TEXT_PLAIN_VALUE)
    public String wordlist() {
        return String.join("\n", WORDLIST) + "\n";
    }

    @GetMapping("/hint")
    public Map<String, Object> hint() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("username", USERNAME);
        result.put("wordlistSize", WORDLIST.size());
        return result;
    }

    @PostMapping("/reset")
    public Map<String, Object> reset() {
        currentPassword = pickRandom();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", "Új jelszó generálva.");
        return result;
    }
}
