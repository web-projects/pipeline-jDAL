package com.trustcommerce.ipa.dal.device.emv;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SimpleTimer {
	
	private final static Logger logger = LoggerFactory.getLogger(SimpleTimer.class);
	private final long timeout;
	private Timer timer = null;

	public SimpleTimer(long timeout) {
		this.timeout = timeout;
	}

	abstract public TimerTask getInstance();

	public void start() {
		try {
			TimerTask task = getInstance();
			if (task == null)
				throw new InstantiationException();

			if (timer == null)
				timer = new Timer();

			timer.schedule(getInstance(), timeout);
			logger.debug("Timer started (timeout=" + timeout + " ms)");
		} catch (Exception ex) {
			logger.debug("Failed to start timer: " + ex.getMessage());
		}
	}

	public void stop() {
		if (timer == null)
			return;

		try {
			timer.cancel();
			timer = null;
			logger.debug("Timer stopped");
		} catch (IllegalStateException e) {
			logger.debug("Timer already cancelled");
		}
	}

	public void restart() {
		stop();
		start();
	}
}
