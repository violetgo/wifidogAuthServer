/**
 * @author weigao<weiga@iflytek.com>
 *
 * @version 1.0.0
 */
package wifi.authserver.httphandler;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpMethod.GET;
import static org.jboss.netty.handler.codec.http.HttpMethod.POST;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jboss.netty.buffer.ByteBufferBackedChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.util.CharsetUtil;

import wifi.authserver.comm.LogHelper;
import wifi.authserver.comm.ReadAll;
import wifi.authserver.dao.SessionFactory;
import wifi.authserver.dao.map.UserMapper;
import wifi.authserver.dao.obj.User;
import wifi.authserver.httphandler.cache.DeviceCache;
import wifi.authserver.httphandler.cache.KeepAliver;

public class HttpHandler extends SimpleChannelUpstreamHandler {
	
	private void parseGet(ChannelHandlerContext ctx, MessageEvent e, HttpRequest request)  {
		QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
		String path = decoder.getPath();
		HttpResponse response = null;;
		LogHelper.info("got get request "+path);
		if(path.equals("/ewifi/ping/")){
			response = parsePing(ctx, decoder);
		}else if(path.equals("/ewifi/auth/")){
			response = parseAuth(ctx, decoder);
		}else if(path.equals("/ewifi/login/")){
			response = parseLogin(ctx, decoder);
		}else if(path.equals("/ewifi/portal/")){
			response = parsePortal(ctx, decoder);
		}else if(path.equals("/ewifi/regedit/")){
			response = parseRegedit(ctx, decoder);
		}else if(path.equals("/ewifi/querymac/")){
			response = parseQuery(ctx, decoder);
		}else{
			response = parseStatic(ctx, decoder);
		}

		Channel ch = e.getChannel();
		if (ch.isConnected()) {
			ch.write(response).addListener(ChannelFutureListener.CLOSE);
		}

	}
	
	private void parsePost(ChannelHandlerContext ctx, MessageEvent e, HttpRequest request)  {
		QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
		String path = decoder.getPath();
		HttpResponse response = null;;
		LogHelper.info("got post request "+path);
		ChannelBuffer buffer = request.getContent();
		String content = buffer.toString(Charset.defaultCharset());
		LogHelper.info("got post request body "+content);
		decoder = new QueryStringDecoder("http://127.0.0.1/?"+content);
		
		if(path.equals("/ewifi/loginValidate/")){
			response = validateLogin(ctx, decoder);
		}else if(path.equals("/ewifi/submitregedit/")){
			response = parseSubmit(ctx, decoder);
		}else if(path.equals("/ewifi/exit/")){
			response = parseExit(ctx, decoder);
		}else if(path.equals("/ewifi/addblacklist/")){
			response = parseAddBlackList(ctx, decoder);
		}else if(path.equals("/ewifi/delblacklist/")){
			response = parseDelBlackList(ctx, decoder);
		}else if(path.equals("/ewifi/removeclient/")){
			response = parseRemoveClient(ctx, decoder);
		}else{
			response = sendPrepare(ctx, "");
		}
		
		Channel ch = e.getChannel();

		if (ch.isConnected()) {
			ch.write(response).addListener(ChannelFutureListener.CLOSE);
		}
	}

	private HttpResponse parsePing(ChannelHandlerContext ctx, QueryStringDecoder content) {
		LogHelper.info(HttpParameterHelper.getParameters(content, "gw_id")+" got ping...");
		HttpResponse response = null;
		response = sendPrepare(ctx,"Pong");
		return response;
	}

	private HttpResponse parsePortal(ChannelHandlerContext ctx, QueryStringDecoder content) {
		HttpResponse response = null;
		String html = ReadAll.readAll("web/portal/"+HttpParameterHelper.getParameters(content, "gw_id")+".html", "utf-8");
		if(html!=null && !html.isEmpty()){
			response = sendPrepare(ctx,html);
		}else{
			response = sendPrepare(ctx,"欢迎你连接"+HttpParameterHelper.getParameters(content, "gw_id")+",你现在可以正常上网了!");
		}
		
		return response;
	}
	
	private HttpResponse parseAuth(ChannelHandlerContext ctx, QueryStringDecoder content) {
		HttpResponse response = null;
		boolean isT = KeepAliver.getInstance().updateLog(content);
		if(isT){
			response = sendPrepare(ctx,"Auth: 1\nMessages: no");
		}else{
			response = sendPrepare(ctx,"Auth: 0\nMessages: no");
		}
		
		return response;
	}
	
