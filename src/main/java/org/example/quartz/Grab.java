package org.example.quartz;

import org.quartz.SchedulerException;

public interface Grab {
    void init() throws SchedulerException;
}
