/**
 * @author  weigao<weiga@iflytek.com>
 *
 * @version 1.0.0
 */
package wifi.authserver.dao.obj;

import java.util.Date;

/**
 * phonemac+appuser is PK
 * */
public class UserLog {
	private int id;
	private String phonemac;
	private String appuser;
	private String device;
	private String ip;
	private long incoming;
	private long outgoing;
	private Date login_time;
	private long online_time;
	private int isonline;
	private long lastPing = -1;
	private int nochangetime = 0;

	public int getNochangetime() {
		return nochangetime;
	}

	public void setNochangetime(int nochangetime) {
		this.nochangetime = nochangetime;
	}

	public long getLastPing() {
		return lastPing;
	}

	public void setLastPing(long lastPing) {
		this.lastPing = lastPing;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPhonemac() {
		return phonemac;
	}

	public void setPhonemac(String phonemac) {
		this.phonemac = phonemac;
	}

	public String getAppuser() {
		return appuser;
	}

	public void setAppuser(String appuser) {
		this.appuser = appuser;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public long getIncoming() {
		return incoming;
	}

	public void setIncoming(long incoming) {
		this.incoming = incoming;
	}

	public long getOutgoing() {
		return outgoing;
	}

	public void setOutgoing(long outgoing) {
		this.outgoing = outgoing;
	}

	public Date getLogin_time() {
		return login_time;
	}

	public void setLogin_time(Date login_time) {
		this.login_time = login_time;
	}

	public long getOnline_time() {
		return online_time;
	}

	public void setOnline_time(long online_time) {
		this.online_time = online_time;
	}

	public int getIsonline() {
		return isonline;
	}

	public void setIsonline(int isonline) {
		this.isonline = isonline;
	}

}
