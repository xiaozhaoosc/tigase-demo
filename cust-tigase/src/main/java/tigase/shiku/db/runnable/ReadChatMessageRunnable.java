package tigase.shiku.db.runnable;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.shiku.commons.thread.pool.AbstractQueueRunnable;

/**
 * @author lidaye
 *
 */
public class ReadChatMessageRunnable extends AbstractQueueRunnable<String>{

	private DBCollection dbCollection;
	
	private static Logger log = LoggerFactory.getLogger(ReadChatMessageRunnable.class.getName());
	/**
	 * @param executor
	 */
	public ReadChatMessageRunnable(DBCollection dbCollection) {
		this.dbCollection=dbCollection;
	}

	
	
	/* (non-Javadoc)
	 * @see tigase.shiku.pool.AbstractSynRunnable#runTask()
	 */
	@Override
	public void runTask() {
		List<String> list=null;
		try {
			try {
				synchronized (msgQueue) {
					list =msgQueue.stream().collect(Collectors.toList());
					msgQueue.clear();
				}
				if(!list.isEmpty()) {
					BasicDBObject query = new BasicDBObject();
					query.append("messageId",new BasicDBObject("$in", list));
					BasicDBObject values = new BasicDBObject("$set",new BasicDBObject("isRead", 1));
					dbCollection.update(query, values, false, true);
				}
			} catch (Exception e) {
				log.error(e.toString(), e);
			}
		} catch (Exception e) {
			log.error(e.toString(), e);
		}finally {
			list=null;
		}
		
	}

}
