package app.notify;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;

import com.fasterxml.jackson.databind.ObjectMapper;

import aphttp.ApHttp;
import cmd.thread.Daemon;
import cmd.thread.LambdaThread;
import kv.str.StrMap;

public class Notify {
	public ConnNotifyDB conn;
	ObjectMapper mapper;


	public Notify() {
		conn = new ConnNotifyDB();
		mapper = new ObjectMapper();

	}
	
	public void handleNotify(String code) throws ClientProtocolException, IOException, SQLException {
		StrMap map = StrMap.ofOrder();
		ObjectMapper mapper = new ObjectMapper();

		map.put("grant_type", "authorization_code");
		map.put("code", code);
		map.put("redirect_uri", "https://linebot.prj.tw/callback");
		map.put("client_id", "0zC41rt83YKO78j86U3UHN");
		map.put("client_secret", "1dSAkg8J1uyroNMFebxphLKaUVcrvpmPiaqG2LAxdP1");

//		String str = map.kvLst().join("&", kv -> kv.k + "=" + kv.v);
//		Request req = ApHttp.ofPostRequest2("https://notify-bot.line.me/oauth/token", str);	
		
		Request req = ApHttp.ofPostRequest("https://notify-bot.line.me/oauth/token", map);	
		String resp =req.execute().returnContent().asString();
		OauthToken ot = mapper.readValue(resp, OauthToken.class);	
		System.out.println(ot.access_token);
		//拿到token先get 用戶資料
		Request getMsg = ApHttp.ofGetRequest("https://notify-api.line.me/api/status");
		getMsg.addHeader("Authorization",
				"Bearer " + ot.access_token);
		String response = getMsg.execute().returnContent().asString();
		ClientInfo info = mapper.readValue(response, ClientInfo.class);	

		conn.insert(ot.access_token, info.targetType, info.target);
		
		// 傳第一篇推送給訂閱用戶
		StrMap helloMsg = StrMap.ofOrder();
		helloMsg.put("message", "\n歡迎訂閱above notify,\n通過QRcode可以加入line bot好友\n若要取消notify訂閱請點擊下面鏈接:\nhttps://notify-bot.line.me/my/");
		helloMsg.put("imageThumbnail", "https://linebot.prj.tw/static/img/linebot.jpg");
		helloMsg.put("imageFullsize", "https://linebot.prj.tw/static/img/linebot.jpg");
		Request replyMsg = ApHttp.ofPostRequest("https://notify-api.line.me/api/notify", helloMsg);
		replyMsg.addHeader("Authorization",
				"Bearer " + ot.access_token);
		replyMsg.execute();
	}
	
	public void pushMsg(String msg ,String imageThumbnailUrl, String imageFullsizeUrl, String stickerPackageId, String stickerId) throws SQLException, ClientProtocolException, IOException{
		
		StrMap msgMap = StrMap.ofOrder();
		if(!(msg == null)) {
			// text message is required
			msgMap.put("message", msg);

			if(!(imageThumbnailUrl.trim().length() == 0) && !(imageFullsizeUrl.trim().length() == 0)) {
				msgMap.put("imageThumbnail", imageThumbnailUrl);
				msgMap.put("imageFullsize", imageFullsizeUrl);
			}
			
			if(!(stickerPackageId.trim().length() == 0) && !(stickerId.trim().length() == 0)) {
				msgMap.put("stickerPackageId", stickerPackageId);
				msgMap.put("stickerId", stickerId);
			}

			Set<String> token = conn.getToken();
			Iterator<String> it = token.iterator();
			while(it.hasNext()) {
				Request replyMsg = ApHttp.ofPostRequest("https://notify-api.line.me/api/notify", msgMap);
				replyMsg.addHeader("Authorization",
						"Bearer " + it.next());
				replyMsg.execute();
			}
		}
	}
	

}
