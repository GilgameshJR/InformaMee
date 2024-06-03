package com.pc.informamee.common;

import java.io.Serializable;
import java.sql.*;

public class Credentials implements Serializable {
    private int forecastID;
    private String Pwd;
    private static final String ARE_VALID_QUERY = "SELECT * FROM forecast WHERE forecastID=? AND password=?";

    public Credentials(int forecastID, String Pwd) {
        this.forecastID = forecastID;
        this.Pwd = Pwd;
    }

    public int getID(){
         return forecastID;
    }

    public boolean areValid(Connection c) throws SQLException {
        PreparedStatement ps = c.prepareStatement(ARE_VALID_QUERY);
        ps.setInt(1, forecastID);
        ps.setString(2, Pwd);
        ResultSet resultSet = ps.executeQuery();
        if(resultSet.next()) return true;
        return false;
    }
}
