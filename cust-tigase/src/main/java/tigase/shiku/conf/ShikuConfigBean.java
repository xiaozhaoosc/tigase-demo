package tigase.shiku.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tigase.muc.MUCComponent;
import tigase.muc.modules.PresenceModuleImpl;
import tigase.server.xmppsession.SessionManager;
import tigase.shiku.SKPresenceModule;
import tigase.shiku.ShikuKeywordFilter;
import tigase.shiku.ShikuPresenceModuleImpl;
import tigase.shiku.db.UserDao;

/**
* @Description: TODO( 的自定义配置类)
* @author lidaye
* @date 2018年8月23日 
*/

public class ShikuConfigBean  {
	
	
	
	private static Logger logger = LoggerFactory.getLogger(ShikuConfigBean.class.getName());
	/**
	 * 开启关键词过滤的
	 */
	public static final String OPENKEYWORD_KEY="confirm-open-keyword";
	public static  int OPENKEYWORD_VAL=0;
	
	/**
	 * 用户 数据库配置
	 */
	public static final String APIDBURI_KEY="api-db-uri";
	public static String APIDBURI_VAL=null;
	
	
	/**
	 *  消息 归档 组建的 jid  配置 virtual-host
	 * 必须有值 切正确 不然  消息不能 存入数据库
	 */
	public static final String shikuArchiveJid_KEY="shiku-archive-jid";
	public static String shikuArchiveJid=null;
	
	
	/**
	 * 调试 模式
	 */
	public static final String shikuDeBug_KEY="shikuDeBug";
	public static int shikuDeBug=0;
	
	/**
	 * xmpp心跳值
	 */
	public static final String ShikuXmppTimeOut_KEY="watchdog_timeout";
	public static int shikuXmppTimeOut=180;
	
	/**
	 * 保存单聊聊天记录
	 */
	public static final String ShikuSaveMsg_KEY="shikuSaveMsg";
	public static int shikuSaveMsg=1;
	
	/**
	 * 保存群聊聊天记录
	 */
	public static final String ShikuSaveMucMsg_KEY="shikuSaveMucMsg";
	public static int shikuSaveMucMsg=1;

	public static final String ShikuSaveSystemMsg_KEY="isSaveSystemMsg";
	public static int isSaveSystemMsg=1;


	
	/**
	 * 强制同步消息发送时间
	 */
	public static final String ShikuMsgSendTime_KEY="shikuMsgSendTime";
	public static int shikuMsgSendTime=0;
	
	public static String VIRTHOSTS=null;
	
	public static final String push_mqAddr_Key="shikuPush_mqAddr";
	public static  String pushMqAddrVal="localhost:9876";
	
	public static final String push_mqProdGid_Key="shikuPush_mqProdGid";
	public static  String pushMqProdGidVal="GID_userStatus";
	
	public static final String push_mqAccesskey_Key="shikuPush_mqAccesskey";
	public static  String pushMqAccesskeyVal="LTAIhqj....";
	
	public static final String push_mqSecretkey_Key="shikuPush_mqSecretkey";
	public static  String pushMqSecretkeyVal="uQ4DmJa7ANI8avj7...";

	
	//链接 tigase 数据库的 Url
	public static final String USER_DB_URI_KEY = "user-db-uri";
	public static String USER_DB_URI=null;
	
	
	public static boolean OPEN_REDIS_AUTH=false;
	public static final String REDIS_URI_KEY = "redis-uri";
	public static  String REDIS_URI_VAL = "redis://47.101.168.153:6388";
	
	public static final String REDIS_DATABASE_KEY = "redis-database";
	public static  int REDIS_DATABASE_VAL = 0;
	
	public static final String REDIS_PASSWORD_KEY = "redis-password";
	public static  String REDIS_PASSWORD_VAL ="";
	
	public static final String REDIS_ISCLUSTER_KEY = "redis-isCluster";
	public static  boolean REDIS_ISCLUSTER_VAL =false;
	
	// 判断是否开启自动加群
	public static boolean ISOPENAUTOJOINROOM = false;
	
