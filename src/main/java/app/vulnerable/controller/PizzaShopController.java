package app.vulnerable.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/pizza")
@CrossOrigin
public class PizzaShopController {

    private static final Map<String, String> USERS = Map.of(
            "xyz",    "xyz123",
            "hacker", "hacker123"
    );

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> req, HttpSession session) {
        String username = req.get("username");
        String password = req.get("password");

        if (username == null || password == null
                || !USERS.containsKey(username)
                || !USERS.get(username).equals(password)) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", false);
            result.put("loggedIn", false);
            result.put("message", "Hibás felhasználónév vagy jelszó.");
            return result;
        }

        session.setAttribute("pizzaUser", username);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("loggedIn", true);
        result.put("username", username);
        result.put("message", "Sikeres belépés.");
        return result;
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        session.removeAttribute("pizzaUser");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("loggedIn", false);
        return result;
    }

    @GetMapping("/me")
    public Map<String, Object> me(HttpSession session) {
        String username = (String) session.getAttribute("pizzaUser");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("loggedIn", username != null);
        result.put("username", username);
        return result;
    }
}
