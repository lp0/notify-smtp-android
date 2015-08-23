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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
@PrepareForTest(fullyQualifiedNames = { "uk.me.sa.android.notify_smtp.util.SequentialRetryRunnable" })
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*" })
@SuppressFBWarnings("SIC_INNER_SHOULD_BE_STATIC_ANON")
public class TestSequentialRetryRunnable {
	@SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
	@Rule
	public PowerMockRule rule = new PowerMockRule();

	@Before
	public void noSleep() throws Exception {
		PowerMockito.mockStatic(Thread.class);
		PowerMockito.doNothing().when(Thread.class);
		Thread.sleep(Mockito.anyLong());
	}

	@Test
	public void success() throws Exception {
		final AtomicInteger callCount = new AtomicInteger();

		Callable<Boolean> callable = new Callable<Boolean>() {
			public Boolean call() {
				callCount.incrementAndGet();
				return true;
			}
		};

		new SequentialRetryRunnable(callable).run();

		assertEquals(1, callCount.get());
		PowerMockito.verifyNoMoreInteractions(Thread.class);
	}

	@Test
	public void failure() throws Exception {
		final AtomicInteger callCount = new AtomicInteger();

		Callable<Boolean> callable = new Callable<Boolean>() {
			public Boolean call() {
				callCount.incrementAndGet();
				return false;
			}
		};

		new SequentialRetryRunnable(callable).run();

		assertEquals(3, callCount.get());
		PowerMockito.verifyStatic(Mockito.times(2));
		Thread.sleep(30000);
		PowerMockito.verifyNoMoreInteractions(Thread.class);
	}

	@Test
	public void exception() throws Exception {
		final AtomicInteger callCount = new AtomicInteger();

		Callable<Boolean> callable = new Callable<Boolean>() {
			public Boolean call() throws Exception {
				callCount.incrementAndGet();
				throw new Exception();
			}
		};

		new SequentialRetryRunnable(callable).run();

		assertEquals(3, callCount.get());
		PowerMockito.verifyStatic(Mockito.times(2));
		Thread.sleep(30000);
		PowerMockito.verifyNoMoreInteractions(Thread.class);
	}

	@Test(expected = Error.class)
	public void error() throws Exception {
		Callable<Boolean> callable = new Callable<Boolean>() {
			public Boolean call() throws Exception {
				throw new Error();
			}
		};

		new SequentialRetryRunnable(callable).run();
	}

	@SuppressFBWarnings({ "WA_NOT_IN_LOOP", "IMSE_DONT_CATCH_IMSE" })
	private static boolean threadOwnsMonitor(Object o) {
		try {
			o.wait(0, 1);
			return true;
		} catch (IllegalMonitorStateException e) {
			return false;
		} catch (InterruptedException e) {
			throw new Error();
		}
	}

	@Test
	public void synchronizedCall() throws Exception {
		final AtomicInteger callCount = new AtomicInteger();

		Callable<Boolean> callable = new Callable<Boolean>() {
			public Boolean call() {
				assertTrue(threadOwnsMonitor(getClass()));
				callCount.incrementAndGet();
				return true;
			}
		};

		assertFalse(threadOwnsMonitor(callable.getClass()));
		new SequentialRetryRunnable(callable).run();
		assertFalse(threadOwnsMonitor(callable.getClass()));

		assertEquals(1, callCount.get());
		PowerMockito.verifyNoMoreInteractions(Thread.class);
	}

	@Test
	public void synchronizedSleep() throws Exception {
		final AtomicInteger callCount = new AtomicInteger();

		final Callable<Boolean> callable = new Callable<Boolean>() {
			public Boolean call() {
				callCount.incrementAndGet();
				return false;
			}
		};

		PowerMockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				assertTrue(threadOwnsMonitor(callable.getClass()));
				return null;
			}
		}).when(Thread.class);
		Thread.sleep(Mockito.anyLong());

		assertFalse(threadOwnsMonitor(callable.getClass()));
		new SequentialRetryRunnable(callable).run();
		assertFalse(threadOwnsMonitor(callable.getClass()));

		assertEquals(3, callCount.get());
		PowerMockito.verifyStatic(Mockito.times(2));
		Thread.sleep(30000);
		PowerMockito.verifyNoMoreInteractions(Thread.class);
	}
}
