package app.bot;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.ImagemapMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.VideoMessage;
import com.linecorp.bot.model.message.flex.component.Box;
import com.linecorp.bot.model.message.flex.component.Button;
import com.linecorp.bot.model.message.flex.component.FlexComponent;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.container.Carousel;
import com.linecorp.bot.model.message.flex.unit.FlexLayout;

import above.db.AbDbMgr;
import aphttp.ApHttp;
import app.bot.entity.ConnToMysql;
import app.bot.entity.MessageUtil;
import app.bot.entity.RespUserDesc;
import app.bot.reqbody.Events;
import app.bot.reqbody.Source;
import app.bot.reqbody.WebHookObject;
import app.bot.tricks.TRt;
import cube.webber.UseStat;
import dbmgr.EDb;
import kv.str.StrMap;
import lazy.Lzy;
import rdb.CacheTime;
import rdb.DbObjsRows0;
import rdb.intfs.ISql;
import twhist.TWQHist;
import webapp.WebAppObj;

/**
 * @author ting
 */
public class Bot extends WebAppObj<BotApp, BotWeb, Bot> {
	ConnToMysql connToMysql = new ConnToMysql();
	public final String idTable = connToMysql.table;
	//line  test bot 
//	public final String auth = "U/EbNr5NH08NNNkqD/GxuGrmBOuT+JH0+pJfJn95lb7KqOzAiZky4xooeuPZEWYcwaEhPUwU0UpQo88t5ih3LAsiGWuHsnAW6kkAJB3YHmy5GEeDcJw/rT9yWgSVjopJZ67qrQdiG16zszeNMyo881GUYhWQfeY8sLGRXgo3xvw=";
	
	//Line bot
	public final String auth = "AerX2Yf5xmrKswi9Bo5yg/X8wV05cfmgGGRa5IIy6W3ny3iXgFq2/RLPed7nqt2ebmEbjFuQ8pZZdJ/Yh1k+Toat66l8qq5b6qBxTy4iCsXUe4g1AXqA4JhovXEp6opK15SLGK8RCiB9O2GUbtLZ1gdB04t89/1O/w1cDnyilFU=";
	
	//line bot bot2 豹名
//	public final String auth = "P9sdkIpbJV1PN/ONEOTUP0cACX+L7VQLc1ip11qQ5J/1Y/u14qEHOLlYWS/wWRdj6JBKPo8WU8TKyxkx2JJdsmcBFxzPW2irDn9mE0nvctxzmR1Bxbmas+IhCVYeXW4JHczjNlxfPz3962TXpmwIkgdB04t89/1O/w1cDnyilFU=";
	
	MessageUtil MsgUtil = new MessageUtil();
	ObjectMapper mapper = new ObjectMapper();

	//String放id, Integer放是否被封鎖(被封鎖放1)
	//被用戶刪除時, 不直接刪掉此id是因為Line沒有刪除, 只有封鎖, 所以在這邊都先留著
	Map<String, Integer> userIdMap = new HashMap<>();
	Map<String, Integer> groupIdMap = new HashMap<>();
	
	//紀錄用戶有沒有完成 第一次追蹤清單(新手任務 完成放1)
	Map<String, Integer> userIsSubMap = new HashMap<>();
	
	//honset是抓個股指標的數據
	Lzy<AbDbMgr> app = Lzy.of(() -> AbDbMgr.of(EDb.app, "maria://appread:readonly@pknuc.prj.tw:29556/honset"));
	//twdb用來看用戶輸入的訊息是不是有效股號或股名
	Lzy<AbDbMgr> tw = Lzy.of(() -> AbDbMgr.of(EDb.tw, "maria://clodread:readonly@pzx.prj.tw:3308/twdb"));

