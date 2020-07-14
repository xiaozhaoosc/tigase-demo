package tigase.shiku;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.shiku.utils.Base64;
import com.shiku.utils.ParamsSign;
import com.shiku.utils.StringUtil;
import com.shiku.utils.encrypt.MAC;

import tigase.conf.Configurable;
import tigase.conf.ConfigurationException;
import tigase.server.ComponentInfo;
import tigase.server.ConnectionManager;
import tigase.server.Message;
import tigase.server.Packet;
import tigase.server.PacketFilterIfc;
import tigase.server.QueueType;
import tigase.server.xmppclient.XMPPIOProcessor;
import tigase.shiku.conf.ShikuConfigBean;
import tigase.stats.StatisticsList;
import tigase.xml.Element;
import tigase.xml.XMLNodeIfc;
import tigase.xmpp.JID;
import tigase.xmpp.StanzaType;
import tigase.xmpp.StreamError;
import tigase.xmpp.XMPPIOService;

/**
 * 已废弃没用用到
 */
@Deprecated
public class ShikuMessageVerifyProcessor implements XMPPIOProcessor{
	
	private static final String ID = "shiku-messageVerify-processor";
	
	private static final String MESSAGE_KEY = "messageKey";
	
	private  Logger logger = LoggerFactory.getLogger(ShikuMessageVerifyProcessor.class.getName());
	
	private ConnectionManager connectionManager;
	/**
	 * 
	 */
	public ShikuMessageVerifyProcessor() {
		logger.info("ShikuMessageVerifyProcessor init ----");
	}
	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return ID;
	}

	
	@Override
	public boolean processIncoming(XMPPIOService service, Packet packet) {
		if (Message.ELEM_NAME!= packet.getElemName()) {
			return false;
		}
		String messageKey = getMessageKey(service);
		logger.info("processIncoming  messageKey {}",getMessageKey(service));
		if(!isEnabled(service)){
			return false;
		}
		if (null==(packet.getElement().findChildStaticStr(Message.MESSAGE_BODY_PATH))){
			return false;
		}else if ((null==packet.getType()) && (packet.getType() != StanzaType.chat) && (packet.getType() != StanzaType.groupchat)) {
			return false;
		}
		
		
			
			long startTime = System.currentTimeMillis();
			String body=packet.getElement().getChildCData(Message.MESSAGE_BODY_PATH);
			
			body = body.replaceAll("&quot;", "\"").replaceAll("&quot;", "\"");
			JSONObject bodyObj=null;
			try {
				bodyObj=JSON.parseObject(body);
				String mac = bodyObj.getString("mac");
				if(StringUtil.isEmpty(mac))
					return false;
				String serverMac = ParamsSign.joinObjectValues(bodyObj.getInnerMap());
				if(!Arrays.equals(Base64.decode(mac), MAC.encode(serverMac.getBytes(),Base64.decode(messageKey)))){
					logger.error("clientMac {}   serverMac {}",mac,serverMac);
					return true;
				}
				return false;

			} catch (Exception e) {
				logger.error(e.getMessage(),e);
				return false;

			}finally {
				//logger.info(" processIncoming time count {}",System.currentTimeMillis()-startTime);
			}
	}
	private String getMessageKey(XMPPIOService service) {
		return (String) service.getSessionData().get(MESSAGE_KEY);
	}
	private boolean isEnabled(XMPPIOService service) {
		return service.getSessionData().containsKey(MESSAGE_KEY);
	}

	
	
	
	
	
	
	
	@Override
	public boolean processOutgoing(XMPPIOService service, Packet packet) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void setConnectionManager(ConnectionManager connectionManager) {
		this.connectionManager=connectionManager;
		
		//connectionManager.serviceStarted(service);
	}
	
	
	@Override
	public void getStatistics(StatisticsList list) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Element[] supStreamFeatures(XMPPIOService service) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void packetsSent(XMPPIOService service) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processCommand(XMPPIOService service, Packet packet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean serviceStopped(XMPPIOService service, boolean streamClosed) {
		// TODO Auto-generated method stub
		return false;
	}

	

	@Override
	public void setProperties(Map<String, Object> props) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void streamError(XMPPIOService service, StreamError streamError) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	

}
