package com.example.boka.service;

import com.example.boka.dto.GymClassMapper;
import com.example.boka.dto.GymClassResponse;
import com.example.boka.entity.ClassType;
import com.example.boka.entity.ClassStatus;
import com.example.boka.repository.ClassTypeRepository;
import com.example.boka.repository.GymClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassSearchService {

    private final EmbeddingModel embeddingModel;
    private final ClassTypeRepository classTypeRepository;
    private final GymClassRepository gymClassRepository;

    /**
     * Semantic search: embed the user query and find class types
     * with similar descriptions, then return upcoming gym classes.
     */
    public List<GymClassResponse> searchClasses(String query, int maxResults) {
        // 1. Embed the user's search query
        float[] queryEmbedding = embeddingModel.embed(query);

        // 2. Convert to pgvector string format: [0.1,0.2,...]
        String embeddingStr = toVectorString(queryEmbedding);

        // 3. Find semantically similar class types
        List<ClassType> matchingTypes = classTypeRepository
                .findByDescriptionSimilarTo(embeddingStr, maxResults);

        // 4. Find upcoming scheduled gym classes for those types
        List<Long> typeIds = matchingTypes.stream()
                .map(ClassType::getId)
                .toList();

        return gymClassRepository
                .findByClassTypeIdInAndStatusAndStartTimeAfter(
                        typeIds, ClassStatus.SCHEDULED, LocalDateTime.now())
                .stream()
                .map(GymClassMapper::toResponse)
                .toList();
    }

    /**
     * Generate and store embedding for a class type's description.
     * Call this when creating or updating a ClassType.
     */
    public void updateEmbedding(ClassType classType) {
        if (classType.getDescription() != null && !classType.getDescription().isBlank()) {
            // Combine name + description for richer semantics
            String textToEmbed = classType.getName() + ": " + classType.getDescription();
            float[] embedding = embeddingModel.embed(textToEmbed);
            classType.setDescriptionEmbedding(embedding);
        }
    }

    private String toVectorString(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}