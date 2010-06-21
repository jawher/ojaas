package jawher.ojaas.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jawher.ojaas.IJobDescriptor;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

public class TaskTracker extends ServiceTracker {
	private static final String GROUP = "OJaaS";
	private Scheduler scheduler;
	private Map<IJobDescriptor, String> jobsNames = new HashMap<IJobDescriptor, String>();
	private int currentJobId = 0;

	public TaskTracker(BundleContext context) {
		super(context, IJobDescriptor.class.getName(), null);

		try {
			scheduler = new StdSchedulerFactory().getScheduler();
		} catch (SchedulerException e) {
			throw new RuntimeException("Can't create the scheduler", e);
		}

		try {
			scheduler.start();
		} catch (SchedulerException e) {
			throw new RuntimeException("Can't start the scheduler", e);
		}
	}

	@Override
	public Object addingService(ServiceReference reference) {
		Object service = super.addingService(reference);

		IJobDescriptor task = (IJobDescriptor) service;
		String jobName = task.getName();

		int id = currentJobId++;

		if (jobsNames.containsValue(jobName)) {
			jobName = jobName + "/" + id;
		}

		JobDetail jd = new JobDetail(jobName, null, DelegatingQuartzJob.class);
		jd.getJobDataMap().put("task", task);
		int idx = 0;
		List<Trigger> triggers = new ArrayList<Trigger>();
		for (String cronExpression : task.getCronExpressions()) {
			try {
				CronTrigger trigger = new CronTrigger(jobName + ".trigger."
						+ (idx + 1), GROUP);

				trigger.setCronExpression(cronExpression);

				triggers.add(trigger);
			} catch (ParseException e) {
				throw new RuntimeException("Invalid cron expression '"
						+ cronExpression + "'", e);
			}
			idx++;

		}

		if (triggers.size() > 0) {
			idx = 0;
			for (Trigger trigger : triggers) {
				try {
					if (idx == 0) {
						scheduler.scheduleJob(jd, trigger);
					} else {
						scheduler.scheduleJob(trigger);
					}
					idx++;
				} catch (SchedulerException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}

			jobsNames.put(task, jobName);
		}

		return service;
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		super.removedService(reference, service);
		IJobDescriptor task = (IJobDescriptor) service;
		String jobName = jobsNames.get(task);
		if (jobName != null) {
			try {
				scheduler.deleteJob(jobName, null);
				task.deactivate();
			} catch (SchedulerException e) {
				e.printStackTrace();
			}
			jobsNames.remove(service);

		}
	}

	@Override
	public void close() {
		super.close();
		try {
			scheduler.shutdown();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

}
