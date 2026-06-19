package com.pcms.customerportal.repository;

import com.pcms.customerportal.entity.Ingredient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, UUID> {

    @Query("""
            SELECT i FROM Ingredient i
            WHERE (:q IS NULL OR :q = ''
                   OR LOWER(i.nameVi) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(i.nameEn) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(i.synonyms) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY i.nameVi ASC
            """)
    Page<Ingredient> searchByName(@Param("q") String q, Pageable pageable);
}