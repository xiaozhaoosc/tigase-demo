package tigase.shiku;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shiku.utils.StringUtil;

import tigase.auth.callbacks.VerifyPasswordCallback;
import tigase.auth.impl.AuthRepoPlainCallbackHandler;
import tigase.db.TigaseDBException;
import tigase.db.UserNotFoundException;
import tigase.shiku.db.RedisService;
import tigase.shiku.db.UserDao;
import tigase.shiku.utils.LoginPassword;

/**
 * @author lidaye
 * 认证 redis user token  类
 *
 */
public class RedisCallbackHandler extends AuthRepoPlainCallbackHandler{

	/**
	 * 
	 */
	public RedisCallbackHandler() {
		
	}
	private  Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	@Override
	protected void handleVerifyPasswordCallback(VerifyPasswordCallback pc) throws IOException {
		if(UserDao.getInstance().queryShieldUserIds(Integer.valueOf(jid.getLocalpart()))){
			logger.info("用户已被系统封号 禁止登陆 ---> {}",jid.getLocalpart());
			pc.setVerified(false);
			return;
		}
		boolean flag = RedisService.getInstance().authUser(jid.getLocalpart(), pc.getPassword());
		pc.setVerified(flag);
//		logger.info("auth user getDomain {}，  pc.getPassword() {}",jid.getDomain(),pc.getPassword());
		if (!flag) {
			super.handleVerifyPasswordCallback(pc);
		}
		if(!pc.isVerified()) {
			try {
				String dbPwd=repo.getPassword(jid);
//				logger.info("handleVerifyPasswordCallback jid {}  dbPwd {},pwd : {}",jid,dbPwd,LoginPassword.encodeFromOldPassword(pc.getPassword()));
				if(StringUtil.isEmpty(pc.getPassword())) {
					pc.setVerified(false);
				}else if(!pc.getPassword().equals(dbPwd)) {
					String password = LoginPassword.encodeFromOldPassword(pc.getPassword());
					pc.setVerified(password.equals(dbPwd));
				}else {
					pc.setVerified(true);
				}
				
			} catch (UserNotFoundException e) {
				logger.error(e.toString(), e);
			} catch (TigaseDBException e) {
				logger.error(e.toString(), e);
			}
			
		}
	}
}
