package tigase.shiku;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.shiku.commons.thread.Callback;
import com.shiku.commons.thread.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.shiku.utils.StringUtil;

import tigase.server.ConnectionManager;
import tigase.server.Iq;
import tigase.server.Message;
import tigase.server.Packet;
import tigase.server.xmppclient.XMPPIOProcessor;
import tigase.stats.StatisticsList;
import tigase.util.TimerTask;
import tigase.xml.Element;
import tigase.xmpp.StanzaType;
import tigase.xmpp.StreamError;
import tigase.xmpp.XMPPIOService;
import tigase.xmpp.impl.C2SDeliveryErrorProcessor;

/**
 * @author lidaye
 *
 */
public class ShikuAckProcessor implements XMPPIOProcessor{

	private  Logger logger = LoggerFactory.getLogger(ShikuAckProcessor.class.getName());
	
	private static final String ID = "shiku-ack-processor";
	/**
	 * s2c  消息送达机制  的命名空间
	 */
	public static final String XMLNS = "xmpp:shiku:ack";
	
	private static final String ACK_NAME = "skack";
	/**
	 * 开启 送达机制的 标签
	 */
	private static final String ENABLE="enable";
	/**
	 * 等待回执的包 key
	 */
	private static final String OUT_WAIT_KEY = "waitack_out";

	/**
	 * 等待回执 任务
	 */
	private static final String OUT_WAIT_TASK = "waitack_task";
	
	public static final String[] ACK_BODY = {Iq.ELEM_NAME, "body" };

	public static final String[] ACK_ENABLE = {Iq.ELEM_NAME, ENABLE};

	final List<Integer> FilterType=Arrays.asList(200,201,123,135);

	/**
	 * 回执超时时间
	 */
	private static final long ACKTIMEOUT=10*1000;


	private static final int RESEND_COUNT=5;

	//protected ConcurrentHashMap<String,Entry> messageMap = new ConcurrentHashMap<String,Entry>();
	private ConnectionManager connectionManager;
	
