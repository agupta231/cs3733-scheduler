/**
 * 
 */
package com.lesath.apps.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;

import com.lesath.apps.model.TimeNotAvailable;

/**
 * @author Andy
 */
public class TimesNotAvailableDAO {
	java.sql.Connection conn;
	
	public TimesNotAvailableDAO() {
		try {
			conn = DatabaseUtil.connect();
		}
		catch(Exception e) {
			conn = null;
		}
	}
	
	public String addTimeNotAvailable(TimeNotAvailable t) throws Exception{
		String uuid = UUID.randomUUID().toString();
		try {
			PreparedStatement ps;
            ps = conn.prepareStatement("INSERT INTO TimesNotAvailable (schedule_id, uuid, start_time, "
            		+ "created_at, deleted_at) values(?,?,?,?,?);");
            ps.setString(1, t.getSchedule_id());
            ps.setString(2, uuid);
            ps.setString(3, t.getStart_time().toString());
            ps.setString(4, t.getCreated_at().toString());
            if(t.getDeleted_at() != null) {
            	ps.setString(5, t.getDeleted_at().toString());
            }
            else {
            	ps.setString(5,  null);
            }
            ps.execute();
            return uuid;
		} catch(Exception e) {
			throw new Exception("Could not add TimeNotAvailable: " + e.getMessage());
		}
	}
	
	public TimeNotAvailable getTimeNotAvailable(String uuid) throws Exception {
		TimeNotAvailable tna;
		try {
			Statement statement = conn.createStatement();
			String query = "SELECT * FROM Scheduler.TimesNotAvailable WHERE uuid=\"" + uuid + "\";";
			ResultSet resultSet = statement.executeQuery(query);
			if(resultSet != null) {
				resultSet.first();
				tna = generateTimeNotAvailable(resultSet);
			}
			else {
				tna = null;
			}
			resultSet.close();
            statement.close();
			return tna;
		} catch (Exception e) {
			throw new Exception("Failed to get TimeNotAvailable: " + e.getMessage());
		}
	}
	
	public ArrayList<TimeNotAvailable> getAllTimesNotAvailable() throws Exception {
		ArrayList<TimeNotAvailable> tnas = new ArrayList<TimeNotAvailable>();
		try {
			Statement statement = conn.createStatement();
			String query = "SELECT * FROM Scheduler.TimesNotAvailable";
			ResultSet resultSet = statement.executeQuery(query);
			
			if(resultSet != null) {
				while(resultSet.next()) {
					tnas.add(generateTimeNotAvailable(resultSet));
				}
			}
			else {
				tnas = null;
			}
			resultSet.close();
            statement.close();
            return tnas;
		} catch(Exception e) {
			throw new Exception("Failed to get TimesNotAvailable: " + e.getMessage());
		}
	}
	
	TimeNotAvailable generateTimeNotAvailable(ResultSet resultSet) throws Exception {
		try {
			String schedule_id = resultSet.getString("schedule_id");
			String id = resultSet.getString("uuid");
			LocalDateTime start_time = LocalDateTime.parse(resultSet.getString("start_time"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
			LocalDateTime created_at = LocalDateTime.parse(resultSet.getString("created_at"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
			LocalDateTime deleted_at = null;
			if(resultSet.getString("deleted_at") != null) {
				deleted_at = LocalDateTime.parse(resultSet.getString("deleted_at"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
			}
			return new TimeNotAvailable(schedule_id, id, start_time, created_at, deleted_at);
		} catch(Exception e) {
			throw new Exception("Failed to generate TimeNotAvailable: " + e.getMessage());
		}
	}
}