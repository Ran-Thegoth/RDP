package rs.cc.connection;

public interface EventListener {
	void OnPreConnect(long instance);

	void OnConnectionSuccess(long instance);

	void OnConnectionFailure(long instance);

	void OnDisconnecting(long instance);

	void OnDisconnected(long instance);
}
