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
package uk.me.sa.android.notify_smtp;

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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.apache.commons.net.smtp.AuthenticatingSMTPClient;
import org.apache.commons.net.smtp.SMTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.me.sa.android.notify_smtp.data.Prefs_;
import android.app.Notification;
import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.widget.Toast;

@EService
public class NotificationListener extends NotificationListenerService {
	private Logger log = LoggerFactory.getLogger(NotificationListener.class);

	@Pref
	Prefs_ prefs;

	@Override
	public void onListenerConnected() {
		for (StatusBarNotification sbn : getActiveNotifications()) {
			log.debug("Active notification:");
			logNotification(sbn);
			processNotification(sbn);
		}
	}

	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		log.debug("Notification posted:");
		logNotification(sbn);
		processNotification(sbn);
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {
		log.debug("Notification removed:");
		logNotification(sbn);
	}

	private void logNotification(StatusBarNotification sbn) {
		Notification n = sbn.getNotification();
		if (log.isDebugEnabled())
			log.debug(" {}/{}@{}: category={} icon={} tickerText={}", sbn.getId(), sbn.getPackageName(), sbn.getPostTime(), n.category, n.icon, n.tickerText);
	}

	private void processNotification(StatusBarNotification sbn) {
		Notification n = sbn.getNotification();
		if (n.icon == android.R.drawable.stat_notify_missed_call) {
			log.debug("  Missed call notification");
			new SendEmail("Missed phone call", new Date(sbn.getPostTime())).start();
		} else if (n.category != null && n.category.equals(Notification.CATEGORY_MESSAGE) && sbn.getPackageName().equals("com.google.android.talk")) {
			log.debug("  Message notification");
			new SendEmail("Message received", new Date(sbn.getPostTime())).start();
		}
	}

	@UiThread
	void makeToast(String text) {
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
	}

	private class SendEmail extends Thread {
		private String message;
		private Date ts;

		private String node = prefs.node().get();
		private String service = prefs.service().get();
		private String username = prefs.username().get();
		private String password = prefs.password().get();
		private String sender = prefs.sender().get();
		private String[] recipients = prefs.recipients().get().split(" ");

		public SendEmail(String message, Date ts) {
			this.message = message;
			this.ts = ts;
		}

		public void run() {
			if (node.isEmpty()) {
				log.warn("node missing");
				makeToast(getString(R.string.pref_node_missing));
				return;
			}

			if (service.isEmpty()) {
				log.warn("service missing");
				makeToast(getString(R.string.pref_service_missing));
				return;
			}

			if (username.isEmpty()) {
				log.warn("username missing");
				makeToast(getString(R.string.pref_username_missing));
				return;
			}

			if (password.isEmpty()) {
				log.warn("password missing");
				makeToast(getString(R.string.pref_password_missing));
				return;
			}

			if (sender.isEmpty()) {
				log.warn("sender missing");
				makeToast(getString(R.string.pref_sender_missing));
				return;
			}

			if (recipients.length == 0) {
				log.warn("recipients missing");
				makeToast(getString(R.string.pref_recipients_missing));
				return;
			}

			PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
			PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getCanonicalName());
			wl.acquire();
			try {
				synchronized (SendEmail.class) {
					for (int i = 0; i < 3; i++) {
						boolean ok = false;

						try {
							log.info("Sending email at {} for: {}", ts, message);
							ok = send();
						} catch (Exception e) {
							log.error("Unable to send email", e);
						}

						if (ok)
							break;
					}
				}
			} finally {
				wl.release();
			}
		}

		private boolean send() throws NoSuchAlgorithmException, SocketException, IOException, InvalidKeyException, InvalidKeySpecException {
			AuthenticatingSMTPClient client = new AuthenticatingSMTPClient();
			client.setDefaultTimeout((int)TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS));
			client.connect("smtp.lp0.eu", 587);
			client.setSoTimeout((int)TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS));
			try {
				if (SMTPReply.isPositiveCompletion(client.getReplyCode())) {
					log.info("connect: {}", client.getReplyString());
				} else {
					log.error("connect: {}", client.getReplyString());
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
					client.ehlo(helo);
					if (SMTPReply.isPositiveCompletion(client.getReplyCode())) {
						log.info("EHLO: {}", client.getReplyStrings()[0]);
					} else {
						log.error("EHLO: {}", client.getReplyString());
						return false;
					}

					client.auth(AuthenticatingSMTPClient.AUTH_METHOD.PLAIN, username, password);
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
					throw new IOException("STARTTLS failed");
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
}
