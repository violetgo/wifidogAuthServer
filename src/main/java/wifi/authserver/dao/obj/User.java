/**
 * @author  weigao<weiga@iflytek.com>
 *
 * @version 1.0.0
 */
package wifi.authserver.dao.obj;

import java.util.Date;

public class User {
	private int id;
	private int auth_value;
	private String username;
	private String password;
	private String description;
	private Date createtime;
	private String email;
	private String gender;

	public int getAuth_value() {
		return auth_value;
	}

	public void setAuth_value(int auth_value) {
		this.auth_value = auth_value;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getCreatetime() {
		return createtime;
	}

	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}

}
