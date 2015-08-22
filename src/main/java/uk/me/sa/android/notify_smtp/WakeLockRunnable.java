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

import android.os.PowerManager;

public class WakeLockRunnable implements Runnable {
	private PowerManager pm;
	private Runnable runnable;

	public WakeLockRunnable(PowerManager pm, Runnable runnable) {
		this.pm = pm;
		this.runnable = runnable;
	}

	@Override
	public void run() {
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, runnable.toString());
		wl.acquire();
		try {
			runnable.run();
		} finally {
			wl.release();
		}
	}
}
