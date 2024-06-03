package com.pc.informamee.common.requests;

import com.pc.informamee.common.events.Event;
import com.pc.informamee.common.events.MalformedEventException;
import com.pc.informamee.common.requests.FindRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

public class FindRequestGlobal extends FindRequest {
    private boolean ReqIsWeather;
    private boolean ReqIsTerrorist;
    private boolean ReqIsSeismic;
    private int ReqDanger;

    private static final String DANGER_Q = " AND event.danger = ?";

    public FindRequestGlobal(Date RequestTime, boolean isWeather, boolean isTerrorist, boolean isSeismic, int Danger)
    {
        super(RequestTime);
        ReqIsSeismic=isSeismic;
        ReqIsTerrorist=isTerrorist;
        ReqIsWeather=isWeather;
        ReqDanger=Danger;
    }

    public FindRequestGlobal(Date RequestTime, boolean isWeather, boolean isTerrorist, boolean isSeismic)
    {
        super(RequestTime);
        ReqIsSeismic=isSeismic;
        ReqIsTerrorist=isTerrorist;
        ReqIsWeather=isWeather;
        ReqDanger=-1;
    }

    public void ExecuteRequest(Connection c) throws SQLException, MalformedEventException {
        Result = new ArrayList<>();
            if (ReqDanger==-1) {
                if (ReqIsSeismic)
                    executeSeismicQuery(c, SEISMIC_QUERY+TIME_QUERY);
                if (ReqIsWeather)
                    executeWeatherQuery(c, WEATHER_QUERY+TIME_QUERY);
                if (ReqIsTerrorist)
                    executeTerroristQuery(c, TERRORIST_QUERY+TIME_QUERY);
            } else {
                if (ReqIsSeismic)
                    executeSeismicQuery(c, SEISMIC_QUERY+TIME_QUERY + DANGER_Q);
                if (ReqIsWeather)
                    executeWeatherQuery(c, WEATHER_QUERY+TIME_QUERY + DANGER_Q);
                if (ReqIsTerrorist)
                    executeTerroristQuery(c, TERRORIST_QUERY+TIME_QUERY + DANGER_Q);
            }
    }

    @Override
    protected ResultSet compileThisQuery(PreparedStatement ps) throws SQLException {
        ps.setTimestamp(1, new Timestamp(getReqTime().getTime()));
        if (ReqDanger!=-1)
            ps.setInt(2, ReqDanger);
        return ps.executeQuery();
    }
}