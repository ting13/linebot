package app.bot.entity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.ImagemapMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.VideoMessage;
import com.linecorp.bot.model.message.flex.component.Box;
import com.linecorp.bot.model.message.flex.component.Button;
import com.linecorp.bot.model.message.flex.component.Button.ButtonHeight;
import com.linecorp.bot.model.message.flex.component.Button.ButtonStyle;
import com.linecorp.bot.model.message.flex.component.Filler;
import com.linecorp.bot.model.message.flex.component.FlexComponent;
import com.linecorp.bot.model.message.flex.component.Image;
import com.linecorp.bot.model.message.flex.component.Image.ImageAspectMode;
import com.linecorp.bot.model.message.flex.component.Image.ImageAspectRatio;
import com.linecorp.bot.model.message.flex.component.Image.ImageSize;
import com.linecorp.bot.model.message.flex.component.Separator;
import com.linecorp.bot.model.message.flex.component.Text;
import com.linecorp.bot.model.message.flex.component.Text.TextWeight;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.container.BubbleStyles;
import com.linecorp.bot.model.message.flex.container.BubbleStyles.BlockStyle;
import com.linecorp.bot.model.message.flex.container.Carousel;
import com.linecorp.bot.model.message.flex.unit.FlexAlign;
import com.linecorp.bot.model.message.flex.unit.FlexDirection;
import com.linecorp.bot.model.message.flex.unit.FlexFontSize;
import com.linecorp.bot.model.message.flex.unit.FlexGravity;
import com.linecorp.bot.model.message.flex.unit.FlexLayout;
import com.linecorp.bot.model.message.flex.unit.FlexMarginSize;
import com.linecorp.bot.model.message.imagemap.ImagemapAction;
import com.linecorp.bot.model.message.imagemap.ImagemapArea;
import com.linecorp.bot.model.message.imagemap.ImagemapBaseSize;
import com.linecorp.bot.model.message.imagemap.URIImagemapAction;
import com.linecorp.bot.model.message.quickreply.QuickReply;
import com.linecorp.bot.model.message.quickreply.QuickReplyItem;

import aphttp.ApHttp;
import tw.quote.objs.TWSymbolQ;
import util.DateUtil;

public class MessageUtil {
	
	//加入好友時的歡迎訊息
	public List<Message> sendFollowMsg() {
		List<Message> msgList = new ArrayList<>();

		msgList.add(TextMessage.builder().text("歡迎來到豹投資，每天簡單報投資！").build());
		msgList.add(TextMessage.builder().text("我們提供盤中、盤後的指標與即時資訊，為了更好的體驗，先一起來建立屬於您的「追蹤清單」吧！").build());
		msgList.add(TextMessage.builder().text("首先，請在輸入框輸入一支您所關心的個股股號或股名：").build());
		return msgList;
	}
	
	//當用戶傳的不是text Message 會出現的
	public TextMessage getDefaultReply(String userId, String displayName)
			throws JsonParseException, JsonMappingException, IOException {
		String emoji = String.valueOf(Character.toChars(0x10008D));

		TextMessage target = TextMessage.builder()
				.text("你好," + displayName + "\r\n很抱歉，我沒有辦法對用戶個別回覆！\r\n請點擊快速回覆按鈕！" + emoji + "：")
				.quickReply(QuickReply.items(getQuickReplyItem())).build();
		return target;
	}

	//個股carousel 的第一個bubble
	/**
	 * 
	 * @param sym 股號
	 * @param actionMap 下面Button要串的Action<label, value>
	 */
	public Bubble getStockInfoBubbF(String sym, Map<String, String> actionMap)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();

		Request req = ApHttp.ofGetRequest("http://pneko.prj.tw:27300/quote?s=" + sym);
		String resp = req.execute().returnContent().asString();
		
		//把拿到的json 轉成 TWSymbolQ
		TWSymbolQ value = mapper.readValue(resp, TWSymbolQ.class);

		SimpleDateFormat datef = new SimpleDateFormat("yyyy/MM/dd");
		SimpleDateFormat timef = new SimpleDateFormat("HH:mm:ss");
		Date clt = DateUtil.convLongTime(value.q.t);

		String date = datef.format(clt);
		String time = timef.format(clt);