	protected Bot(BotApp app) throws SQLException {
		super(app, BotWeb.class, UseStat.yes, _Bot.sm_BotPort, "/res/icon/hat.ico", _Bot.sm_fileCacheSize,
				_Bot.sm_botResourcePath);

		// true map放進user id
		// 程式一跑起來就先把 id 放到對應的 map
		// 這樣用戶傳訊息過來就不用一直去db檢查, 檢查map就好 
		connToMysql.id2Map(userIdMap, true);
		connToMysql.id2Map(groupIdMap, false);
		
		//將用戶是否完成第一次追蹤清單放到 userIsSubMap
		connToMysql.isSub2Map(userIsSubMap);
	}

	public void start() throws SQLException {
	}
	
	public void handleWebHookObj(WebHookObject whobj)
			throws SQLException, JsonParseException, JsonMappingException, IOException {
		
		//一個webhook 可能有不只一個events 
		while (whobj.events.size() > 0) {
			Events event = whobj.events.get(0);
			String type = event.type;
			Source source = event.source;
			
			// 所有的event 都會有userId 
			String userId = source.userId;

			// Message Event
			if (type.equals("message")) {
				// 檢查userId 有沒有在idMap裡面
				chkdbId(userId, "user");
				if (source.type.equals("group")) {
					chkdbId(event.source.groupId, source.type);
				}
				if (source.type.equals("room")) {
					chkdbId(event.source.roomId, source.type);
				}

				// handle TextMessage Event
				String msgType = event.message.type;
				if (msgType.equals("text")) { 
					handleTxtMsgEvnt(event);
					
					// handle Image Video Audio File Message Event
				} else if (msgType.equals("image") || msgType.equals("video") || msgType.equals("audio")
						|| msgType.equals("file")) {
					// saveContent(event);
//					TextMessage target = MsgUtil.getDefaultReply(userId, getUserDesc(userId).displayName);
//					ReplyMessage rm = new ReplyMessage(event.replyToken, target);
//					reply(rm);
					
					// handle Location Sticker Message Event
				} else {
//					TextMessage target = MsgUtil.getDefaultReply(userId, getUserDesc(userId).displayName);
//					ReplyMessage rm = new ReplyMessage(event.replyToken, target);
//					reply(rm);
				}

				// 用戶觸發postback action
			} else if (type.equals("postback")) {
				handlePostbackEvnt(event);

				//用戶解除封鎖、加入好友
			} else if (type.equals("follow")) {
				ReplyMessage rm = new ReplyMessage(event.replyToken, MsgUtil.sendFollowMsg());
				reply(rm);

				// 檢查set裡面有沒有此用戶id資料
				chkdbId(userId, source.type);

				// 被用戶封鎖的時候
			} else if (type.equals("unfollow")) {
				userIdMap.put(userId, 1);
				connToMysql.updateStatus("isDelete", 1, userId);
				String sql = "update "+idTable+" set isSub=0 where id='" + event.source.userId + "'";

				connToMysql.update(sql);
				userIsSubMap.replace(event.source.userId, 0);

				// Join Event 加入群組or聊天室 group room
			} else if (type.equals("join")) {
				ReplyMessage rm = new ReplyMessage(event.replyToken, MsgUtil.sendFollowMsg());
				reply(rm);
				
				if (source.type.equals("group")) {
					chkdbId(source.groupId, source.type);

				} else if (source.type.equals("room")) {
					chkdbId(source.roomId, source.type);

				} else {
					System.out.println("不是被join 到room 或group");
				}

				// 被移出群組或聊天室 Leave Event
			} else if (type.equals("leave")) {
				if (source.type.equals("group")) {
					groupIdMap.put(source.groupId, 1);
					connToMysql.updateStatus("isDelete", 1, source.groupId);
				}
				if (source.type.equals("room")) {
					groupIdMap.put(source.roomId, 1);
					connToMysql.updateStatus("isDelete", 1, source.roomId);
				}

				// Other Event
			} else {
				// Postback Beacon AccountLink event...
//				System.out.println("Postback Beacon AccountLink event...");
			}
			whobj.events.remove(0);
		}
	}
	
