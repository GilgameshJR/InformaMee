package com.pc.informamee.common.events;

import com.pc.informamee.common.requests.RequestNotDoneException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public interface EventInterface {
    boolean ExecuteQuery(Connection c, int WhoAmI) throws SQLException, RequestNotDoneException;
    ArrayList<Integer> getInvolvedCap();
}
