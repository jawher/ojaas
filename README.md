OSGi Jobs as a Service
=======================

A small utility usable in a OSGi environment that watches for services registration implementing a specific interface and schedules them for periodic execution using Quartz.

Building
--------

You need a Java 5 (or newer) environment and Maven 3 installed:

    $ mvn --version
    Apache Maven 3.0-beta-1 (r935667; 2010-04-19 19:00:39+0200)
    Java version: 1.6.0_20
    Java home: /usr/lib/jvm/java-6-sun-1.6.0.20/jre
    Default locale: en_US, platform encoding: UTF-8
    OS name: "linux" version: "2.6.32-22-generic" arch: "amd64" Family: "unix"

You should now be able to do a full build of `ojaas`:

    $ git clone git://github.com/jawher/ojaas.git
    $ cd ojaas
    $ mvn clean package



Troubleshooting
---------------

Please consider using [Github issues tracker](http://github.com/jawher/ojaas/issues) to submit bug reports or feature requests.


Using this library
------------------

Here is a sample showing a job definition that'll print 'I ran' every 10 seconds :

    Dictionary<String, Object> params = new Hashtable<String, Object>();
    params.put("ojaas.cron", "0/10 * * * * ?;5/11 * * * * ?");
    params.put("ojaas.name", "Look Ma");
    reg = context.registerService(Runnable.class.getName(), new Runnable() {
    
    	@Override
    	public void run() {
    		System.out.println("Look ma, clean API !");
    		if (Math.random() < 0.01) {
    			Thread.currentThread().interrupt();
    		}
    	}
    }, params);
    


For this to work, you'll need to :
* Register the job's runnable as a service with the interface `java.lang.Runnable` and with the **required** service property `ojaas.cron` (a comma seperated list of cron expressions according to whom the job is to be scheduled for execution). Optionally, `ojaas.name` can be used to specify the job name. 
* Have the bundle `jawher.ojaas` in the `ACTIVE` state in you OSGi container

A job can 'indicate' to the scheduler that it is to be canceled by calling `Thread.currentThread().interrupt()`

License
-------

See `LICENSE` for details.

Credits
-------
Thanks to [Neil Bartlett](http://njbartlett.name/blog) who [suggested](http://twitter.com/njbartlett/status/16730020595) the usage of `java.lang.Runnable` and service properties instead of implementing a OJaaS-specific interface to describe jobs.
