package jawher.ojaas.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

public class TaskTracker extends ServiceTracker implements InterrupCallback {
	private static final Logger log = Logger.getLogger("ojaas");
	private static final String GROUP = "OJaaS";
	private static final String CRON_KEY = "ojaas.cron";
	private static final String NAME_KEY = "ojaas.name";
	private final Scheduler scheduler;
	private final Map<Runnable, String> jobsNames = new HashMap<Runnable, String>();
	private int currentJobId = 0;
	private final Lock lock = new ReentrantLock();

	public TaskTracker(BundleContext context) {
		super(context, Runnable.class.getName(), null);

		try {
			scheduler = new StdSchedulerFactory().getScheduler();
		} catch (SchedulerException e) {
			throw new RuntimeException("Can't create the scheduler", e);
		}

		try {
			scheduler.start();
			log.info("Started OJaaS scheduler");
		} catch (SchedulerException e) {
			throw new RuntimeException("Can't start the scheduler", e);
		}

	}

	private String getJobName(ServiceReference reference, Runnable job) {
		String jobName = (String) reference.getProperty(NAME_KEY);
		if (jobName == null) {
			jobName = job.toString();
		}
		return jobName;
	}

	@Override
	public Object addingService(ServiceReference reference) {
		Object service = super.addingService(reference);
		String rawCron = (String) reference.getProperty(CRON_KEY);
		String[] cronExpressions = rawCron.split(";");

		Runnable job = (Runnable) service;
		String jobName = getJobName(reference, job);

		int idx = 0;
		List<Trigger> triggers = new ArrayList<Trigger>();
		for (String cronExpression : cronExpressions) {
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

		try {
			lock.lock();
			int id = currentJobId++;

			if (jobsNames.containsValue(jobName)) {
				jobName = jobName + "/" + id;
			}

			JobDetail jd = new JobDetail(jobName, GROUP,
					DelegatingQuartzJob.class);
			jd.getJobDataMap().put("task", job);
			jd.getJobDataMap().put("interruptCallback", this);

			if (triggers.size() > 0) {
				idx = 0;
				for (Trigger trigger : triggers) {
					try {
						if (idx == 0) {
							scheduler.scheduleJob(jd, trigger);
						} else {
							trigger.setJobGroup(GROUP);
							trigger.setJobName(jobName);
							scheduler.scheduleJob(trigger);
						}
						idx++;
					} catch (SchedulerException e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				}

				jobsNames.put(job, jobName);
				log.info("scheduled job '" + jobName+"'");
			}

		} finally {
			lock.unlock();
		}
		return service;
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		super.removedService(reference, service);

		Runnable job = (Runnable) service;
		log.info("Job '" + getJobName(reference, job)
				+ "' is about to be removed");
		interrupt(job);

	}

	
	public void interrupt(Runnable job) {
		String jobName = jobsNames.get(job);
		if (jobName != null) {
			try {
				scheduler.deleteJob(jobName, GROUP);
				log.info("Unscheduled '" + jobName + "'");
			} catch (SchedulerException e) {
				e.printStackTrace();
			}
			try {
				lock.lock();
				jobsNames.remove(job);
			} finally {
				lock.unlock();
			}
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
