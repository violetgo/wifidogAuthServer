/**
 * @author weigao<weiga@iflytek.com>
 *
 * @version 1.0.0
 */
package wifi.authserver.comm;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class LogHelper {
	
	private static final Logger LOGGER = Logger.getLogger(LogHelper.class.getName());
	
	static{
		DOMConfigurator.configure("conf/log4j.xml");
	}
	
	private LogHelper(){
		
	}
	
	public static void debug(String name,String log) {
		Logger LOGGER = Logger.getLogger(name);
		LOGGER.debug(log);
	}
	
	public static void warn(String name,String log) {
		Logger LOGGER = Logger.getLogger(name);
		LOGGER.warn(log);
	}
	
	public static void info(String name,String log) {
		Logger LOGGER = Logger.getLogger(name);
		LOGGER.info(log);
	}
	
	public static void error(String name,String log) {
		Logger LOGGER = Logger.getLogger(name);
		LOGGER.error(log);
	}
	
	public static void debug(String log) {
		LOGGER.debug(log);
	}
	
	public static void warn(String log) {
		LOGGER.warn(log);
	}
	
	public static void info(String log) {
		LOGGER.info(log);
	}
	
	public static void error(String log) {
		LOGGER.error(log);
	}

}