	// 設定關鍵字回覆內容
	// 一次最多 發送五個訊息
	void handleTxtMsgEvnt(Events event) throws ClientProtocolException, IOException, SQLException {
		//這個List用來存準備要發出去的messages
		List<Message> msgList;
		
		// TRt
		if (!(TRt.getEnum(event.message.text) == null)) {
			TRt valueOf = TRt.getEnum(event.message.text);

			String name = valueOf.getName();
			Request getMsg = ApHttp.ofGetRequest(valueOf.getTrickURL());
			String resp = getMsg.execute().returnContent().asString();

			JSONObject jsonOb = new JSONObject(new String(resp.getBytes("iso8859-1"), "utf-8"));

			Bubble container = MsgUtil.getTableBubb(name, valueOf.getFieldsMaps(), jsonOb);
			FlexMessage fm = FlexMessage.builder().altText(name).contents(container).build();
			ReplyMessage rm = new ReplyMessage(event.replyToken, fm);
			reply(rm);

		} else if (event.message.text.equals("即時策略")) {
			List<FlexComponent> bodyContents = new ArrayList<>();
			for (TRt e : TRt.values()) {
				String name = e.getName();
				bodyContents.add(Button.builder().action(new MessageAction(name, name)).build());
	        }			

			Bubble container = Bubble.builder()
					.body(Box.builder().layout(FlexLayout.VERTICAL).contents(bodyContents).build()).build();

			FlexMessage fm = FlexMessage.builder().altText("即時策略").contents(container).build();
			ReplyMessage rm = new ReplyMessage(event.replyToken, fm);
			reply(rm);

		} else if (event.message.text.equals("策略選股")) {
			Bubble bigDataFM = MsgUtil.getBigDataFM();

			FlexMessage fm = FlexMessage.builder().altText("策略選股").contents(bigDataFM).build();
			ReplyMessage rm = new ReplyMessage(event.replyToken, fm);
			reply(rm);

		} else if (event.message.text.equals("追蹤清單")) {
			msgList = new ArrayList<>();
			Map<String, String> subMap = getSubMap(event.source.userId);
			
			Bubble stockInfoFM = MsgUtil.getSubListBubb(subMap);
			FlexMessage fm = FlexMessage.builder().altText("追蹤清單").contents(stockInfoFM).build();
			msgList.add(fm);
			
			//新手任務
			if(userIsSubMap.get(event.source.userId) == 0) {
				//完成新手任務
				if(!subMap.isEmpty()) {
//					String sql = "update " + idTable + " set isSub=1 where id='" + event.source.userId + "'";

					connToMysql.updateStatus("isSub", 1, event.source.userId);
					userIsSubMap.replace(event.source.userId, 1);
					
					msgList.add(TextMessage.builder().text("🎉 🎉 🎉  恭喜您，追蹤成功！\n建立一個屬於您的追蹤清單，往後查看個股更快速喔！").build());
					msgList.add(TextMessage.builder().text("我們還有許多實用功能，用來協助您的投資大小事，點擊「點我展開功能表」繼續探索吧🤗").build());
				
				//還未完成新手任務
				}else {		
					msgList.add(FlexMessage.builder().altText("建立「追蹤清單」").contents(MsgUtil.getTryGetSubListBubble()).build());

				}
				
			}
			
			ReplyMessage rm = new ReplyMessage(event.replyToken, msgList);
			reply(rm);

		} else if (event.message.text.equals("知識補給")) {
			Bubble linkFM = MsgUtil.getLinkAboveBubb();
			FlexMessage fm = FlexMessage.builder().altText("知識補給").contents(linkFM).build();
			ReplyMessage rm = new ReplyMessage(event.replyToken, fm);
			reply(rm);

		} else if (event.message.text.equals("豹章影音免費看")) {
			Carousel linkCarousel = MsgUtil.getLinkCarousel();
			FlexMessage fm = FlexMessage.builder().altText("豹章影音免費看").contents(linkCarousel).build();
			ReplyMessage rm = new ReplyMessage(event.replyToken, fm);
			reply(rm);

		} 

		// 這邊因為有些ac跳出來的股號 twdb 找不到, 但是 https://testwww.above.tw/symName 查得到, 這邊先都留下
		else if (!(isStock(event.message.text) == null) || !getName(event.message.text).equals("NA")) {
			String name = getName(event.message.text);
			String sym;
			msgList = new ArrayList<>(); 
			
			if (!name.equals("NA")) {
				sym = event.message.text;
			} else {
				sym = isStock(event.message.text);
				name = event.message.text;
			}
			
			// sym, name, picUrl, picLable, picUrlLick, map<lable, url>
			Carousel stockInfoCarousel = MsgUtil.getStockInfoCarousel(sym, connToMysql.isExist(event.source.userId, idTable, sym));

			FlexMessage stockInfoFM = FlexMessage.builder().altText(sym).contents(stockInfoCarousel).build();

			msgList.add(stockInfoFM);
			try {
				if(userIsSubMap.get(event.source.userId) == 0) {
					msgList.add(TextMessage.builder()
							.text("↑↑ 找到對應的個股囉 ↑↑\n這些卡片包含盤中即時資料，也提供多面向分析，為了方便往後查看更快速，請點選上方卡片的「加入追蹤清單」按鈕：").build());
					
				}
				
			} catch (NullPointerException e) {
				System.out.println("在group 中 用戶打股號, 但沒有加入此bot好友");
			}
			
			ReplyMessage rm = new ReplyMessage(event.replyToken, msgList);
			reply(rm);
		}
//		} else if(event.message.text.equals("kchart")) {
////			ImagemapMessage im = MsgUtil.getImagemapMsg("https://3cefaef6.ngrok.io/kchart2#", "kchart", "https://www.above.tw/stock/2330?trick=candlestick");
//			ImageMessage im = ImageMessage.builder().originalContentUrl("https://3cefaef6.ngrok.io/kchart2#").previewImageUrl("https://3cefaef6.ngrok.io/kchart2#").build();
//			ReplyMessage rm = new ReplyMessage(event.replyToken, im);
//			reply(rm);
//		}
		// 沒有符合的關鍵字
		else {
//			FlexMessage defaultFM = MsgUtil.getDefaultFM();
			if(!event.source.type.equals("group") && !event.source.type.equals("room")) {
				FlexMessage acBubb = MsgUtil.getAcBubb(URLEncoder.encode(event.message.text, "utf-8"));			
				ReplyMessage rm = new ReplyMessage(event.replyToken, acBubb);
				reply(rm);
			}else {
				if(event.message.text.contains("@豹投資")) {
					String str = event.message.text.split(" ")[1];
					FlexMessage acBubb = MsgUtil.getAcBubb(URLEncoder.encode(str, "utf-8"));			
					ReplyMessage rm = new ReplyMessage(event.replyToken, acBubb);
					reply(rm);
				}
			}
			
		}
		
		//賺豹豹 功能拔掉
//		else if (event.message.text.equals("賺豹豹")) {
//
//			String subList = connToMysql
//					.getSubList("select subscribe from "+idTable+" where id='" + event.source.userId + "'");
//			Map<String, String> subMap = new HashMap<>();
//
//			if (!subList.equals("")) {
//				String[] syms = subList.split(",");
//				for (int i = 0; i < syms.length; i++) {
//					Request getMsg = ApHttp.ofGetRequest("https://testwww.above.tw/symName?sym=" + syms[i] + "");
//					String name = getMsg.execute().returnContent().asString();
//
//					subMap.put(syms[i], name);
//				}
//			}
//			Bubble subAbove = MsgUtil.getSubAbove("賺豹豹", subMap);
//
//			FlexMessage fm = FlexMessage.builder().altText("賺豹豹").contents(subAbove).build();
//
//			ReplyMessage rm = new ReplyMessage(event.replyToken, fm);
//			reply(rm);}
	}
	
