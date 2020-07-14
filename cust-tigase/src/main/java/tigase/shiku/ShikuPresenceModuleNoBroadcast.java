package tigase.shiku;

import tigase.component.exceptions.RepositoryException;
import tigase.criteria.Criteria;
import tigase.criteria.ElementCriteria;
import tigase.muc.Affiliation;
import tigase.muc.Role;
import tigase.muc.Room;
import tigase.muc.exceptions.MUCException;
import tigase.muc.history.HistoryProvider;
import tigase.muc.modules.PresenceModuleImpl;
import tigase.muc.modules.PresenceModuleNoBroadcast;
import tigase.util.TigaseStringprepException;
import tigase.xml.Element;
import tigase.xmpp.BareJID;
import tigase.xmpp.JID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 群组成员状态 不广播
 * @author lidaye
 */
public class ShikuPresenceModuleNoBroadcast extends PresenceModuleNoBroadcast implements SKPresenceModule {

	public ShikuPresenceModuleNoBroadcast() {
		log.info("ShikuPresenceModuleNoBroadcast init------");
	}

	protected static final Logger log = Logger.getLogger(ShikuPresenceModuleNoBroadcast.class.getName());

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
