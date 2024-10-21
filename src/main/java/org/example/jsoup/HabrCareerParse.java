package org.example.jsoup;

import org.example.model.Post;
import org.example.utils.DateTimeParser;
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
    private static final Logger LOG = LoggerFactory.getLogger(HabrCareerParse.class.getName());
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PREFIX = "/vacancies?page=";
    private static final String SUFFIX = "&q=";
    private static final String POSTFIX = "&type=all";
    private final DateTimeParser dateTimeParser;
    private String keywords;

    public HabrCareerParse(DateTimeParser dateTimeParser, String keywords) {
        this.dateTimeParser = dateTimeParser;
        this.keywords = keywords;
    }

    /**
     * The method is expected to return a list of fully completed posts.
     * But if the GUI has been changed,
     * then the posts in the list may be partially filled
     * or the list will be filled with null values.
     * @return list of posts
     */
    @Override
    public List<Post> list(String currentLink) {
        List<Post> posts = new ArrayList<>();

            try {
                Connection connection = Jsoup.connect(currentLink);
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> {
                    Element titleElement = row.select(".vacancy-card__title").first();
                    Post post = null;
                    if (titleElement != null) {
                        Element linkElement = titleElement.child(0);
                        post = new Post();
                        post.setTitle(titleElement.text());
                        String link = "%s%s".formatted(SOURCE_LINK, linkElement.attr("href"));
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
                LOG.error("Something is wrong with link {}", currentLink, e);
            }
        return posts;
    }

    @Override
    public String currentPage(int index) {
        return "%s%s%d%s%s%s".formatted(
                SOURCE_LINK,
                PREFIX,
                index,
                SUFFIX,
                this.keywords,
                POSTFIX);
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
}