	//處理postback event
	public void handlePostbackEvnt(Events event) throws ClientProtocolException, IOException, SQLException {
		List<Message> msgList = null;
//		String userId = event.source.userId;
		String data = event.postback.data;
		// data拆成 action 跟回傳的值
		String[] dataCon = data.split("&");
		
		//ex: action=tryGetSubList&itemId=true
		String action = dataCon[0].split("=")[1];
		String itemId = dataCon[1].split("=")[1];
		
		switch(action) {
			//當用戶點 要建立第一次的追蹤清單
			case "tryGetSubList":
				if(itemId.equals("true")) {
					msgList = new ArrayList<>();
					msgList.add(TextMessage.builder()
							.text("首先，請在輸入框輸入一支您所關心的個股股號或股名：").build());
				}
				break;
			// 將個股加入 追蹤清單
			case "addSubList":
				msgList = new ArrayList<>();
				if(!connToMysql.isExist(event.source.userId, idTable, itemId)) {
					String sql = "UPDATE " + idTable + " SET subscribe= CONCAT(subscribe,'" + itemId + ",') WHERE id= '"+ event.source.userId + "'";
					connToMysql.update(sql);
					
					if(userIsSubMap.get(event.source.userId) == 0) {
						//未完成新手任務
						msgList.add(TextMessage.builder().text("已將 "+itemId+" 加入追蹤清單囉！\n請點選下方的「點我展開功能表」>「追蹤清單」，找找您已追蹤的個股：").build());										
					
					}else {
						msgList.add(TextMessage.builder().text("已將 "+itemId+" 加入追蹤清單囉！").build()); 			
						
					}
				
				}else {
					msgList.add(TextMessage.builder().text(itemId+" 已在追蹤清單囉！").build());

				}
				break;
			// 將個股移除 追蹤清單
			case "deleteSubList":
				msgList = new ArrayList<>();
				String sql = "UPDATE "+idTable+" SET subscribe= REPLACE(subscribe,'" + itemId + ",','') WHERE id= '"
						+ event.source.userId + "'";
				connToMysql.update(sql);

				msgList.add(TextMessage.builder().text("已將" + itemId + "從追蹤清單移除").build());
				break;
			default:
				System.out.println("沒有此action");
				break;
		}
			
		ReplyMessage rm = new ReplyMessage(event.replyToken, msgList);
		reply(rm);
	}

