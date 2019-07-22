package app.bot.tricks;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import lst.EnumLst;

//即時策略
public enum TRt {
	三大法人買進重點量增幅("三大法人買進重點量增幅", "https://testwww.above.tw/json/TRt_volIncII3?psize=5&p=1"){
		
		@Override
		public Map<String, String> getFieldsMaps() {
			Map<String, String> fieldsMaps = new LinkedHashMap<>();
			fieldsMaps.put("Symbol", "股號/股名");
			fieldsMaps.put("Price", "成交價");
			fieldsMaps.put("PriceChgPcent", "漲跌\n百分比\n(%)");
			fieldsMaps.put("RTLatestVol", "最新\n一筆\n成交量");
			
			return fieldsMaps;
		}
	},
	主力買進重點量增幅("主力買進重點量增幅", "https://testwww.above.tw/json/TRt_volIncMForce?psize=5&p=1"){
		@Override
		public Map<String, String> getFieldsMaps() {	
			Map<String, String> fieldsMaps = new LinkedHashMap<>();
			fieldsMaps.put("Symbol", "股號/股名");
			fieldsMaps.put("Price", "成交價");
			fieldsMaps.put("PriceChgPcent", "漲跌\n百分比\n(%)");
			fieldsMaps.put("RTLatestVol", "最新\n一筆\n成交量");
			
			return fieldsMaps;
		}
	},
	連5筆外盤成交("連5筆外盤成交", "https://testwww.above.tw/json/TRt_cont5Buy?psize=5&p=1"){
		@Override
		public Map<String, String> getFieldsMaps() {
			Map<String, String> fieldsMaps = new LinkedHashMap<>();
			fieldsMaps.put("Symbol", "股號/股名");
			fieldsMaps.put("Price", "成交價");
			fieldsMaps.put("PriceChgPcent", "漲跌\n百分比\n(%)");
			fieldsMaps.put("RTTradeVol", "成交\n張數");

			return fieldsMaps;
		}
		
	},
	盤中連三高("盤中連三高", "https://testwww.above.tw/json/TRt_cont3High?psize=5&p=1"){
		@Override
		public Map<String, String> getFieldsMaps() {
			Map<String, String> fieldsMaps = new LinkedHashMap<>();
			fieldsMaps.put("Symbol", "股號/股名");
			fieldsMaps.put("Price", "成交價");
			fieldsMaps.put("PriceChgPcent", "漲跌\n百分比\n(%)");
			fieldsMaps.put("RTTradeVol", "成交\n張數");

			return fieldsMaps;
		}
	},
	盤中緩步增長("盤中緩步增長", "https://testwww.above.tw/json/TRt_slowGrowing?psize=5&p=1"){
		@Override
		public Map<String, String> getFieldsMaps() {
			Map<String, String> fieldsMaps = new LinkedHashMap<>();
			fieldsMaps.put("Symbol", "股號/股名");
			fieldsMaps.put("Price", "成交價");
			fieldsMaps.put("PriceChgPcent", "漲跌\n百分比\n(%)");
			fieldsMaps.put("RTTradeVol", "成交\n張數");
			
			return fieldsMaps;
		}
	},
	成交價跳三檔("成交價跳三檔", "https://testwww.above.tw/json/TRt_tickLeap3?psize=5&p=1"){
		@Override
		public Map<String, String> getFieldsMaps() {
			Map<String, String> fieldsMaps = new LinkedHashMap<>();
			fieldsMaps.put("Symbol", "股號/股名");
			fieldsMaps.put("Price", "成交價");
			fieldsMaps.put("PriceChgPcent", "漲跌\n百分比\n(%)");
			fieldsMaps.put("RTTradeVol", "成交\n張數");
			
			return fieldsMaps;
		}
	},
	開盤前三分鐘量增30("開盤前三分鐘量增30%","https://testwww.above.tw/json/TRt_open3MinVolInc30P?psize=5&p=1"){
		@Override
		public Map<String, String> getFieldsMaps() {
			Map<String, String> fieldsMaps = new LinkedHashMap<>();
			fieldsMaps.put("Symbol", "股號/股名");
			fieldsMaps.put("Price", "成交價");
			fieldsMaps.put("PriceChgPcent", "漲跌\n百分比\n(%)");
			fieldsMaps.put("RTTradeVol", "成交\n張數");
			
			return fieldsMaps;
		}
	},
	前三分量增20("前三分量增20%", "https://testwww.above.tw/json/TRt_open3MinVolIncYest20P?psize=5&p=1"){
		@Override
		public Map<String, String> getFieldsMaps() {
			Map<String, String> fieldsMaps = new LinkedHashMap<>();
			fieldsMaps.put("Symbol", "股號/股名");
			fieldsMaps.put("Price", "成交價");
			fieldsMaps.put("PriceChgPcent", "漲跌\n百分比\n(%)");
			fieldsMaps.put("RTTradeVol", "成交\n張數");

			return fieldsMaps;
		}
	},
	開盤量倍增("開盤量倍增", "https://testwww.above.tw/json/TRt_openVolDouble?psize=5&p=1"){
		@Override
		public Map<String, String> getFieldsMaps() {
			Map<String, String> fieldsMaps = new LinkedHashMap<>();
			fieldsMaps.put("Symbol", "股號/股名");
			fieldsMaps.put("Price", "成交價");
			fieldsMaps.put("PriceChgPcent", "漲跌\n百分比\n(%)");
			fieldsMaps.put("RTTradeVol", "成交\n張數");

			return fieldsMaps;
		}
	},
	開盤價量齊揚("開盤價量齊揚", "https://testwww.above.tw/json/TRt_openPVBurst?psize=5&p=1"){
		@Override
		public Map<String, String> getFieldsMaps() {
			Map<String, String> fieldsMaps = new LinkedHashMap<>();
			fieldsMaps.put("Symbol", "股號/股名");
			fieldsMaps.put("Price", "成交價");
			fieldsMaps.put("PriceChgPcent", "漲跌\n百分比\n(%)");
			fieldsMaps.put("RTTradeVol", "成交\n張數");
			
			return fieldsMaps;
		}
	};
	
	String m_tableName;
	String m_trickURL;
	Map<String, String> m_fieldsMaps;
	

	TRt(String name, String trickURL) {
		m_tableName = name;
		m_trickURL = trickURL;
	}
	
	public static Set<String> getAllNames(){
		Set<String> nameSet = new HashSet<>();
		EnumSet.allOf(TRt.class).forEach(x ->{
			nameSet.add(x.m_tableName);
		});
		
		return nameSet;
		
	}
	
	
	
	public String getName() {
		return m_tableName;
	}

	public String getTrickURL() {
		return m_trickURL;
	}

	public Map<String, String> getFieldsMaps() {
		return m_fieldsMaps;
	}

	@Override
	public String toString() {
		return this.m_tableName;
	}
	
	//
	public static TRt getEnum(String name) {
		for (TRt v : values())
			if (v.getName().equalsIgnoreCase(name))
				return v;
		return null;
	}
	
}