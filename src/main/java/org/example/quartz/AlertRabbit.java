package org.example.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {

    private static final Logger LOG = LoggerFactory.getLogger(AlertRabbit.class.getName());

    public static Properties readProps(String path) {
        var properties = new Properties();
        try (InputStream input = AlertRabbit.class.getClassLoader()
                .getResourceAsStream(path)) {
            properties.load(input);
        } catch (IOException e) {
            LOG.error("Error reading properties.", e);
        }
        return properties;
    }

    public static Connection connect(Properties config) throws ClassNotFoundException, SQLException {
        Class.forName(config.getProperty("driver-class-name"));
        return DriverManager.getConnection(
                config.getProperty("url"),
                config.getProperty("username"),
                config.getProperty("password")
        );
    }

    public static void main(String[] args) throws InterruptedException {
        var properties = readProps("rabbit.properties");
        try (var connection = connect(properties)) {
            var scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            var jobDataMap = new JobDataMap();
            jobDataMap.put("connection", connection);
            var jobDetail = newJob(AlertRabbit.Rabbit.class)
                    .usingJobData(jobDataMap)
                    .build();
            var interval = Integer.parseInt(properties.getProperty("rabbit.interval"));
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(interval)
                    .repeatForever();
            var trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(jobDetail, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (SchedulerException se) {
            LOG.error("Something is wrong with schedule", se);
        } catch (SQLException | ClassNotFoundException e) {
            LOG.error("Something is wrong with connection to database", e);
        }
    }

    public static class Rabbit implements Job {

        public Rabbit() {
            System.out.println(hashCode());
        }

        @Override
        public void execute(JobExecutionContext context) {
            System.out.println("Rabbit runs here ...");
            var jobDataMap = context.getJobDetail().getJobDataMap();
            var connection = (Connection) jobDataMap.get("connection");
            String sql = "INSERT INTO rabbit (created_date) VALUES (?)";
            var timestamp = Timestamp.valueOf(LocalDateTime.now().withNano(0));
            try (var prepareStatement = connection.prepareStatement(sql)) {
                prepareStatement.setTimestamp(1, timestamp);
                prepareStatement.execute();
            } catch (SQLException e) {
                LOG.error("Something is wrong with request to database", e);
            }
        }
    }
}