	void reply(ReplyMessage rm) throws ClientProtocolException, IOException {
		String msgJson = mapper.writeValueAsString(rm);
		System.out.println(msgJson);
		Request replyMsg = ApHttp.ofPostRequest("https://api.line.me/v2/bot/message/reply", msgJson);

		replyMsg.addHeader("Authorization",
				"Bearer "+ auth);
		replyMsg.addHeader("Content-Type", "application/json; charset=utf-8;");
		replyMsg.execute().returnContent().asString();
	}

	void push(String pushtext) throws ClientProtocolException, IOException {
		Request pushMsg = ApHttp.ofPostRequest("https://api.line.me/v2/bot/message/push", pushtext);

		// test push msg
		pushMsg.addHeader("Authorization",
				"Bearer " + auth);
		pushMsg.addHeader("Content-Type", "application/json; charset=utf-8;");
		pushMsg.execute().returnContent().asString();
	}
	
	// 用在開盤日9:02把 盤中熱力大盤圖鏈接push 給使用者
	public void sendOutThermalToLineUsers() throws ClientProtocolException, IOException {
//		FlexMessage thermalMsg = MsgUtil.getThermalMsg();
		String picURL = "https://testwww.above.tw/getTreeMap?num=50";
		ImageMessage im = ImageMessage.builder().originalContentUrl(picURL).previewImageUrl(picURL).build();

		System.out.println("盤中熱力大盤圖訊息共發給： "+userIdMap.size()+" 個用戶");
		
		userIdMap.forEach((x, y)->{
			// 如果用戶封鎖我們, 會自動被忽略(不會發送)
			PushMessage pm = new PushMessage(x, im);
			try {
				String pushMsg = mapper.writeValueAsString(pm);
				push(pushMsg);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		});


	};
	
	//收盤後兩點 把個別用戶的自選股新聞push給使用者
	public void sendOutNewsToLineUsers() throws ClientProtocolException, IOException, SQLException {
		
		userIdMap.forEach((x, y)->{
			Bubble subNewsMsg;
			try {
				subNewsMsg = MsgUtil.getSubNewsBubb(getSubMap(x));
				FlexMessage fm = FlexMessage.builder().altText("個股新聞清單").contents(subNewsMsg).build();

				PushMessage pm = new PushMessage(x, fm);
				String pushMsg = mapper.writeValueAsString(pm);
				push(pushMsg);
			} catch (SQLException | IOException e1) {
				e1.printStackTrace();
			}
			
		});
		


	};
	
	
	public void pushMsg(StrMap reqBodyMap) throws SQLException, ClientProtocolException, IOException {
		// push message 有些要符合1~3個條件
		String get;
		String get2;
		String get3;
		Set<String> idSet = new HashSet<>();
		List<Message> msgList = new ArrayList<>();
		if (!(get = reqBodyMap.getOrNull("target")).equals("")) {
			switch (get) {
			case "users":
				idSet = connToMysql.getId("user");
				break;
			case "group":
				idSet = connToMysql.getId("group");
				break;
			case "singleUser":
				break;
			default:
				idSet = connToMysql.getId("all");
			}

		}

		if (!(get = reqBodyMap.getOrNull("singleUser")).equals("")) {
			idSet.add(get);
		}
		if (!(get = reqBodyMap.getOrNull("text")).equals("")) {
			TextMessage txtMsg = TextMessage.builder().text(get).build();
			msgList.add(txtMsg);
		}
		if (!(get = reqBodyMap.getOrNull("packageId")).equals("")
				&& !(get2 = reqBodyMap.getOrNull("stickerId")).equals("")) {
			StickerMessage stickerMsg = StickerMessage.builder().packageId(get).stickerId(get2).build();
			msgList.add(stickerMsg);
		}

		if (!(get = reqBodyMap.getOrNull("picOriginalContentUrl")).equals("")
				&& !(get2 = reqBodyMap.getOrNull("picPreviewImageUrl")).equals("")) {
			ImageMessage imageMsg = ImageMessage.builder().originalContentUrl(get).previewImageUrl(get2).build();
			msgList.add(imageMsg);
		}

		if (!(get = reqBodyMap.getOrNull("viedoOriginalContentUrl")).equals("")
				&& !(get2 = reqBodyMap.getOrNull("videoPreviewImageUrl")).equals("")) {
			VideoMessage videoMsg = VideoMessage.builder().originalContentUrl(get).previewImageUrl(get2).build();
			msgList.add(videoMsg);

		}

		if (!(get = reqBodyMap.getOrNull("mapBaseUrl")).equals("")
				&& !(get2 = reqBodyMap.getOrNull("mapAltText")).equals("")
				&& !(get3 = reqBodyMap.getOrNull("mapLinkUri")).equals("")) {
			ImagemapMessage imagemapMsg = MsgUtil.getImagemapMsg(get, get2, get3);
			msgList.add(imagemapMsg);
		}

		if (!(get = reqBodyMap.getOrNull("json")).equals("")) {
			Message msg = mapper.readValue(get, Message.class);
			msgList.add(msg);

		}
		Iterator<String> it = idSet.iterator();
		while (it.hasNext()) {
			String next = it.next();
			PushMessage pm = new PushMessage(next, msgList);
			String pushtext = mapper.writeValueAsString(pm);
			push(pushtext);
		}
	}
	
	
	public Set<String> getSubSet(String userId) throws SQLException, ClientProtocolException, IOException {

		String[] syms = connToMysql.getSubList("select subscribe from "+idTable+" where id='" + userId + "'").split(",");
		Set<String> subSet = new HashSet<>();
		Request getMsg;
		String name;
		for (int i = 0; i < syms.length; i++) {
			getMsg = ApHttp.ofGetRequest("https://testwww.above.tw/symName?sym=" + syms[i] + "");
			name = getMsg.execute().returnContent().asString();
			subSet.add(name);
		}
		return subSet;
	}
	
	public Map<String, String> getSubMap(String userId) throws SQLException, ClientProtocolException, IOException {
		String subList = connToMysql
				.getSubList("select subscribe from "+idTable+" where id='" + userId + "'");
		Map<String, String> subMap = new HashMap<>();

		if (!subList.equals("")) {
			String[] syms = subList.split(",");
			for (int i = 0; i < syms.length; i++) {
				Request getMsg = ApHttp.ofGetRequest("https://testwww.above.tw/symName?sym=" + syms[i] + "");
				String name = getMsg.execute().returnContent().asString();

				subMap.put(syms[i], name);
			}
		}
		return subMap;
	}

	public String selectHonsetdb(String symbol) throws SQLException {
		String latestTradeDate = TWQHist.getLatestTradeDate();

		String sSql = "select Date,Symbol, StockName, cdesc from module1 where Date(Date)='" + latestTradeDate
				+ "' AND Symbol='" + symbol + "'";
		DbObjsRows0 lstRows = null;
		List<Module1> rows = new ArrayList<>();
		String moduleJson = null;

		lstRows = app.get().select(EDb.app, ISql.of(sSql), CacheTime.ofSec(10));
		int dateNdx = lstRows.metaLst().colIndex("Date");
		int symbolNdx = lstRows.metaLst().colIndex("Symbol");
		int symbolNameNdx = lstRows.metaLst().colIndex("StockName");
		int cdescNdx = lstRows.metaLst().colIndex("cdesc");

		lstRows.rows().forEach(r -> {
			// 如果結束日期大於現在的時間才視為合法的使用者 (日期有可能是 NULL -> 表示沒輸入，跳掉)

			Module1 m = new Module1();
			m.symbol = r.getString(symbolNdx);
			m.stockName = r.getString(symbolNameNdx);
			m.cdesc = r.getString(cdescNdx);
			m.date = r.getDate(dateNdx);

			rows.add(m);

		});

		try {
			moduleJson = mapper.writeValueAsString(rows);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return moduleJson;
	}

	class Module1 {
		public String symbol;
		public String stockName;
		public String cdesc;
		public Date date;
	}

	private String getName(String sym) throws ClientProtocolException, IOException {
		Request getMsg;
		try {
			getMsg = ApHttp.ofGetRequest("https://testwww.above.tw/symName?sym=" + sym + "");
			
		} catch (Exception e) {
			return "NA";
		}
		return getMsg.execute().returnContent().asString();
	}

	// 檢查傳來的訊息是不是有效股號or股名
	private String isStock(String symOrName) throws SQLException {
		String sSqlSym = "select Symbol, StockName from Symbols where Symbol='" + symOrName + "'";
		String sSqlName = "select Symbol from Symbols where StockName='" + symOrName + "'";

		DbObjsRows0 lstRows = null;

		lstRows = tw.get().select(EDb.tw, ISql.of(sSqlSym), CacheTime.ofSec(10));
		int symbolNdx = lstRows.metaLst().colIndex("Symbol");

		// 先以股號為標準去查 如果有, 則返回此股號
		if (!lstRows.rows().isEmpty()) {

			return symOrName;
			// 不符合股號就繼續檢查是不是有效股名
		} else {
			lstRows = tw.get().select(EDb.tw, ISql.of(sSqlName), CacheTime.ofSec(10));
			// 是有效股名的話, 返回此股名的股號
			if (!lstRows.rows().isEmpty()) {

				String sym = lstRows.rows().first$().getString(symbolNdx);
				return sym;
				// 都不是的話返回null
			} else {
				return null;
			}

		}

	}
	// 檢查map 有沒有此id 如果沒有的話就在db userIdMap userIsSubMap 加入此用戶id
	public void chkdbId(String id, String type)
			throws JsonParseException, JsonMappingException, IOException, SQLException {
		// 由user發出的Message
		if (type.equals("user")) {
			if (!userIdMap.containsKey(id)) {
				RespUserDesc userDesc;
				try {
					userDesc = getUserDesc(id);
					
				} catch (Exception e) {
					System.out.println("此用戶沒有加bot好友!");
					return;
				}
				userIdMap.put(id, 0);
				userIsSubMap.put(id, 0);
				// 將新的id insert到mysql
				connToMysql.insertId(userDesc.userId, userDesc.displayName, type);
			
			} else {
				//之前被刪除過, 現在加回來
				// isDelete 是不是 1
				if (userIdMap.get(id) == 1) {
					userIdMap.replace(id, 0);
					connToMysql.updateStatus("isDelete",0, id);
				}
			}
			// room or group 發出
		} else {
			if (!groupIdMap.containsKey(id)) {
				groupIdMap.put(id, 0);
				// 將新的id insert到mysql
				connToMysql.insertId(id, null, type);
			} else {
				// isDelete 是不是 1, 如果是的話, 表示之前被移出, 現在又被加回去, 所以要改status
				if (groupIdMap.get(id) == 1) {
					groupIdMap.replace(id, 0);
					connToMysql.updateStatus("isDelete", 0, id);
				}
			}
		}

	}

	// 拿到使用者資料
	RespUserDesc getUserDesc(String userId) throws JsonParseException, JsonMappingException, IOException {
		Request getMsg = ApHttp.ofGetRequest("https://api.line.me/v2/bot/profile/" + userId);
		getMsg.addHeader("Authorization",
				"Bearer "+ auth);
		String response = getMsg.execute().returnContent().asString();
		//轉成 RespUserDesc
		RespUserDesc resp = mapper.readValue(response, RespUserDesc.class);
		return resp;
	}

	// 接收使用者傳來的檔案照片... 目前沒有用到
	void saveContent(Events event) throws ClientProtocolException, IOException {
		// Gets image, video, and audio data sent by users. sent by users
		// 其他訊息類型會get失敗
		Request getContent = ApHttp.ofGetRequest("https://api.line.me/v2/bot/message/" + event.message.id + "/content");
		getContent.addHeader("Authorization",
				"Bearer "+auth);
		String path = "/Users/mac/Desktop/bot/UserSentContents/";
		File file = null;
		String fileName = event.source.userId + "_" + event.timestamp;
		if (event.message.type.equals("image")) {
			file = new File(path + fileName + ".jpg");
		} else if (event.message.type.equals("video")) {
			file = new File(path + fileName + ".mp4");

		} else if (event.message.type.equals("audio")) {
			file = new File(path + fileName + ".m4a");

		} else if (event.message.type.equals("file")) {
			file = new File(path + fileName + event.message.filename);

		} else {
			return;
		}
		file.createNewFile();
		getContent.execute().saveContent(file);
	}
	


	// 這裡用一般push msg 可以取代
//	// 推送給所有 user Id
//	public void sendMultiMsg() throws ClientProtocolException, IOException, SQLException {
//		connToMysql.getId(idSet, true);
//		Multicast pm = new Multicast(idSet, getPushMsg());
//		// object轉json
//		String pushtext = mapper.writeValueAsString(pm);
//
//		System.out.println(pushtext);
//		Request pushMsg = ApHttp.ofPostRequest("https://api.line.me/v2/bot/message/multicast", pushtext);
//
//		// test push msg
//		pushMsg.addHeader("Authorization",
//				"Bearer "+auth);
//		pushMsg.addHeader("Content-Type", "application/json; charset=utf-8;");
//		pushMsg.execute().returnContent().asString();
//		idSet.clear();
//	}

}
