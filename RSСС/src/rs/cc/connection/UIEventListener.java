package rs.cc.connection;

public interface UIEventListener {
	void OnSettingsChanged(int width, int height, int bpp);

	boolean OnAuthenticate(StringBuilder username, StringBuilder domain,
	                       StringBuilder password);

	boolean OnGatewayAuthenticate(StringBuilder username, StringBuilder domain,
	                              StringBuilder password);

	int OnVerifiyCertificate(String commonName, String subject, String issuer,
	                         String fingerprint, boolean mismatch);

	int OnVerifyChangedCertificate(String commonName, String subject, String issuer,
	                               String fingerprint, String oldSubject, String oldIssuer,
	                               String oldFingerprint);

	void OnGraphicsUpdate(int x, int y, int width, int height);

	void OnGraphicsResize(int width, int height, int bpp);

	void OnRemoteClipboardChanged(String data);

}
