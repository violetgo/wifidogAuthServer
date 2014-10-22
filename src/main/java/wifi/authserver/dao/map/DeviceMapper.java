/**
 * @author  weigao<weiga@iflytek.com>
 *
 * @version 1.0.0
 */
package wifi.authserver.dao.map;

import java.util.List;

import org.apache.ibatis.annotations.Select;


public interface DeviceMapper {
	@Select("SELECT merchant_device.status FROM merchant_device WHERE mac = #{param1} limit 1")
	public List<String> select(String mac);
	
}
