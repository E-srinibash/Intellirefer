package com.yourcompany.intellirefer.repository;

import com.yourcompany.intellirefer.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Integer> {

    /**
     * Finds a skill by its name (case-insensitive).
     * Useful for checking if a skill already exists before creating a new one.
     * @param name The name of the skill to search for.
     * @return An Optional containing the Skill if found.
     */
    Optional<Skill> findByNameIgnoreCase(String name);
}