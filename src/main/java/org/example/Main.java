package org.example;

import org.example.jsoup.HabrCareerParse;
import org.example.jsoup.Parse;
import org.example.quartz.Grabber;
import org.example.repo.PsqlPostStore;
import org.example.repo.Store;
import org.example.server.Simple;
import org.example.utils.HabrCareerDateTimeParser;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;

import java.io.InputStream;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws Exception {
        var config = new Properties();
        try (InputStream input = Grabber.class.getClassLoader()
                .getResourceAsStream("app.properties")) {
            config.load(input);
        }
        String keywords = config.getProperty("keywords");
        Parse parse = new HabrCareerParse(new HabrCareerDateTimeParser(), keywords);

        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();

        Store store = new PsqlPostStore(config);
        Simple server = new Simple(Integer.parseInt(config.getProperty("port")));
        server.web(store);
        new Grabber(parse, store, scheduler, config).init();
    }
}
