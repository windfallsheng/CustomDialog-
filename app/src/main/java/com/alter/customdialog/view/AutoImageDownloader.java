package com.alter.customdialog.view;

import android.content.Context;

import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;

public class AutoImageDownloader extends BaseImageDownloader {

	private SSLSocketFactory mSSLSocketFactory;

	public AutoImageDownloader(Context context) {
		super(context);
		SSLContext sslContext = sslContextForTrustedCertificates();
		mSSLSocketFactory = sslContext.getSocketFactory();
	}

	public AutoImageDownloader(Context context, int connectTimeout, int readTimeout) {
		super(context, connectTimeout, readTimeout);
		SSLContext sslContext = sslContextForTrustedCertificates();
		mSSLSocketFactory = sslContext.getSocketFactory();
	}

	@Override
	protected InputStream getStreamFromNetwork(String imageUri, Object extra) {
		URL url = null;
		InputStream inputStream = null;
		try {
			url = new URL(imageUri);
		} catch (MalformedURLException e) {
		}
		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(connectTimeout);
			conn.setReadTimeout(readTimeout);

			if (conn instanceof HttpsURLConnection) {
				((HttpsURLConnection) conn).setSSLSocketFactory(mSSLSocketFactory);
				((HttpsURLConnection) conn).setHostnameVerifier((DO_NOT_VERIFY));
			}
			inputStream = conn.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new BufferedInputStream(inputStream, BUFFER_SIZE);
	}

	// always verify the host - dont check for certificate
	final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};

	public SSLContext sslContextForTrustedCertificates() {
		javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
		javax.net.ssl.TrustManager tm = new miTM();
		trustAllCerts[0] = tm;
		SSLContext sc = null;
		try {
			sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, null);
			// javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} finally {
			return sc;
		}
	}

	class miTM implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		public boolean isServerTrusted(java.security.cert.X509Certificate[] certs) {
			return true;
		}

		public boolean isClientTrusted(java.security.cert.X509Certificate[] certs) {
			return true;
		}

		public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
				throws java.security.cert.CertificateException {
			return;
		}

		public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
				throws java.security.cert.CertificateException {
			return;
		}
	}
}
