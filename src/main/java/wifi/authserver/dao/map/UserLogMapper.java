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
import org.apache.ibatis.annotations.Update;

import wifi.authserver.dao.obj.UserLog;


public interface UserLogMapper {
	
	@Insert("insert into merchant_wifiuserlog(phonemac,appuser,device,ip,incoming,outgoing,login_time,online_time,isonline) values(#{phonemac},#{appuser},#{device},#{ip},#{incoming},#{outgoing},#{login_time},#{online_time},#{isonline})")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	public boolean insert(UserLog status);
	
	@Update("update merchant_wifiuserlog set incoming=#{incoming},outgoing=#{outgoing},online_time=#{online_time},isonline=#{isonline} where id=#{id}")
	public boolean updatet(UserLog status);
	
	
	@Update("update merchant_wifiuserlog set isonline=0 where id=#{param1}")
	public boolean exit(int id);
	
	@Select("select * from merchant_wifiuserlog where appuser=#{param1} order by login_time desc limit 1")
	public List<UserLog> select(String name);
	
}
