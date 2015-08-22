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

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ReflectionHelpers;
import org.robolectric.shadows.ShadowPowerManager;
import org.robolectric.shadows.ShadowPreferenceManager;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.util.ServiceController;

import uk.me.sa.android.notify_smtp.data.Prefs_;
import uk.me.sa.android.notify_smtp.net.SendEmail;
import android.app.Notification;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PowerManager;
import android.service.notification.StatusBarNotification;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
@PrepareForTest(fullyQualifiedNames = { "uk.me.sa.android.notify_smtp.TestNotificationListener", "uk.me.sa.android.notify_smtp.NotificationListener",
		"uk.me.sa.android.notify_smtp.NotificationListener_", "uk.me.sa.android.notify_smtp.NotificationListener.SendEmail" })
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*" })
public class TestNotificationListener {
	SharedPreferences sharedPreferences;
	ServiceController<NotificationListener_> controller;
	NotificationListener_ service;

	@SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
	@Rule
	public PowerMockRule rule = new PowerMockRule();

	@Mock
	StatusBarNotification sbnNormalOther;

	@Mock
	Notification nNormalOther;

	@Mock
	StatusBarNotification sbnNormalMessage;

	@Mock
	Notification nNormalMessage;

	@Mock
	StatusBarNotification sbnMissedCallIcon;

	@Mock
	Notification nMissedCallIcon;

	@Mock
	StatusBarNotification sbnTalkMessage;

	@Mock
	Notification nTalkMessage;

	@Mock
	SendEmail sendEmail;

	StatusBarNotification[] allMessages;
	StatusBarNotification[] boringMessages;

	JoinThreadsAnswer threads;

	@Before
	public void create() throws Exception {
		MockitoAnnotations.initMocks(this);

		ReflectionHelpers.setStaticFieldReflectively(Build.VERSION.class, "SDK_INT", 18);

		Mockito.doReturn(nNormalOther).when(sbnNormalOther).getNotification();
		Mockito.doReturn("com.example.system").when(sbnNormalOther).getPackageName();
		// nNormalMessage.category = Notification.CATEGORY_SYSTEM;

		Mockito.doReturn(nNormalMessage).when(sbnNormalMessage).getNotification();
		Mockito.doReturn("com.example.message").when(sbnNormalMessage).getPackageName();
		// nNormalMessage.category = Notification.CATEGORY_MESSAGE;

		Mockito.doReturn(nMissedCallIcon).when(sbnMissedCallIcon).getNotification();
		Mockito.doReturn("com.google.android.dialer").when(sbnMissedCallIcon).getPackageName();
		// nNormalMessage.category = null;
		nMissedCallIcon.icon = android.R.drawable.stat_notify_missed_call;

		Mockito.doReturn(nTalkMessage).when(sbnTalkMessage).getNotification();
		Mockito.doReturn("com.google.android.talk").when(sbnTalkMessage).getPackageName();
		// nNormalMessage.category = Notification.CATEGORY_MESSAGE;

		allMessages = new StatusBarNotification[] { sbnNormalMessage, sbnNormalOther, sbnMissedCallIcon, sbnTalkMessage };
		boringMessages = new StatusBarNotification[] { sbnNormalMessage, sbnNormalOther };

		threads = new JoinThreadsAnswer();

		PowerMockito.whenNew(SendEmail.class).withAnyArguments().thenReturn(sendEmail);
		PowerMockito.whenNew(Thread.class).withAnyArguments().thenAnswer(threads);

		ShadowToast.reset();
		ShadowPowerManager.reset();
		sharedPreferences = ShadowPreferenceManager.getDefaultSharedPreferences(Robolectric.application.getApplicationContext());
		controller = Robolectric.buildService(NotificationListener_.class);
		service = PowerMockito.spy(controller.create().bind().get());
	}

	@After
	public void destroy() {
		// controller.unbind().destroy();
	}

