/**
 * @author  weigao<weiga@iflytek.com>
 *
 * @version 1.0.0
 */
package wifi.authserver.httphandler.cache;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;

import wifi.authserver.comm.LogHelper;
import wifi.authserver.dao.SessionFactory;
import wifi.authserver.dao.map.UserLogMapper;
import wifi.authserver.dao.obj.UserLog;
import wifi.authserver.httphandler.HttpParameterHelper;

public class KeepAliver implements Runnable {

	private static KeepAliver instance = new KeepAliver();

	private Lock lock = new ReentrantLock();
	private Map<String, UserLog> cacheMap = new HashMap<String, UserLog>();
	private Set<String> blackset = new HashSet<String>();
	private Set<String> removeset = new HashSet<String>();

	private KeepAliver() {

	}
	
	
	/**
	 * when check ping and PK in cache map ,update it
	 * */
	private boolean updateLogByPing(String incoming, String outgoing, String key, UserLog userlog) {

		long in = Long.parseLong(incoming) / 1024;
		long out = Long.parseLong(outgoing) / 1024;

		if ((in - userlog.getIncoming()) < 1 && (out - userlog.getOutgoing()) < 1) {
			userlog.setNochangetime(userlog.getNochangetime() + 1);
		} else {
			userlog.setNochangetime(0);
		}

		LogHelper.info("in = " + in + " & last = " + userlog.getIncoming() + "  " + ((in - userlog.getIncoming()) < 1));
		LogHelper.info("out = " + out + " & last = " + userlog.getOutgoing() + "  " + ((out - userlog.getOutgoing()) < 1));

		userlog.setIncoming(in);
		userlog.setOutgoing(out);
		userlog.setOnline_time(userlog.getOnline_time() + ((System.currentTimeMillis() - userlog.getLastPing()) / 1000));
		userlog.setLastPing(System.currentTimeMillis());

		SqlSession session = null;
		try {
			SqlSessionFactory factory = SessionFactory.getInstance();
			session = factory.openSession();
			UserLogMapper logMapper = session.getMapper(UserLogMapper.class);
			logMapper.updatet(userlog);
			lock.lock();
			try {
				cacheMap.put(key, userlog);
			} catch (Exception e) {
				LogHelper.error(ExceptionUtils.getFullStackTrace(e));
			} finally {
				lock.unlock();
			}
		} catch (Exception e) {
			LogHelper.error(ExceptionUtils.getFullStackTrace(e));
		} finally {
			if (session != null) {
				session.close();
			}
		}

		if (userlog.getNochangetime() >= 3) {
			return false;
		}

		return true;
	}

