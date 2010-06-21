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

    public class DumbJob implements IJobDescriptor {
    
    	@Override
    	public void deactivate() {
    
    	}
    
    	@Override
    	public void execute(Date fireTime) {
    		System.out.println("I ran !");
    
    	}
    
    	@Override
    	public Collection<String> getCronExpressions() {
    		return Arrays.asList("*/10 * * * * ?");
    	}
    
    	@Override
    	public String getName() {
    		return "Dumb job";
    	}
    
    }


For this to work, you'll need to :
* Import the package jawher.ojaas (in MANIFEST.MF)
* Register the job descriptor as a service with the interface jawher.ojaas.IJobDescriptor
* Have the bundle jawher.ojaas.impl in the ACTIVE state in you OSGi container

License
-------

See `LICENSE` for details.
