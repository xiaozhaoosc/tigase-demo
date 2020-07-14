package tigase.shiku;

import tigase.component.exceptions.RepositoryException;
import tigase.muc.exceptions.MUCException;
import tigase.muc.modules.PresenceModule;
import tigase.util.TigaseStringprepException;
import tigase.xml.Element;
import tigase.xmpp.BareJID;
import tigase.xmpp.JID;

import java.util.Date;

public interface SKPresenceModule extends PresenceModule {

    /**
     * 自动加入群组接口
     * @param roomJID
     * @param roomCreated
     * @param element
     * @param senderJID
     * @param nickname
     */
    public void processEntering(BareJID roomJID, boolean roomCreated, Element element, JID senderJID, String nickname) throws MUCException, TigaseStringprepException;


    /**
     * 拉取群组历史消息接口
     * @param roomJID
     * @param senderJID
     * @param maxchars
     * @param maxstanzas
     * @param seconds
     * @param since
     * @throws MUCException
     * @throws RepositoryException
     */
    public void sendHistoryToUser(BareJID roomJID, JID senderJID, Integer maxchars,
                                  Integer maxstanzas, Integer seconds, Date since) throws MUCException, RepositoryException;
}
