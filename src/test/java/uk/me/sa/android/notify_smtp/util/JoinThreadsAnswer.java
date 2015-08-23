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

import java.util.ArrayList;
import java.util.List;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import android.annotation.SuppressLint;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class JoinThreadsAnswer implements Answer<Thread> {
	private List<Thread> threads = new ArrayList<Thread>();

	@SuppressLint("Assert")
	@SuppressFBWarnings("DM_USELESS_THREAD")
	@Override
	public synchronized Thread answer(InvocationOnMock invocation) throws Throwable {
		Object[] args = invocation.getArguments();
		Thread t = null;

		if (args.length > 0 && args[0] == null)
			return null; /* !? */

		if (args.length == 1) {
			if (args[0] instanceof Runnable) {
				t = new Thread((Runnable)args[0]);
			} else if (args[0] instanceof String) {
				t = new Thread((String)args[0]);
			}
		} else if (args.length == 2) {
			if (args[1] instanceof Runnable) {
				t = new Thread((ThreadGroup)args[0], (Runnable)args[1]);
			} else if (args[1] instanceof String) {
				t = new Thread((ThreadGroup)args[0], (String)args[1]);
			}
		} else if (args.length == 3) {
			t = new Thread((ThreadGroup)args[0], (Runnable)args[1], (String)args[2]);
		} else if (args.length == 4) {
			t = new Thread((ThreadGroup)args[0], (Runnable)args[1], (String)args[2], (long)args[3]);
		}

		assert (t != null);
		threads.add(t);
		return t;
	}

	public synchronized int join() throws InterruptedException {
		for (Thread t : threads)
			t.join();

		int size = threads.size();
		threads.clear();
		return size;
	}
}
