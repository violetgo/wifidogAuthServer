/**
 * @author weigao<weiga@iflytek.com>
 *
 * @version 1.0.0
 */
package wifi.authserver.dao;

import java.io.ByteArrayInputStream;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import wifi.authserver.comm.ReadAll;
import wifi.authserver.dao.map.DeviceMapper;
import wifi.authserver.dao.map.UserLogMapper;
import wifi.authserver.dao.map.UserMapper;


public class SessionFactory {
	
	private SessionFactory(){
		
	}
	
	private static SqlSessionFactory sqlSessionFactory;

	public static SqlSessionFactory getInstance() {
		if (sqlSessionFactory != null){
			return sqlSessionFactory;
		}
		String fileContent = ReadAll.readAll("conf/mybatis-config.xml", "utf-8");
		sqlSessionFactory = new SqlSessionFactoryBuilder().build(new ByteArrayInputStream(fileContent.getBytes()));
		sqlSessionFactory.getConfiguration().addMapper(UserMapper.class);
		sqlSessionFactory.getConfiguration().addMapper(UserLogMapper.class);
		sqlSessionFactory.getConfiguration().addMapper(DeviceMapper.class);
		sqlSessionFactory.getConfiguration().setDefaultStatementTimeout(10);
		return sqlSessionFactory;
	}
	
}
