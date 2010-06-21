package jawher.ojaas;

import java.util.Collection;
import java.util.Date;

public interface IJobDescriptor {
	/**
	 * 
	 * @return the list of cron experessions that this job wants to be fired according to.
	 */
	Collection<String> getCronExpressions();

	String getName();

	/**
	 * This method gets called periodically according to the job's cron expressions
	 * @param fireTime The theoretical time this job should have run. Usually fireTime is equal to
	 * now, but when the scheduler is busy, there may be a delay
	 */
	void execute(Date fireTime);

	/**
	 * Called once the job has been unscheduled.
	 */
	void deactivate();
}
