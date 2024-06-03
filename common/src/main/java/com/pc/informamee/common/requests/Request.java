package com.pc.informamee.common.requests;

import com.pc.informamee.common.events.MalformedEventException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface Request {
    public abstract void ExecuteRequest(Connection c) throws SQLException, MalformedEventException;
    public abstract Object getResult() throws RequestNotDoneException;
}
