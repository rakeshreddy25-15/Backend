package com.studyleague.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PracticeTestCase {
    @Column(name = "input", columnDefinition = "TEXT")
    private String input;

    @Column(name = "expected_output", columnDefinition = "TEXT")
    private String expectedOutput;
}