	public static void initConfig(){
		//java.util.logging.Logger.getLogger("org.mongodb.driver").setLevel(Level.SEVERE);
		 logger.info("ShikuConfigBean=============>");
		 String openWord = System.getProperty(OPENKEYWORD_KEY,"0");
		 OPENKEYWORD_VAL=Integer.valueOf(openWord);
		 APIDBURI_VAL=System.getProperty(APIDBURI_KEY, null);
		 
		 //redis 配置 
		 REDIS_URI_VAL=System.getProperty(REDIS_URI_KEY,REDIS_URI_VAL);
		 REDIS_DATABASE_VAL=Integer.valueOf(System.getProperty(REDIS_DATABASE_KEY, "0"));
		 REDIS_PASSWORD_VAL=System.getProperty(REDIS_PASSWORD_KEY,"");
		 REDIS_ISCLUSTER_VAL=Boolean.valueOf(System.getProperty(REDIS_ISCLUSTER_KEY,"false"));
		 
		 VIRTHOSTS=System.getProperty("virt-hosts", null);
		 shikuArchiveJid=System.getProperty(shikuArchiveJid_KEY,null);
		 
		 pushMqAddrVal=System.getProperty(push_mqAddr_Key,pushMqAddrVal);
		 pushMqProdGidVal=System.getProperty(push_mqProdGid_Key,pushMqProdGidVal);
		 pushMqAccesskeyVal=System.getProperty(push_mqAccesskey_Key,pushMqAccesskeyVal);
		 pushMqSecretkeyVal=System.getProperty(push_mqSecretkey_Key,pushMqSecretkeyVal);
		 
		 shikuXmppTimeOut=Integer.valueOf(System.getProperty(ShikuXmppTimeOut_KEY, "180"));
		 shikuSaveMsg=Integer.valueOf(System.getProperty(ShikuSaveMsg_KEY, "1"));
		 shikuSaveMucMsg=Integer.valueOf(System.getProperty(ShikuSaveMucMsg_KEY, "1"));

		 isSaveSystemMsg=Integer.valueOf(System.getProperty(ShikuSaveMucMsg_KEY, "1"));

		 shikuMsgSendTime=Integer.valueOf(System.getProperty(ShikuMsgSendTime_KEY, "0"));
		 
		 String debug=System.getProperty(shikuDeBug_KEY,"0");
		  shikuDeBug=Integer.valueOf(debug);
		  
		  logger.info("==ShikuConfigBean== shiku-archive-jid ===> " + shikuArchiveJid);
		  logger.info("==ShikuConfigBean== VIRTHOSTS ===> "+VIRTHOSTS);
		  logger.info("==ShikuConfigBean== confirm-open-keyword ===> "+openWord);
		  logger.info("==ShikuConfigBean== api-db-uri ===> "+APIDBURI_VAL);
		  if(ShikuConfigBean.OPEN_REDIS_AUTH) {
			  logger.info("==ShikuConfigBean== redis-uri ===> "+REDIS_URI_VAL);
			  logger.info("==ShikuConfigBean== redis-database ===> "+REDIS_DATABASE_VAL);
			  logger.info("==ShikuConfigBean== redis-password ===> "+REDIS_PASSWORD_VAL);
			  logger.info("==ShikuConfigBean== redis-isCluster ===> "+REDIS_ISCLUSTER_VAL);
		  }
		 
		  
		  logger.info("==ShikuConfigBean== push_mqAddr_Key ===> "+pushMqAddrVal);
		  logger.info("==ShikuConfigBean== push_mqProdGid_Key ===> "+pushMqProdGidVal);
		  logger.info("==ShikuConfigBean== push_mqAccesskey_Key ===> "+pushMqAccesskeyVal);
		  logger.info("==ShikuConfigBean== push_mqSecretkey_Key ===> "+pushMqSecretkeyVal);
		  
		  logger.info("==ShikuConfigBean== shikuDeBug ===> "+shikuDeBug);
		  logger.info("==ShikuConfigBean== shikuXmppTimeOut===> "+shikuXmppTimeOut);
		  logger.info("==ShikuConfigBean== shikuSaveMsg ===>"+shikuSaveMsg);
		  logger.info("==ShikuConfigBean== shikuSaveMucMsg ===>"+shikuSaveMucMsg);

		  logger.info("==ShikuConfigBean== isSaveSystemMsg ===>"+isSaveSystemMsg);

		  logger.info("==ShikuConfigBean== shikuMsgSendTime ===>"+shikuMsgSendTime);

		  logger.info("==ShikuConfigBean== auto join room ===> "+ISOPENAUTOJOINROOM);
		 if(null==APIDBURI_VAL){
			 logger.error("==请配置  api-db-uri 的数据库地址 ===> ");
		 }
		
		 if(1==OPENKEYWORD_VAL){
			  	long startTime = System.currentTimeMillis();
				
				new Thread(new Runnable() {
					@Override
					public void run() {
						ShikuKeywordFilter.keyWords = UserDao.getInstance().getAllKeyWord();

						long endTime=System.currentTimeMillis();
						logger.info("getALLKeyWord end: {}", (endTime-startTime));
					}
				}).start();
		}
		//UserDao.shieldUserIds=UserDao.getInstance().queryShieldUserIds();
		
		
	}
	
	public static SKPresenceModule presenceModule;
	
	public static MUCComponent mucComponent;
	
	public static boolean isDeBugMode(){
		return 1==shikuDeBug;
	}

	public static void setMucComponent(MUCComponent mucComponent) {
		// TODO Auto-generated method stub
		ShikuConfigBean.mucComponent = mucComponent;
	}
	public static MUCComponent getMucComponent() {
		return mucComponent;
	}

	public static void setPresenceModule(SKPresenceModule module) {
		// TODO Auto-generated method stub
		presenceModule = module;
	}

	public static SKPresenceModule getPresenceModule() {
		// TODO Auto-generated method stub
		return presenceModule;
	}
	
	
	

	
	
	
}

