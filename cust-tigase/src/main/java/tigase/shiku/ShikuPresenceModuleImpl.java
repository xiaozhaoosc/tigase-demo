package tigase.shiku;

import java.util.Date;
import java.util.logging.Level;

import tigase.component.exceptions.RepositoryException;
import tigase.muc.Room;
import tigase.muc.exceptions.MUCException;
import tigase.muc.history.HistoryProvider;
import tigase.muc.modules.PresenceModuleImpl;
import tigase.util.TigaseStringprepException;
import tigase.xml.Element;
import tigase.xmpp.BareJID;
import tigase.xmpp.JID;

/**
 * 
 * @Description: TODO(todo)
 * @author zhm
 * @date 2019年8月18日 上午10:05:04
 * @version V1.0
 */
public class ShikuPresenceModuleImpl extends PresenceModuleImpl implements SKPresenceModule{
	
	public ShikuPresenceModuleImpl() {
		System.out.println("ShikuPresenceModuleImpl init------");
	}
	
	// 加入房间
	@Override
	public void processEntering(BareJID roomJID, boolean roomCreated, Element element, JID senderJID, String nickname)
			throws MUCException, TigaseStringprepException {
		try {
			Room room = context.getMucRepository().getRoom(roomJID);
			if(null == room) {
				return;
			}
			super.processEntering(room, roomCreated, element, senderJID, nickname);;
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}
	
	// 发送消息
	@Override
	public void sendHistoryToUser(BareJID roomJID, JID senderJID, Integer maxchars,
			   Integer maxstanzas, Integer seconds, Date since) throws MUCException, RepositoryException {
		HistoryProvider historyProvider = context.getHistoryProvider();
		if (log.isLoggable(Level.FINEST)) {
			log.finest("Sending history to user using: " + historyProvider + " history provider");
		}
		Room room = context.getMucRepository().getRoom(roomJID);
			if(null == room){
				return;
			}
		
		if (historyProvider != null) {
			historyProvider.getHistoryMessages(room, senderJID, maxchars, maxstanzas, seconds, since,
								   context.getWriter());
		}
	}
	
}
