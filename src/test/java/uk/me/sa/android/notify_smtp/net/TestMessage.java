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
package uk.me.sa.android.notify_smtp.net;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ReflectionHelpers;

import uk.me.sa.android.notify_smtp.data.Message;
import android.os.Build;

import com.btmatthews.hamcrest.regex.PatternMatcher;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)
public class TestMessage {
	@Test(expected = IllegalArgumentException.class)
	public void noRecipients() throws Exception {
		new Message("subject", new Date(), "sender", new String[0]);
	}

	@Test
	public void oneRecipient() throws Exception {
		ReflectionHelpers.setStaticFieldReflectively(Build.class, "MANUFACTURER", "Android");
		ReflectionHelpers.setStaticFieldReflectively(Build.class, "MODEL", "Device");

		assertThat(new Message("subject", new Date(), "sender", new String[] { "recipient1" }).toString(),
				PatternMatcher.matches("Message-Id: <[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[0-9a-f]{4}-[0-9a-f]{12}@android.invalid>\r\n"
						+ "Date: [A-Z][a-z][a-z], [0-9]{2} [A-Z][a-z][a-z] [0-9]{4} [0-9]{2}:[0-9]{2}:[0-9]{2} [+-][0-9]{4}\r\n" + "Subject: subject\r\n"
						+ "From: Android Device <sender>\r\n" + "To: <recipient1>\r\n" + "Content-Type: text/plain; charset=UTF-8\r\n"
						+ "Content-Transfer-Encoding: 8bit\r\n" + "X-Auto-Response-Suppress: OOF\r\n" + "\r\n"));
	}

	@Test
	public void twoRecipients() throws Exception {
		ReflectionHelpers.setStaticFieldReflectively(Build.class, "MANUFACTURER", "Android");
		ReflectionHelpers.setStaticFieldReflectively(Build.class, "MODEL", "Device");

		assertThat(new Message("subject", new Date(), "sender", new String[] { "recipient1", "recipient2" }).toString(),
				PatternMatcher.matches("Message-Id: <[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[0-9a-f]{4}-[0-9a-f]{12}@android.invalid>\r\n"
						+ "Date: [A-Z][a-z][a-z], [0-9]{2} [A-Z][a-z][a-z] [0-9]{4} [0-9]{2}:[0-9]{2}:[0-9]{2} [+-][0-9]{4}\r\n" + "Subject: subject\r\n"
						+ "From: Android Device <sender>\r\n" + "To: <recipient1>, <recipient2>\r\n" + "Content-Type: text/plain; charset=UTF-8\r\n"
						+ "Content-Transfer-Encoding: 8bit\r\n" + "X-Auto-Response-Suppress: OOF\r\n" + "\r\n"));
	}

	@Test
	public void threeRecipients() throws Exception {
		ReflectionHelpers.setStaticFieldReflectively(Build.class, "MANUFACTURER", "Android");
		ReflectionHelpers.setStaticFieldReflectively(Build.class, "MODEL", "Device");

		assertThat(new Message("subject", new Date(), "sender", new String[] { "recipient1", "recipient2", "recipient3" }).toString(),
				PatternMatcher.matches("Message-Id: <[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[0-9a-f]{4}-[0-9a-f]{12}@android.invalid>\r\n"
						+ "Date: [A-Z][a-z][a-z], [0-9]{2} [A-Z][a-z][a-z] [0-9]{4} [0-9]{2}:[0-9]{2}:[0-9]{2} [+-][0-9]{4}\r\n" + "Subject: subject\r\n"
						+ "From: Android Device <sender>\r\n" + "To: <recipient1>, <recipient2>, <recipient3>\r\n"
						+ "Content-Type: text/plain; charset=UTF-8\r\n" + "Content-Transfer-Encoding: 8bit\r\n" + "X-Auto-Response-Suppress: OOF\r\n" + "\r\n"));
	}
}
