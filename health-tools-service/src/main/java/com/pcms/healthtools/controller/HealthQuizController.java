package com.pcms.healthtools.controller;

import com.pcms.healthtools.dto.request.SubmitQuizRequest;
import com.pcms.healthtools.dto.response.QuizResponse;
import com.pcms.healthtools.dto.response.QuizResultResponse;
import com.pcms.healthtools.service.HealthQuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/health")
@Tag(name = "UC18 - Health Tools (HEALTH-QUIZ)")
public class HealthQuizController {

    private final HealthQuizService service;

    public HealthQuizController(HealthQuizService service) {
        this.service = service;
    }

    @GetMapping("/quizzes")
    @Operation(summary = "List all available health quizzes (8 quizzes)")
    public ResponseEntity<List<QuizResponse>> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/quizzes/{slug}")
    @Operation(summary = "Get a specific quiz by slug (HEALTH-QUIZ-LIST)")
    public ResponseEntity<QuizResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(service.getBySlug(slug));
    }

    @PostMapping("/quizzes/{slug}/submit")
    @Operation(summary = "Submit quiz answers and get result (HEALTH-QUIZ-RESULT)")
    public ResponseEntity<QuizResultResponse> submit(
            @PathVariable String slug,
            @Valid @RequestBody SubmitQuizRequest request) {
        return ResponseEntity.ok(service.submit(slug, request));
    }

    @GetMapping("/quiz-results/me")
    @Operation(summary = "Get my quiz results (history)")
    public ResponseEntity<List<QuizResultResponse>> getMyResults(
            @RequestParam UUID customerId) {
        return ResponseEntity.ok(service.getMyResults(customerId));
    }
}