	/**
	 * if PK not in cache map ,add new log to db and cache it
	 * */
	private void insertNewLog(String ip, String mac, String token, String gw_id, String incoming, String outgoing, String key) {
		UserLog log = new UserLog();
		log.setIp(ip);
		log.setPhonemac(mac);
		log.setAppuser(token);
		log.setDevice(gw_id);

		log.setLogin_time(new Date());
		log.setIsonline(1);
		// mean online
		log.setIncoming(Long.parseLong(incoming) / 1024);
		log.setOutgoing(Long.parseLong(outgoing) / 1024);
		log.setLastPing(System.currentTimeMillis());
		log.setOnline_time(0);
		SqlSession session = null;
		try {
			SqlSessionFactory factory = SessionFactory.getInstance();
			session = factory.openSession();
			UserLogMapper logMapper = session.getMapper(UserLogMapper.class);
			logMapper.insert(log);
			lock.lock();
			try {
				cacheMap.put(key, log);
			} catch (Exception e) {
				LogHelper.error(ExceptionUtils.getFullStackTrace(e));
			} finally {
				lock.unlock();
			}
		} catch (Exception e) {
			LogHelper.error(ExceptionUtils.getFullStackTrace(e));
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}
	
	/**
	 * @param key
	 * @param userlog
	 */
	private void exitByKey(String key, UserLog userlog) {
		userlog.setIsonline(0);
		SqlSession session = null;
		try {
			SqlSessionFactory factory = SessionFactory.getInstance();
			session = factory.openSession();
			UserLogMapper logMapper = session.getMapper(UserLogMapper.class);
			logMapper.updatet(userlog);
			lock.lock();
			try {
				cacheMap.remove(key);
			} catch (Exception e) {
				LogHelper.error(ExceptionUtils.getFullStackTrace(e));
			} finally {
				lock.unlock();
			}
		} catch (Exception e) {
			LogHelper.error(ExceptionUtils.getFullStackTrace(e));
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}


	private void deleteLog(String key, UserLog userlog) {
		LogHelper.info("run delete cache " + key);
		exitByKey(key, userlog);
	}
	
	public void exit(String token,String gw_id) {
		LogHelper.info("exit by " +gw_id+ token);
		String key = null;
		UserLog userlog = null;
		lock.lock();
		try {
			for(String cur:cacheMap.keySet()){
				if(cur.contains(gw_id + token)){
					userlog = cacheMap.get(cur);
					key = cur;
				}
			}
		} catch (Exception e) {
			LogHelper.error(ExceptionUtils.getFullStackTrace(e));
		} finally {
			lock.unlock();
		}
		if(userlog!=null){
			exitByKey(key, userlog);
		}
	}


	
	public static KeepAliver getInstance() {
		return instance;
	}
	

	public void addRemoveSet(String blackname) {
		this.removeset.add(blackname);
	}
	
	public Set<String> getBlackset() {
		return blackset;
	}

	public void setBlackset(Set<String> blackset) {
		this.blackset = blackset;
	}

	public void addBlackset(String blackname) {
		this.blackset.add(blackname);
	}
	
	public void delBlackset(String blackname) {
		this.blackset.remove(blackname);
	}
	
	public boolean updateLog(QueryStringDecoder content) {
		String ip = HttpParameterHelper.getParameters(content, "ip");
		String mac = HttpParameterHelper.getParameters(content, "mac");
		String token = HttpParameterHelper.getParameters(content, "token");
		String gw_id = HttpParameterHelper.getParameters(content, "gw_id");

		String incoming = HttpParameterHelper.getParameters(content, "incoming");
		String outgoing = HttpParameterHelper.getParameters(content, "outgoing");

		String key = gw_id + token + mac+ ip;
		
		if(blackset.contains(gw_id+"-"+token) || removeset.contains(gw_id+"-"+token)){
			UserLog userlog = null;
			lock.lock();
			try {
				userlog = cacheMap.get(key);
			} catch (Exception e) {
				LogHelper.error(ExceptionUtils.getFullStackTrace(e));
			} finally {
				lock.unlock();
			}
			if(userlog!=null){
				deleteLog(key, userlog);
				if(removeset.contains(gw_id+"-"+token)){
					LogHelper.info("delete by remove client "+key);
				}else{
					LogHelper.info("delete by black list "+key);
				}
				
			}
			
			removeset.remove(gw_id+"-"+token);
			return false;
		}
		
		LogHelper.info("update auth " + key);
		UserLog userlog = null;
		lock.lock();
		try {
			userlog = cacheMap.get(key);
		} catch (Exception e) {
			LogHelper.error(ExceptionUtils.getFullStackTrace(e));
		} finally {
			lock.unlock();
		}
		if (userlog == null) {
			insertNewLog(ip, mac, token, gw_id, incoming, outgoing, key);
		} else {
			boolean isvail = updateLogByPing(incoming, outgoing, key, userlog);
			return isvail;
		}
		return true;
	}

	public void run() {
		while (true) {
			try {
				LogHelper.info("run keeplive...");
				Map<String, UserLog> tempCacheMap = new HashMap<String, UserLog>(cacheMap);
				for (String key : tempCacheMap.keySet()) {
					UserLog log = tempCacheMap.get(key);
					LogHelper.info("parse is need delete " + key + " and not change is  " + log.getNochangetime() + " cur time is " + System.currentTimeMillis() + " and last ping is " + log.getLastPing() + " interval is " + (System.currentTimeMillis() - log.getLastPing()));
					if (log.getNochangetime() >= 3 || (System.currentTimeMillis() - log.getLastPing()) > 1000 * 60 * 10) {
						deleteLog(key, log);
					}
				}
				Thread.sleep(60 * 1000);
			} catch (Exception e) {
				LogHelper.error(ExceptionUtils.getFullStackTrace(e));
			}
		}
	}
}
