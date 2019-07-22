package app.bot.entity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConnToMysql {
	protected Connection con;
	protected Statement st;
	
//	public final String table = "line_id_testbot";
	public final String table = "line_id_bot";
	public final String url = "jdbc:mysql://pvu1.prj.tw:29556/linebot";
	public final String mysqlname = "com.mysql.jdbc.Driver";
	public final String user = "linebot";
	public final String password = "aboveLine284cl3";
	
	//新id加到mysql
	public void insertId(String id, String displayName, String type) throws SQLException {
		con = getConn();
		String sql = null;
		PreparedStatement ps;
		
		if (type.equals("user")) {
			sql = "insert into "+table+"(id, displayName, type, date)values(?,?,?,?)";
			ps = con.prepareStatement(sql);  
			ps.setString(1, id);  
			ps.setString(2, displayName); 
			ps.setString(3, type); 
			ps.setDate(4, new java.sql.Date(System.currentTimeMillis()));  
		
		}else {
			//因為line不提供群組顯示的名稱, 所以不插入displayName
			sql = "insert into "+table+"(id, type, date)values(?,?,?)";
			ps = con.prepareStatement(sql);  
		
			ps.setString(1, id);  
			ps.setString(2, type); 
			ps.setDate(3, new java.sql.Date(System.currentTimeMillis()));  
		}
		       
        ps.executeUpdate();  

		con.close();

	}
	
	/**
	 * 
	 * @param column
	 * @param 1-> true, 0-> false
	 * @param id
	 * @throws SQLException
	 */
	public void updateStatus(String column, int isDelete, String id) throws SQLException {
		String sql = "update "+table+" set "+ column +"='" + isDelete + "' where id='" + id + "'";
		update(sql);
		
	}
	
	public void update(String sql) throws SQLException {
		con = getConn();
		
		st = con.createStatement();

		st.executeUpdate(sql);
		con.close();
	}
	
	//用戶是否已追蹤此股號
	public boolean isExist(String userId, String idTable, String sym) throws SQLException {
	
		con = getConn();
		st = con.createStatement();
		String sql = "select *  from "+idTable+" where locate('" + sym + ",',subscribe)>0 and id='"
				+ userId + "'";
		ResultSet rs = st.executeQuery(sql);
		boolean isExist =rs.next();
		rs.close();
		con.close();
		
		return isExist;
	}
	
	public String getSubList(String sql) throws SQLException {
		con = getConn();
		st = con.createStatement();
		
		ResultSet rs = st.executeQuery(sql);

		String syms = null;

		while (rs.next()) {
			syms = rs.getString("subscribe");
		}
		rs.close();
		con.close();
		return syms;
	}

	
	/**
	 * 
	 * @param idMap 
	 * @param isUser 
	 * @throws SQLException
	 */
	//剛起Bot時, 把mysql的id放到map
	public void id2Map(Map<String, Integer> idMap, boolean isUser) throws SQLException {
		con = getConn();
		st = con.createStatement();
		String sql =null;
		if(isUser) {
			sql = "select * from "+table+" where type='user'";

		}else {
			sql = "select * from "+table+" where type='group' or type='room'";

		}
		
		ResultSet rs = st.executeQuery(sql);

		String id = null;
		int isDelete = 0;
		while (rs.next()) {
			// 獲取id
			id = rs.getString("id");
			isDelete = rs.getInt("isDelete");

			// 放到set
			idMap.put(id, isDelete);
			// 输出结果
		}
		rs.close();
		con.close();
	}
	
	//用戶的isSub放到userIsSubMap
	public void isSub2Map(Map<String, Integer> userIsSubMap) throws SQLException {
		con = getConn();
		st = con.createStatement();
		String sql = "select * from "+table+" where type='user'";
				
		ResultSet rs = st.executeQuery(sql);

		String id = null;
		int isSub = 0;
		while (rs.next()) {
			// 獲取id
			id = rs.getString("id");
			isSub = rs.getInt("isSub");

			// 放到set
			userIsSubMap.put(id, isSub);

		}
		rs.close();
		con.close();
	}
	
	//要push 的 user id
    public Set<String> getId(String target) throws SQLException {
    	Set<String> idSet = new HashSet<>();
    	con = getConn();
		st = con.createStatement();
		String sql;
		String id = null;
		switch (target){
			case "user" :
	    		sql = "select id from "+table+" where type='user'";
				break;
			case "group" :
	    		sql = "select id from "+table+" where type='group' or type='room'";
				break;
			default  :
	    		sql = "select id from "+table;
				break;
		}

		ResultSet rs = st.executeQuery(sql);
		while (rs.next()) {
			// 獲取id
			id = rs.getString("id");
			//放到set
			idSet.add(id);
			System.out.println("id--->"+id);
		}
		rs.close();
		con.close();
		
		return idSet;
    }
  
	public Connection getConn() {
		try {
			// 註冊驅動
			Class.forName(mysqlname);
			// 創建鏈接
			con = DriverManager.getConnection(url, user, password);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return con;
	}
}
