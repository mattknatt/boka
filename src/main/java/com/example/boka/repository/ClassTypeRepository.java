package com.example.boka.repository;

import com.example.boka.entity.ClassType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClassTypeRepository extends JpaRepository<ClassType, Long> {

    Optional<ClassType> findByName(String name);

    boolean existsByName(String name);

    List<ClassType> findByIsActiveTrue();

    // Vector similarity search using pgvector's cosine distance operator
    @Query(value = """
            SELECT * FROM class_types
            WHERE is_active = true
              AND description_embedding IS NOT NULL
            ORDER BY description_embedding <=> CAST(:embedding AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<ClassType> findByDescriptionSimilarTo(
            @Param("embedding") String embedding,
            @Param("limit") int limit
    );

    @Query(value = """
            SELECT ct.* FROM class_types ct
            JOIN (
                SELECT description_embedding FROM class_types WHERE id = :classTypeId
            ) target ON true
            WHERE ct.id != :classTypeId
              AND ct.is_active = true
              AND ct.description_embedding IS NOT NULL
            ORDER BY ct.description_embedding <=> target.description_embedding
            LIMIT :limit
            """, nativeQuery = true)
    List<ClassType> findSimilarTo(@Param("classTypeId") Long classTypeId, @Param("limit") int limit);
}