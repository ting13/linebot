package app.bot;

import java.util.Calendar;
import java.util.TimeZone;

import app.twquotecore.TWQCore;
import excp.NoThrow;
import mars.app.AppDir;
import mars.app.TomlApp;
import mars.app.config.EApp;
import opt.Opt;
import time.Timing;
import timer.ITimer;

/**
 * @author wayne on 2018/9/7.
 */
public class BotApp extends TomlApp<BotApp> {
    Bot m_bot;
    
    Boolean m_bThermalPushed = false;
    Boolean m_bNewsPushed = false;
    Boolean m_bMarketOpen = false;
    int m_curDay = 0;
    Boolean m_bMarketOpenChecked = false;

    protected BotApp(AppDir appDir) {
        super(EApp.bot, appDir, _Bot.VERSION);
        // 還沒有要直接拉資料 所以可能先用不到
        // Sf.init();
        // Sf2.init();
        // SymField.init();
    }

    @Override
	protected boolean _isSelfAlive() {
        return true;
    }

    @Override
	protected void _extraCheck() {

    }

    @Override
	protected void _exec() {
        NoThrow.act(() -> {
            m_bot = new Bot(this);

            // 確定有 toml config 裡面設定 backend 為 1 才開始 run backend (default 是不跑 -> 因為前端會持續上版測試)
            Opt<Integer> optBackend = tomlInt("backend");
            if (optBackend.hasValue() && optBackend.get() == 1) {
                m_bot.start();
            }
        });
        
        //每兩秒跑一次
        ITimer.getDefault().period("linebotRT", Timing.ofSec(2), () -> NoThrow.act(() -> {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Taipei"));
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int min = cal.get(Calendar.MINUTE);

            // 處理換日
            if (day != m_curDay) {
            	//訊息發過了沒
                m_bThermalPushed = false;
                m_bNewsPushed = false;
                
                //是不是交易日 (日期)
                m_bMarketOpen = false;
               
                //檢查過交易入了沒
                m_bMarketOpenChecked = false;
                
                //今天的日期
                m_curDay = day;
            }

            // 處理開盤狀態判定
            if (m_bMarketOpenChecked == false && hour >= 8) {
                m_bMarketOpen = TWQCore.isMarketOpen();
                 m_bMarketOpenChecked = true;
            }


            // 處理 sendMsg
            // 開盤日9:02把 盤中熱力大盤圖push給使用者
            if (m_bMarketOpen && m_bThermalPushed == false) {
            	//時間
                if (hour == 9 && min == 2) {
                	m_bot.sendOutThermalToLineUsers();
                    m_bThermalPushed = true;
                }
            }
            
            // 處理 sendMsg
            // 收盤後兩點 把個別用戶的自選股新聞push給使用者
            if (m_bMarketOpen && m_bNewsPushed == false) {
            	//時間
                if (hour == 14 && min == 0) {
                	
                	//TODO 這裡要寫成開多個thread 去push??
                	m_bot.sendOutNewsToLineUsers();
                	m_bNewsPushed = true;
                }
            }
        }));
        
    }

    public static void main(String[] args) {
        new BotApp(AppDir.of(args))
        	.startAndWait();
    }
}
