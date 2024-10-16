package org.example.jsoup;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class HabrCareerParse {

    public static final String PREFIX = "/vacancies?page=";
    public static final String SUFFIX = "&q=Java%20developer&type=all";
    public static final int PAGES = 5;
    private static final String SOURCE_LINK = "https://career.habr.com";

    public static void main(String[] args) throws IOException {
        for (int i = 0; i < PAGES; i++) {
            String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, PAGES, SUFFIX);
            Connection connection = Jsoup.connect(fullLink);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                Element dateElement = row.select(".vacancy-card__date").first();
                Element datetimeElement = dateElement.child(0);
                System.out.printf("%s %s %s%n", datetimeElement.attr("datetime"), vacancyName, link);
            });
        }
    }
}
