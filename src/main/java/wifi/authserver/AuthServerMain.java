/**
 * @author  weigao<weiga@iflytek.com>
 *
 * @version 1.0.0
 */
package wifi.authserver;

import wifi.authserver.comm.LogHelper;
import wifi.authserver.httphandler.HttpServer;
import wifi.authserver.httphandler.cache.KeepAliver;

public class AuthServerMain {

	public static void main(String[] args) {
		if(args.length!=1){
			LogHelper.error("cant statrt ,please input port,like 'java -jar authServer <port>'");
			return ;
		}
		int port = Integer.parseInt(args[0]);
		HttpServer server = new HttpServer(port);
		
		new Thread(KeepAliver.getInstance()).start();;
		
		server.start();
	}

}
