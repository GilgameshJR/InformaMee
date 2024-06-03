package com.pc.informamee.sys;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//DB
public class DatabaseConnection {

    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/eventsdb?serverTimezone=Europe/Rome";
    private static final String USER = "admin2";
    private static final String PWD = "strongpwd";

    public static synchronized Connection Connect() throws SQLException, ClassNotFoundException {
        Class.forName(DRIVER);
        return DriverManager.getConnection(DATABASE_URL, USER, PWD);
    }
}