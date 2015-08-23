/*
	notify-smtp-android - Android Notify to SMTP Service

	Copyright 2015  Simon Arlott

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.me.sa.android.notify_smtp.util;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class SequentialRetryRunnable implements Runnable {
	protected static final Logger log = LoggerFactory.getLogger(SequentialRetryRunnable.class);
	protected static final int ATTEMPTS = 3;

	protected Callable<Boolean> callable;

	public SequentialRetryRunnable(Callable<Boolean> callable) {
		this.callable = callable;
	}

	@SuppressFBWarnings("SWL_SLEEP_WITH_LOCK_HELD")
	public void run() {
		try {
			synchronized (callable.getClass()) {
				for (int attempt = 1; attempt <= ATTEMPTS; attempt++) {
					try {
						log.info("Attempt {} of {}: {}", attempt, ATTEMPTS, callable);

						if (callable.call())
							break;
					} catch (Exception e) {
						log.error("Attempt {} failed", attempt, e);
					}

					if (attempt < ATTEMPTS)
						Thread.sleep((int)TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS));
				}
			}
		} catch (InterruptedException e) {
			log.warn("Interrupted while sleeping", e);
		}
	}
}
