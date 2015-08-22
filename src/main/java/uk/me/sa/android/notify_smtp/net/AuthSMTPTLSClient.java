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

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.smtp.AuthenticatingSMTPClient;
import org.apache.commons.net.smtp.SMTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class AuthSMTPTLSClient extends AuthenticatingSMTPClient implements ProtocolCommandListener {
	private static final Logger log = LoggerFactory.getLogger(AuthSMTPTLSClient.class);
	private static final HostnameVerifier HOSTNAME_VERIFIER = HttpsURLConnection.getDefaultHostnameVerifier();

	private String hostname;
	private String command;

	public AuthSMTPTLSClient() throws NoSuchAlgorithmException {
		super("TLS", "UTF-8");
		addProtocolCommandListener(this);
	}

	@Override
	public void connect(InetAddress host, int port) throws SocketException, IOException {
		this.hostname = null;
		command = "CONN";
		super.connect(host, port);
		this.hostname = host.getHostAddress();
	}

	@Override
	public void connect(String hostname, int port) throws SocketException, IOException {
		this.hostname = null;
		super.connect(hostname, port);
		this.hostname = hostname;
	}

	@Override
	public void connect(InetAddress host, int port, InetAddress localAddr, int localPort) throws SocketException, IOException {
		this.hostname = null;
		super.connect(host, port, localAddr, localPort);
		this.hostname = host.getHostAddress();
	}

	@Override
	public void connect(String hostname, int port, InetAddress localAddr, int localPort) throws SocketException, IOException {
		this.hostname = null;
		super.connect(hostname, port, localAddr, localPort);
		this.hostname = hostname;
	}

	@Override
	public void connect(InetAddress host) throws SocketException, IOException {
		this.hostname = null;
		super.connect(host);
		this.hostname = host.getHostAddress();
	}

	@Override
	public void connect(String hostname) throws SocketException, IOException {
		this.hostname = null;
		super.connect(hostname);
		this.hostname = hostname;
	}

	@Override
	protected void _connectAction_() throws IOException {
		command = "CONN";
		super._connectAction_();
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

	@SuppressFBWarnings("BC_UNCONFIRMED_CAST")
	@Override
	public boolean execTLS() throws IOException {
		boolean ret = super.execTLS();
		// Android does not support SSLParameters.setEndpointIdentificationAlgorithm("HTTPS") and the TrustManager is insecure by default
		if (ret && !HOSTNAME_VERIFIER.verify(hostname, ((SSLSocket)_socket_).getSession()))
			throw new SSLException("Hostname doesn't match certificate");
		return ret;
	}

	@Override
	public boolean auth(AUTH_METHOD method, String username, String password) throws IOException, NoSuchAlgorithmException, InvalidKeyException,
			InvalidKeySpecException {
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
