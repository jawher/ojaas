package jawher.ojaas.impl;

import jawher.ojaas.IJobDescriptor;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class DelegatingQuartzJob implements Job {

	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		IJobDescriptor task = (IJobDescriptor) context.getJobDetail().getJobDataMap().get("task");
		task.execute(context.getFireTime());

	}

}
