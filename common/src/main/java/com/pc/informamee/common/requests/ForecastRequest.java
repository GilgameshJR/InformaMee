package com.pc.informamee.common.requests;

import com.pc.informamee.common.events.Event;
import com.pc.informamee.common.events.EventInterface;
import com.pc.informamee.common.events.MalformedEventException;
import com.pc.informamee.common.requests.RequestNotDoneException;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;

public class ForecastRequest extends FindRequest {

    protected static final String STANDARD_FORECAST = " WHERE ? < event.endTime AND event.forecastID = ?";
    private static final String SEISMIC = EVENT_QUERY + ", seismicevent.richterMagnitude, seismicevent.mercalliMagnitude, seismicevent.epicentreCAP FROM seismicevent JOIN event ON seismicevent.eventID=event.eventID " + STANDARD_FORECAST;
    private static final String WEATHER = EVENT_QUERY + ", weatherevent.type, weatherevent.windSpeed FROM weatherevent JOIN event on weatherevent.eventID = event.eventID " + STANDARD_FORECAST;
    private static final String TERRORIST = EVENT_QUERY + " FROM terroristevent JOIN event ON terroristevent.eventID=event.eventID " + STANDARD_FORECAST;

    private int ForecastID;

    public ForecastRequest(int ForecastID) {
        super(Calendar.getInstance().getTime());
        this.ForecastID = ForecastID;
    }

    @Override
    public void ExecuteRequest(Connection c) throws SQLException, MalformedEventException {
        Result = new ArrayList<>();
        executeSeismicQuery(c, SEISMIC);
        executeWeatherQuery(c, WEATHER);
        executeTerroristQuery(c, TERRORIST);
    }

    @Override
    protected ResultSet compileThisQuery(PreparedStatement ps) throws SQLException {
        ps.setTimestamp(1, new Timestamp(getReqTime().getTime()));
        ps.setInt(2, this.ForecastID);
        return ps.executeQuery();
    }

}
