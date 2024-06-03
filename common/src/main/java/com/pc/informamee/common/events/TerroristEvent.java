package com.pc.informamee.common.events;

import com.pc.informamee.common.requests.RequestNotDoneException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

public class TerroristEvent extends Event {
    private static final String INSERT_QUERY = "INSERT INTO terroristevent(eventID) VALUES (?)";

    public TerroristEvent(int eventID, int danger, String description, Timestamp beginTime, Timestamp endTime, ArrayList<Integer> CAPList) throws MalformedEventException {
        super(eventID, danger, description, beginTime, endTime, CAPList);
    }

    public TerroristEvent(int danger, String description, Timestamp beginTime, Timestamp endTime, ArrayList<Integer> CAPList) throws MalformedEventException {
        super(danger, description, beginTime, endTime, CAPList);
    }

    public boolean ExecuteQuery(Connection c, int WhoAmI) throws SQLException, RequestNotDoneException {
            boolean Res=super.ExecuteQuery(c, WhoAmI);
            if (Res) {
                PreparedStatement ps = c.prepareStatement(INSERT_QUERY);
                ps.setInt(1, getEventID());
                int rs = ps.executeUpdate();
                System.out.println("DB insertion row count: " + rs);
            }
            return Res;
    }
}