		return getTradeBubble(value, time, date, actionMap);

	}
	
	//個股carousel 的bubble(除了第一個)
	public Bubble getStockInfoBubb(String picUrl, Map<String, String> actionMap) {
		List<FlexComponent> footerContents = new ArrayList<>();

		actionMap.forEach((x, y) -> {
			footerContents.add(Button.builder().style(ButtonStyle.LINK).action(new URIAction(x, y)).build());
		});

		Image hero = Image.builder().url(picUrl).margin(FlexMarginSize.NONE).aspectRatio(ImageAspectRatio.R2TO1)
				.backgroundColor("#FFFFFF").aspectMode(ImageAspectMode.Cover).size(ImageSize.FULL_WIDTH).build();
		Box footer = Box.builder().layout(FlexLayout.VERTICAL).contents(footerContents).build();

		BlockStyle footerStyle = BlockStyle.builder().separator(true).build();

		BubbleStyles bubbleStyle = BubbleStyles.builder().footer(footerStyle).build();

		Bubble bubble = Bubble.builder().hero(hero).footer(footer).styles(bubbleStyle).build();

		return bubble;
	}
	
	//如果個股carousel(除了第一張卡片) hero的image需要用到action 用這個方法 
	public Bubble getStockInfoBubb(String picUrl, String picLabel, String picUrlLink, Map<String, String> linkMap) {
		List<FlexComponent> bodyContents = new ArrayList<>();

		linkMap.forEach((x, y) -> {
			bodyContents.add(Button.builder().style(ButtonStyle.LINK).height(ButtonHeight.SMALL)
					.action(new URIAction(x, y)).build());
		});

		URIAction picAction = new URIAction(picLabel, picUrlLink);
		Box body = Box.builder().layout(FlexLayout.VERTICAL).spacing(FlexMarginSize.XS).contents(bodyContents).build();

		Image hero = Image.builder().action(picAction).url(picUrl).aspectRatio(ImageAspectRatio.R1_91TO1)
				.aspectMode(ImageAspectMode.Cover).size(ImageSize.FULL_WIDTH).build();

		Bubble bubble = Bubble.builder().direction(FlexDirection.LTR).hero(hero).body(body).build();

		return bubble;

	}

	//輸入不認識的訊息時出現的message
	public FlexMessage getAcBubb(String words) throws ClientProtocolException, IOException {
		Request getMsg;
//		String enK = URLEncoder.encode(next.getValue(), "utf-8");
		getMsg = ApHttp.ofGetRequest("https://mac.above.tw/q?type=tw&cat=tseotc&word=" + words + "");
		JSONArray acArr = new JSONArray(getMsg.execute().returnContent().asString());

		List<FlexComponent> bodyContents = new ArrayList<>();

		bodyContents.add(Text.builder().text("我們為您找到最接近的個股：").size(FlexFontSize.SM).build());
		bodyContents.add(Separator.builder().color("#ffffff").margin(FlexMarginSize.MD).build());
		// 顯示前五個結果
		for (int i = 0; i < 5; i++) {

			Pattern pattern = Pattern.compile("(.*)#(.*)\\(TW\\)");
			Matcher matcher = pattern.matcher(acArr.getString(i));
			String sym = null;
			String stockName = null;

			if (matcher.find()) {
				stockName = matcher.group(1);
				sym = matcher.group(2);
			}

			bodyContents.add(Button.builder().style(ButtonStyle.LINK)
					.action(new MessageAction(sym + " " + stockName, sym)).build());
		}
		bodyContents.add(Separator.builder().color("#ffffff").margin(FlexMarginSize.MD).build());
		bodyContents.add(Text.builder().text("或可再嘗試輸入其他關鍵字喔！").size(FlexFontSize.SM).build());

		Box body = Box.builder().layout(FlexLayout.VERTICAL).contents(bodyContents).build();

		Bubble bubble = Bubble.builder().body(body).build();
		FlexMessage fm = FlexMessage.builder().altText("找到最接近的個股").contents(bubble).build();

		return fm;
	}
	
	//個股carousel
	// isExist-> 用戶是否已追蹤此股號
	public Carousel getStockInfoCarousel(String sym, boolean isExist)
			throws JsonParseException, JsonMappingException, IOException {
		List<Bubble> bubbleList = new ArrayList<>();
		Map<String, String> actionMap = new LinkedHashMap<>();
		actionMap.put("個股新聞", "https://www.above.tw/stock/" + sym + "?trick=news");
		actionMap.put("AI指標判讀", "https://linebot.prj.tw/module1?sym=" + sym);
		if (isExist == true) {
			actionMap.put("從追蹤清單移除", "action=deleteSubList&itemId="+sym);

		} else {
			actionMap.put("加入追蹤清單", "action=addSubList&itemId="+sym);

		}

		// String picUrl,String picLabel, String picUrlLink, Map<String, String> linkMap
		bubbleList.add(getStockInfoBubbF(sym, actionMap));
		actionMap.clear();

		actionMap.put("個股月營收", "https://www.above.tw/stock/" + sym + "?trick=sym_score");
		actionMap.put("成長性分析", "https://www.above.tw/stock/" + sym + "?trick=GrowthAnly");
		actionMap.put("現金分析 (年)", "https://www.above.tw/stock/" + sym + "?trick=Cashratio");

//		getStockInfoFM(server+"static/img/news.png", "個股新聞", "https://www.above.tw/stock/"+sym+"?trick=news", linkMap);

		// 基本分析
		bubbleList.add(getStockInfoBubb("https://i.imgur.com/mKusiKo.png", actionMap));
		actionMap.clear();

		actionMap.put("獲利能力分析 (季)", "https://www.above.tw/stock/" + sym + "?trick=ProfitByQr");
		actionMap.put("償債能力分析 (季)", "https://www.above.tw/stock/" + sym + "?trick=SolvencyBySym");
		actionMap.put("經營能力分析 (年)", "https://www.above.tw/stock/" + sym + "?trick=Oper");

		// 財務分析
		bubbleList.add(getStockInfoBubb("https://i.imgur.com/Hh9Ztoj.png", actionMap));
		actionMap.clear();

		actionMap.put("分點買賣明細", "https://www.above.tw/stock/" + sym + "?trick=BrkTrade");
		actionMap.put("分點券商買賣超", "https://www.above.tw/stock/" + sym + "?trick=BrkBuyNet");
		actionMap.put("董監大股東持股", "https://www.above.tw/stock/" + sym + "?trick=ShareBal");

		// 籌碼分析
		bubbleList.add(getStockInfoBubb("https://i.imgur.com/UTqqH0E.jpg", actionMap));
		actionMap.clear();

		actionMap.put("資本形成 - 股東權益 (季)", "https://www.above.tw/stock/" + sym + "?trick=Capstreq");
		actionMap.put("持股轉讓", "https://www.above.tw/stock/" + sym + "?trick=xnotfer");
		actionMap.put("內部人設質解質", "https://www.above.tw/stock/" + sym + "?trick=SharePledge");

		// 股權分析
		bubbleList.add(getStockInfoBubb("https://i.imgur.com/lqAJ5R2.png", actionMap));
		actionMap.clear();

		actionMap.put("獲利能力分析 (年)", "https://www.above.tw/stock/" + sym + "?trick=Profit");
		actionMap.put("償債能力分析 (年)", "https://www.above.tw/stock/" + sym + "?trick=Liquid");
		actionMap.put("股利政策", "https://www.above.tw/stock/" + sym + "?trick=Divpolicy");

		// 長期財務分析
		bubbleList.add(getStockInfoBubb("https://i.imgur.com/wnKcn9h.png", actionMap));

		Carousel carousel = Carousel.builder().contents(bubbleList).build();
		return carousel;
	}

	// 知識補給
	public Bubble getLinkAboveBubb() {
		URIAction above1to1 = new URIAction("豹小妞一對一問答", "https://line.me/R/ti/p/%40yjt7517n");

		URIAction hahow = new URIAction("Hahow 線上課程", "https://hahow.in/cr/usstocks");
		URIAction accupass = new URIAction("Accupass 實體課程",
				"https://usstocks.accupass.com/org/detail/r/1711271516339982202150/1/0");
		MessageAction randomMsg = new MessageAction("豹章影音免費看", "豹章影音免費看");

		List<FlexComponent> footerContents = Arrays.asList(
				Button.builder().style(ButtonStyle.LINK).action(hahow).build(),
				Button.builder().style(ButtonStyle.LINK).action(accupass).build(),
				Button.builder().style(ButtonStyle.LINK).action(randomMsg).build());

		Box footer = Box.builder().layout(FlexLayout.VERTICAL).contents(footerContents).build();

		Image hero = Image.builder().action(above1to1).margin(FlexMarginSize.NONE)
				.url("https://i.imgur.com/odE0h8q.png").aspectRatio(ImageAspectRatio.R2TO1)
				.aspectMode(ImageAspectMode.Cover).size(ImageSize.FULL_WIDTH).build();

		BlockStyle footerStyle = BlockStyle.builder().separator(true).build();
		BubbleStyles bubbleStyle = BubbleStyles.builder().footer(footerStyle).build();

		Bubble bubble = Bubble.builder().hero(hero).footer(footer).styles(bubbleStyle).build();

		return bubble;
	}

	//報章影音免費看的bubble
	public Bubble getLinkBubb(List<Entry<String, String>> list) {
		List<FlexComponent> bodyContents = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			bodyContents.add(Button.builder().style(ButtonStyle.LINK).height(ButtonHeight.SMALL)
					.action(new URIAction(list.get(i).getKey(), list.get(i).getValue())).build());

		}

		Box body = Box.builder().layout(FlexLayout.VERTICAL).spacing(FlexMarginSize.XS).contents(bodyContents).build();

		Bubble bubble = Bubble.builder().direction(FlexDirection.LTR).body(body).build();

		return bubble;
	}
	
	//當用戶輸入的不是text message, 會出現的選項
	public List<QuickReplyItem> getQuickReplyItem() {
		// quickReply 設定關鍵字給用戶點擊，在後台提供關鍵字回覆的訊息
		// 讓用戶傳關鍵字 再用replyMessage回覆
		MessageAction msgActionQuote = new MessageAction("查詢最新股票訊息！", "教我查詢報價");
		MessageAction msgActionNewVideo = new MessageAction("看看最新影片！", "新影片");
		MessageAction msgActionRdVideo = new MessageAction("隨機看！", "隨機看");
		MessageAction msgAction = new MessageAction("關於above", "關於above");
		MessageAction msgActionImagemap = new MessageAction("圖文廣告", "圖文廣告");

		List<QuickReplyItem> items = Arrays.asList(QuickReplyItem.builder().action(msgAction).build(),
				QuickReplyItem.builder().action(msgActionQuote).build(),
				QuickReplyItem.builder().action(msgActionRdVideo).build(),
				QuickReplyItem.builder().action(msgActionNewVideo).build(),
				QuickReplyItem.builder().action(msgActionImagemap).build());
		return items;
	}

	public FlexMessage getDefaultFM() {

		URIAction stockApp = new URIAction("輸入正確股號股名", "line://app/1613073569-rlv7kVnK");

		List<FlexComponent> bodyContents = Arrays.asList(
				Text.builder().text("<請輸入股名或者股號>").align(FlexAlign.CENTER).gravity(FlexGravity.CENTER).flex(1)
						.color("#000000").size(FlexFontSize.Md).weight(TextWeight.BOLD).build(),
				Text.builder().text("例如：2330").align(FlexAlign.CENTER).gravity(FlexGravity.CENTER).flex(1)
						.color("#000000").size(FlexFontSize.Md).weight(TextWeight.BOLD).build(),
				Button.builder().style(ButtonStyle.SECONDARY).action(stockApp).build());

		Box body = Box.builder().layout(FlexLayout.VERTICAL).spacing(FlexMarginSize.MD).contents(bodyContents).build();

		Bubble bubble = Bubble.builder().direction(FlexDirection.LTR).body(body).build();

		FlexMessage fm = FlexMessage.builder().altText("輸入正確股號股名").contents(bubble).build();
		return fm;
	}

	// --------------------------------------------------------------------------------------
	// 表格3
	// 橫向, 有按鈕, 最多5個欄位, 內容不可以太長, 每欄最多2個字
	// --------------------------------------------------------------------------------------
	/**
	 * 
	 * @param title 大標題
	 * @param fieldsMaps 選取的欄位
	 * @param jsonOb trick GET 下來的 json
	 * @return
	 */
	public Bubble getTableBubb(String title, Map<String, String> fieldsMaps, JSONObject jsonOb) {
		//表第一列的顏色(欄位名稱)
		String fieldColor = "#808080";
		//表其他列的顏色(內容)
		String valueColor = "#000000";
		
		int buttonFlex = 3; 
		int otherFlex = 2;
		
		//處理欄位列
		List<FlexComponent> fieldsList = handleFieldFlex(buttonFlex, otherFlex, fieldsMaps, fieldColor);
		Bubble bubble = getTableBubb(title, "Above Technology 逸志科技", fieldsMaps, jsonOb, fieldsList, valueColor, buttonFlex, otherFlex);
		return bubble;
	}

	public Bubble getBigDataFM() {
		List<FlexComponent> bodyContents = new ArrayList<>();
		bodyContents.add(Text.builder().text("市場脈動").wrap(true).color("#000000").size(FlexFontSize.LG)
				.weight(TextWeight.BOLD).margin(FlexMarginSize.MD).build());

		bodyContents.add(Button.builder().height(ButtonHeight.SMALL)
				.action(new URIAction("法人買賣超-產業", "https://www.above.tw/market?trick=II3NetBuy")).build());
		bodyContents.add(Button.builder().height(ButtonHeight.SMALL)
				.action(new URIAction("八大行庫買賣超金額", "https://www.above.tw/market?trick=BrkBig8NetBuy")).build());
		bodyContents.add(Button.builder().height(ButtonHeight.SMALL)
				.action(new URIAction("整體月營收", "https://www.above.tw/market?trick=MarketCsM")).build());
		bodyContents.add(Button.builder().height(ButtonHeight.SMALL)
				.action(new URIAction("產業獲利能力(季)", "https://www.above.tw/market?trick=IfrsIncomeQ")).build());
		bodyContents.add(Button.builder().height(ButtonHeight.SMALL)
				.action(new URIAction("產業月營收", "https://www.above.tw/market?trick=SectCsRevenue")).build());
		bodyContents.add(Button.builder().height(ButtonHeight.SMALL)
				.action(new URIAction("綜合損益", "https://www.above.tw/market?trick=AllEarningChange")).build());
		bodyContents.add(Button.builder().height(ButtonHeight.SMALL)
				.action(new URIAction("個別產業月營收", "https://www.above.tw/market?trick=marketCsRevenue")).build());
		bodyContents.add(Button.builder().height(ButtonHeight.SMALL)
				.action(new URIAction("類股報酬率排行", "https://www.above.tw/market?trick=IdxReturn")).build());
		bodyContents.add(Button.builder().height(ButtonHeight.SMALL)
				.action(new URIAction("個別產業獲利能力(季)", "https://www.above.tw/market?trick=SectIfrsIncomeQ")).build());
		bodyContents.add(Separator.builder().build());
		bodyContents.add(Text.builder().text("期貨選擇權").wrap(true).color("#000000").size(FlexFontSize.LG)
				.weight(TextWeight.BOLD).margin(FlexMarginSize.MD).build());

		bodyContents.add(Button.builder().height(ButtonHeight.SMALL)
				.action(new URIAction("台指期大額交易人部位變動", "https://www.above.tw/market?trick=FutHugeTraderChg")).build());
		bodyContents.add(Button.builder().height(ButtonHeight.SMALL)
				.action(new URIAction("期貨法人多空未平倉", "https://www.above.tw/market?trick=FutTradeByII3")).build());
		bodyContents.add(Button.builder().height(ButtonHeight.SMALL)
				.action(new URIAction("台指選擇權大額交易人變動", "https://www.above.tw/market?trick=OptHugeTraderChg")).build());

		bodyContents.add(Separator.builder().build());
		bodyContents.add(Text.builder().text("關鍵排行").wrap(true).color("#000000").size(FlexFontSize.LG)
				.weight(TextWeight.BOLD).margin(FlexMarginSize.MD).build());

		bodyContents.add(Button.builder().height(ButtonHeight.SMALL)
				.action(new URIAction("法人買賣超佔股本", "https://www.above.tw/rank?trick=II3SellBuyNetRank")).build());
		bodyContents.add(Button.builder().height(ButtonHeight.SMALL)
				.action(new URIAction("個股大戶增減", "https://www.above.tw/rank?trick=SymStkMajHold")).build());
		bodyContents.add(Button.builder().height(ButtonHeight.SMALL)
				.action(new URIAction("千張大戶增減", "https://www.above.tw/rank?trick=StkMajHold")).build());
		bodyContents.add(Button.builder().height(ButtonHeight.SMALL)
				.action(new URIAction("當沖率排行", "https://www.above.tw/rank?trick=DayTradeRatio")).build());
		bodyContents.add(Button.builder().height(ButtonHeight.SMALL)
				.action(new URIAction("當日沖銷數據", "https://www.above.tw/rank?trick=DayTrade")).build());
		bodyContents.add(Button.builder().height(ButtonHeight.SMALL)
				.action(new URIAction("高動能低估值", "https://www.above.tw/rank?trick=MomentumUndervalued")).build());
		bodyContents.add(Button.builder().height(ButtonHeight.SMALL)
				.action(new URIAction("營收創高", "https://www.above.tw/rank?trick=m_score")).build());
		bodyContents.add(Button.builder().height(ButtonHeight.SMALL)
				.action(new URIAction("毛利率正成長排行", "https://www.above.tw/rank?trick=GrossMarginChoseSort")).build());
		bodyContents.add(Button.builder().height(ButtonHeight.SMALL)
				.action(new URIAction("PE排行", "https://www.above.tw/rank?trick=pe")).build());
		bodyContents.add(Button.builder().height(ButtonHeight.SMALL)
				.action(new URIAction("營收交叉向上", "https://www.above.tw/rank?trick=ShortTermRevOverLongTermRev"))
				.build());
		bodyContents.add(Button.builder().height(ButtonHeight.SMALL)
				.action(new URIAction("EPS排行", "https://www.above.tw/rank?trick=EPSrank")).build());
		bodyContents.add(Button.builder().height(ButtonHeight.SMALL)
				.action(new URIAction("ROE排行", "https://www.above.tw/rank?trick=roeChoice")).build());
		bodyContents.add(Button.builder().height(ButtonHeight.SMALL)
				.action(new URIAction("PB排行", "https://www.above.tw/rank?trick=pb")).build());

		Bubble container = Bubble.builder().body(
				Box.builder().layout(FlexLayout.VERTICAL).spacing(FlexMarginSize.MD).contents(bodyContents).build())
				.build();
		return container;
	}
	/**
	 * 
	 * @param title
	 * @param details
	 * @param subMap actionMap
	 * @param type  action類型 "msg" or "news"
	 * @return
	 */
	private Bubble getSubListFM(String title, String details, Map<String, String> subMap, String type) {

		List<FlexComponent> headerContents = new ArrayList<>();
		List<FlexComponent> footerContents = new ArrayList<>();

		headerContents.add(Text.builder().text(title).size(FlexFontSize.XL).weight(TextWeight.BOLD).build());
		headerContents.add(Text.builder().text(details).size(FlexFontSize.SM).weight(TextWeight.REGULAR)
				.margin(FlexMarginSize.LG).align(FlexAlign.START).build());
		Iterator<Entry<String, String>> subIt = subMap.entrySet().iterator();
		
		if(subMap.isEmpty()) {
			footerContents.add(Text.builder().text("目前無追蹤的個股").align(FlexAlign.CENTER).gravity(FlexGravity.CENTER).color("#000000").build());
		}

		while (subIt.hasNext()) {
			List<FlexComponent> boxContents = null;
			Entry<String, String> nextSub = subIt.next();
			boxContents = new ArrayList<>();
			
			//contents中 action 的類型
			if(type.equals("msg")) {
				boxContents.add(Text.builder().text(nextSub.getKey() + "\n" + nextSub.getValue())
						.action(subConMsgAction(nextSub)).size(FlexFontSize.LG).align(FlexAlign.CENTER)
						.gravity(FlexGravity.CENTER).wrap(true).weight(TextWeight.BOLD).color("#42659A").build());				
			}else {
				boxContents.add(Text.builder().text(nextSub.getKey() + "\n" + nextSub.getValue())
						.action(subConURIAction(nextSub)).size(FlexFontSize.LG).align(FlexAlign.CENTER)
						.gravity(FlexGravity.CENTER).wrap(true).weight(TextWeight.BOLD).color("#42659A").build());
			}
			if(subIt.hasNext()) {
				nextSub = subIt.next();
				if(type.equals("msg")) {
					boxContents.add(Text.builder().text(nextSub.getKey() + "\n" + nextSub.getValue())
							.action(subConMsgAction(nextSub)).size(FlexFontSize.LG).align(FlexAlign.CENTER)
							.gravity(FlexGravity.CENTER).wrap(true).weight(TextWeight.BOLD).color("#42659A").build());				
				}else {
					boxContents.add(Text.builder().text(nextSub.getKey() + "\n" + nextSub.getValue())
							.action(subConURIAction(nextSub)).size(FlexFontSize.LG).align(FlexAlign.CENTER)
							.gravity(FlexGravity.CENTER).wrap(true).weight(TextWeight.BOLD).color("#42659A").build());
				}
			
			}else {
				//剩下一個就用填充格補充 
				boxContents.add(new Filler());
			}
			footerContents.add(
					Box.builder().layout(FlexLayout.HORIZONTAL).spacing(FlexMarginSize.MD).contents(boxContents).build());
			footerContents.add(Separator.builder().color("#ffffff").margin(FlexMarginSize.SM).build());
		};

		Box footer = Box.builder().layout(FlexLayout.VERTICAL).flex(0).spacing(FlexMarginSize.SM)
				.contents(footerContents).build();
		Box header = Box.builder().layout(FlexLayout.VERTICAL).flex(0).contents(headerContents).build();

		Bubble bubble = Bubble.builder().header(header).footer(footer).build();

		return bubble;
	}
	
	// 追蹤清單、追蹤個股新聞傳出去的Flex Msg只差在 點下去的action
	//這個是 追蹤清單的action 
	private MessageAction subConMsgAction(Entry<String, String> nextSub) {
		return new MessageAction(nextSub.getKey() + "\n" + nextSub.getValue(), nextSub.getKey());
	}
	public Bubble getSubListBubb(Map<String, String> subMap) {
		Bubble subListFM = getSubListFM("追蹤清單", "點擊可查看該股即時資訊、指標", subMap, "msg");
		return subListFM;
	}
	
	//這個是 追蹤個股新聞的action 
	public URIAction subConURIAction(Entry<String, String> nextSub) {
		return new URIAction(nextSub.getKey() + "\n" + nextSub.getValue(), "https://www.above.tw/stock/"+ nextSub.getKey() +"?trick=news");
	}
	public Bubble getSubNewsBubb(Map<String, String> subMap) {
		Bubble subListFM = getSubListFM("追蹤個股", "點擊可查看該股新聞", subMap, "uri");
		return subListFM;
	}

	
	// 賺豹豹
	public Bubble getSubAbove(String title, Map<String, String> subMap) throws UnsupportedEncodingException {

		List<FlexComponent> bodyContents = new ArrayList<>();
		bodyContents.add(Text.builder().text("觀察個股").align(FlexAlign.START).color("#000000").size(FlexFontSize.XL)
				.weight(TextWeight.BOLD).flex(0).build());

		bodyContents.add(Separator.builder().color("#2ca8c1").build());

		Iterator<Entry<String, String>> subIt = subMap.entrySet().iterator();
		List<List<FlexComponent>> newsFCList = new ArrayList<>();
		while (subIt.hasNext()) {
			List<FlexComponent> infoList = new ArrayList<>();
			List<FlexComponent> newsList = new ArrayList<>();

			// 一列放兩個
			for (int i = 0; i < 2; i++) {
				Entry<String, String> next = subIt.next();
				String enK = URLEncoder.encode(next.getValue(), "utf-8");

				infoList.add(
						Button.builder().action(new MessageAction(next.getKey() + " " + next.getValue(), next.getKey()))
								.height(ButtonHeight.SMALL).style(ButtonStyle.LINK).margin(FlexMarginSize.SM)
								.gravity(FlexGravity.CENTER).build());
				newsList.add(Button.builder()
						.action(new URIAction(next.getKey() + " " + next.getValue(),
								"https://news.above.tw/all-news?k=" + enK))
						.height(ButtonHeight.SMALL).style(ButtonStyle.LINK).margin(FlexMarginSize.SM)
						.gravity(FlexGravity.CENTER).build());
				if (!subIt.hasNext()) {
					break;

				} else {
					continue;
				}
			}
			bodyContents.add(
					Box.builder().layout(FlexLayout.HORIZONTAL).contents(infoList).margin(FlexMarginSize.MD).build());
			newsFCList.add(newsList);
		}

//		bodyContents.add(Spacer.builder().size(FlexMarginSize.XL).build());

		bodyContents.add(Text.builder().text("自選新聞").align(FlexAlign.START).color("#000000").size(FlexFontSize.XL)
				.weight(TextWeight.BOLD).margin(FlexMarginSize.XXL).flex(0).build());

		bodyContents.add(Separator.builder().color("#2ca8c1").build());

		newsFCList.forEach(x -> {
			bodyContents.add(Box.builder().layout(FlexLayout.HORIZONTAL).contents(x).margin(FlexMarginSize.MD).build());
		});

		Box body = Box.builder().layout(FlexLayout.VERTICAL).spacing(FlexMarginSize.NONE).contents(bodyContents)
				.build();

		Bubble bubble = Bubble.builder().direction(FlexDirection.LTR).body(body).styles(tableBubbleStyle()).build();

		return bubble;
	}

	/**
	 * 
	 * @param title header 大標題
	 * @param detail header 小標題
	 * @param fieldsMaps 選取的欄位
	 * @param jsonOb trick GET 下來的json轉成的jsonObj
	 * @param fieldsList 欄位列, 第一個 bodyContents 要add的 Components
	 * @param valueColor 內容列的顏色
	 * @param buttonFlex 第一個欄位 如果是按鈕(包含股號/股名) 因為內容value比較長 會給大一點flex參數
	 * @param otherFlex 其他欄位的 flex參數
	 * @return
	 */
	public Bubble getTableBubb(String title, String detail, Map<String, String> fieldsMaps, JSONObject jsonOb,
			List<FlexComponent> fieldsList, String valueColor, int buttonFlex, int otherFlex) {

		List<FlexComponent> bodyContents = new ArrayList<>();
		//先把之前處理好的 欄位列 加到bodyContents
		bodyContents.add(Box.builder().layout(FlexLayout.HORIZONTAL).contents(fieldsList).build());
		bodyContents.add(Separator.builder().margin(FlexMarginSize.LG).build());
		bodyContents.add(Separator.builder().color("#ffffff").margin(FlexMarginSize.MD).build());
		
		// 拿到 jsonObj 裡面的data JsonArray
		JSONArray arr = jsonOb.getJSONArray("data");

		// arr.length()筆資料, 之前GET的時候有加參數psize=5, 所以這邊直接拿全部
		for (int i = 0; i < arr.length(); i++) {
			// 每筆 arr2.length()個欄位
			List<FlexComponent> value = new ArrayList<>();
			
			//這裡的Entry<header, 表要顯示的欄位名稱> , 這裡只需要用header 找到對應的data
			Iterator<Entry<String, String>> fieldsIt = fieldsMaps.entrySet().iterator();
			
			//處理每個欄位內容 
			while (fieldsIt.hasNext()) {
				String nextKey = fieldsIt.next().getKey();
				Object obj;
				String sym = null;
				String stockName = null;
				if (nextKey.toLowerCase().startsWith("sym")) {
					// 把一列data value轉成JSON object , 用headers 當作names, 再去拿所需要的欄位value
					// 這邊如果headers 是 Symbol的話 就把StockName一起拿了
					sym = arr.getJSONArray(i).toJSONObject(jsonOb.getJSONArray("headers")).get(nextKey).toString();
					stockName = arr.getJSONArray(i).toJSONObject(jsonOb.getJSONArray("headers")).get("StockName")
							.toString();
					obj = sym + "\n" + stockName;

				} else {
					obj = arr.getJSONArray(i).toJSONObject(jsonOb.getJSONArray("headers")).get(nextKey);

				}

				handleObj(obj, value, sym, title, nextKey, valueColor, buttonFlex, otherFlex);
			}
			bodyContents.add(Box.builder().layout(FlexLayout.HORIZONTAL).contents(value).build());
			bodyContents.add(Separator.builder().margin(FlexMarginSize.MD).color("#ffffff").build());
		}

		Box body = Box.builder().layout(FlexLayout.VERTICAL).spacing(FlexMarginSize.NONE).contents(bodyContents)
				.build();

		Bubble bubble = Bubble.builder().header(tableHeader(title, detail)).body(body).styles(tableBubbleStyle()).build();

		return bubble;
	}

	private BubbleStyles tableBubbleStyle() {
		BlockStyle headerStyle = BlockStyle.builder().backgroundColor("#FCFCFC").build();
		BubbleStyles bubbleStyle = BubbleStyles.builder().header(headerStyle).build();
		return bubbleStyle;
	}
	
	/**
	 * 
	 * @param obj 要處理的 obj
	 * @param eList 那一列data的 List<FlexComponent> 
	 * @param sym 股號
	 * @param title 大標題
	 * @param key header 用來區分要不要做成button
	 * @param valueColor data列顏色
	 * @param buttonFlex
	 * @param otherFlex 非Button欄位的Flex 參數
	 */
	private void handleObj(Object obj, List<FlexComponent> eList, String sym, String title, String key, String valueColor, int buttonFlex,int otherFlex) {
		if (obj.getClass().getSimpleName().equals("String")) {
			
				// 如果header 是 Symbol, 將 sym + stockName變成Button
				if (key.toLowerCase().startsWith("sym")) {
					eList.add(Text.builder().flex(buttonFlex).text(obj.toString())
							.action(new MessageAction(obj.toString(), sym)).weight(TextWeight.BOLD)
							.gravity(FlexGravity.CENTER).align(FlexAlign.CENTER).color("#42659A").wrap(true)
							.size(FlexFontSize.SM).build());

				} else {
					eList.add(Text.builder().text(obj.toString()).align(FlexAlign.CENTER).gravity(FlexGravity.CENTER)
							.flex(otherFlex).color(valueColor).size(FlexFontSize.SM).weight(TextWeight.REGULAR)
							.build());
				}


		// 如果是Double類型的 處理它要不要 *100 或 + %
		} else if (obj.getClass().getSimpleName().equals("Double")) {

			handleDoubleObj(title, key, eList, obj, otherFlex, valueColor);

		} else if (obj.getClass().getSimpleName().equals("Integer")) {

				eList.add(Text.builder().text(obj.toString()).wrap(true).align(FlexAlign.CENTER)
						.gravity(FlexGravity.CENTER).flex(otherFlex).color(valueColor).size(FlexFontSize.SM)
						.weight(TextWeight.REGULAR).build());


		} else {
			System.out.println("data value 是其他類型： " + obj.getClass().getSimpleName());
		}
	}

	//在table裡面value double 處理加%和留小數後第幾位的問題
	/**
	 * 
	 * @param title 大標題
	 * @param nextKey 這個data obj 的 header (從這裡判斷要不要 *100 或 +% )
	 * @param value 這一列的Components
	 * @param obj 要處理的data object
	 * @param otherFlex 非Button欄位的Flex參數
	 * @param valueColor data列的顏色
	 */
	private void handleDoubleObj(String title, String nextKey, List<FlexComponent> value, Object obj, int otherFlex,
			String valueColor) {
		// 有些比率不用 *100 + %, 只要 + %
		if (title.equals("現金分析(年)") || nextKey.contains("Margin")) {
			DecimalFormat d = new DecimalFormat("#.##");
			value.add(Text.builder().text(d.format((Double) obj) + "%").align(FlexAlign.CENTER)
					.gravity(FlexGravity.CENTER).flex(otherFlex).color(valueColor).size(FlexFontSize.SM)
					.weight(TextWeight.REGULAR).margin(FlexMarginSize.XS).build());
		// 這裡是要*100 + %的
		} else if (nextKey.endsWith("Percent") || nextKey.endsWith("Ratio") || nextKey.endsWith("Pcent")) {
			DecimalFormat d = new DecimalFormat("#.##");
			value.add(Text.builder().text(d.format((Double) obj * 100) + "%").align(FlexAlign.CENTER)
					.gravity(FlexGravity.CENTER).flex(otherFlex).color(valueColor).size(FlexFontSize.SM)
					.weight(TextWeight.REGULAR).margin(FlexMarginSize.XS).build());
		// 這裡是 不用*100, 也不用 + %
		} else {
			DecimalFormat d2 = new DecimalFormat("#.##");

			value.add(Text.builder().text(d2.format((Double) obj)).align(FlexAlign.CENTER).gravity(FlexGravity.CENTER)
					.flex(otherFlex).color(valueColor).size(FlexFontSize.SM).weight(TextWeight.REGULAR).margin(FlexMarginSize.XS)
					.build());
		}
	}
	
	//table 的default header
	/**
	 * 
	 * @param title 大標題
	 * @param detail 小標題
	 * @return
	 */
	public Box tableHeader(String title, String detail) {
		List<FlexComponent> headerContents = Arrays.asList(
				Text.builder().text(title).color("#000000").wrap(true).size(FlexFontSize.LG).weight(TextWeight.BOLD)
						.build(),
				Text.builder().text(detail).margin(FlexMarginSize.MD).flex(0).wrap(true)
						.color("#808080").size(FlexFontSize.XS).build());
		Box header = Box.builder().layout(FlexLayout.VERTICAL).contents(headerContents).build();
		return header;
	}

	// ---------------------------------------------------------------------------------------
	// 交易面bubble
	public Bubble getTradeBubble(TWSymbolQ value, String time, String date, Map<String, String> actionMap) {
		DecimalFormat d = new DecimalFormat("##.##");
		String ud;
		String udp = d.format(value.q.udp);
		String p_ud_udpColor;

		if (value.q.ud > 0) {
			ud = "▲ " + d.format(value.q.ud);
			p_ud_udpColor = "#ff0000";
		} else if (value.q.ud < 0) {
			ud = "▼ " + d.format(value.q.ud);
			p_ud_udpColor = "#4CC51F";
		} else {
			ud = d.format(value.q.ud);
			p_ud_udpColor = "#808080";
		}

		List<FlexComponent> quote = Arrays.asList(
				Text.builder().text(String.valueOf(value.q.p)).flex(0).color(p_ud_udpColor).size(FlexFontSize.XXL)
						.weight(TextWeight.BOLD).wrap(true).build(),
				new Filler(),
				Text.builder().text(ud).flex(0).color(p_ud_udpColor).wrap(true).size(FlexFontSize.Md)
						.weight(TextWeight.BOLD).build(),
				new Filler(), Text.builder().text(udp).flex(0).color(p_ud_udpColor).wrap(true).size(FlexFontSize.Md)
						.weight(TextWeight.BOLD).build());

		List<FlexComponent> footerContents = new ArrayList<>();

		actionMap.forEach((x, y) -> {
			// 如果 action的value是https開頭的那就是URIAction
			if (y.startsWith("https")) {
				footerContents.add(Button.builder().style(ButtonStyle.LINK).action(new URIAction(x, y)).build());
			
			// PostbackAction 
			} else if(y.startsWith("action=")){
				// 這個判斷只是要區分Button顏色, 如果Action的label是"從追蹤清單移除", 他的Button就給紅色
				if(!x.equals("從追蹤清單移除")) {
					footerContents.add(Button.builder().style(ButtonStyle.LINK).action(new PostbackAction(x, y)).build());
					
				}else {
					footerContents.add(Button.builder().style(ButtonStyle.LINK).color("#C74242")
							.action(new PostbackAction(x, y)).build());
					
				}

			// MessageAction 
			} else {
				footerContents.add(Button.builder().style(ButtonStyle.LINK).action(new MessageAction(x, y)).build());	

			}

		});

		List<FlexComponent> bodyContents = Arrays.asList(
				Text.builder().text(value.bi.id + " " + value.bi.nm).flex(1).color("#000000").size(FlexFontSize.LG)
						.weight(TextWeight.BOLD).wrap(true).build(),
				Box.builder().layout(FlexLayout.BASELINE).flex(1).contents(quote).build(),
				Text.builder().text(date + " " + time).flex(0).color("#808080").margin(FlexMarginSize.MD)
						.size(FlexFontSize.XS).wrap(true).build()

		);

		Box body = Box.builder().layout(FlexLayout.VERTICAL).spacing(FlexMarginSize.MD).contents(bodyContents).build();
		Box footer = Box.builder().layout(FlexLayout.VERTICAL).spacing(FlexMarginSize.SM).contents(footerContents)
				.build();

		BlockStyle bodyStyle = BlockStyle.builder().backgroundColor("#FCFCFC").build();
		BubbleStyles bubbleStyle = BubbleStyles.builder().body(bodyStyle).build();

		Bubble bubble = Bubble.builder().body(body).footer(footer).styles(bubbleStyle).build();

		return bubble;
	}
	
	// handle第一列table(通常是欄位名) 
	/**
	 * 
	 * @param ButtonFlex 第一個欄位 如果是按鈕(包含股號/股名) 因為內容value比較長 會給大一點flex參數
	 * @param otherFlex 其他欄位的 flex參數
	 * @param fieldsMaps 所選取的欄位map
	 * @param fieldColor 欄位的顏色
	 * @return
	 */
	private List<FlexComponent> handleFieldFlex(int ButtonFlex, int otherFlex, Map<String, String> fieldsMaps,
			String fieldColor) {
		List<FlexComponent> fieldsList = new ArrayList<>();
		Iterator<Entry<String, String>> fieldsIt = fieldsMaps.entrySet().iterator();
		// 處理第一列 欄位
		while (fieldsIt.hasNext()) {
			Entry<String, String> next = fieldsIt.next();
			// 表要顯示的欄位名稱
			String nextValue = next.getValue();
			// header 
			String nextKey = next.getKey();
			
			// header 拿下來如果是股號 就做成button（或有action的 text）
			if (nextKey.toLowerCase().startsWith("sym")) {
				fieldsList.add(Text.builder().text(nextValue).wrap(true).align(FlexAlign.CENTER)
						.gravity(FlexGravity.CENTER).flex(ButtonFlex).color(fieldColor).size(FlexFontSize.SM)
						.weight(TextWeight.REGULAR).build());
			} else {
				fieldsList.add(Text.builder().text(nextValue).wrap(true).align(FlexAlign.CENTER)
						.gravity(FlexGravity.CENTER).flex(otherFlex).color(fieldColor).size(FlexFontSize.SM)
						.weight(TextWeight.REGULAR).build());
			}

		}
		return fieldsList;
	}

	//知識補給 -> 報章影音免費看會出現的carousel
	public Carousel getLinkCarousel() {
		Map<String, String> linkMap = new HashMap<>();
		linkMap.put("豹投資Blog", "https://blog.above.tw/");
		linkMap.put("狙擊手Blog", "https://blog.usstocks.com.tw");
		linkMap.put("狙擊手Youtube頻道", "https://www.youtube.com/channel/UC0ZqcI3wssgZQOAjkLfN43g");

		linkMap.put("豹投資-個股趨勢", "https://www.above.tw/stock/");
		linkMap.put("豹投資-關鍵排行", "https://www.above.tw/rank/");
		linkMap.put("豹投資-市場脈動", "https://www.above.tw/market/");
		linkMap.put("豹投資粉專", "https://www.facebook.com/AboveTW/");
		linkMap.put("狙擊手粉專", "https://www.facebook.com/forUSstocks/");
		linkMap.put("逸志公司官網", "https://corp.above.tw/");

		List<Map.Entry<String, String>> entryList = new ArrayList<Map.Entry<String, String>>(linkMap.entrySet());

		Collections.shuffle(entryList);

		List<Bubble> bubbleList = new ArrayList<>();

		for (int i = 0; i < 3; i++) {
			List<Entry<String, String>> list = new ArrayList<>();
			list.add(entryList.remove(0));
			list.add(entryList.remove(0));
			list.add(entryList.remove(0));
			bubbleList.add(getLinkBubb(list));
		}

		Carousel carousel = Carousel.builder().contents(bubbleList).build();

		return carousel;
	}

	//在https://news.above.tw/all-news 將所有用戶追蹤的股名當作關鍵字查詢
	public String getNewsURL(Set<String> subSet) throws UnsupportedEncodingException {
		if (subSet.isEmpty()) {
			String url = "https://news.above.tw/all-news";
			return url;
		}

		Iterator<String> subIt = subSet.iterator();
		StringBuilder sb = new StringBuilder();

		while (subIt.hasNext()) {
			String next = subIt.next();

			if (subIt.hasNext()) {
				sb.append(next + " ");

			} else {
				sb.append(next);
			}

		}

		String enK = URLEncoder.encode(sb.toString(), "utf-8");

		return "https://news.above.tw/all-news?k=" + enK;
	}
	
	//用戶還沒完成新手任務就點擊“追蹤清單”會出現的Message
	public Bubble getTryGetSubListBubble() {
		List<FlexComponent> bodyContents = Arrays
				.asList(Text.builder().text("啊呀，您目前還未建立一份方便實用的個股「追蹤清單」，要不要花一分鐘快速上手呢 🤗？").margin(FlexMarginSize.LG)
						.size(FlexFontSize.SM).align(FlexAlign.START).weight(TextWeight.REGULAR).wrap(true).build());
		Box body = Box.builder().layout(FlexLayout.VERTICAL).spacing(FlexMarginSize.NONE).contents(bodyContents)
				.build();
		List<FlexComponent> footerContents = Arrays
				.asList(Button.builder().action(new PostbackAction("好，只能花一分鐘喔", "action=tryGetSubList&itemId=true"))
						.height(ButtonHeight.SMALL).build());
		Box footer = Box.builder().layout(FlexLayout.VERTICAL).flex(0).spacing(FlexMarginSize.SM)
				.contents(footerContents).build();

		BlockStyle bodyStyle = BlockStyle.builder().backgroundColor("#ffffff").build();

		BubbleStyles bubbleStyle = BubbleStyles.builder().body(bodyStyle).build();

		Bubble bubble = Bubble.builder().body(body).footer(footer).styles(bubbleStyle).build();
		return bubble;
	}
	
	
	
	//做簡單的image map message 用linebot.prj.tw/pushMsg.html push 
	//可以用line bot Designer json取代
	public ImagemapMessage getImagemapMsg(String mapBaseUrl, String mapAltText, String mapLinkUri) {
		// 點擊的有效區域
		URIImagemapAction uriAction = new URIImagemapAction(mapLinkUri, new ImagemapArea(0, 0, 1040, 400));
		List<ImagemapAction> actionLst = Arrays.asList(uriAction);
		ImagemapMessage imm = ImagemapMessage.builder().baseSize(new ImagemapBaseSize(400, 1040)).baseUrl(mapBaseUrl)
				.altText(mapAltText).actions(actionLst).build();
		return imm;
	}
	
	public FlexMessage getThermalMsg() {
		List<FlexComponent> bodyContents = new ArrayList<>();
		bodyContents.add(Button.builder().action(new URIAction("盤中大盤熱力圖", "https://www.above.tw/market?trick=thermal")).style(ButtonStyle.LINK).build());
		Box body = Box.builder().layout(FlexLayout.VERTICAL).contents(bodyContents).build();

		Bubble bubble = Bubble.builder().body(body).build();
		FlexMessage fm = FlexMessage.builder().altText("盤中大盤熱力圖").contents(bubble).build();
		return fm;
	}

}
