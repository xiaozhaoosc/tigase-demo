package tigase.shiku;


import com.shiku.commons.thread.Callback;
import com.shiku.commons.thread.ThreadUtils;
import com.shiku.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tigase.cluster.SessionManagerClustered;
import tigase.shiku.conf.ShikuConfigBean;
import tigase.shiku.db.RedisService;
import tigase.shiku.db.UserDao;
import tigase.xml.Element;
import tigase.xmpp.BareJID;
import tigase.xmpp.JID;
import tigase.xmpp.XMPPResourceConnection;

import java.util.List;

public class ShikuSessionManagerClustered extends SessionManagerClustered {

	private  Logger logger = LoggerFactory.getLogger(ShikuSessionManagerClustered.class.getName());
	public ShikuSessionManagerClustered() {
		logger.info(" ShikuSessionManagerClustered init =========>");
	}
	
	@Override
	public void handleResourceBind(XMPPResourceConnection conn) {
		// TODO Auto-generated method stub
		/*
		 *   2018 8月 18 日 修改  xmmp 用户登陆修改 数据库用户 状态
		 * */

		Long userId = UserDao.getInstance().handleLogin(conn);
		
		super.handleResourceBind(conn);

		if(null != userId && ShikuConfigBean.ISOPENAUTOJOINROOM){
			ThreadUtils.executeInThread(new Callback() {

				@Override
				public void execute(Object obj) {
					autoJoinRoom(conn,userId.toString());
				}
			},1);
		}
		
	}
	
	@Override
	protected void closeConnection(XMPPResourceConnection connection, JID connectionId, String userId,
			boolean closeOnly) {
		// TODO Auto-generated method stub
		/**
		 * 2018 年 8月 18日 修改  xmpp 关闭 链接时 更新数据库用户状态
		 */
		if(null==connection) {
			connection=getResourceConnection(connectionId);
		}
		UserDao.getInstance().closeConnection(connection, userId);
		super.closeConnection(connection, connectionId, userId, closeOnly);
	}

	/**
	 * 自动加群
	 * @param conn
	 */
	private void autoJoinRoom(XMPPResourceConnection conn,String userId){
		try {
			if(null==ShikuConfigBean.getPresenceModule()){
				return;
			}
			List<String> roomJidList = RedisService.getInstance().queryUserRoomList(userId);

			for(String roomJid:roomJidList){

				Element presenceEl = new Element("presence", new String[] {"id","xmlns","from", "to" },
						new String[] {StringUtil.randomCode(),"jabber:client",conn.getjid().toString(), roomJid+"@muc."+conn.getDomainAsJID()+"/"+userId});
				String[] a = {"http://jabber.org/protocol/muc"};
				String[] x={"xmlns"};
				presenceEl.addChild(new Element("x", x, a));

				String[] xmlns = {"http://jabber.org/protocol/caps","tUtPo6aj3mjjQWfXQO8eNy8Pwm8=","sha-1","http://www.igniterealtime.org/projects/smack"};
				String[] names = {"xmlns","ver","hash","node"};
				presenceEl.addChild(new Element("c", names, xmlns));

				String[] hisname = {"seconds"};
				String[] hisvale = {"0"};
				presenceEl.getChild("x").addChild(new Element("history",hisname,hisvale));

				BareJID bareRoomJid = BareJID.bareJIDInstance(roomJid+"@muc."+conn.getDomainAsJID());
				JID userJid = JID.jidInstance(conn.getjid().toString());
				//logger.info("auto Join room  {}",bareRoomJid);
				//System.out.println("auto Join room  "+bareRoomJid);
				ShikuConfigBean.getPresenceModule().processEntering(bareRoomJid, false, presenceEl, userJid, userId);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
