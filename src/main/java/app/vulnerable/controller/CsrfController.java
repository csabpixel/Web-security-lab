package app.vulnerable.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/csrf")
@CrossOrigin
public class CsrfController {

    private static final String INITIAL_EMAIL = "te@gmail.com";
    private static final String USERNAME = "Te";

    @PostMapping("/reset")
    public Map<String, Object> reset(HttpSession session) {
        session.setAttribute("csrfEmail", INITIAL_EMAIL);
        session.setAttribute("csrfHistory", new ArrayList<String>());
        session.setAttribute("csrfToken", UUID.randomUUID().toString().substring(0, 16));
        if (session.getAttribute("csrfMode") == null) {
            session.setAttribute("csrfMode", "vulnerable");
        }
        return state(session);
    }

    @GetMapping("/state")
    public Map<String, Object> state(HttpSession session) {
        if (session.getAttribute("csrfEmail") == null) {
            session.setAttribute("csrfEmail", INITIAL_EMAIL);
            session.setAttribute("csrfHistory", new ArrayList<String>());
            session.setAttribute("csrfToken", UUID.randomUUID().toString().substring(0, 16));
            session.setAttribute("csrfMode", "vulnerable");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("username", USERNAME);
        result.put("email", session.getAttribute("csrfEmail"));
        result.put("history", session.getAttribute("csrfHistory"));
        result.put("csrfToken", session.getAttribute("csrfToken"));
        result.put("mode", session.getAttribute("csrfMode"));
        return result;
    }

    @PostMapping("/set-mode")
    public Map<String, Object> setMode(@RequestBody Map<String, String> req, HttpSession session) {
        session.setAttribute("csrfMode", req.get("mode"));
        return state(session);
    }


    @GetMapping("/change-email")
    public Map<String, Object> changeEmail(
            @RequestParam("email") String newEmail,
            HttpSession session) {

        String mode = (String) session.getAttribute("csrfMode");
        if ("secure".equals(mode)) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", false);
            result.put("message", "GET endpoint le van tiltva secure módban.");
            result.put("username", USERNAME);
            result.put("email", session.getAttribute("csrfEmail"));
            result.put("history", session.getAttribute("csrfHistory"));
            result.put("csrfToken", session.getAttribute("csrfToken"));
            result.put("mode", "secure");
            return result;
        }
        return performChange(newEmail, session, "GET (vulnerable)");
    }

    @PostMapping("/change-email-secure")
    public Map<String, Object> changeEmailSecure(
            @RequestBody Map<String, Object> request,
            HttpSession session) {

        String storedToken = (String) session.getAttribute("csrfToken");
        Object providedToken = request.get("csrfToken");

        if (storedToken == null || !storedToken.equals(providedToken)) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", false);
            result.put("message", "Hiányzó vagy érvénytelen CSRF token — kérés elutasítva.");
            result.put("username", USERNAME);
            result.put("email", session.getAttribute("csrfEmail"));
            result.put("history", session.getAttribute("csrfHistory"));
            result.put("csrfToken", storedToken);
            result.put("mode", session.getAttribute("csrfMode"));
            return result;
        }

        String newEmail = String.valueOf(request.get("email"));
        return performChange(newEmail, session, "POST (CSRF token OK)");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> performChange(String newEmail, HttpSession session, String how) {
        String oldEmail = (String) session.getAttribute("csrfEmail");
        if (oldEmail == null) oldEmail = INITIAL_EMAIL;
        session.setAttribute("csrfEmail", newEmail);

        List<String> history = (List<String>) session.getAttribute("csrfHistory");
        if (history == null) history = new ArrayList<>();
        history.add(oldEmail + "  →  " + newEmail + "  [" + how + "]");
        session.setAttribute("csrfHistory", history);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("username", USERNAME);
        result.put("email", newEmail);
        result.put("history", history);
        result.put("csrfToken", session.getAttribute("csrfToken"));
        result.put("mode", session.getAttribute("csrfMode"));
        result.put("message", "Email frissítve: " + newEmail);
        return result;
    }
}
