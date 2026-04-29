package app.vulnerable.controller;

import app.vulnerable.dto.ReflectedXssRequest;
import app.vulnerable.dto.ReflectedXssResponse;
import app.vulnerable.dto.XssCommentRequest;
import app.vulnerable.dto.XssCommentResponse;
import app.vulnerable.service.XssService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/xss")
@CrossOrigin
public class XssController {

    private final XssService xssService;

    public XssController(XssService xssService) {
        this.xssService = xssService;
    }

    @PostMapping("/comments")
    public XssCommentResponse addComment(@RequestBody XssCommentRequest request) {
        return xssService.addComment(request.getAuthor(), request.getContent());
    }

    @GetMapping("/comments")
    public List<XssCommentResponse> getComments() {
        return xssService.getAllComments();
    }

    @PostMapping("/reflected")
    public ReflectedXssResponse reflected(@RequestBody ReflectedXssRequest request) {
        String message = "A szerver ezt kapta: " + request.getInput();
        return new ReflectedXssResponse(request.getInput(), request.getMode(), message);
    }
}