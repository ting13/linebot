package app.bot;

import mars.app.config.ClodPort;

import java.text.SimpleDateFormat;

/**
 * @author wayne on 2018/9/7.
 */
public class _Bot {
    static public String sm_botResourcePath = "/res/";
    static public int sm_fileCacheSize = 100;

    static public int sm_BotPort = ClodPort.BotWeb.port();

    public static String VERSION = "v0.1-bot 初始化版本";

    public static String sm_sDateFormat = "yyyy-MM-dd";
    public static SimpleDateFormat dFormat = new SimpleDateFormat(_Bot.sm_sDateFormat);

    //add by wayne
    static public String sm_corsAllowDomain = "*";
}
