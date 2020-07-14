package tigase.shiku.plugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.shiku.utils.Base64;
import com.shiku.utils.ParamsSign;
import com.shiku.utils.StringUtil;
import com.shiku.utils.encrypt.MAC;

import tigase.db.NonAuthUserRepository;
import tigase.server.Message;
import tigase.server.Packet;
import tigase.xmpp.StanzaType;
import tigase.xmpp.XMPPException;
import tigase.xmpp.XMPPPreprocessorIfc;
import tigase.xmpp.XMPPProcessor;
import tigase.xmpp.XMPPProcessorIfc;
import tigase.xmpp.XMPPResourceConnection;
import tigase.xmpp.impl.annotation.Handle;
import tigase.xmpp.impl.annotation.Handles;
import tigase.xmpp.impl.annotation.Id;

/*@Id("shiku-messageVerify-plugin")
@Handles({
  @Handle(path={ "message" },xmlns="jabber:client")
})*/
public class ShikuMessageVerifyPlugin extends XMPPProcessor
		implements XMPPPreprocessorIfc,XMPPProcessorIfc {
	
	private static final String ID = "shiku-verify-plugin";
	
	private static final String[][] ELEMENT_PATHS = { { "message" } };

	private static final String[] XMLNSS = { Packet.CLIENT_XMLNS };

	private static final Set<StanzaType> TYPES;
	static {
		HashSet<StanzaType> tmpTYPES = new HashSet<StanzaType>();
		tmpTYPES.add(null);
		tmpTYPES.addAll(EnumSet.of(StanzaType.groupchat, StanzaType.chat));
		TYPES = Collections.unmodifiableSet(tmpTYPES);
	}
	
	private static final String MESSAGE_KEY = "messageKey";
	
	private  Logger logger = LoggerFactory.getLogger(ShikuMessageVerifyPlugin.class.getName());
	
	/**
	 * 
	 */
	public ShikuMessageVerifyPlugin() {
		logger.info("ShikuMessageVerifyPlugin init ----");
	}
	

	
	private String getMessageKey(XMPPResourceConnection session) {
		return (String) session.getSessionData(MESSAGE_KEY);
	}
	private boolean isEnabled(XMPPResourceConnection session) {
		return null!=session.getSessionData(MESSAGE_KEY);
	}



	@Override
	public boolean preProcess(Packet packet, XMPPResourceConnection session, NonAuthUserRepository repo,
			Queue<Packet> results, Map<String, Object> settings) {
		if(null==session||Message.ELEM_NAME!= packet.getElemName())
			return false;
		
		long startTime = System.currentTimeMillis();
		String messageKey = getMessageKey(session);
		//logger.info("preProcess  messageKey {}",messageKey);
		if(!isEnabled(session)){
			return false;
		}
		if (null==(packet.getElement().findChildStaticStr(Message.MESSAGE_BODY_PATH))){
			return false;
		}else if ((null==packet.getType()) && (packet.getType() != StanzaType.chat) && (packet.getType() != StanzaType.groupchat)) {
			return false;
		}else if(null!=packet.getAttributeStaticStr("reSend")){
			/**
			 * 重发的消息不处理
			 */
			return false;
		}
		//logger.info("message from {} ",packet.getStanzaFrom());
		if(null!=session.getjid()&&null!=packet.getStanzaFrom()&&!packet.getFrom().getBareJID().equals(session.getjid().getBareJID()))
			return false;
		String body=packet.getElement().getChildCData(Message.MESSAGE_BODY_PATH);
		
		body = body.replaceAll("&quot;", "\"").replaceAll("&quot;", "\"");
		JSONObject bodyObj=null;
		try {
			bodyObj=JSON.parseObject(body);
			//String mac = bodyObj.getString("mac");
			String	mac=(String) bodyObj.remove("mac");
			if(StringUtil.isEmpty(mac))
				return false;
			String serverMac = ParamsSign.joinObjectValues(bodyObj.getInnerMap());
			if(!Arrays.equals(Base64.decode(mac), MAC.encode(serverMac.getBytes(),Base64.decode(messageKey)))){
				logger.error("clientMac {}   serverMac {}",mac,serverMac);
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}finally {
			//logger.info(" processIncoming time count {}",System.currentTimeMillis()-startTime);
		}
		
	}



	@Override
	public String id() {
		// TODO Auto-generated method stub
		return ID;
	}

	@Override
	public String[][] supElementNamePaths() {
		return ELEMENT_PATHS;
	}

	@Override
	public String[] supNamespaces() {
		return XMLNSS;
	}

	@Override
	public Set<StanzaType> supTypes() {
		return TYPES;
	}



	@Override
	public void process(Packet packet, XMPPResourceConnection session, NonAuthUserRepository repo,
			Queue<Packet> results, Map<String, Object> settings) throws XMPPException {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
	
	
	
	

}
