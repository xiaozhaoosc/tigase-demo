package tigase.shiku.utils;

import com.shiku.utils.encrypt.AES;
import com.shiku.utils.encrypt.HEX;
import com.shiku.utils.encrypt.MD5;

public class LoginPassword {
	 public static byte[] encode(String payPassword) {
	        return MD5.encrypt(payPassword);
	    }

	    /**
	     * 在服务器保存的登录密码md5,
	     *
	     * @param key {@link LoginPassword#encode(String)}不可打印的登录密码,
	     */
	    public static String md5(byte[] key) {
	        return MD5.encryptHex(AES.encrypt(key, key));
	    }

	    public static String encodeMd5(String payPassword) {
	        return md5(encode(payPassword));
	    }

	    /**
	     * 通过老算法的密码计算新算法的密码，
	     *
	     * @param oldPassword 服务器数据库保存的老算法的登录密码，
	     * @return 新算法下的登录密码密文，
	     */
	    public static String encodeFromOldPassword(String oldPassword) {
	        return md5(HEX.decode(oldPassword));
	    }
	}