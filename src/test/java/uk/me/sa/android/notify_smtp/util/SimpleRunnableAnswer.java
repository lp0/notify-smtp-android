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

import java.util.concurrent.Callable;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class SimpleRunnableAnswer implements Answer<Runnable> {
	@SuppressWarnings("unchecked")
	@Override
	public synchronized Runnable answer(InvocationOnMock invocation) throws Throwable {
		return new SequentialRetryRunnable(invocation.getArgumentAt(0, Callable.class)) {
			public void run() {
				try {
					callable.call();
				} catch (Exception e) {
					throw new Error(e);
				}
			}
		};
	}
}
