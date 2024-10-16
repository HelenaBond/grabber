package org.example.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class HabrCareerDateTimeParserTest {

    @Test
    public void whenParseDateToCurrentFormat() {
        DateTimeParser habrParser = new HabrCareerDateTimeParser();
        LocalDateTime actual = LocalDateTime.parse("2024-09-19T11:54:30");
        LocalDateTime expected = habrParser.parse("2024-09-19T11:54:30+03:00");
        assertThat(actual).isEqualTo(expected);
    }
}
