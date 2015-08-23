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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ReflectionHelpers;
import org.robolectric.shadows.ShadowPowerManager;

import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class TestWakeLockRunnable {
	PowerManager pm;

	@Before
	public void create() throws Exception {
		MockitoAnnotations.initMocks(this);

		ReflectionHelpers.setStaticFieldReflectively(Build.VERSION.class, "SDK_INT", 18);

		ShadowPowerManager.reset();
		pm = (PowerManager)Robolectric.application.getApplicationContext().getSystemService(Context.POWER_SERVICE);
	}

	static class WakeLockCheck {
		WakeLock wakeLock;
		boolean hasWakeLock;

		WakeLockCheck() {
			wakeLock = ShadowPowerManager.getLatestWakeLock();
			if (wakeLock != null)
				hasWakeLock = wakeLock.isHeld();
		}
	}

	@Test
	public void noWakeLock() throws Exception {
		final AtomicReference<WakeLockCheck> wlCheck = new AtomicReference<WakeLockCheck>();

		Runnable runnable = new Runnable() {
			public void run() {
				wlCheck.set(new WakeLockCheck());
			}
		};

		assertFalse(new WakeLockCheck().hasWakeLock);
		runnable.run();
		assertFalse(wlCheck.get().hasWakeLock);
		assertNull(wlCheck.get().wakeLock);
		assertFalse(new WakeLockCheck().hasWakeLock);
	}

	@Test
	public void acquiresAndReleasesWakeLock() throws Exception {
		final AtomicReference<WakeLockCheck> wlCheck = new AtomicReference<WakeLockCheck>();

		Runnable runnable = new Runnable() {
			public void run() {
				wlCheck.set(new WakeLockCheck());
			}
		};

		assertFalse(new WakeLockCheck().hasWakeLock);
		new WakeLockRunnable(pm, runnable).run();
		assertTrue(wlCheck.get().hasWakeLock);
		// WakeLock API doesn't allow the type to be checked
		assertFalse(wlCheck.get().wakeLock.isHeld());
		assertFalse(new WakeLockCheck().hasWakeLock);
	}

	@Test
	public void releasesWakeLockException() throws Exception {
		final AtomicReference<WakeLockCheck> wlCheck = new AtomicReference<WakeLockCheck>();

		Runnable runnable = new Runnable() {
			public void run() {
				wlCheck.set(new WakeLockCheck());
				throw new RuntimeException();
			}
		};

		assertFalse(new WakeLockCheck().hasWakeLock);
		try {
			new WakeLockRunnable(pm, runnable).run();
		} catch (Throwable t) {
		}
		assertTrue(wlCheck.get().hasWakeLock);
		assertFalse(wlCheck.get().wakeLock.isHeld());
		assertFalse(new WakeLockCheck().hasWakeLock);
	}

	@Test
	public void releasesWakeLockError() throws Exception {
		final AtomicReference<WakeLockCheck> wlCheck = new AtomicReference<WakeLockCheck>();

		Runnable runnable = new Runnable() {
			public void run() {
				wlCheck.set(new WakeLockCheck());
				throw new Error();
			}
		};

		assertFalse(new WakeLockCheck().hasWakeLock);
		try {
			new WakeLockRunnable(pm, runnable).run();
		} catch (Throwable t) {
		}
		assertTrue(wlCheck.get().hasWakeLock);
		assertFalse(wlCheck.get().wakeLock.isHeld());
		assertFalse(new WakeLockCheck().hasWakeLock);
	}
}
