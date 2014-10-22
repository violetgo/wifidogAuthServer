/**
 * @author  weigao<weiga@iflytek.com>
 *
 * @version 1.0.0
 */
package wifi.authserver.httphandler;

import java.util.List;

import org.jboss.netty.handler.codec.http.QueryStringDecoder;

public class HttpParameterHelper {
	/**
	 * get Parameter from URI
	 * */
	public static String getParameters(QueryStringDecoder content,String query){
		List<String> list = content.getParameters().get(query);
		if(list==null){
			return "";
		}
		
		StringBuilder respone = new StringBuilder();
		for(int i=0;i<list.size();i++){
			if(i==list.size()-1){
				respone.append(list.get(i));
			}else{
				respone.append(list.get(i)+",");
			}
		}
		return respone.toString();
	}
}
