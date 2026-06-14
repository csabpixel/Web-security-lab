package app.vulnerable.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/pizza")
@CrossOrigin
public class PizzaShopController {

    private static final Map<String, String> USERS = Map.of(
            "xyz",    "xyz123",
            "hacker", "hacker123"
    );

    private static final int INITIAL_BALANCE = 10000;


    private static final Map<String, Integer> BALANCES = new ConcurrentHashMap<>();


    private static final Map<String, List<String>> ORDERS = new ConcurrentHashMap<>();

    private static final List<Map<String, Object>> MENU = List.of(
            menuItem("mexikoi",       "Mexikói Pizza",       "pizza",  3500, "/images/pizza&drink/Mexikói.png"),
            menuItem("egyiptomos",    "Egyiptomos Pizza",    "pizza",  4200, "/images/pizza&drink/Egyiptomos.png"),
            menuItem("futurisztikus", "Futurisztikus Pizza", "pizza",  4500, "/images/pizza&drink/Futurisztikus.png"),
            menuItem("hawaii",        "Hawaii Koktél",       "drink",  1500, "/images/pizza&drink/HAWAII.png"),
            menuItem("concert",       "Concert Üdítő",       "drink",   700, "/images/pizza&drink/Concert.png"),
            menuItem("ufo",           "UFO Ital",          "drink",  1200, "/images/pizza&drink/UFO.png")
    );

    private static Map<String, Object> menuItem(String id, String name, String category, int price, String image) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("name", name);
        m.put("category", category);
        m.put("price", price);
        m.put("image", image);
        return m;
    }

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
        BALANCES.putIfAbsent(username, INITIAL_BALANCE);
        ORDERS.putIfAbsent(username, new ArrayList<>());

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

    @GetMapping("/menu")
    public List<Map<String, Object>> menu() {
        return MENU;
    }

    @GetMapping("/balance")
    public Map<String, Object> balance(HttpSession session) {
        String username = (String) session.getAttribute("pizzaUser");
        Map<String, Object> result = new LinkedHashMap<>();
        if (username == null) {
            result.put("loggedIn", false);
            return result;
        }
        Integer bal = BALANCES.computeIfAbsent(username, u -> INITIAL_BALANCE);
        List<String> hist = ORDERS.computeIfAbsent(username, u -> new ArrayList<>());
        result.put("loggedIn", true);
        result.put("username", username);
        result.put("balance", bal);
        result.put("orders", hist);
        return result;
    }

    @GetMapping("/order")
    public Map<String, Object> order(@RequestParam("item") String itemId, HttpSession session) {
        String username = (String) session.getAttribute("pizzaUser");
        Map<String, Object> result = new LinkedHashMap<>();
        if (username == null) {
            result.put("success", false);
            result.put("message", "Nincs bejelentkezve.");
            return result;
        }

        Map<String, Object> item = MENU.stream()
                .filter(m -> itemId.equals(m.get("id")))
                .findFirst().orElse(null);
        if (item == null) {
            result.put("success", false);
            result.put("message", "Ismeretlen tétel: " + itemId);
            return result;
        }

        int price = (int) item.get("price");
        int balance = BALANCES.computeIfAbsent(username, u -> INITIAL_BALANCE);
        if (balance < price) {
            result.put("success", false);
            result.put("message", "Nincs elég egyenleg. (Egyenleg: " + balance + " Ft, ár: " + price + " Ft)");
            result.put("balance", balance);
            return result;
        }

        balance -= price;
        BALANCES.put(username, balance);
        ORDERS.computeIfAbsent(username, u -> new ArrayList<>())
              .add(item.get("name") + " (" + price + " Ft)");

        result.put("success", true);
        result.put("message", "Rendelve: " + item.get("name"));
        result.put("balance", balance);
        result.put("item", item);
        return result;
    }
}
