package uk.me.sa.android.notify_smtp;

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