	private HttpResponse parseQuery(ChannelHandlerContext ctx, QueryStringDecoder content) {
		HttpResponse response = null;
		String key = DeviceCache.getInstance().getKey(HttpParameterHelper.getParameters(content, "mac"));
		if(key==null){
			response = sendPrepare(ctx,"error:cant search mac info");
			return response;
		}
		response = sendPrepare(ctx,key);
		return response;
	}
	
	/**
	 * redict to login page
	 * */
	private HttpResponse parseLogin(ChannelHandlerContext ctx, QueryStringDecoder content) {
		
		String key = DeviceCache.getInstance().getKey(HttpParameterHelper.getParameters(content, "gw_id"));
		if(key==null || key.isEmpty()){
			return sendPrepare(ctx,"非联盟商家,无法认证!");
		}
		HttpResponse response = null;
		response = returnLoginPage(ctx, content,null);
		return response;
	}

	/**
	 * return login page,if user & password is error.return error tips!
	 * */
	private HttpResponse returnRegeditPage(ChannelHandlerContext ctx, QueryStringDecoder content,String tips) {
		HttpResponse response;
		String responseContent = ReadAll.readAll("web/regedit.html", "utf-8");
		responseContent = responseContent.replaceAll("<<param.gw_address>>",HttpParameterHelper.getParameters(content, "gw_address"));
		responseContent = responseContent.replaceAll("<<param.gw_port>>",HttpParameterHelper.getParameters(content, "gw_port"));
		responseContent = responseContent.replaceAll("<<param.gw_id>>",HttpParameterHelper.getParameters(content, "gw_id"));
		responseContent = responseContent.replaceAll("<<param.url>>",HttpParameterHelper.getParameters(content, "url"));
		
		if(tips==null){
			responseContent = responseContent.replaceAll("<<showtips>>","");
		}else{
			responseContent = responseContent.replaceAll("<<showtips>>","<input type='button' onclick='return;' class='get_btn' value='"+tips+"'>");
		}
		
		response = sendPrepare(ctx, responseContent);
		return response;
	}
	
	private HttpResponse parseRegedit(ChannelHandlerContext ctx, QueryStringDecoder content) {
		HttpResponse response = null;
		response = returnRegeditPage(ctx, content,null);
		return response;
	}
	
	
	private HttpResponse parseRemoveClient(ChannelHandlerContext ctx, QueryStringDecoder content) {
		String token = HttpParameterHelper.getParameters(content, "token");
		String gw_id = HttpParameterHelper.getParameters(content, "gw_id");
		KeepAliver.getInstance().addRemoveSet(gw_id+"-"+token);
		LogHelper.info("add remove client name "+gw_id+"-"+token);
		return returnLoginPage(ctx, content,"SUCCESS");
	}
	
	
	private HttpResponse parseAddBlackList(ChannelHandlerContext ctx, QueryStringDecoder content) {
		String token = HttpParameterHelper.getParameters(content, "token");
		String gw_id = HttpParameterHelper.getParameters(content, "gw_id");
		KeepAliver.getInstance().addBlackset(gw_id+"-"+token);
		LogHelper.info("add black name "+gw_id+"-"+token);
		return returnLoginPage(ctx, content,"SUCCESS");
	}
	
	private HttpResponse parseExit(ChannelHandlerContext ctx, QueryStringDecoder content) {
		String token = HttpParameterHelper.getParameters(content, "token");
		String gw_id = HttpParameterHelper.getParameters(content, "gw_id");
		KeepAliver.getInstance().exit(token,gw_id);
		LogHelper.info("exit name "+gw_id+"-"+token);
		return returnLoginPage(ctx, content,"SUCCESS");
	}
	
	private HttpResponse parseDelBlackList(ChannelHandlerContext ctx, QueryStringDecoder content) {
		String token = HttpParameterHelper.getParameters(content, "token");
		String gw_id = HttpParameterHelper.getParameters(content, "gw_id");
		KeepAliver.getInstance().delBlackset(gw_id+"-"+token);
		LogHelper.info("del black name "+gw_id+"-"+token);
		return returnLoginPage(ctx, content,"SUCCESS");
	}
	
	private HttpResponse parseSubmit(ChannelHandlerContext ctx, QueryStringDecoder content) {
		HttpResponse response = null;
		String method = HttpParameterHelper.getParameters(content, "method");
		if(method==null || method.isEmpty()){
			return response = submitByHtml(ctx, content, response);
		}else if(method.equals("app")){
			return response = submitByApp(ctx, content, response);
		}else{
			return response = submitByHtml(ctx, content, response);
		}
		
	}

