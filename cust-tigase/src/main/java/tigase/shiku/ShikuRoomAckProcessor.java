package tigase.shiku;

import com.shiku.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tigase.server.ConnectionManager;
import tigase.server.Iq;
import tigase.server.Packet;
import tigase.server.xmppclient.XMPPIOProcessor;
import tigase.shiku.conf.ShikuConfigBean;
import tigase.shiku.db.RedisService;
import tigase.stats.StatisticsList;
import tigase.xml.Element;
import tigase.xmpp.BareJID;
import tigase.xmpp.JID;
import tigase.xmpp.StreamError;
import tigase.xmpp.XMPPIOService;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
/**
 * 
 * @Description: TODO(自动加群后批量获取群离线消息)
 * @author zhm
 * @date 2019年9月16日 下午3:14:33
 * @version V1.0
 */
public class ShikuRoomAckProcessor implements XMPPIOProcessor{
	
	private  Logger logger = LoggerFactory.getLogger(ShikuRoomAckProcessor.class.getName());
	
	private static final String ID = "shiku-roomAck-processor";
	
	/**
	 * s2c  批量获取群离线消息  的命名空间
	 */
	public static final String XMLNS = "xmpp:shiku:roomAck";
	
	public static final String[] ROOMDATA = {Iq.ELEM_NAME, "body" };

	public static final String MUC_DOMAIN="@muc.";
	
	private ConnectionManager connectionManager;
	
	private ScheduledExecutorService rxecutorScheduler;
	
	public ShikuRoomAckProcessor() {
		logger.info("ShikuRoomAckProcessor init ----");
		rxecutorScheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
	}
	
	@Override
	public String getId() {
		return ID;
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
	public boolean processIncoming(XMPPIOService service, Packet packet) {
		if (Iq.ELEM_NAME!=packet.getElemName()) {
			return false;
		}else if(null!=packet.getElement().getChild("body")){
			if(!XMLNS.equals(packet.getElement().getChild("body").getXMLNS()))
				return false;
			if(null==ShikuConfigBean.getPresenceModule()){
				return false;
			}
			String senderJID = service.getUserJid();
			String roomdata = packet.getElemCDataStaticStr(ROOMDATA);
			if(StringUtil.isEmpty(roomdata)){
				return false;
			}
			String[] arr = roomdata.split("\\|");
			Integer maxchars = null;
			Integer maxstanzas = null;
			Integer seconds = null;
			String roomJid=null;
			for (String data : arr) {
				seconds = Integer.valueOf(data.split(",")[0]);
				if(seconds<=0){
					continue;
				}
				roomJid=data.split(",")[1];
				if(!RedisService.getInstance().existsUserRoomList(getUserId(service.getUserJid()),roomJid)){
					continue;
				}
				roomJid =roomJid+MUC_DOMAIN+packet.getStanzaTo();

				//logger.info("userId {} join roomHistory {} seconds {}",senderJID,roomJid,seconds);
				try {
					ShikuConfigBean.getPresenceModule().sendHistoryToUser(BareJID.bareJIDInstance(roomJid), JID.jidInstance(senderJID), maxchars, maxstanzas, seconds, null);
				} catch (Exception e) {
					logger.error(e.getMessage(),e);
				}

			}
		}
		
		return false;
	}
	private String getUserId(String userJid) {
		int index = userJid.indexOf("@");
		return userJid.substring(0, index);
	}

	@Override
	public boolean processOutgoing(XMPPIOService service, Packet packet) {
		// TODO Auto-generated method stub
		return false;
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
	public void setConnectionManager(ConnectionManager connectionManager) {
		
		this.connectionManager=connectionManager;
		
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
