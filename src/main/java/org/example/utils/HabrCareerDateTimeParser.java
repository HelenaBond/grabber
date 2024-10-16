package org.example.utils;

import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;

public class HabrCareerDateTimeParser implements DateTimeParser {

    @Override
    public LocalDateTime parse(String parse) {
        LocalDateTime dateTime = LocalDateTime.parse(parse, ISO_OFFSET_DATE_TIME);
        return LocalDateTime.parse(dateTime.format(ISO_LOCAL_DATE_TIME));
    }
}
