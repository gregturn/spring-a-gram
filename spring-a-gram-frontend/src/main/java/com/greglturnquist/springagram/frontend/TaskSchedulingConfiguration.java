package com.greglturnquist.springagram.frontend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * Workaround multiple TaskScheduler beans by setting the default
 */
@Configuration
public class TaskSchedulingConfiguration implements SchedulingConfigurer {

	@Autowired
	private TaskScheduler taskScheduler;

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setScheduler(this.taskScheduler);
	}

}
