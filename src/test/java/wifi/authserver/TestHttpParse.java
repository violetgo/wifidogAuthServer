/**
 * @author  weigao<weiga@iflytek.com>
 *
 * @version 1.0.0
 */
package wifi.authserver;

import org.jboss.netty.handler.codec.http.QueryStringDecoder;

import wifi.authserver.httphandler.HttpParameterHelper;

public class TestHttpParse {
	
	public static void main(String []a){
		String content = "wifiname=ggs&wifipass=e10adc3949ba59abbe56e057f20f883e&gw_address=192.168.2.1&gw_port=2060&gw_id=20%3AAA%3A4B%3ABD%3AC9%3AB9";
		QueryStringDecoder decode = new QueryStringDecoder("http://127.0.0.1/?"+content);
		System.out.print(HttpParameterHelper.getParameters(decode, "wifiname"));
	}
}
