package http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class HttpTest {
    public static void main(String[] args) {

         /**

            * 调用 tigase http rest api 不注意字符编码 会导致收到的中文消息 出现乱码

            */

         try{

            HttpClient client = new DefaultHttpClient();

             HttpPost post = new HttpPost("http://localhost:9090/rest/stream/10000@im.shiku.co?api-key=666666");



         String sendString ="<message xmlns=\"jabber:client\" type=\"chat\" from=\"admin@im.shiu.co\"  to=\"id1111@im.shiku.co\"><body>{\"type\":1,\"msgid\":\"dsf\",\"content\":\"真爽啊！！！！！\"} </body></message>";

          System.out.println(sendString);

           //这里是关键

          StringEntity entity = new StringEntity(sendString,"utf-8");

           //tigase rest api 必须要设置  

                    entity.setContentType("application/xml");

         //tigase rest api 必须要设置  

                    entity.setContentEncoding("utf-8");

                    post.setEntity(entity);



                    HttpResponse response=client.execute(post);

                    int resCode=response.getStatusLine().getStatusCode();

                    String resStr = response.getStatusLine().getReasonPhrase();

                    System.out.println(resCode+"  "+resStr);

                    HttpEntity entitys = response.getEntity();



                    System.out.println(EntityUtils.toString(entitys));


         } catch( Exception e){

         e.printStackTrace();

         }
    }
}
