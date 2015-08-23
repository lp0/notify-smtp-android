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

import java.util.Date;

import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.me.sa.android.notify_smtp.data.Prefs_;
import uk.me.sa.android.notify_smtp.data.ValidatedPrefs;
import uk.me.sa.android.notify_smtp.net.SendEmail;
import uk.me.sa.android.notify_smtp.util.SequentialRetryRunnable;
import uk.me.sa.android.notify_smtp.util.WakeLockRunnable;
import android.app.Notification;
import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

@EService
public class NotificationListener extends NotificationListenerService {
	private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

	private PowerManager pm;

	@Pref
	Prefs_ prefs;

	@Override
	public void onCreate() {
		super.onCreate();

		pm = (PowerManager)getApplicationContext().getSystemService(Context.POWER_SERVICE);
	}

	@Override
	public void onListenerConnected() {
		for (StatusBarNotification sbn : getActiveNotifications()) {
			if (log.isDebugEnabled()) {
				log.debug("Active notification:");
				logNotification(sbn);
			}
			processNotification(sbn);
		}
	}

	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		if (log.isDebugEnabled()) {
			log.debug("Notification posted:");
			logNotification(sbn);
		}
		processNotification(sbn);
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {
		if (log.isDebugEnabled()) {
			log.debug("Notification removed:");
			logNotification(sbn);
		}
	}

	private void logNotification(StatusBarNotification sbn) {
		Notification n = sbn.getNotification();
		log.debug(" {}/{}@{}: category={} icon={} tickerText={}", sbn.getId(), sbn.getPackageName(), sbn.getPostTime(), n.category, n.icon, n.tickerText);
	}

	private void processNotification(StatusBarNotification sbn) {
		if (!prefs.enabled().get())
			return;

		Notification n = sbn.getNotification();
		if (n.icon == android.R.drawable.stat_notify_missed_call) {
			log.debug("  Missed call notification");
			sendEmail(getString(R.string.email_missed_call_notification), sbn);
		} else if (((Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) || Notification.CATEGORY_MESSAGE.equals(n.category))
				&& sbn.getPackageName().equals("com.google.android.talk")) {
			log.debug("  Message notification");
			sendEmail(getString(R.string.email_message_notification), sbn);
		}
	}

	private void sendEmail(String text, StatusBarNotification sbn) {
		Date ts = new Date(sbn.getPostTime());
		ValidatedPrefs vp = new ValidatedPrefs(prefs);
		if (vp.isActiveAt(ts))
			new Thread(new WakeLockRunnable(pm, new SequentialRetryRunnable(new SendEmail(vp, text, ts)))).start();
	}
}
