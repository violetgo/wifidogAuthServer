/**
 * @author  weigao<weiga@iflytek.com>
 *
 * @version 1.0.0
 */
package wifi.authserver.dao.map;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import wifi.authserver.dao.obj.User;


public interface UserMapper {
	@Select("SELECT * FROM merchant_appuser WHERE username = #{param1} limit 1")
	public List<User> select(String user);
	
	@Insert("insert into merchant_appuser(username,auth_value,password,createtime) values(#{username},1,#{password},#{createtime})")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	public boolean insert(User user);
	
}
