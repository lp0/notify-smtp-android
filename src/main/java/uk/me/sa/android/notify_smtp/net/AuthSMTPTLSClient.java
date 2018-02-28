/*
	notify-smtp-android - Android Notify to SMTP Service

	Copyright 2015,2018  Simon Arlott

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

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.smtp.AuthenticatingSMTPClient;
import org.apache.commons.net.smtp.SMTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class AuthSMTPTLSClient extends AuthenticatingSMTPClient implements ProtocolCommandListener {
	private static final Logger log = LoggerFactory.getLogger(AuthSMTPTLSClient.class);

	private String command;

	public AuthSMTPTLSClient() throws NoSuchAlgorithmException {
		super("TLS", true, "UTF-8");
		setHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier());
		addProtocolCommandListener(this);
	}

	@Override
	protected void _connectAction_() throws IOException {
		command = "CONN";
		super._connectAction_();
	}

	@SuppressFBWarnings("DE_MIGHT_IGNORE")
	@Override
	public void disconnect() {
		try {
			// This doesn't throw IOException
			super.disconnect();
		} catch (IOException e) {
		}
	}

	@Override
	public boolean elogin() throws IOException {
		InetAddress addr = getLocalAddress();
		if (addr instanceof Inet4Address) {
			return elogin("[" + addr.getHostAddress() + "]");
		} else if (addr instanceof Inet6Address) {
			return elogin("[IPv6:" + addr.getHostAddress() + "]");
		} else {
			return elogin("android.invalid");
		}
	}

	@Override
	public boolean auth(AUTH_METHOD method, String username, String password)
			throws IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		removeProtocolCommandListener(this);

		boolean ok = super.auth(method, username, password);
		if (SMTPReply.isPositiveCompletion(getReplyCode())) {
			log.info("AUTH {}", getReplyStrings()[0]);
		} else {
			log.error("AUTH {}", getReplyString());
		}

		addProtocolCommandListener(this);
		return ok;
	}

	@Override
	public void protocolCommandSent(ProtocolCommandEvent event) {
		command = event.getCommand();
	}

	@Override
	public void protocolReplyReceived(ProtocolCommandEvent event) {
		if (SMTPReply.isPositiveCompletion(event.getReplyCode())) {
			log.info("{} {}", command, getReplyStrings()[0]);
		} else {
			log.error("{} {}", command, event.getMessage());
		}
	}
}
