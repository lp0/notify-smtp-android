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
package uk.me.sa.android.notify_smtp.net;

import java.io.IOException;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.net.smtp.AuthenticatingSMTPClient.AUTH_METHOD;
import org.apache.commons.net.smtp.SMTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.me.sa.android.notify_smtp.data.Message;
import uk.me.sa.android.notify_smtp.data.Prefs_;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class SendEmail implements Runnable {
	private static final int ATTEMPTS = 3;
	private static final int TIMEOUT_MS = (int)TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS);

	private Logger log = LoggerFactory.getLogger(SendEmail.class);

	private String message;
	private Date ts;

	private Set<String> days;
	private String startTime;
	private String stopTime;
	private String node;
	private int port;
	private String username;
	private String password;
	private String sender;
	private String[] recipients;

	public SendEmail(Prefs_ prefs, String message, Date ts) {
		this.message = message;
		this.ts = (Date)ts.clone();

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

	public boolean isActive() {
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

	@SuppressFBWarnings("SWL_SLEEP_WITH_LOCK_HELD")
	public void run() {
		try {
			synchronized (SendEmail.class) {
				for (int i = 0; i < ATTEMPTS; i++) {
					try {
						log.info("Sending email at {} for: {}", ts, message);

						if (send())
							break;
					} catch (Exception e) {
						log.error("Unable to send email", e);
					}

					if (i + 1 < ATTEMPTS)
						Thread.sleep((int)TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS));
				}
			}
		} catch (InterruptedException e) {
			log.warn("Interrupted while sleeping", e);
		}
	}

	private boolean send() throws NoSuchAlgorithmException, SocketException, IOException, InvalidKeyException, InvalidKeySpecException {
		AuthSMTPTLSClient client = new AuthSMTPTLSClient();
		client.setDefaultTimeout(TIMEOUT_MS);
		client.connect(node, port);
		client.setSoTimeout(TIMEOUT_MS);
		try {
			if (!SMTPReply.isPositiveCompletion(client.getReplyCode()))
				return false;

			if (!client.elogin() || !client.execTLS())
				return false;

			if (!client.elogin() || !client.auth(AUTH_METHOD.PLAIN, username, password))
				return false;

			if (!client.setSender(sender))
				return false;

			for (String recipient : recipients)
				if (!client.addRecipient(recipient))
					return false;

			if (!client.sendShortMessageData(new Message(message, ts, sender, recipients).toString()))
				return false;

			return client.logout();
		} finally {
			try {
				client.disconnect();
			} catch (IOException e) {
				log.error("Error disconnecting", e);
			}
		}
	}
}