	@Test
	public void disabledNoMessages() throws Exception {
		sharedPreferences.edit().putBoolean("enabled", false).commit();
		PowerMockito.doReturn(new StatusBarNotification[0]).when(service).getActiveNotifications();
		service.onListenerConnected();
		assertEquals(0, threads.join());
		PowerMockito.verifyNoMoreInteractions(SendEmail.class);
	}

	@Test
	public void disabledBoringMessages() throws Exception {
		sharedPreferences.edit().putBoolean("enabled", false).commit();
		PowerMockito.doReturn(boringMessages).when(service).getActiveNotifications();
		service.onListenerConnected();
		assertEquals(0, threads.join());
		PowerMockito.verifyNoMoreInteractions(SendEmail.class);
	}

	@Test
	public void disabledAllMessages() throws Exception {
		sharedPreferences.edit().putBoolean("enabled", false).commit();
		PowerMockito.doReturn(allMessages).when(service).getActiveNotifications();
		service.onListenerConnected();
		assertEquals(0, threads.join());
		PowerMockito.verifyNoMoreInteractions(SendEmail.class);
	}

	@Test
	public void enabledNoMessages() throws Exception {
		sharedPreferences.edit().putBoolean("enabled", true).commit();
		PowerMockito.doReturn(new StatusBarNotification[0]).when(service).getActiveNotifications();
		service.onListenerConnected();
		assertEquals(0, threads.join());
		PowerMockito.verifyNoMoreInteractions(SendEmail.class);
	}

	@Test
	public void enabledBoringMessages() throws Exception {
		sharedPreferences.edit().putBoolean("enabled", true).commit();
		PowerMockito.doReturn(boringMessages).when(service).getActiveNotifications();
		service.onListenerConnected();
		assertEquals(0, threads.join());
		PowerMockito.verifyNoMoreInteractions(SendEmail.class);
	}

	@Test
	public void enabledAllMessages_Inactive() throws Exception {
		sharedPreferences.edit().putBoolean("enabled", true).commit();
		PowerMockito.doReturn(allMessages).when(service).getActiveNotifications();
		Mockito.doReturn(false).when(sendEmail).isActive();
		service.onListenerConnected();
		assertEquals(2, threads.join());
		PowerMockito.verifyNew(SendEmail.class).withArguments(Mockito.isA(PowerManager.class), Mockito.isA(Prefs_.class), Mockito.eq("Missed phone call"),
				Mockito.isA(Date.class));
		PowerMockito.verifyNew(SendEmail.class).withArguments(Mockito.isA(PowerManager.class), Mockito.isA(Prefs_.class), Mockito.eq("Message received"),
				Mockito.isA(Date.class));
		PowerMockito.verifyNoMoreInteractions(SendEmail.class);
		Mockito.verify(sendEmail, Mockito.times(2)).isActive();
		Mockito.verifyNoMoreInteractions(sendEmail);
	}

	@Test
	public void enabledAllMessages_Active() throws Exception {
		sharedPreferences.edit().putBoolean("enabled", true).commit();
		PowerMockito.doReturn(allMessages).when(service).getActiveNotifications();
		Mockito.doReturn(true).when(sendEmail).isActive();
		service.onListenerConnected();
		assertEquals(2, threads.join());
		PowerMockito.verifyNew(SendEmail.class).withArguments(Mockito.isA(PowerManager.class), Mockito.isA(Prefs_.class), Mockito.eq("Missed phone call"),
				Mockito.isA(Date.class));
		PowerMockito.verifyNew(SendEmail.class).withArguments(Mockito.isA(PowerManager.class), Mockito.isA(Prefs_.class), Mockito.eq("Message received"),
				Mockito.isA(Date.class));
		PowerMockito.verifyNoMoreInteractions(SendEmail.class);
		Mockito.verify(sendEmail, Mockito.times(2)).isActive();
		Mockito.verify(sendEmail, Mockito.times(2)).run();
		Mockito.verifyNoMoreInteractions(sendEmail);
	}
}
