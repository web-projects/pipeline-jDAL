package com.trustcommerce.ipa.dal.logger;

import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.net.SyslogAppender;

import com.trustcommerce.ipa.dal.logger.utils.ConfigurationUtil;



/** 
 * This log4J initializer is designed for the logs generated under
 * /var/log/lmc.log. The logs generated under /mtn/thales/.lmc/.log use the
 * log4j.xml configuration that resides on /etc/thales/fml-lmc.
 * 
 * @author thales
 *
 */
public class DefaultLoggerInitializer {

    /**
     * Pattern use for /var/log/lmc.log
     */
    private static final String PATTERN = "%d{dd-MM-yyyy HH:mm:ss} %c{1}:%5L - %5p - %m%n";

    private static final String PATTERN_DEV = "%d{dd-MM-yyyy HH:mm:ss} %c:%5L - %5p - %m%n";

    /**
	 * 
	 */
    public static void initSyslog() {

	final SyslogAppender syslogAppender = new SyslogAppender();
	syslogAppender.setName("syslog");

	String environmentType = null;
    environmentType =  ConfigurationUtil.getConfiguration().getEnvironmentType();

	if (environmentType != null && environmentType.equalsIgnoreCase("dev")) {
	    syslogAppender.setLayout(new PatternLayout(PATTERN_DEV));
	} else {
	    syslogAppender.setLayout(new PatternLayout(PATTERN));
	}
	syslogAppender.setFacility("LOCAL7");
	syslogAppender.setFacilityPrinting(false);
	syslogAppender.setSyslogHost("localhost");
	syslogAppender.activateOptions();
	Logger.getRootLogger().addAppender(syslogAppender);
	// } else {
	// ConsoleAppender consoleAppender = new ConsoleAppender();
	// consoleAppender.setName("console");
	// consoleAppender.setLayout(new PatternLayout(PATTERN));
	// consoleAppender.activateOptions();
	// Logger.getRootLogger().addAppender(consoleAppender);
	// }
    }
};