	//private ScheduledExecutorService rxecutorScheduler;
	/**
	 * 
	 */
	public ShikuAckProcessor() {
		logger.info("ShikuAckProcessor init ----");
		//rxecutorScheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

	}
	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return ID;
	}

	@Override
	public Element[] supStreamFeatures(XMPPIOService service) {
		// TODO Auto-generated method stub
		return null;
	}

	
	/**
	 * <iq from="10005629@im.shiku.co/web" to="im.shiku.co" type="set" >
	 *    <enable xmlns="xmpp:shiku:ack">
	 * 	    enable
	 *    </enable>
	 * </iq>
	 */
	@Override
	public boolean processIncoming(XMPPIOService service, Packet packet) {
		
			if (Iq.ELEM_NAME!=packet.getElemName()) {
				return false;
			}else if(!isEnabled(service)&&null!=packet.getElement().getChild(ENABLE)){
				if(!XMLNS.equals(packet.getElement().getChild(ENABLE).getXMLNS()))
					return false;
				/**
				 * 客户端 没有开启 送达机制的情况
				 */
				try {
					if(null!=packet.getElemCDataStaticStr(ACK_ENABLE)){
						/**
						 * 客户端启用送达机制  标识
						 */
						ConcurrentHashMap<String,Entry> messageMap=new ConcurrentHashMap<String, Entry>();
						service.getSessionData().put(OUT_WAIT_KEY, messageMap);
						
						SkAckTimeoutTask ackTimeoutTask=new SkAckTimeoutTask(service);

						connectionManager.addTimerTask(ackTimeoutTask,5000,5000);

						//rxecutorScheduler.scheduleAtFixedRate(ackTimeoutTask, 5, 5, TimeUnit.SECONDS);

						service.getSessionData().put(OUT_WAIT_TASK, ackTimeoutTask);
						service.addPacketToSend(packet.copyElementOnly());
					}
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
				return true;
			}else if(null!=packet.getElement().getChild("body")) {
				if(!XMLNS.equals(packet.getElement().getChild("body").getXMLNS())) {
					return false;
				}
				try {
					String packetIds = packet.getElemCDataStaticStr(ACK_BODY);
					if(StringUtil.isEmpty(packetIds))
						return true;
					Object map = service.getSessionData().get(OUT_WAIT_KEY);
					if(null==map) {
						return true;
					}
					ConcurrentHashMap<String,Entry> messageMap=(ConcurrentHashMap<String, Entry>) map;
					
					Arrays.stream(packetIds.split(",")).filter(a ->!StringUtil.isEmpty(a))
					.forEach(msgId->{
						messageMap.remove(msgId);
					});;
				/*
				 * for (String msgId : split) { if(!StringUtil.isEmpty(msgId))
				 * messageMap.remove(msgId); }
				 */
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
				return true;
		}
		return false;
		
	}
	public static boolean isEnabled(XMPPIOService service) {
		return service.getSessionData().containsKey(OUT_WAIT_KEY);
	}
	
	/**
	 * <iq from="10005629@im.shiku.co/web" to="im.shiku.co" type="set" >
	 *    <body xmlns="xmpp:shiku:ack">
	 * 	    messagid,messagid1,
	 *    </body>
	 * </iq>
	 */
	@Override
	public boolean processOutgoing(XMPPIOService service, Packet packet) {
		if(!isEnabled(service)){
			return false;
		}else if(Message.ELEM_NAME!=packet.getElemName()||null==packet.getStanzaId())
			return false;
		else if(StanzaType.chat!=packet.getType()&&StanzaType.groupchat!=packet.getType()) {
			return false;
		}else if(null==(packet.getElement().findChildStaticStr(
				Message.MESSAGE_BODY_PATH))) {
			return false;
			
			// ignoring packets resent from c2s for redelivery as processing
			// them would create unnecessary duplication of messages in
			// archive
		}else if (C2SDeliveryErrorProcessor.isDeliveryError(packet)){
			return false;
		}



		if(StanzaType.chat==packet.getType()) {
			String body = packet.getElement().getChildCData(new String[] { "message", "body" });
			if(null == body)
				return false;
			if(null!=body){
				try {
					JSONObject bodyObj= JSON.parseObject(body.replaceAll("&quot;", "\"").replaceAll("&quot;", "\""));
					int type = bodyObj.getIntValue("type");
					if(FilterType.contains(type)) {
						return false;
					}
				} catch (Exception e) {
					return false;
				}
			}
		 }


			try {
				//synchronized (service) {
					/**
					 * 取出等待回执的  消息 map
					 */
					ConcurrentHashMap<String, Entry> messageMap = (ConcurrentHashMap<String, Entry>) service.getSessionData().get(OUT_WAIT_KEY);
					Entry entry = messageMap.get(packet.getStanzaId());
					if (null == entry) {
						entry = new Entry(packet);
						messageMap.put(packet.getStanzaId(), entry);
					} else {
						entry.addReCount();
					}
				//}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);

			}

		 
		
		return false;
	}

	@Override
	public void packetsSent(XMPPIOService service) throws IOException {
		
		
	}

	@Override
	public void processCommand(XMPPIOService service, Packet packet) {
		//logger.log(Level.INFO,"processCommand ===> {0}",packet.getStanzaId());
	}

	@Override
	public boolean serviceStopped(XMPPIOService service, boolean streamClosed) {
		if(!service.isConnected()){
			return false;
		}
		Object map = service.getSessionData().get(OUT_WAIT_KEY);
		if(null!=map) {
			//logger.warn("{} serviceStopped start {}",service.getUserJid(),service.isConnected());
			 ConcurrentHashMap<String,Entry> messageMap=(ConcurrentHashMap<String, Entry>) map;
           	//logger.warn("{} serviceStopped messageMap is null {}",service.getUserJid(),messageMap.isEmpty());
			if(!messageMap.isEmpty()){
				/**
				 * 这里需要创建一个新的map对象
				 */
				final HashMap<String,Entry> sendMsgMap=new HashMap(messageMap);
				/**
				 * 这里需要异步延时执行 不然可能重发的时候链接还没有被断开
				 */
				ThreadUtils.executeInThread(obj -> {
					//logger.warn("{} serviceStopped start Thread messageMap null {}",service.getUserJid(),messageMap.isEmpty());
					//logger.warn("{} serviceStopped start Thread sendMsgMap null {}",service.getUserJid(),sendMsgMap.isEmpty());
					sendMsgMap.values().forEach(entry->{
						String body = entry.getPacket().getElement().getChildCData(new String[] { "message", "body" });
						JSONObject bodyObj= JSON.parseObject(body.replaceAll("&quot;", "\"").replaceAll("&quot;", "\""));
						//logger.warn("{} serviceStopped addPacket  {}  {}",service.getUserJid(),bodyObj.getString("content"),entry.getPacket().getStanzaId());
						connectionManager.addPacket(entry.getPacket().copyElementOnly());

					});
					//messageMap.clear();
				},3);
			}


		}
		/*SkAckTimeoutTask ackTimeoutTask = (SkAckTimeoutTask) service.getSessionData().get(OUT_WAIT_TASK);
		if(null!=ackTimeoutTask) {
			if(null!=map){
				ThreadUtils.executeInThread(obj -> {
					ackTimeoutTask.cancel();
				},2);
			}else {
				ackTimeoutTask.cancel();
			}
		}*/

		service.getSessionData().remove(OUT_WAIT_TASK);
		return false;
	}

	@Override
	public void setConnectionManager(ConnectionManager connectionManager) {
		this.connectionManager=connectionManager;
	}

	@Override
	public void setProperties(Map<String, Object> props) {
		
	}

	@Override
	public void streamError(XMPPIOService service, StreamError streamError) {
		
		
	}
	@Override
	public void getStatistics(StatisticsList list) {
		// TODO Auto-generated method stub
		
	}
	
	
	/**
	 * ResumptionTimeoutTask class is used for handing of timeout used during 
	 * session resumption
	 */
	private class SkAckTimeoutTask extends TimerTask {

		private final XMPPIOService service;
		
		public SkAckTimeoutTask(XMPPIOService service) {
			this.service = service;
		}
		
		@Override
		public void run() {	
			Object map = service.getSessionData().get(OUT_WAIT_KEY);
			
			if(null!=map) {
				ConcurrentHashMap<String,Entry> messageMap=(ConcurrentHashMap<String, Entry>) map;
				if(messageMap.isEmpty()){
					return;
				}
				if(service.isConnected()) {
					long cuTim=System.currentTimeMillis();
					Iterator<Entry> iterator = messageMap.values().iterator();
					Entry entry=null;
					while (iterator.hasNext()) {
						 entry=iterator.next();
						 if(cuTim-entry.getStamp()>ACKTIMEOUT) {
								if(RESEND_COUNT>entry.getReCount()) {
									logger.warn(" {}  未收到回执  重发   》 {}",entry.getPacket().getStanzaTo(),
											entry.getPacket().getStanzaId());
									service.addPacketToSend(entry.getPacket().copyElementOnly());
								}else {
									logger.warn(" {} {}次未收到回执 执行离线操作 》 {}",entry.getPacket().getStanzaTo(),RESEND_COUNT,
											entry.getPacket().getStanzaId());
									iterator.remove();
									service.forceStop();
								}
							}
					}
					
				}else {
					messageMap.values().stream().forEach((entry)->{
					    connectionManager.writePacketToSocket(service,entry.getPacket().copyElementOnly());
					});
					messageMap.clear();
				}
			}
		}
		
	}
		
	private class Entry {
		private  Packet packet;
		private  long stamp = System.currentTimeMillis();
		
		private int reCount=0;
		/**
		 * @return the stamp
		 */
		public long getStamp() {
			return stamp;
		}
		public void refreshStamp() {
			this.stamp = System.currentTimeMillis();
		}
		
		public void addReCount() {
			this.reCount++;
			this.refreshStamp();
		}
		/**
		 * @return the reCount
		 */
		public int getReCount() {
			return reCount;
		}
		
		/**
		 * @return the packet
		 */
		public Packet getPacket() {
			return packet;
		}
		public Entry(Packet packet) {
			this.packet = packet.copyElementOnly();
			this.packet.getElement().addAttribute("reSend", "true");;
		}
	}

	

}
