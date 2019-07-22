package app.notify;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import app.bot.entity.ConnToMysql;

public class ConnNotifyDB extends ConnToMysql{
	//新id加到mysql
	public void insert(String token, String type, String name) throws SQLException {
		con = getConn();
		st = con.createStatement();
		String sql = "insert into line_notify values('"+token+"', '"+type+"', '"+name+"')";
		
		st.executeUpdate(sql);
		st.close();
		con.close();

	}
	
    public void deleteToken(String token) throws SQLException {
    	con = getConn();
		st = con.createStatement();
		String sql = "delete from line_notify where token='"+token+"'";

		st.executeUpdate(sql);
		st.close();
		con.close();
    }

	//要push 的 id
    public Set<String> getToken() throws SQLException {
    	Set<String> tokenSet = new HashSet<>();
    	con = getConn();
		st = con.createStatement();
		String sql = "select token from line_notify";
		String token = null;

		ResultSet rs = st.executeQuery(sql);
		while (rs.next()) {
			// 獲取id
			token = rs.getString("token");
			//放到set
			tokenSet.add(token);
			System.out.println("token--->"+token);
		}
		rs.close();
		con.close();
		return tokenSet;
    }

}
