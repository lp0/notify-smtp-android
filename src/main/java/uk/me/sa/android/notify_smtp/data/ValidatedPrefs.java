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
package uk.me.sa.android.notify_smtp.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidatedPrefs {
	private static final Logger log = LoggerFactory.getLogger(ValidatedPrefs.class);

	private Set<String> days;
	private String startTime;
	private String stopTime;
	public final String node;
	public final int port;
	public final String username;
	public final String password;
	public final String sender;
	public final String[] recipients;

	public ValidatedPrefs(Prefs_ prefs) {
		days = prefs.days().get();
		startTime = prefs.startTime().get();
		stopTime = prefs.stopTime().get();
		node = prefs.node().get();
		port = prefs.port().get();
		username = prefs.username().get();
		password = prefs.password().get();
		sender = prefs.sender().get();
		recipients = prefs.recipients().get().split(" ");
	}

	public boolean hasAllPrefs() {
		if (startTime.isEmpty()) {
			log.warn("startTime missing");
			return false;
		}

		if (stopTime.isEmpty()) {
			log.warn("stopTime missing");
			return false;
		}

		if (node.isEmpty()) {
			log.warn("node missing");
			return false;
		}

		if (port == 0) {
			log.warn("port missing");
			return false;
		}

		if (username.isEmpty()) {
			log.warn("username missing");
			return false;
		}

		if (password.isEmpty()) {
			log.warn("password missing");
			return false;
		}

		if (sender.isEmpty()) {
			log.warn("sender missing");
			return false;
		}

		if (recipients.length == 0) {
			log.warn("recipients missing");
			return false;
		}

		return true;
	}

	public boolean isActiveAt(Date ts) {
		if (!hasAllPrefs())
			return false;

		Calendar c = Calendar.getInstance(Locale.ENGLISH);
		c.setTime(ts);
		final int dowEvent = c.get(Calendar.DAY_OF_WEEK);
		final int hEvent = c.get(Calendar.HOUR_OF_DAY);
		final int mEvent = c.get(Calendar.MINUTE);

		if (!days.contains(String.valueOf(dowEvent)))
			return false;

		try {
			c.setTime(new SimpleDateFormat("HH:mm", Locale.ENGLISH).parse(startTime));
		} catch (ParseException e) {
			log.warn("startTime invalid: {}", startTime);
			return false;
		}
		final int hStart = c.get(Calendar.HOUR_OF_DAY);
		final int mStart = c.get(Calendar.MINUTE);

		if (!((hEvent == hStart && mEvent >= mStart) || hEvent > hStart))
			return false;

		try {
			c.setTime(new SimpleDateFormat("HH:mm", Locale.ENGLISH).parse(stopTime));
		} catch (ParseException e) {
			log.warn("stopTime invalid: {}", startTime);
			return false;
		}
		final int hStop = c.get(Calendar.HOUR_OF_DAY);
		final int mStop = c.get(Calendar.MINUTE);

		if (!((hEvent == hStop && mEvent <= mStop) || hEvent < hStop))
			return false;

		return true;
	}
}
