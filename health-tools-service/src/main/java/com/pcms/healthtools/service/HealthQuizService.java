package com.pcms.healthtools.service;

import com.pcms.common.exception.ResourceNotFoundException;
import com.pcms.healthtools.dto.request.SubmitQuizRequest;
import com.pcms.healthtools.dto.response.QuizResponse;
import com.pcms.healthtools.dto.response.QuizResultResponse;
import com.pcms.healthtools.entity.HealthQuiz;
import com.pcms.healthtools.entity.HealthQuizResult;
import com.pcms.healthtools.repository.HealthQuizRepository;
import com.pcms.healthtools.repository.HealthQuizResultRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class HealthQuizService {

    private final HealthQuizRepository quizRepository;
    private final HealthQuizResultRepository resultRepository;
    private final ObjectMapper objectMapper;

    public HealthQuizService(HealthQuizRepository quizRepository,
                              HealthQuizResultRepository resultRepository,
                              ObjectMapper objectMapper) {
        this.quizRepository = quizRepository;
        this.resultRepository = resultRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<QuizResponse> list() {
        return quizRepository.findByStatusOrderByNameAsc("ACTIVE").stream()
                .map(this::toQuizResponse).toList();
    }

    @Transactional(readOnly = true)
    public QuizResponse getBySlug(String slug) {
        HealthQuiz quiz = quizRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("HealthQuiz", "slug=" + slug));
        return toQuizResponse(quiz);
    }

    @Transactional
    public QuizResultResponse submit(String slug, SubmitQuizRequest request) {
        HealthQuiz quiz = quizRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("HealthQuiz", "slug=" + slug));

        // Calculate score (sum of answer values, simplified)
        int score = request.answers().values().stream()
                .filter(v -> v instanceof Number)
                .mapToInt(v -> ((Number) v).intValue())
                .sum();

        // Determine risk level
        String riskLevel = determineRiskLevel(slug, score);

        // Generate advice (simplified)
        String advice = generateAdvice(slug, riskLevel);

        HealthQuizResult result = new HealthQuizResult();
        result.setCustomerId(request.customerId());
        result.setQuizSlug(slug);
        try {
            result.setAnswersJson(objectMapper.writeValueAsString(request.answers()));
        } catch (Exception e) {
            result.setAnswersJson("{}");
        }
        result.setScore(score);
        result.setRiskLevel(riskLevel);
        result.setAdvice(advice);
        result.setCompletedAt(LocalDateTime.now());
        result = resultRepository.save(result);

        return toResultResponse(result);
    }

    @Transactional(readOnly = true)
    public List<QuizResultResponse> getMyResults(UUID customerId) {
        return resultRepository.findByCustomerIdOrderByCompletedAtDesc(customerId).stream()
                .map(this::toResultResponse).toList();
    }

    private QuizResponse toQuizResponse(HealthQuiz q) {
        int questionCount = 0;
        if (q.getQuestionsJson() != null && !q.getQuestionsJson().isBlank()) {
            try {
                List<?> questions = objectMapper.readValue(q.getQuestionsJson(), List.class);
                questionCount = questions.size();
            } catch (Exception ignored) {}
        }
        return new QuizResponse(q.getId(), q.getSlug(), q.getName(), q.getDescription(),
                questionCount, q.getScoringLogic());
    }

    private QuizResultResponse toResultResponse(HealthQuizResult r) {
        return new QuizResultResponse(r.getId(), r.getCustomerId(), r.getQuizSlug(),
                r.getScore(), r.getRiskLevel(), r.getAdvice(), r.getCompletedAt());
    }

    private String determineRiskLevel(String slug, int score) {
        // Simplified risk level logic per quiz
        return switch (slug) {
            case "memory" -> score >= 20 ? "HIGH" : score >= 10 ? "MODERATE" : "LOW";
            case "pre-diabetes" -> score >= 15 ? "HIGH" : score >= 7 ? "MODERATE" : "LOW";
            case "thyroid" -> score >= 12 ? "HIGH" : score >= 6 ? "MODERATE" : "LOW";
            case "asthma" -> score >= 20 ? "POOR" : score >= 16 ? "NOT_WELL" : "WELL_CONTROLLED";
            case "cardiac" -> score >= 20 ? "HIGH" : score >= 10 ? "INTERMEDIATE" : "LOW";
            case "alzheimer" -> score >= 3 ? "IMPAIRED" : "NORMAL";
            case "gerd" -> score >= 12 ? "HIGH" : score >= 8 ? "MODERATE" : "LOW";
            case "inhaler" -> score >= 3 ? "ADDICTED" : score >= 1 ? "WARNING" : "OK";
            default -> score >= 15 ? "HIGH" : score >= 8 ? "MODERATE" : "LOW";
        };
    }

    private String generateAdvice(String slug, String riskLevel) {
        if ("HIGH".equals(riskLevel) || "POOR".equals(riskLevel) || "IMPAIRED".equals(riskLevel) || "ADDICTED".equals(riskLevel)) {
            return "Kết quả cho thấy nguy cơ cao. Bạn nên đặt lịch tư vấn với dược sĩ và cân nhắc khám chuyên khoa.";
        }
        if ("MODERATE".equals(riskLevel) || "NOT_WELL".equals(riskLevel) || "WARNING".equals(riskLevel)) {
            return "Kết quả trung bình. Hãy theo dõi sức khỏe định kỳ và tham khảo dược sĩ.";
        }
        return "Kết quả tốt. Duy trì lối sống lành mạnh và kiểm tra sức khỏe hàng năm.";
    }
}
