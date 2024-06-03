package com.pc.informamee.common.events;

import com.pc.informamee.common.requests.RequestNotDoneException;

import java.sql.*;
import java.util.ArrayList;

public class WeatherEvent extends Event {
    private float windSpeed;
    private int type;
    private static final String INSERT_QUERY = "INSERT INTO weatherevent(eventID, windSpeed, type) VALUES (?, ?, ?)";


    public WeatherEvent(int eventID, int danger, String description, Timestamp beginTime, Timestamp endTime, ArrayList<Integer> CAPList, int type, float windSpeed) throws MalformedEventException {
        super(eventID, danger, description, beginTime, endTime, CAPList);
        setType(type);
        setWindSpeed(windSpeed);
    }

    public WeatherEvent(int danger, String description, Timestamp beginTime, Timestamp endTime, ArrayList<Integer> CAPList, int type, float windSpeed) throws MalformedEventException {
        super(danger, description, beginTime, endTime, CAPList);
        setType(type);
        setWindSpeed(windSpeed);
    }

    public int getType() {
        return type;
    }
    public float getWindSpeed() {
        return windSpeed;
    }

    private void setType(int type) throws MalformedEventException {
        if (type<1 || type>10) throw new MalformedEventException("Invalid type");
        this.type=type;
    }
    private void setWindSpeed(float windSpeed) throws MalformedEventException {
        if (windSpeed < 0.0d || windSpeed > 400.0d) throw new MalformedEventException("WeatherEvent windSpeed out of scale: " + Float.toString(windSpeed) + " is greater than 50 or less than 0");
        this.windSpeed = windSpeed;
    }

    @Override
    public boolean ExecuteQuery(Connection c, int WhoAmI) throws SQLException, RequestNotDoneException {
        boolean Res = super.ExecuteQuery(c, WhoAmI);
        if (Res) {
            PreparedStatement ps = c.prepareStatement(INSERT_QUERY);
            ps.setInt(1, getEventID());
            if (getWindSpeed() != 0)
                ps.setFloat(2, getWindSpeed());
            else
                ps.setNull(2, Types.FLOAT);
            if (getType() != 0)
                ps.setInt(3, getType());
            else
                ps.setNull(3, Types.INTEGER);
            int rs = ps.executeUpdate();
            System.out.println("DB insertion row count: " + rs);
        }
        return Res;
    }
}