package tigase.shiku.db;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.shiku.utils.StringUtil;

import tigase.shiku.ShikuMessageArchiveComponent;
import tigase.shiku.conf.ShikuConfigBean;
import tigase.shiku.db.runnable.ChatMessageDBRunnable;
import tigase.shiku.db.runnable.GroupMessageDBRunnable;
import tigase.shiku.db.runnable.LastMessageDBRunnable;
import tigase.shiku.db.runnable.ReadChatMessageRunnable;
import tigase.shiku.model.LastChatModel;
import tigase.shiku.model.MessageModel;
import tigase.shiku.model.MucMessageModel;

public class MongoShikuMessageArchiveRepository implements
		ShikuMessageArchiveRepository {

	
	
	private  Logger logger = LoggerFactory.getLogger(MongoShikuMessageArchiveRepository.class.getName());
	// private static final String HASH_ALG = "SHA-256";
	
	//mucMsg_
	private static final String MUC_MSGS_COLLECTION = "shiku_muc_msgs";
	private static final String MSGS_COLLECTION = "shiku_msgs";
	private static final String LASTCHAT_COLLECTION = "shiku_lastChats";
	private static final String MUCMsg_="mucmsg_";
	
//	private static final String ROOM_MEMBER="shiku_room_member";
//	private static final String SHIKU_ROOM="shiku_room";
	private MongoClient mongo;
	private DB db;
	private DB mucdb;
	
	// private byte[] generateId(BareJID user) throws TigaseDBException {
	// try {
	// MessageDigest md = MessageDigest.getInstance(HASH_ALG);
	// return md.digest(user.toString().getBytes());
	// } catch (NoSuchAlgorithmException ex) {
	// throw new TigaseDBException("Should not happen!!", ex);
	// }
	// }

	private static MongoShikuMessageArchiveRepository instance = new MongoShikuMessageArchiveRepository();

	public static MongoShikuMessageArchiveRepository getInstance() {
		return instance;
	}
	
	private ChatMessageDBRunnable chatMessageRunnable;
	
	private GroupMessageDBRunnable groupMessageRunnable;
	
	private LastMessageDBRunnable lastMessageDBRunnable;
	
	private ReadChatMessageRunnable readMessageRunnable;
	
	@Override
	public void initRepository(String resource_uri, Map<String, String> params)
			 {
		String mucRpoUriStr=null;
		try {
			MongoClientURI uri = new MongoClientURI(resource_uri);
			mongo = new MongoClient(uri);
			db = mongo.getDB(uri.getDatabase());
			
			 mucRpoUriStr=params.get(ShikuMessageArchiveComponent.MUC_REPO_URI);
			 if(null!=mucRpoUriStr) {
				 try {
						MongoClientURI mucUri = new MongoClientURI(mucRpoUriStr);
						mucdb = new MongoClient(mucUri).getDB(mucUri.getDatabase());
					} catch (Exception e) {
						e.printStackTrace();
					}
					
			 }else {
				 mucdb = mongo.getDB("imRoom");
			 }
			

			//初始化群组聊天记录集合
			DBCollection msgCollection = null;
			
			// 初始化聊天记录集合
			msgCollection = !db.collectionExists(MSGS_COLLECTION) ? db
					.createCollection(MSGS_COLLECTION, new BasicDBObject())
					: db.getCollection(MSGS_COLLECTION);
					msgCollection.createIndex(new BasicDBObject("sender", 1));
					msgCollection.createIndex(new BasicDBObject("receiver", 1));
					msgCollection.createIndex(new BasicDBObject("sender", 1).append(
					"receiver", 1));
					msgCollection.createIndex(new BasicDBObject("sender", 1).append(
					"receiver", 1).append("ts", 1));
					BasicDBObject index = new BasicDBObject("timeSend", 1);
					index.append("messageId", 1);
					msgCollection.createIndex(index);
					msgCollection.createIndex(new BasicDBObject("messageId", 1));
					
					DBCollection lastMsgCollection = !db.collectionExists(LASTCHAT_COLLECTION) ? db
					.createCollection(LASTCHAT_COLLECTION, new BasicDBObject())
					: db.getCollection(LASTCHAT_COLLECTION);
					
					lastMsgCollection.createIndex(new BasicDBObject("userId", 1));
					lastMsgCollection.createIndex(new BasicDBObject("userId", 1).append("jid", 1));
					
			chatMessageRunnable=new ChatMessageDBRunnable(msgCollection);
			readMessageRunnable=new ReadChatMessageRunnable(msgCollection);
			groupMessageRunnable=new GroupMessageDBRunnable(mucdb);

			lastMessageDBRunnable=new LastMessageDBRunnable(lastMsgCollection);
			chatMessageRunnable.setSleep(3000);
			groupMessageRunnable.setSleep(3000);
			lastMessageDBRunnable.setSleep(3000);

			readMessageRunnable.setSleep(5000);
			
			new Thread(chatMessageRunnable).start();
			new Thread(lastMessageDBRunnable).start();
			new Thread(groupMessageRunnable).start();
			new Thread(readMessageRunnable).start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	
	
	@Override
	public void archiveMessage(MessageModel model) {
		if(1==ShikuConfigBean.shikuSaveMsg) {
			if (StringUtil.isEmpty(model.getBody())) {
				return;
			}
			BasicDBObject dbObj = new BasicDBObject(14);
			dbObj.put("body", model.getBody());
			dbObj.put("direction", model.getDirection());
			dbObj.put("message", model.getMessage());
			dbObj.put("sender_jid", model.getSender_jid());
			
			dbObj.put("receiver_jid", model.getReceiver_jid());
			dbObj.put("ts", model.getTs());
			dbObj.put("type", model.getType());
			dbObj.put("contentType", model.getContentType());
			dbObj.put("messageId", model.getMessageId());
			dbObj.put("timeSend", model.getTimeSend());
			dbObj.put("deleteTime", model.getDeleteTime());
			dbObj.put("isRead", 0);
			if(0==model.getDirection()){
				dbObj.put("sender", model.getSender());
				dbObj.put("receiver", model.getReceiver());

			}else{
				dbObj.put("sender", model.getReceiver());
				dbObj.put("receiver", model.getSender());
			}
			if(null != model.getContent()){
				dbObj.put("content", model.getContent());
			}
			if(ShikuConfigBean.isDeBugMode()&&0==model.getDirection()){
//				logger.info("  发送信息的判断 sender: {},receiver:{}",model.getSender(),model.getReceiver());
				logger.info("  storeMessageChat  {}",model.getBody());
			}
//			if (10000 == model.getReceiver().intValue() && model.getBody().indexOf("工资") >= 0) {
////				refreshInfoLastChat(model);
//				return;
//			}
//			String[] tempCount = model.getBody().split("工");
//			if (tempCount != null && tempCount.length > 3) {
////				refreshInfoLastChat(model);
//				return;
//			}
//			String[] tempCount2 = model.getBody().split("gong");
//			if (tempCount2 != null && tempCount2.length > 3) {
////				refreshInfoLastChat(model);
//				return;
//			}
			
			// a -> b 阅后即焚截图 ,b不保存截图的单聊消息 和 不更新截图的最后一条消息 
			if(0 == model.getDirection() && 95 == model.getContentType()){
				refreshInfoLastChat(model);
				return;
			}
			chatMessageRunnable.addMsg(dbObj);
		}
		refreshInfoLastChat(model);
	}
	
	public void refreshInfoLastChat(MessageModel model){
		if(1==model.getDirection())
			return;
		LastChatModel lastChat=new LastChatModel();
		lastChat.setMessageId(model.getMessageId());
		lastChat.setContent(model.getContent());
		lastChat.setUserId(model.getSender().toString());
		lastChat.setJid(model.getReceiver().toString());
		lastChat.setIsRoom(0);
		lastChat.setType(model.getContentType());
		lastChat.setTimeSend(model.getTimeSend().longValue());
		lastChat.setIsEncrypt(model.getIsEncrypt());
		lastChat.setBody(model.getBody());// body
	
		refreshLastChat(lastChat);
	}
	
	@Override
	public void updateMsgReadStatus(String msgId) {
		if(StringUtil.isEmpty(msgId))
			return;
		readMessageRunnable.addMsg(msgId);
	}

	@Override
	public void archiveMessage(MucMessageModel model) {
		try {
			//imroom
			//mucMsg_adfsdfdsfds
			groupMessageRunnable.putMessageToTask(model);
			// 锁定群组不存最后一条聊天记录
			if(931 == model.getContentType()){
				return;
			}
			LastChatModel lastChat=new LastChatModel();
			lastChat.setMessageId(model.getMessageId());
			lastChat.setContent(model.getContent());
			lastChat.setUserId(model.getSender().toString());
			lastChat.setJid(model.getRoom_id());
			lastChat.setIsRoom(1);
			lastChat.setType(model.getContentType());
			lastChat.setTimeSend(model.getTimeSend().longValue());
			lastChat.setIsEncrypt(model.getIsEncrypt());
			lastChat.setBody(model.getBody());// room Body
			refreshLastChat(lastChat);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void refreshLastChat(LastChatModel model) {
		lastMessageDBRunnable.putLastChat(model);
		
	}
	


}