	/**
	 * @param ctx
	 * @param content
	 * @param response
	 * @return
	 */
	private HttpResponse submitByHtml(ChannelHandlerContext ctx, QueryStringDecoder content, HttpResponse response) {
		String wifiname = HttpParameterHelper.getParameters(content, "wifiname");
		String wifipass = HttpParameterHelper.getParameters(content, "wifipass");
		LogHelper.info("get rededit user:"+wifiname+" pass:"+wifipass);
		if(wifiname.contains("'") || wifiname.contains("\"")){
			LogHelper.info("get rededit 不合法字符");
			response = returnRegeditPage(ctx, content,"不合法字符!");
			return response;
		}
		
		if(wifipass.contains("'") || wifipass.contains("\"")){
			LogHelper.info("get rededit 不合法字符");
			response = returnRegeditPage(ctx, content,"不合法字符!");
			return response;
		}
		SqlSession session = null;
		try{
			SqlSessionFactory factory = SessionFactory.getInstance();
			session = factory.openSession();
			UserMapper userMapper = session.getMapper(UserMapper.class);
			List<User> result = userMapper.select(wifiname);
	
			if(result!=null && result.size()!=0){
				LogHelper.info("get rededit 用户名存在");
				response = returnLoginPage(ctx, content,"用户名存在!");
				return response;
			}
			
			User user = new User();
			user.setUsername(wifiname);
			user.setPassword(wifipass);
			user.setCreatetime(new Date());
			userMapper.insert(user);
			
			String redirectUrl="http://"+HttpParameterHelper.getParameters(content, "gw_address")+":"+HttpParameterHelper.getParameters(content, "gw_port")+"/wifidog/auth?"+"token="+wifiname+"&url="+HttpParameterHelper.getParameters(content, "url");
			LogHelper.info("get rededit redirecturl:"+redirectUrl);
			
			response = sendPrepare(ctx, "");
			response.setStatus(HttpResponseStatus.TEMPORARY_REDIRECT);
			response.addHeader(HttpHeaders.Names.LOCATION, redirectUrl);
		}catch(Exception e){
			LogHelper.error(ExceptionUtils.getFullStackTrace(e));
		}finally{
			if(session!=null){
				session.close();
			}
		}
		return response;
	}
	
	/**
	 * @param ctx
	 * @param content
	 * @param response
	 * @return
	 */
	private HttpResponse submitByApp(ChannelHandlerContext ctx, QueryStringDecoder content, HttpResponse response) {
		String wifiname = HttpParameterHelper.getParameters(content, "wifiname");
		String wifipass = HttpParameterHelper.getParameters(content, "wifipass");
		LogHelper.info("get rededit user:"+wifiname+" pass:"+wifipass);
		if(wifiname.contains("'") || wifiname.contains("\"")){
			LogHelper.info("get rededit 不合法字符");
			response = sendPrepare(ctx, "{errorcode:000001}");
			return response;
		}
		
		if(wifipass.contains("'") || wifipass.contains("\"")){
			LogHelper.info("get rededit 不合法字符");
			response = sendPrepare(ctx, "{errorcode:000001}");
			return response;
		}
		SqlSession session = null;
		try{
			SqlSessionFactory factory = SessionFactory.getInstance();
			session = factory.openSession();
			UserMapper userMapper = session.getMapper(UserMapper.class);
			List<User> result = userMapper.select(wifiname);
	
			if(result!=null && result.size()!=0){
				LogHelper.info("get rededit 用户名存在");
				response = sendPrepare(ctx, "{errorcode:000005}");
				return response;
			}
			
			User user = new User();
			user.setUsername(wifiname);
			user.setPassword(wifipass);
			user.setCreatetime(new Date());
			userMapper.insert(user);
			
			String redirectUrl="http://"+HttpParameterHelper.getParameters(content, "gw_address")+":"+HttpParameterHelper.getParameters(content, "gw_port")+"/wifidog/auth?"+"token="+wifiname+"&url="+HttpParameterHelper.getParameters(content, "url");
			LogHelper.info("get rededit redirecturl:"+redirectUrl);
			
			response = sendPrepare(ctx, "");
			response.setStatus(HttpResponseStatus.TEMPORARY_REDIRECT);
			response.addHeader(HttpHeaders.Names.LOCATION, redirectUrl);
		}catch(Exception e){
			LogHelper.error(ExceptionUtils.getFullStackTrace(e));
		}finally{
			if(session!=null){
				session.close();
			}
		}
		return response;
	}
	
