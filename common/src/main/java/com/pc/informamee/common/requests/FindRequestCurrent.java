package com.pc.informamee.common.requests;

import com.pc.informamee.common.events.Event;
import com.pc.informamee.common.events.MalformedEventException;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;

public class FindRequestCurrent extends FindRequest {
    private Integer CAP;
    protected final String CURRENT_REQUEST = //select all Events that are not yet finished
            " WHERE ? < event.endTime AND event.eventID IN (SELECT eventID FROM happeningplace WHERE CodiceAvviamentoPostale = ?)";

    public FindRequestCurrent(Integer CAP){
        super(Calendar.getInstance().getTime()); /*the object is initialised with the local date*/
        this.CAP = CAP;
    }

    @Override
    public void ExecuteRequest(Connection c) throws SQLException, MalformedEventException {
        Result = new ArrayList<>();
            executeSeismicQuery(c, SEISMIC_QUERY+CURRENT_REQUEST);
            executeWeatherQuery(c, WEATHER_QUERY+CURRENT_REQUEST);
            executeTerroristQuery(c, TERRORIST_QUERY+CURRENT_REQUEST);
    }

    @Override
    protected ResultSet compileThisQuery(PreparedStatement ps) throws SQLException {
        ps.setTimestamp(1, new Timestamp(getReqTime().getTime()));
        ps.setInt(2, this.CAP);
        return ps.executeQuery();
    }
}
