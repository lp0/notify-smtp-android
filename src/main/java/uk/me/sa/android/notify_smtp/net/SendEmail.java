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
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;

import org.apache.commons.net.smtp.AuthenticatingSMTPClient;
import org.apache.commons.net.smtp.SMTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.me.sa.android.notify_smtp.data.Prefs_;
import android.os.Build;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class SendEmail implements Runnable {
	private static final HostnameVerifier HOSTNAME_VERIFIER = HttpsURLConnection.getDefaultHostnameVerifier();
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

		Calendar cEvent = Calendar.getInstance(Locale.ENGLISH);
		cEvent.setTime(ts);
		final int dowEvent = cEvent.get(Calendar.DAY_OF_WEEK);
		final int hEvent = cEvent.get(Calendar.HOUR_OF_DAY);
		final int mEvent = cEvent.get(Calendar.MINUTE);
		if (days.contains(String.valueOf(dowEvent))) {
			if (log.isDebugEnabled())
				log.debug("days match: {} in {}", dowEvent, days.toArray());
		} else {
			if (log.isDebugEnabled())
				log.debug("days mismatch: {} not in {}", dowEvent, days.toArray());
			return false;
		}

		Calendar cStart = Calendar.getInstance(Locale.ENGLISH);
		try {
			cStart.setTime(new SimpleDateFormat("HH:mm", Locale.ENGLISH).parse(startTime));
		} catch (ParseException e) {
			log.warn("startTime invalid: {}", startTime);
			return false;
		}
		final int hStart = cStart.get(Calendar.HOUR_OF_DAY);
		final int mStart = cStart.get(Calendar.MINUTE);

		if ((hEvent == hStart && mEvent >= mStart) || hEvent > hStart) {
			if (log.isDebugEnabled())
				log.debug("startTime match: {} {} >= {} {}", hEvent, mEvent, hStart, mStart);
		} else {
			if (log.isDebugEnabled())
				log.debug("startTime mismatch: {} {} < {} {}", hEvent, mEvent, hStart, mStart);
			return false;
		}

		Calendar cStop = Calendar.getInstance(Locale.ENGLISH);
		try {
			cStop.setTime(new SimpleDateFormat("HH:mm", Locale.ENGLISH).parse(stopTime));
		} catch (ParseException e) {
			log.warn("stopTime invalid: {}", startTime);
			return false;
		}
		final int hStop = cStop.get(Calendar.HOUR_OF_DAY);
		final int mStop = cStop.get(Calendar.MINUTE);

		if ((hEvent == hStop && mEvent <= mStop) || hEvent < hStop) {
			if (log.isDebugEnabled())
				log.debug("stopTime match: {} {} <= {} {}", hEvent, mEvent, hStop, mStop);
		} else {
			if (log.isDebugEnabled())
				log.debug("stopTime mismatch: {} {} > {} {}", hEvent, mEvent, hStop, mStop);
			return false;
		}

		return true;
	}

	@SuppressFBWarnings("SWL_SLEEP_WITH_LOCK_HELD")
	public void run() {
		synchronized (SendEmail.class) {
			for (int i = 0; i < ATTEMPTS; i++) {
				boolean ok = false;

				try {
					log.info("Sending email at {} for: {}", ts, message);
					ok = send();
				} catch (Exception e) {
					log.error("Unable to send email", e);
				}

				if (ok)
					break;

				if (i + 1 < ATTEMPTS) {
					try {
						Thread.sleep((int)TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS));
					} catch (InterruptedException e) {
						log.warn("Interrupted while sleeping", e);
					}
				}
			}
		}
	}

	private boolean send() throws NoSuchAlgorithmException, SocketException, IOException, InvalidKeyException, InvalidKeySpecException {
		AuthenticatingSMTPClient client = new AuthenticatingSMTPClient() {
			@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
			@Override
			public boolean execTLS() throws IOException {
				boolean ret = super.execTLS();
				// Android does not support SSLParameters.setEndpointIdentificationAlgorithm("HTTPS") and the TrustManager is insecure by default
				if (ret && !HOSTNAME_VERIFIER.verify(node, ((SSLSocket)_socket_).getSession()))
					throw new SSLException("Hostname doesn't match certificate");
				return ret;
			}
		};
		client.setDefaultTimeout(TIMEOUT_MS);
		client.connect(node, port);
		client.setSoTimeout(TIMEOUT_MS);
		try {
			if (SMTPReply.isPositiveCompletion(client.getReplyCode())) {
				log.info("CONN: {}", client.getReplyString());
			} else {
				log.error("CONN: {}", client.getReplyString());
				return false;
			}

			String helo = "android.invalid";
			InetAddress addr = client.getLocalAddress();
			if (addr instanceof Inet4Address) {
				helo = "[" + addr.getHostAddress() + "]";
			} else if (addr instanceof Inet6Address) {
				helo = "[IPv6:" + addr.getHostAddress() + "]";
			}

			client.ehlo(helo);
			if (SMTPReply.isPositiveCompletion(client.getReplyCode())) {
				log.info("EHLO: {}", client.getReplyStrings()[0]);
			} else {
				log.error("EHLO: {}", client.getReplyString());
				return false;
			}

			if (client.execTLS()) {
				log.info("STARTTLS: {}", client.getReplyString());

				client.ehlo(helo);
				if (SMTPReply.isPositiveCompletion(client.getReplyCode())) {
					log.info("EHLO: {}", client.getReplyStrings()[0]);
				} else {
					log.error("EHLO: {}", client.getReplyString());
					return false;
				}

				client.auth(AuthenticatingSMTPClient.AUTH_METHOD.CRAM_MD5, username, password);
				if (SMTPReply.isPositiveCompletion(client.getReplyCode())) {
					log.info("AUTH: {}", client.getReplyString());
				} else {
					log.error("AUTH: {}", client.getReplyString());
					return false;
				}

				client.setSender(sender);
				if (SMTPReply.isPositiveCompletion(client.getReplyCode())) {
					log.info("MAIL: {}", client.getReplyString());
				} else {
					log.error("MAIL: {}", client.getReplyString());
					return false;
				}

				for (String recipient : recipients) {
					client.addRecipient(recipient);
					if (SMTPReply.isPositiveCompletion(client.getReplyCode())) {
						log.info("RCPT: {}", client.getReplyString());
					} else {
						log.error("RCPT: {}", client.getReplyString());
						return false;
					}
				}

				Writer w = client.sendMessageData();
				if (w != null) {
					log.info("DATA: {}", client.getReplyString());

					PrintWriter pw = new PrintWriter(w);
					pw.println("Message-Id: <" + UUID.randomUUID() + "@android.invalid>");
					pw.println("Date: " + new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").format(ts));
					pw.println("Subject: " + message);
					pw.println("From: " + Build.MANUFACTURER + " " + Build.MODEL + " <" + sender + ">");
					pw.print("To: ");
					boolean first = true;
					for (String recipient : recipients) {
						if (!first)
							pw.print(", ");
						pw.print("<" + recipient + ">");
						first = false;
					}
					pw.println();
					pw.println("Content-Type: text/plain; charset=UTF-8");
					pw.println("Content-Transfer-Encoding: 8bit");
					pw.println("X-Auto-Response-Suppress: OOF");
					pw.println("");
					pw.println();
					pw.close();
				} else {
					log.error("DATA: {}", client.getReplyString());
					return false;
				}

				client.completePendingCommand();
				if (SMTPReply.isPositiveCompletion(client.getReplyCode())) {
					log.info("DATA: {}", client.getReplyString());
				} else {
					log.error("DATA: {}", client.getReplyString());
					return false;
				}
			} else {
				log.error("STARTTLS: {}", client.getReplyString());
				return false;
			}

			client.logout();
			if (SMTPReply.isPositiveCompletion(client.getReplyCode())) {
				log.info("QUIT: {}", client.getReplyString());
				return true;
			} else {
				log.warn("QUIT: {}", client.getReplyString());
				return false;
			}
		} finally {
			try {
				client.disconnect();
			} catch (IOException e) {
				log.error("Error disconnecting", e);
			}
		}
	}
}