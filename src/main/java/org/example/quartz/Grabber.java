package org.example.quartz;

import org.example.jsoup.Parse;
import org.example.model.Post;
import org.example.repo.Store;
import org.quartz.*;

import java.util.List;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class Grabber implements Grab {
    private final Parse parse;
    private final Store store;
    private final Scheduler scheduler;
    private int interval;
    private int pages;

    public Grabber(Parse parse, Store store, Scheduler scheduler, Properties config) {
        this.parse = parse;
        this.store = store;
        this.scheduler = scheduler;
        this.interval = Integer.parseInt(config.getProperty("interval"));
        this.pages = Integer.parseInt(config.getProperty("pages"));
    }

    @Override
    public void init() throws SchedulerException {
        JobDataMap data = new JobDataMap();
        data.put("store", store);
        data.put("parse", parse);
        data.put("pages", pages);
        JobDetail job = newJob(GrabJob.class)
                .usingJobData(data)
                .build();
        SimpleScheduleBuilder times = simpleSchedule()
                .withIntervalInSeconds(interval)
                .repeatForever();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(times)
                .build();
        scheduler.scheduleJob(job, trigger);
    }

    public static class GrabJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {
            JobDataMap map = context.getJobDetail().getJobDataMap();
            Store store = (Store) map.get("store");
            Parse parse = (Parse) map.get("parse");
            int pages = (int) map.get("pages");
            for (int i = 1; i <= pages; i++) {
                List<Post> posts = parse.list(parse.currentPage(i));
                posts.forEach(store :: save);
            }
        }
    }
}
