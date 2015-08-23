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
package uk.me.sa.android.notify_smtp.data;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.collections.Sets;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ReflectionHelpers;
import org.robolectric.shadows.ShadowPreferenceManager;

import uk.me.sa.android.notify_smtp.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class TestValidatedPrefs {
	Context context;
	SharedPreferences sharedPreferences;
	Prefs_ prefs;

	@Before
	public void create() throws Exception {
		MockitoAnnotations.initMocks(this);

		ReflectionHelpers.setStaticFieldReflectively(Build.VERSION.class, "SDK_INT", 18);

		context = Robolectric.application.getApplicationContext();
		sharedPreferences = ShadowPreferenceManager.getDefaultSharedPreferences(context);
		prefs = new Prefs_(context);

		sharedPreferences.edit().putStringSet("days", Sets.<String>newSet(context.getResources().getStringArray(R.array.pref_days_entry_values))).commit();
		sharedPreferences.edit().putString("startTime", "00:00").commit();
		sharedPreferences.edit().putString("stopTime", "23:59").commit();
		sharedPreferences.edit().putString("node", "node").commit();
		sharedPreferences.edit().putString("port", "1").commit();
		sharedPreferences.edit().putString("username", "username").commit();
		sharedPreferences.edit().putString("password", "password").commit();
		sharedPreferences.edit().putString("sender", "sender").commit();
		sharedPreferences.edit().putString("recipients", "recipients").commit();
	}

	@Test
	public void testAll() {
		Assert.assertTrue(new ValidatedPrefs(prefs).hasAllPrefs());
		Assert.assertTrue(new ValidatedPrefs(prefs).isActiveAt(new Date(0)));
	}

	@Test
	public void testNoDays() {
		sharedPreferences.edit().remove("days").commit();
		Assert.assertTrue(new ValidatedPrefs(prefs).hasAllPrefs());
		Assert.assertFalse(new ValidatedPrefs(prefs).isActiveAt(new Date(0)));

		sharedPreferences.edit().putStringSet("days", Collections.<String>emptySet()).commit();
		Assert.assertTrue(new ValidatedPrefs(prefs).hasAllPrefs());
		Assert.assertFalse(new ValidatedPrefs(prefs).isActiveAt(new Date(0)));
	}

	@Test
	public void testNoStartTime() {
		sharedPreferences.edit().remove("startTime").commit();
		Assert.assertFalse(new ValidatedPrefs(prefs).hasAllPrefs());
		Assert.assertFalse(new ValidatedPrefs(prefs).isActiveAt(new Date(0)));

		sharedPreferences.edit().putString("startTime", "").commit();
		Assert.assertFalse(new ValidatedPrefs(prefs).hasAllPrefs());
		Assert.assertFalse(new ValidatedPrefs(prefs).isActiveAt(new Date(0)));
	}

	@Test
	public void testNoStopTime() {
		sharedPreferences.edit().remove("stopTime").commit();
		Assert.assertFalse(new ValidatedPrefs(prefs).hasAllPrefs());
		Assert.assertFalse(new ValidatedPrefs(prefs).isActiveAt(new Date(0)));

		sharedPreferences.edit().putString("stopTime", "").commit();
		Assert.assertFalse(new ValidatedPrefs(prefs).hasAllPrefs());
		Assert.assertFalse(new ValidatedPrefs(prefs).isActiveAt(new Date(0)));
	}

	@Test
	public void testNoNode() {
		sharedPreferences.edit().remove("node").commit();
		Assert.assertFalse(new ValidatedPrefs(prefs).hasAllPrefs());
		Assert.assertFalse(new ValidatedPrefs(prefs).isActiveAt(new Date(0)));

		sharedPreferences.edit().putString("node", "").commit();
		Assert.assertFalse(new ValidatedPrefs(prefs).hasAllPrefs());
		Assert.assertFalse(new ValidatedPrefs(prefs).isActiveAt(new Date(0)));
	}

	@Test
	public void testNoPort() {
		sharedPreferences.edit().remove("port").commit();
		Assert.assertFalse(new ValidatedPrefs(prefs).hasAllPrefs());
		Assert.assertFalse(new ValidatedPrefs(prefs).isActiveAt(new Date(0)));

		sharedPreferences.edit().putInt("port", 0).commit();
		Assert.assertFalse(new ValidatedPrefs(prefs).hasAllPrefs());
		Assert.assertFalse(new ValidatedPrefs(prefs).isActiveAt(new Date(0)));
	}

	@Test
	public void testNoUsername() {
		sharedPreferences.edit().remove("username").commit();
		Assert.assertFalse(new ValidatedPrefs(prefs).hasAllPrefs());
		Assert.assertFalse(new ValidatedPrefs(prefs).isActiveAt(new Date(0)));

		sharedPreferences.edit().putString("username", "").commit();
		Assert.assertFalse(new ValidatedPrefs(prefs).hasAllPrefs());
		Assert.assertFalse(new ValidatedPrefs(prefs).isActiveAt(new Date(0)));
	}

	@Test
	public void testNoPassword() {
		sharedPreferences.edit().remove("password").commit();
		Assert.assertFalse(new ValidatedPrefs(prefs).hasAllPrefs());
		Assert.assertFalse(new ValidatedPrefs(prefs).isActiveAt(new Date(0)));

		sharedPreferences.edit().putString("password", "").commit();
		Assert.assertFalse(new ValidatedPrefs(prefs).hasAllPrefs());
		Assert.assertFalse(new ValidatedPrefs(prefs).isActiveAt(new Date(0)));
	}

	@Test
	public void testNoSender() {
		sharedPreferences.edit().remove("sender").commit();
		Assert.assertFalse(new ValidatedPrefs(prefs).hasAllPrefs());
		Assert.assertFalse(new ValidatedPrefs(prefs).isActiveAt(new Date(0)));

		sharedPreferences.edit().putString("sender", "").commit();
		Assert.assertFalse(new ValidatedPrefs(prefs).hasAllPrefs());
		Assert.assertFalse(new ValidatedPrefs(prefs).isActiveAt(new Date(0)));
	}

	@Test
	public void testNoRecipients() {
		sharedPreferences.edit().remove("recipients").commit();
		Assert.assertFalse(new ValidatedPrefs(prefs).hasAllPrefs());
		Assert.assertFalse(new ValidatedPrefs(prefs).isActiveAt(new Date(0)));

		sharedPreferences.edit().putString("recipients", "").commit();
		Assert.assertFalse(new ValidatedPrefs(prefs).hasAllPrefs());
		Assert.assertFalse(new ValidatedPrefs(prefs).isActiveAt(new Date(0)));

		sharedPreferences.edit().putString("recipients", " ").commit();
		Assert.assertFalse(new ValidatedPrefs(prefs).hasAllPrefs());
		Assert.assertFalse(new ValidatedPrefs(prefs).isActiveAt(new Date(0)));

		sharedPreferences.edit().putString("recipients", "  ").commit();
		Assert.assertFalse(new ValidatedPrefs(prefs).hasAllPrefs());
		Assert.assertFalse(new ValidatedPrefs(prefs).isActiveAt(new Date(0)));

		sharedPreferences.edit().putString("recipients", "   ").commit();
		Assert.assertFalse(new ValidatedPrefs(prefs).hasAllPrefs());
		Assert.assertFalse(new ValidatedPrefs(prefs).isActiveAt(new Date(0)));
	}

	@Test
	public void testAnyDay() {
		Assert.assertTrue(new ValidatedPrefs(prefs).isActiveAt(new Date()));
	}

	private void testDay(int dayOfWeek) {
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.ENGLISH);
		c.set(Calendar.DAY_OF_WEEK, dayOfWeek);
		Date ts = c.getTime();
		Assert.assertTrue(new ValidatedPrefs(prefs).isActiveAt(ts));

		Set<String> days = sharedPreferences.getStringSet("days", Collections.<String>emptySet());
		days.remove(String.valueOf(dayOfWeek));
		sharedPreferences.edit().putStringSet("days", days).commit();
		Assert.assertFalse(new ValidatedPrefs(prefs).isActiveAt(ts));

		sharedPreferences.edit().remove("days").commit();
		Assert.assertFalse(new ValidatedPrefs(prefs).isActiveAt(ts));
	}

	@Test
	public void testMonday() {
		testDay(Calendar.MONDAY);
	}

	@Test
	public void testTuesday() {
		testDay(Calendar.TUESDAY);
	}

	@Test
	public void testWednesday() {
		testDay(Calendar.WEDNESDAY);
	}

	@Test
	public void testThursday() {
		testDay(Calendar.THURSDAY);
	}

	@Test
	public void testFriday() {
		testDay(Calendar.FRIDAY);
	}

	@Test
	public void testSaturday() {
		testDay(Calendar.SATURDAY);
	}

	@Test
	public void testSunday() {
		testDay(Calendar.SUNDAY);
	}

	private boolean testTime(int hour, int minute, int second) {
		Calendar c = Calendar.getInstance(Locale.ENGLISH);
		while (c.get(Calendar.HOUR_OF_DAY) != hour || c.get(Calendar.MINUTE) != minute || c.get(Calendar.SECOND) != second) {
			c.add(Calendar.DAY_OF_MONTH, 1);
			c.set(Calendar.HOUR_OF_DAY, hour);
			c.set(Calendar.MINUTE, minute);
			c.set(Calendar.SECOND, second);
		}
		Date ts = c.getTime();
		return new ValidatedPrefs(prefs).isActiveAt(ts);
	}

	@Test
	public void testStartTime() {
		Assert.assertTrue(testTime(0, 0, 0));
		Assert.assertTrue(testTime(0, 0, 1));
		Assert.assertTrue(testTime(0, 0, 59));
		Assert.assertTrue(testTime(0, 1, 0));
		Assert.assertTrue(testTime(23, 59, 59));

		sharedPreferences.edit().putString("startTime", "00:01").commit();

		Assert.assertFalse(testTime(0, 0, 0));
		Assert.assertFalse(testTime(0, 0, 1));
		Assert.assertFalse(testTime(0, 0, 59));
		Assert.assertTrue(testTime(0, 1, 0));
		Assert.assertTrue(testTime(0, 1, 1));
		Assert.assertTrue(testTime(0, 1, 59));
		Assert.assertTrue(testTime(0, 2, 0));
		Assert.assertTrue(testTime(23, 59, 59));

		sharedPreferences.edit().putString("startTime", "00:59").commit();

		Assert.assertFalse(testTime(0, 0, 0));
		Assert.assertFalse(testTime(0, 0, 1));
		Assert.assertFalse(testTime(0, 0, 59));
		Assert.assertFalse(testTime(0, 1, 0));
		Assert.assertFalse(testTime(0, 1, 1));
		Assert.assertFalse(testTime(0, 1, 59));
		Assert.assertTrue(testTime(0, 59, 0));
		Assert.assertTrue(testTime(0, 59, 1));
		Assert.assertTrue(testTime(0, 59, 59));
		Assert.assertTrue(testTime(1, 0, 0));
		Assert.assertTrue(testTime(1, 0, 1));
		Assert.assertTrue(testTime(1, 0, 59));
		Assert.assertTrue(testTime(1, 1, 0));
		Assert.assertTrue(testTime(2, 0, 0));
		Assert.assertTrue(testTime(23, 59, 59));

		sharedPreferences.edit().putString("startTime", "01:00").commit();

		Assert.assertFalse(testTime(0, 0, 0));
		Assert.assertFalse(testTime(0, 0, 1));
		Assert.assertFalse(testTime(0, 0, 59));
		Assert.assertFalse(testTime(0, 1, 0));
		Assert.assertFalse(testTime(0, 1, 1));
		Assert.assertFalse(testTime(0, 1, 59));
		Assert.assertFalse(testTime(0, 59, 0));
		Assert.assertFalse(testTime(0, 59, 1));
		Assert.assertFalse(testTime(0, 59, 59));
		Assert.assertTrue(testTime(1, 0, 0));
		Assert.assertTrue(testTime(1, 0, 1));
		Assert.assertTrue(testTime(1, 0, 59));
		Assert.assertTrue(testTime(1, 1, 0));
		Assert.assertTrue(testTime(1, 59, 59));
		Assert.assertTrue(testTime(2, 0, 0));
		Assert.assertTrue(testTime(23, 59, 59));

		sharedPreferences.edit().putString("startTime", "01:59").commit();

		Assert.assertFalse(testTime(0, 0, 0));
		Assert.assertFalse(testTime(0, 0, 1));
		Assert.assertFalse(testTime(0, 0, 59));
		Assert.assertFalse(testTime(0, 1, 0));
		Assert.assertFalse(testTime(0, 1, 1));
		Assert.assertFalse(testTime(0, 1, 59));
		Assert.assertFalse(testTime(0, 59, 0));
		Assert.assertFalse(testTime(0, 59, 1));
		Assert.assertFalse(testTime(0, 59, 59));
		Assert.assertFalse(testTime(1, 0, 0));
		Assert.assertFalse(testTime(1, 0, 1));
		Assert.assertFalse(testTime(1, 0, 59));
		Assert.assertFalse(testTime(1, 1, 0));
		Assert.assertTrue(testTime(1, 59, 0));
		Assert.assertTrue(testTime(1, 59, 1));
		Assert.assertTrue(testTime(1, 59, 59));
		Assert.assertTrue(testTime(2, 0, 0));
		Assert.assertTrue(testTime(23, 59, 59));

		sharedPreferences.edit().putString("startTime", "13:00").commit();

		Assert.assertFalse(testTime(7, 0, 0));
		Assert.assertFalse(testTime(7, 30, 0));
		Assert.assertFalse(testTime(8, 0, 0));
		Assert.assertFalse(testTime(12, 0, 0));
		Assert.assertFalse(testTime(12, 30, 0));
		Assert.assertTrue(testTime(13, 0, 0));
		Assert.assertTrue(testTime(15, 0, 0));
		Assert.assertTrue(testTime(17, 0, 0));

		sharedPreferences.edit().putString("startTime", "23:59").commit();

		Assert.assertFalse(testTime(0, 0, 0));
		Assert.assertFalse(testTime(1, 0, 0));
		Assert.assertFalse(testTime(7, 0, 0));
		Assert.assertFalse(testTime(11, 0, 0));
		Assert.assertFalse(testTime(12, 0, 0));
		Assert.assertFalse(testTime(13, 0, 0));
		Assert.assertFalse(testTime(22, 59, 59));
		Assert.assertFalse(testTime(23, 0, 0));
		Assert.assertFalse(testTime(23, 58, 59));
		Assert.assertTrue(testTime(23, 59, 0));
		Assert.assertTrue(testTime(23, 59, 1));
		Assert.assertTrue(testTime(23, 59, 59));
	}

	@Test
	public void testStopTime() {
		Assert.assertTrue(testTime(0, 0, 0));
		Assert.assertTrue(testTime(0, 0, 1));
		Assert.assertTrue(testTime(0, 0, 59));
		Assert.assertTrue(testTime(0, 1, 0));
		Assert.assertTrue(testTime(23, 58, 59));
		Assert.assertTrue(testTime(23, 59, 0));
		Assert.assertTrue(testTime(23, 59, 1));
		Assert.assertTrue(testTime(23, 59, 59));

		sharedPreferences.edit().putString("stopTime", "00:00").commit();

		Assert.assertTrue(testTime(0, 0, 0));
		Assert.assertTrue(testTime(0, 0, 1));
		Assert.assertTrue(testTime(0, 0, 59));
		Assert.assertFalse(testTime(0, 1, 0));
		Assert.assertFalse(testTime(0, 1, 1));
		Assert.assertFalse(testTime(0, 1, 59));
		Assert.assertFalse(testTime(23, 58, 59));
		Assert.assertFalse(testTime(23, 59, 0));
		Assert.assertFalse(testTime(23, 59, 1));
		Assert.assertFalse(testTime(23, 59, 59));

		sharedPreferences.edit().putString("stopTime", "00:01").commit();

		Assert.assertTrue(testTime(0, 0, 0));
		Assert.assertTrue(testTime(0, 0, 1));
		Assert.assertTrue(testTime(0, 0, 59));
		Assert.assertTrue(testTime(0, 1, 0));
		Assert.assertTrue(testTime(0, 1, 1));
		Assert.assertTrue(testTime(0, 1, 59));
		Assert.assertFalse(testTime(0, 2, 0));
		Assert.assertFalse(testTime(23, 58, 59));
		Assert.assertFalse(testTime(23, 59, 0));
		Assert.assertFalse(testTime(23, 59, 1));
		Assert.assertFalse(testTime(23, 59, 59));

		sharedPreferences.edit().putString("stopTime", "00:59").commit();

		Assert.assertTrue(testTime(0, 0, 0));
		Assert.assertTrue(testTime(0, 0, 1));
		Assert.assertTrue(testTime(0, 0, 59));
		Assert.assertTrue(testTime(0, 1, 0));
		Assert.assertTrue(testTime(0, 1, 1));
		Assert.assertTrue(testTime(0, 1, 59));
		Assert.assertTrue(testTime(0, 59, 0));
		Assert.assertTrue(testTime(0, 59, 1));
		Assert.assertTrue(testTime(0, 59, 59));
		Assert.assertFalse(testTime(1, 0, 0));
		Assert.assertFalse(testTime(1, 0, 1));
		Assert.assertFalse(testTime(1, 0, 59));
		Assert.assertFalse(testTime(1, 1, 0));
		Assert.assertFalse(testTime(2, 0, 0));
		Assert.assertFalse(testTime(23, 58, 59));
		Assert.assertFalse(testTime(23, 59, 0));
		Assert.assertFalse(testTime(23, 59, 1));
		Assert.assertFalse(testTime(23, 59, 59));

		sharedPreferences.edit().putString("stopTime", "01:00").commit();

		Assert.assertTrue(testTime(0, 0, 0));
		Assert.assertTrue(testTime(0, 0, 1));
		Assert.assertTrue(testTime(0, 0, 59));
		Assert.assertTrue(testTime(0, 1, 0));
		Assert.assertTrue(testTime(0, 1, 1));
		Assert.assertTrue(testTime(0, 1, 59));
		Assert.assertTrue(testTime(0, 59, 0));
		Assert.assertTrue(testTime(0, 59, 1));
		Assert.assertTrue(testTime(0, 59, 59));
		Assert.assertTrue(testTime(1, 0, 0));
		Assert.assertTrue(testTime(1, 0, 1));
		Assert.assertTrue(testTime(1, 0, 59));
		Assert.assertFalse(testTime(1, 1, 0));
		Assert.assertFalse(testTime(1, 59, 59));
		Assert.assertFalse(testTime(2, 0, 0));
		Assert.assertFalse(testTime(23, 58, 59));
		Assert.assertFalse(testTime(23, 59, 0));
		Assert.assertFalse(testTime(23, 59, 1));
		Assert.assertFalse(testTime(23, 59, 59));

		sharedPreferences.edit().putString("stopTime", "01:59").commit();

		Assert.assertTrue(testTime(0, 0, 0));
		Assert.assertTrue(testTime(0, 0, 1));
		Assert.assertTrue(testTime(0, 0, 59));
		Assert.assertTrue(testTime(0, 1, 0));
		Assert.assertTrue(testTime(0, 1, 1));
		Assert.assertTrue(testTime(0, 1, 59));
		Assert.assertTrue(testTime(0, 59, 0));
		Assert.assertTrue(testTime(0, 59, 1));
		Assert.assertTrue(testTime(0, 59, 59));
		Assert.assertTrue(testTime(1, 0, 0));
		Assert.assertTrue(testTime(1, 0, 1));
		Assert.assertTrue(testTime(1, 0, 59));
		Assert.assertTrue(testTime(1, 1, 0));
		Assert.assertTrue(testTime(1, 59, 0));
		Assert.assertTrue(testTime(1, 59, 1));
		Assert.assertTrue(testTime(1, 59, 59));
		Assert.assertFalse(testTime(2, 0, 0));
		Assert.assertFalse(testTime(23, 58, 59));
		Assert.assertFalse(testTime(23, 59, 0));
		Assert.assertFalse(testTime(23, 59, 1));
		Assert.assertFalse(testTime(23, 59, 59));

		sharedPreferences.edit().putString("stopTime", "13:00").commit();

		Assert.assertTrue(testTime(7, 0, 0));
		Assert.assertTrue(testTime(7, 30, 0));
		Assert.assertTrue(testTime(8, 0, 0));
		Assert.assertTrue(testTime(12, 0, 0));
		Assert.assertTrue(testTime(12, 30, 0));
		Assert.assertTrue(testTime(13, 0, 0));
		Assert.assertFalse(testTime(15, 0, 0));
		Assert.assertFalse(testTime(17, 0, 0));

		sharedPreferences.edit().putString("stopTime", "23:59").commit();

		Assert.assertTrue(testTime(0, 0, 0));
		Assert.assertTrue(testTime(1, 0, 0));
		Assert.assertTrue(testTime(7, 0, 0));
		Assert.assertTrue(testTime(11, 0, 0));
		Assert.assertTrue(testTime(12, 0, 0));
		Assert.assertTrue(testTime(13, 0, 0));
		Assert.assertTrue(testTime(22, 59, 59));
		Assert.assertTrue(testTime(23, 0, 0));
		Assert.assertTrue(testTime(23, 58, 59));
		Assert.assertTrue(testTime(23, 59, 0));
		Assert.assertTrue(testTime(23, 59, 1));
		Assert.assertTrue(testTime(23, 59, 59));
	}

	@Test
	public void testStartStopTime() {
		Assert.assertTrue(testTime(0, 0, 0));
		Assert.assertTrue(testTime(0, 0, 1));
		Assert.assertTrue(testTime(0, 0, 59));
		Assert.assertTrue(testTime(0, 1, 0));
		Assert.assertTrue(testTime(23, 58, 59));
		Assert.assertTrue(testTime(23, 59, 0));
		Assert.assertTrue(testTime(23, 59, 1));
		Assert.assertTrue(testTime(23, 59, 59));

		sharedPreferences.edit().putString("startTime", "09:00").commit();
		sharedPreferences.edit().putString("stopTime", "15:00").commit();

		Assert.assertFalse(testTime(7, 0, 0));
		Assert.assertFalse(testTime(7, 30, 0));
		Assert.assertFalse(testTime(8, 0, 0));
		Assert.assertFalse(testTime(8, 59, 59));
		Assert.assertTrue(testTime(9, 0, 0));
		Assert.assertTrue(testTime(12, 0, 0));
		Assert.assertTrue(testTime(12, 30, 0));
		Assert.assertTrue(testTime(13, 0, 0));
		Assert.assertTrue(testTime(14, 59, 0));
		Assert.assertTrue(testTime(14, 59, 59));
		Assert.assertTrue(testTime(15, 0, 0));
		Assert.assertTrue(testTime(15, 0, 59));
		Assert.assertFalse(testTime(15, 1, 0));
		Assert.assertFalse(testTime(17, 0, 0));

		sharedPreferences.edit().putString("stopTime", "14:59").commit();

		Assert.assertTrue(testTime(12, 30, 0));
		Assert.assertTrue(testTime(13, 0, 0));
		Assert.assertTrue(testTime(14, 58, 0));
		Assert.assertTrue(testTime(14, 59, 0));
		Assert.assertTrue(testTime(14, 59, 1));
		Assert.assertTrue(testTime(14, 59, 58));
		Assert.assertTrue(testTime(14, 59, 59));
		Assert.assertFalse(testTime(15, 0, 0));
		Assert.assertFalse(testTime(15, 0, 59));
		Assert.assertFalse(testTime(15, 1, 0));
		Assert.assertFalse(testTime(17, 0, 0));
	}

	@Test
	public void testInvalidStartTime() {
		Assert.assertTrue(testTime(7, 0, 0));

		sharedPreferences.edit().putString("startTime", "test").commit();

		Assert.assertFalse(testTime(7, 0, 0));
	}

	@Test
	public void testInvalidStopTime() {
		Assert.assertTrue(testTime(7, 0, 0));

		sharedPreferences.edit().putString("stopTime", "test").commit();

		Assert.assertFalse(testTime(7, 0, 0));
	}
}
