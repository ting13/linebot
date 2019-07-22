package app.bot.entity;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;

import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.flex.component.Text;
import com.linecorp.bot.model.message.flex.component.Text.TextWeight;
import com.linecorp.bot.model.message.flex.unit.FlexAlign;
import com.linecorp.bot.model.message.flex.unit.FlexFontSize;
import com.linecorp.bot.model.message.flex.unit.FlexGravity;

import aphttp.ApHttp;
import app.bot.tricks.TRt;
import times.Day;
import times.Month;
import twhist.TWQHist;
import aphttp.ApHttp;

public class Test {
	public static void main(String[] args) throws ClientProtocolException, IOException, SQLException {
//		Request getMsg = ApHttp.ofGetRequest(
//				"https://testtrick.prj.tw/json/RankChips_II3SellBuyNetRank?ed=2018-11-09&exg=Tse&sd=2018-11-08");
//		String response = getMsg.execute().returnContent().asString();
//
//		JSONObject jsonOb = new JSONObject(response);
//		JSONArray arr = jsonOb.getJSONArray("data");
//		Object obj = arr.getJSONArray(0).toJSONObject(jsonOb.getJSONArray("headers")).get("FIIBuyPercent");
//		if (obj.getClass().isInstance(String.class)) {
//			System.out.println("String !!");
//		} else if (obj.getClass().isInstance(Double.class)) {
//			System.out.println("Double !!");
//		} else {
//			System.out.println("others !!");
//			System.out.println(obj.getClass().getSimpleName().equals("Double") );
//		}

//	    Pattern pattern = Pattern.compile("^symbol=.*");
//	    Matcher matcher = pattern.matcher("DealerTradeBuyPercent");
//	    if(matcher.find()) {
//	    	System.out.println("匹配");
//	    }
//		String str = "DealerTradeBuyPercent";
//		boolean endsWith = str.toLowerCase().endsWith("percent");
//		boolean matches = Pattern.matches(".*[Percent]$", "Price");
//		System.out.println(endsWith);
//		DecimalFormat d1 = new DecimalFormat("#.##");
//
//		DecimalFormat d2 = new DecimalFormat("0.00");
//		Double d = 4333.0;
//		String format = d1.format((Double) d); 
//		System.out.println(format);
		// sd
//		Day lastDay = Month.of(TWQHist.getLatestTradeDate()).prev().prev().lastDay();
//		//ed
//		Day lastDay2 = Month.of(TWQHist.getLatestTradeDate()).prev().lastDay();
//		System.out.println(lastDay2);
//		String str = "OpearatingMarginPer";
//		if(str.contains("Margin")) {
//			System.out.println("匹配");
//		}

//		Request getMsg = ApHttp.ofGetRequest("https://testwww.above.tw/json/RankChips_StkMajHold?day=2018-12-17&exg=TseOtc&numSeq=15&psize=5&p=1");
//		String response = getMsg.execute().returnContent().asString();
//
//		System.out.println(new String(response.getBytes("iso8859-1"), "utf-8"));

//		Date date = new Date();  
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
//        String dateNowStr = sdf.format(date); 
//        System.out.println(dateNowStr);

//		String s = "將233bgfhfhjgj0加入觀察";
//		// 把要匹配的字符串写成正则表达式，然后要提取的字符使用括号括起来
//		// 在这里，我们要提取最后一个数字，正则规则就是“一个数字加上大于等于0个非数字再加上结束符”
//		Pattern pattern = Pattern.compile("將(.*)加入觀察");
//		Matcher matcher = pattern.matcher(s);
//		if (matcher.find())
//			System.out.println(matcher.group(1));

		
//		Request getMsg = ApHttp.ofGetRequest("https://testwww.above.tw/symName?sym=00700");
//		String name = getMsg.execute().returnContent().asString();
//		System.out.println(name.equals("NA"));
//		SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd");
//		String date = datef.format(System.currentTimeMillis());
//		System.out.println(date);
//		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei"));
//        int day = cal.get(Calendar.DAY_OF_MONTH);
//        int hour = cal.get(Calendar.HOUR_OF_DAY);
//        int min = cal.get(Calendar.MINUTE);
//        System.out.println(min);
//		String tableName = "開盤前三分鐘量增30%";
//		long start = System.currentTimeMillis();
//		if(!(TRt.getEnum(tableName) == null )) {
//			System.out.println(TRt.getEnum(tableName).getFieldsMaps());
//		}else {
//			System.out.println("error");
//		}
//		long end = System.currentTimeMillis();
//		System.out.println(end - start);
		for (TRt e : TRt.values()) {
            System.out.println(e);
        }
		
		
	}



	
}