	/**
	 * 获取 static 文件并返回
	 * */
	private HttpResponse parseStatic(ChannelHandlerContext ctx, QueryStringDecoder content) {
		
		if(!content.getPath().startsWith("/ewifi/")){
			HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
			response.addHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
			response.setContent(new ByteBufferBackedChannelBuffer(ByteBuffer.wrap(new byte[0])));
			return response;
		}
		
		String path = content.getPath().substring("/ewifi/".length());
		byte[] responseContent = ReadAll.readAllByte("web/"+path);
		if(responseContent==null){
			responseContent = new byte[0];
		}
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
		
		if(path.contains("css/")){
			response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/css");
		}else if(path.contains("js/")){
			response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/x-javascript;");
		}else if(path.contains("img/")){
			response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "image/*");
		}else if(path.contains("bin/")){
			response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/octet-stream");
		}
		
		response.addHeader(HttpHeaders.Names.CONTENT_LENGTH,responseContent.length);
		response.addHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
		response.setContent(new ByteBufferBackedChannelBuffer(ByteBuffer.wrap(responseContent)));
		return response;
	}
	
	/**
	 * auth by user & password
	 * */
	private HttpResponse validateLogin(ChannelHandlerContext ctx, QueryStringDecoder content) {
		HttpResponse response = null;
		
		String method = HttpParameterHelper.getParameters(content, "method");
		if(method==null || method.isEmpty()){
			return loginByHtml(ctx, content, response);
		}else if(method.equals("app")){
			return loginByApp(ctx, content, response);
		}else{
			return loginByHtml(ctx, content, response);
		}

	}

	/**
	 * @param ctx
	 * @param content
	 * @param response
	 * @return
	 */
	private HttpResponse loginByHtml(ChannelHandlerContext ctx, QueryStringDecoder content, HttpResponse response) {
		String wifiname = HttpParameterHelper.getParameters(content, "wifiname");
		String wifipass = HttpParameterHelper.getParameters(content, "wifipass");
		LogHelper.info("get login user:"+wifiname+" pass:"+wifipass);
		
		if(wifiname.trim().equals("") || wifipass.trim().equals("")){
			LogHelper.info("get login 不合法字符");
			response = returnLoginPage(ctx, content,"不合法字符!");
			return response;
		}
		
		if(wifiname.contains("'") || wifiname.contains("\"")){
			LogHelper.info("get login 不合法字符");
			response = returnLoginPage(ctx, content,"不合法字符!");
			return response;
		}
		
		SqlSessionFactory factory = SessionFactory.getInstance();
		SqlSession session = null;
		try{
			session = factory.openSession();
			UserMapper userMapper = session.getMapper(UserMapper.class);
			List<User> result = userMapper.select(wifiname);
			if(result==null || result.size()==0){
				LogHelper.info("get login 用户名错误");
				response = returnLoginPage(ctx, content,"用户名密码错误!");
				return response;
			}
			
			if(!result.get(0).getPassword().equals(wifipass)){
				LogHelper.info("get login 密码错误");
				response = sendPrepare(ctx, "用户名密码错误!");
				return response;
			}
			
//			UserLogMapper logMapper = session.getMapper(UserLogMapper.class);
//			List<UserLog> log = logMapper.select(wifiname);
//			if(log!=null && log.size()>0 && log.get(0).getIsonline()==1){
//				LogHelper.info("用户已经登录...");
//				response = returnLoginPage(ctx, content,"用户已经登录...");
//				return response;
//			}

			String redirectUrl="http://"+HttpParameterHelper.getParameters(content, "gw_address")+":"+HttpParameterHelper.getParameters(content, "gw_port")+"/wifidog/auth?"+"token="+wifiname+"&url="+HttpParameterHelper.getParameters(content, "url");
			response = sendPrepare(ctx, "");
			response.setStatus(HttpResponseStatus.TEMPORARY_REDIRECT);
			response.addHeader(HttpHeaders.Names.LOCATION, redirectUrl);
			LogHelper.info("redirectUrl is  : "+redirectUrl);
			
		}catch(Exception e){
			LogHelper.error(ExceptionUtils.getFullStackTrace(e));
		}finally{
			if(session!=null){
				session.close();
			}
			
		}
		return response;
	}
	
	private HttpResponse loginByApp(ChannelHandlerContext ctx, QueryStringDecoder content, HttpResponse response) {
		String wifiname = HttpParameterHelper.getParameters(content, "wifiname");
		String wifipass = HttpParameterHelper.getParameters(content, "wifipass");
		LogHelper.info("get login by app user:"+wifiname+" pass:"+wifipass);
		
		if(wifiname.trim().equals("") || wifipass.trim().equals("")){
			LogHelper.info("get login 不合法字符");
			response = sendPrepare(ctx, "{errorcode:000001}");
			return response;
		}
		
		if(wifiname.contains("'") || wifiname.contains("\"")){
			LogHelper.info("get login 不合法字符");
			response = sendPrepare(ctx, "{errorcode:000001}");
			return response;
		}
		
		SqlSessionFactory factory = SessionFactory.getInstance();
		SqlSession session = null;
		try{
			session = factory.openSession();
			UserMapper userMapper = session.getMapper(UserMapper.class);
			List<User> result = userMapper.select(wifiname);
			if(result==null || result.size()==0){
				LogHelper.info("get login 用户名错误");
				response = sendPrepare(ctx, "{errorcode:000002}");
				return response;
			}
			
			if(!result.get(0).getPassword().equals(wifipass)){
				LogHelper.info("get login 密码错误");
				response = sendPrepare(ctx, "{errorcode:000003}");
				return response;
			}
			
//			UserLogMapper logMapper = session.getMapper(UserLogMapper.class);
//			List<UserLog> log = logMapper.select(wifiname);
//			if(log!=null && log.size()>0 && log.get(0).getIsonline()==1){
//				LogHelper.info("用户已经登录...");
//				response = sendPrepare(ctx, "{errorcode:000004}");
//				return response;
//			}

			String redirectUrl="http://"+HttpParameterHelper.getParameters(content, "gw_address")+":"+HttpParameterHelper.getParameters(content, "gw_port")+"/wifidog/auth?"+"token="+wifiname+"&url="+HttpParameterHelper.getParameters(content, "url");
			response = sendPrepare(ctx, "");
			response.setStatus(HttpResponseStatus.TEMPORARY_REDIRECT);
			response.addHeader(HttpHeaders.Names.LOCATION, redirectUrl);
			LogHelper.info("redirectUrl is  : "+redirectUrl);
			
		}catch(Exception e){
			LogHelper.error(ExceptionUtils.getFullStackTrace(e));
		}finally{
			if(session!=null){
				session.close();
			}
			
		}
		return response;
	}
	
	private HttpResponse sendPrepare(ChannelHandlerContext ctx,String body) {
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
		response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
		response.addHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
		StringBuilder builder = new StringBuilder();
		builder.append(body);

		response.setContent(ChannelBuffers.copiedBuffer(builder.toString(), CharsetUtil.UTF_8));
		return response;
	}

	private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
		response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
		response.setContent(ChannelBuffers.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));

		// Close the connection as soon as the error message is sent.
		ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
	}
	
	/**
	 * return login page,if user & password is error.return error tips!
	 * */
	private HttpResponse returnLoginPage(ChannelHandlerContext ctx, QueryStringDecoder content,String tips) {
		HttpResponse response;
		String responseContent = ReadAll.readAll("web/login.html", "utf-8");
		responseContent = responseContent.replaceAll("<<param.gw_address>>",HttpParameterHelper.getParameters(content, "gw_address"));
		responseContent = responseContent.replaceAll("<<param.gw_port>>",HttpParameterHelper.getParameters(content, "gw_port"));
		responseContent = responseContent.replaceAll("<<param.gw_id>>",HttpParameterHelper.getParameters(content, "gw_id"));
		responseContent = responseContent.replaceAll("<<param.url>>",HttpParameterHelper.getParameters(content, "url"));
		
		if(tips==null){
			responseContent = responseContent.replaceAll("<<showtips>>","");
		}else{
			responseContent = responseContent.replaceAll("<<showtips>>","<input type='button' onclick='return;' class='get_btn' value='"+tips+"'>");
		}
		
		response = sendPrepare(ctx, responseContent);
		return response;
	}
	
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		try{
			HttpRequest request = (HttpRequest) e.getMessage();
			if (request.getMethod() == GET) {
				parseGet(ctx,e,request);
			}else if(request.getMethod() == POST){
				parsePost(ctx, e, request);
			}
		}catch(Exception e1){
			LogHelper.error(ExceptionUtils.getFullStackTrace(e1));
		}
		sendError(ctx,METHOD_NOT_ALLOWED);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		Throwable cause = e.getCause();
		if (cause instanceof TooLongFrameException) {
			sendError(ctx, BAD_REQUEST);
			return;
		}
	}
	
}
