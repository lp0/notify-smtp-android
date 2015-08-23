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

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import android.os.Build;

public class Message {
	private String content;

	public Message(String subject, Date date, String sender, Collection<String> recipients) {
		if (recipients.isEmpty())
			throw new IllegalArgumentException("No recipients");

		StringBuilder sb = new StringBuilder();
		sb.append("Message-Id: <").append(UUID.randomUUID()).append("@android.invalid>\r\n");
		sb.append("Date: ").append(new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").format(date)).append("\r\n");
		sb.append("Subject: ").append(subject).append("\r\n");
		sb.append("From: ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append(" <").append(sender).append(">\r\n");

		sb.append("To: ");
		boolean first = true;
		for (String recipient : recipients) {
			if (!first)
				sb.append(", ");
			sb.append("<" + recipient + ">");
			first = false;
		}
		sb.append("\r\n");

		sb.append("Content-Type: text/plain; charset=UTF-8\r\n");
		sb.append("Content-Transfer-Encoding: 8bit\r\n");
		sb.append("X-Auto-Response-Suppress: OOF\r\n");
		sb.append("\r\n");
		content = sb.toString();
	}

	public String toString() {
		return content;
	}
}
