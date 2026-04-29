package app.vulnerable.service;

import app.vulnerable.dto.XssCommentResponse;
import app.vulnerable.model.Comment;
import app.vulnerable.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class XssService {

    private final CommentRepository commentRepository;

    public XssService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public XssCommentResponse addComment(String author, String content) {
        Comment saved = commentRepository.save(new Comment(author, content));
        return new XssCommentResponse(saved.getId(), saved.getAuthor(), saved.getContent());
    }

    public List<XssCommentResponse> getAllComments() {
        return commentRepository.findAll().stream()
                .map(c -> new XssCommentResponse(c.getId(), c.getAuthor(), c.getContent()))
                .toList();
    }
}