package jawher.ojaas.impl;

import java.util.logging.Logger;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class DelegatingQuartzJob implements Job {
	private static final Logger log = Logger.getLogger("ojaas");

	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		Runnable task = (Runnable) context.getJobDetail().getJobDataMap().get(
				"task");

		task.run();
		if (Thread.interrupted()) {
			log.info("Job '"+context.getJobDetail().getName()+"' was interrupted");
			InterrupCallback interrupCallback = (InterrupCallback) context
					.getJobDetail().getJobDataMap().get("interruptCallback");
			interrupCallback.interrupt(task);
		}

	}

}
