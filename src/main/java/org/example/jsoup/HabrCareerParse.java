package org.example.jsoup;

import org.example.model.Post;
import org.example.utils.DateTimeParser;
import org.example.utils.HabrCareerDateTimeParser;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    public static final String PREFIX = "/vacancies?page=";
    public static final String SUFFIX = "&q=Java%20developer&type=all";
    public static final int PAGES = 5;

    private static final Logger LOG = LoggerFactory.getLogger(HabrCareerParse.class.getName());
    private static final String SOURCE_LINK = "https://career.habr.com";

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    /**
     * The method is expected to return a list of fully completed posts.
     * But if the GUI has been changed,
     * then the posts in the list may be partially filled
     * or the list will be filled with null values.
     * @param fullLink link to resource for parsing
     * @return list of posts
     */
    @Override
    public List<Post> list(String fullLink) {
        List<Post> posts = new ArrayList<>();
        for (int i = 1; i <= PAGES; i++) {
            Connection connection = Jsoup.connect(fullLink);
            try {
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> {
                    Element titleElement = row.select(".vacancy-card__title").first();
                    Post post = null;
                    if (titleElement != null) {
                        Element linkElement = titleElement.child(0);
                        post = new Post();
                        post.setTitle(titleElement.text());
                        String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                        post.setLink(link);
                        post.setDescription(retrieveDescription(link));
                        Element dateElement = row.select(".vacancy-card__date").first();
                        if (dateElement != null) {
                            Element datetimeElement = dateElement.child(0);
                            String date = datetimeElement.attr("datetime");
                            LocalDateTime dateTime = dateTimeParser.parse(date);
                            post.setCreated(dateTime);
                        }
                    }
                    posts.add(post);
                });
            } catch (IOException e) {
                LOG.error("Something is wrong with link {}", fullLink, e);
            }
        }
        return posts;
    }

    private String retrieveDescription(String link) {
        Connection connection = Jsoup.connect(link);
        String text = null;
        try {
            Document document = connection.get();
            Element row = document.select(".vacancy-description__text").first();
            if (row != null) {
                text = row.text();
            }
        } catch (IOException e) {
            LOG.error("Something is wrong with link {}", link, e);
        }
        return text;
    }

    public static void main(String[] args) {
        String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, PAGES, SUFFIX);
        HabrCareerParse parse = new HabrCareerParse(new HabrCareerDateTimeParser());
        System.out.println(parse.list(fullLink));
    }
}
