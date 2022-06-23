package rs.cc.tty;

public abstract class TTY {
	public abstract int available();
	public abstract int read(byte [] buf, int offset, int size);
	public int read(byte [] buf) {
		if(buf == null || buf.length == 0) return 0;
		return read(buf,0,buf.length);
	}
	public abstract void write(byte [] buf, int offset, int size);
	public void write(byte [] buf) {
		if(buf == null || buf.length == 0) return;
		write(buf,0,buf.length);
	}
	public abstract void store(byte [] b);
	public void onEnabled() { };
	public void onDisabled() { }

}
