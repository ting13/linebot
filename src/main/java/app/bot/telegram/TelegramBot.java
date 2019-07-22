package app.bot.telegram;

import static java.lang.Math.toIntExact;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.client.fluent.Request;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.ChosenInlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import aphttp.ApHttp;


public class TelegramBot extends TelegramLongPollingBot{
	@Override
	public void onUpdateReceived(Update update) {
		//@AboveTelegramBot...
		if (update.hasInlineQuery()) {
			InlineQuery inlineQuery = update.getInlineQuery();
			String query = inlineQuery.getQuery();
//			update.getChosenInlineQuery();
			if(query.equals("news")) {

	        	InlineQueryResult r1 = new InlineQueryResultArticle()
	        			.setId("n")
	        			.setTitle("title")
	        			.setInputMessageContent(new InputTextMessageContent().setMessageText("test inline query1"));
	        	
	        	InlineQueryResult r2 = new InlineQueryResultArticle()
						.setId("m")
						.setTitle("title2")
						.setInputMessageContent(new InputTextMessageContent().setMessageText("test inline query2"));
	        	
	        	
	        	try {
					execute(new AnswerInlineQuery().setInlineQueryId(inlineQuery.getId()).setResults(r1, r2));
				} catch (TelegramApiException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else if(!query.isEmpty()) {
				Request req = ApHttp.ofGetRequest("https://mac.above.tw/q?type=tw&cat=tseotc&word="+query);
				String resp = null;
				try {
					resp = req.execute().returnContent().asString();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				JSONArray jsonOb = null;
				try {
					jsonOb = new JSONArray(new String(resp.getBytes("iso8859-1"), "utf-8"));
				} catch (JSONException | UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				List<InlineQueryResult> iResult = new ArrayList<>();
				
				jsonOb.forEach(x->{
					iResult.add(
							new InlineQueryResultArticle()
							.setId("m")
							.setTitle(x.toString())
							.setInputMessageContent(new InputTextMessageContent().setMessageText(x.toString())));
					
				});
					        	
	        	
	        	try {
					execute(new AnswerInlineQuery().setInlineQueryId(inlineQuery.getId()).setResults(iResult));
				} catch (TelegramApiException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
			
		}else if (update.hasMessage() && update.getMessage().hasText()) {
	        // Set variables
	        String message_text = update.getMessage().getText();
	        long chat_id = update.getMessage().getChatId();
	        BotApiMethod<Message> message;
	        
	        if(message_text.equals("/start")) {
	        	message = new SendMessage() // Create a message object object
		                .setChatId(chat_id)
		                .setText("start!");
	        	List<KeyboardRow> kbl = new ArrayList<>();
		        KeyboardRow krR1 = new KeyboardRow();
		        KeyboardRow krR2 = new KeyboardRow();
		        krR1.add(new KeyboardButton().setText("豹新聞"));
		        krR1.add(new KeyboardButton().setText("AI指標判讀"));
		        krR1.add(new KeyboardButton().setText("即時豹選股"));
		        kbl.add(krR1);
		        
		        krR2.add(new KeyboardButton().setText("賺豹豹"));
		        krR2.add(new KeyboardButton().setText("豹小妞豹輕鬆"));
		        krR2.add(new KeyboardButton().setText("大數據豹分析"));
		        kbl.add(krR2);
		        
		        //default ketbord
		        ReplyKeyboardMarkup rkm = new ReplyKeyboardMarkup()
		        		.setResizeKeyboard(true)
		        		.setKeyboard(kbl);
		        
		        ((SendMessage) message).setReplyMarkup(rkm);
		        
	        }else if(message_text.equals("大數據豹分析")) {
	        	InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                List<InlineKeyboardButton> rowInline = new ArrayList<>();
                rowInline.add(new InlineKeyboardButton().setText("市場脈動").setCallbackData("市場脈動"));
                rowInline.add(new InlineKeyboardButton().setText("期貨選擇權").setCallbackData("期貨選擇權"));
                rowInline.add(new InlineKeyboardButton().setText("關鍵排行").setCallbackData("關鍵排行"));
                // Set the keyboard to the markup
                rowsInline.add(rowInline);
                // Add it to the message
                markupInline.setKeyboard(rowsInline);
                
	        	message = new SendMessage() // Create a message object object
		                .setChatId(chat_id)
		                .setReplyMarkup(markupInline)
		                .setText(message_text);
	        	
	        }else {
	        	message = new SendMessage() 
		                .setChatId(chat_id)
		                .setText(message_text);
	        
	        }
	        
	        try {

	        	execute(message); // Sending our message object to user
	        } catch (TelegramApiException e) {
	            e.printStackTrace();
	        }
	    }else if (update.hasCallbackQuery()) {
            // Set variables
            String call_data = update.getCallbackQuery().getData();
            long message_id = update.getCallbackQuery().getMessage().getMessageId();
            long chat_id = update.getCallbackQuery().getMessage().getChatId();

            if (call_data.equals("市場脈動") || call_data.equals("期貨選擇權") ||call_data.equals("關鍵排行")) {
            	//TODO 改成method 
            	InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                List<InlineKeyboardButton> rowInline = new ArrayList<>();
               
                if(call_data.equals("市場脈動")) {
                	rowsInline.add(Arrays.asList(new InlineKeyboardButton().setText("法人買賣超-產業").setUrl("https://www.above.tw/market?trick=II3NetBuy")));
                	rowsInline.add(Arrays.asList(new InlineKeyboardButton().setText("法人買賣超-產業").setUrl("https://www.above.tw/market?trick=II3NetBuy")));
                	rowsInline.add(Arrays.asList(new InlineKeyboardButton().setText("法人買賣超-產業").setUrl("https://www.above.tw/market?trick=II3NetBuy")));
                	rowsInline.add(Arrays.asList(new InlineKeyboardButton().setText("法人買賣超-產業").setUrl("https://www.above.tw/market?trick=II3NetBuy")));
                	rowsInline.add(Arrays.asList(new InlineKeyboardButton().setText("法人買賣超-產業").setUrl("https://www.above.tw/market?trick=II3NetBuy")));
                	rowsInline.add(Arrays.asList(new InlineKeyboardButton().setText("法人買賣超-產業").setUrl("https://www.above.tw/market?trick=II3NetBuy")));
                	rowsInline.add(Arrays.asList(new InlineKeyboardButton().setText("法人買賣超-產業").setUrl("https://www.above.tw/market?trick=II3NetBuy")));
                	rowsInline.add(Arrays.asList(new InlineKeyboardButton().setText("法人買賣超-產業").setUrl("https://www.above.tw/market?trick=II3NetBuy")));
                	rowsInline.add(Arrays.asList(new InlineKeyboardButton().setText("法人買賣超-產業").setUrl("https://www.above.tw/market?trick=II3NetBuy")));

                
                	rowInline.add(new InlineKeyboardButton().setText("期貨選擇權").setCallbackData("期貨選擇權"));
                    rowInline.add(new InlineKeyboardButton().setText("關鍵排行").setCallbackData("關鍵排行"));
                }else if(call_data.equals("期貨選擇權")){
                	rowsInline.add(Arrays.asList(new InlineKeyboardButton().setText("台指期大額交易人部位變動").setUrl("https://www.above.tw/market?trick=FutHugeTraderChg")));
                	rowsInline.add(Arrays.asList(new InlineKeyboardButton().setText("台指期大額交易人部位變動").setUrl("https://www.above.tw/market?trick=FutHugeTraderChg")));
                	rowsInline.add(Arrays.asList(new InlineKeyboardButton().setText("台指期大額交易人部位變動").setUrl("https://www.above.tw/market?trick=FutHugeTraderChg")));
                	
                	
                    rowInline.add(new InlineKeyboardButton().setText("市場脈動").setCallbackData("市場脈動"));
                    rowInline.add(new InlineKeyboardButton().setText("關鍵排行").setCallbackData("關鍵排行"));
                }else { //關鍵排行
                	rowsInline.add(Arrays.asList(new InlineKeyboardButton().setText("法人買賣超佔股本").setUrl("https://www.above.tw/rank?trick=II3SellBuyNetRank")));
                	rowsInline.add(Arrays.asList(new InlineKeyboardButton().setText("法人買賣超佔股本").setUrl("https://www.above.tw/rank?trick=II3SellBuyNetRank")));
                	rowsInline.add(Arrays.asList(new InlineKeyboardButton().setText("法人買賣超佔股本").setUrl("https://www.above.tw/rank?trick=II3SellBuyNetRank")));
                	rowsInline.add(Arrays.asList(new InlineKeyboardButton().setText("法人買賣超佔股本").setUrl("https://www.above.tw/rank?trick=II3SellBuyNetRank")));
                	rowsInline.add(Arrays.asList(new InlineKeyboardButton().setText("法人買賣超佔股本").setUrl("https://www.above.tw/rank?trick=II3SellBuyNetRank")));
                	rowsInline.add(Arrays.asList(new InlineKeyboardButton().setText("法人買賣超佔股本").setUrl("https://www.above.tw/rank?trick=II3SellBuyNetRank")));
                	rowsInline.add(Arrays.asList(new InlineKeyboardButton().setText("法人買賣超佔股本").setUrl("https://www.above.tw/rank?trick=II3SellBuyNetRank")));
                	rowsInline.add(Arrays.asList(new InlineKeyboardButton().setText("法人買賣超佔股本").setUrl("https://www.above.tw/rank?trick=II3SellBuyNetRank")));
                	rowsInline.add(Arrays.asList(new InlineKeyboardButton().setText("法人買賣超佔股本").setUrl("https://www.above.tw/rank?trick=II3SellBuyNetRank")));
                	rowsInline.add(Arrays.asList(new InlineKeyboardButton().setText("法人買賣超佔股本").setUrl("https://www.above.tw/rank?trick=II3SellBuyNetRank")));
                	rowsInline.add(Arrays.asList(new InlineKeyboardButton().setText("法人買賣超佔股本").setUrl("https://www.above.tw/rank?trick=II3SellBuyNetRank")));
                	rowsInline.add(Arrays.asList(new InlineKeyboardButton().setText("法人買賣超佔股本").setUrl("https://www.above.tw/rank?trick=II3SellBuyNetRank")));
                	rowsInline.add(Arrays.asList(new InlineKeyboardButton().setText("法人買賣超佔股本").setUrl("https://www.above.tw/rank?trick=II3SellBuyNetRank")));
                
                	
                    rowInline.add(new InlineKeyboardButton().setText("市場脈動").setCallbackData("市場脈動"));
                    rowInline.add(new InlineKeyboardButton().setText("期貨選擇權").setCallbackData("期貨選擇權"));
                }
                

                // Set the keyboard to the markup
                rowsInline.add(rowInline);
                // Add it to the message
                markupInline.setKeyboard(rowsInline);
                
                
                String answer = "--"+call_data+"!!update!!--";
                EditMessageText new_message = new EditMessageText()
                        .setChatId(chat_id)
                        .setMessageId(toIntExact(message_id))
                        .setReplyMarkup(markupInline)
                        .setText(answer);
                try {
                    execute(new_message); 
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
		
	}

	@Override
	public String getBotUsername() {
		// TODO Auto-generated method stub
		return "AboveTelegramBot";
	}

	@Override
	public String getBotToken() {
		// TODO Auto-generated method stub
		return "798229718:AAHTgF6Znk3agvv-lHmK1vdHCwb6niWHoso";
	}
	


}
