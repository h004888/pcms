package com.pcms.customerportal.service.impl;

import com.pcms.customerportal.dto.request.ReviewRequest;
import com.pcms.customerportal.dto.response.ReviewResponse;
import com.pcms.customerportal.entity.Review;
import com.pcms.customerportal.repository.ReviewRepository;
import com.pcms.customerportal.service.ReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository repo;

    public ReviewServiceImpl(ReviewRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getByMedicine(UUID medicineId) {
        return repo.findByMedicineIdOrderByCreatedAtDesc(medicineId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getMine(UUID customerId) {
        return repo.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * SPRINT 3 - T11: Upsert = nếu customer đã review medicine này → update,
     * nếu chưa → insert. Đảm bảo unique constraint (customer_id, medicine_id).
     */
    @Override
    @Transactional
    public ReviewResponse upsert(UUID customerId, ReviewRequest request) {
        Optional<Review> existing = repo.findByCustomerIdAndMedicineId(customerId, request.medicineId());
        Review review = existing.orElseGet(() -> {
            Review r = new Review();
            r.setCustomerId(customerId);
            r.setMedicineId(request.medicineId());
            return r;
        });
        review.setRating(request.rating());
        review.setComment(request.comment());
        Review saved = repo.save(review);
        return toDto(saved);
    }

    private ReviewResponse toDto(Review r) {
        return new ReviewResponse(
                r.getId(),
                r.getCustomerId(),
                r.getMedicineId(),
                r.getRating(),
                r.getComment(),
                r.getCreatedAt()
        );
    }
}