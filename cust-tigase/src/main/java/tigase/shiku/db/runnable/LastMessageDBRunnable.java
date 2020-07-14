package tigase.shiku.db.runnable;
import java.util.Iterator;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.shiku.commons.thread.pool.AbstractMapRunnable;

import tigase.shiku.model.LastChatModel;

/**
 * @author lidaye
 *
 */
public class LastMessageDBRunnable extends AbstractMapRunnable<LastChatModel>{

	private DBCollection dbCollection;
	
	private static Logger log = LoggerFactory.getLogger(LastMessageDBRunnable.class.getName());
	/**
	 * @param executor
	 */
	public LastMessageDBRunnable(DBCollection dbCollection) {
		this.dbCollection=dbCollection;
	}

	public void putLastChat(LastChatModel lastChat){
		if(1==lastChat.getIsRoom()) {
			addMsg(lastChat.getJid(), lastChat);
		}else  {
			addMsg(lastChat.getUserId()+"_"+lastChat.getJid(), lastChat);
		}
	}

	/* (non-Javadoc)
	 * @see tigase.shiku.pool.AbstractSynRunnable#runTask()
	 */
	@Override
	public void runTask() {
		try {
			Iterator<Entry<String, LastChatModel>> iterator=null;
			Entry<String, LastChatModel> entry=null;
			
			iterator = maps.entrySet().iterator();
			while (iterator.hasNext()) {
				 entry =iterator.next();
				 refreshLastChat(entry.getValue());
				 iterator.remove();
			}
			
		} catch (Exception e) {
			log.error(e.toString(), e);
		}finally {
			
		}
		
	}
	public void refreshLastChat(LastChatModel model) {
		if(null==model.getJid())
			return;
		JSONObject bodyJson = bodyToJson(model.getBody());
		// 过滤群聊领取红包消息
		if(83 == model.getType()){
			Object object = bodyJson.get("objectId");
			if(null != object)
				return;
		}
		BasicDBObject query=new BasicDBObject("jid", model.getJid());
		
		BasicDBObject values=new BasicDBObject("type", model.getType());
		values.append("messageId", model.getMessageId());
		values.append("timeSend", model.getTimeSend());
		values.append("content", model.getContent());
		
		values.append("from", model.getUserId());
		
		values.append("fromUserName", bodyJson.get("fromUserName"));
		values.append("to", bodyJson.get("toUserId"));
		values.append("toUserName", (null == bodyJson.get("toUserName") ? "" : bodyJson.get("toUserName")));
		
		values.append("userId", model.getUserId());
		values.append("jid", model.getJid());
		values.append("isRoom", model.getIsRoom());
		values.append("isEncrypt",model.getIsEncrypt());
		// 端到端消息验签
		if(3 == model.getIsEncrypt()){
			values.append("signature", bodyJson.get("signature"));
		}else{
			values.append("signature", "");
		}
		if(1!=model.getIsRoom()){
			query.append("userId", model.getUserId());
		}
		// 过滤阅后即焚消息截屏自己收到的消息
		if(95!=model.getType())
			dbCollection.update(query, new BasicDBObject("$set", values), true, false);
		
		if(1==model.getIsRoom()||model.getUserId().equals(model.getJid()))
			return;
		if(1!=model.getIsRoom()){
			query.replace("userId", model.getJid());
			query.replace("jid", model.getUserId());
			
			values.replace("userId", model.getJid());
			values.replace("jid", model.getUserId());
		}
		dbCollection.update(query, new BasicDBObject("$set", values), true, false);
	}

	// 格式化
	private JSONObject bodyToJson(String body){
		JSONObject jsonObj=null;
		try {
			jsonObj= JSON.parseObject(body.replaceAll("&quot;", "\"").replaceAll("&quot;", "\""));
			return jsonObj;
		} catch (Exception e) {
			return null;
		}
	}
}
