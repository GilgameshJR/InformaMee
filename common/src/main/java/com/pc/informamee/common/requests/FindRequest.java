package com.pc.informamee.common.requests;
import com.pc.informamee.common.events.*;
import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;


public abstract class FindRequest implements Serializable, Request {

    protected ArrayList<Event> Result=null;
    protected Date ReqTime;

    public void setReqTime(Date reqTime) {
        ReqTime = reqTime;
    }

    public Date getReqTime() {
        return ReqTime;
    }

    protected static final String EVENT_QUERY ="SELECT event.eventID, event.danger, event.description, event.beginTime, event.endTime, event.forecastID";
    protected static final String TIME_QUERY = " WHERE ?  BETWEEN event.beginTime AND event.endTime ";
    protected static final String SEISMIC_QUERY =
            EVENT_QUERY + ", seismicevent.richterMagnitude, seismicevent.mercalliMagnitude, seismicevent.epicentreCAP "+
                    "FROM seismicevent JOIN event ON seismicevent.eventID=event.eventID " ;

    protected final String WEATHER_QUERY =
            EVENT_QUERY + ", weatherevent.type, weatherevent.windSpeed  FROM weatherevent JOIN event on weatherevent.eventID = event.eventID ";
    protected final String TERRORIST_QUERY =
            EVENT_QUERY + " FROM terroristevent JOIN event ON terroristevent.eventID=event.eventID ";

    public abstract void ExecuteRequest(Connection c) throws SQLException, MalformedEventException;

    public FindRequest(Date RequestTime){
        ReqTime=RequestTime;
    }

    private static final String GETINVOLVEDCAP = "SELECT CodiceAvviamentoPostale FROM happeningplace WHERE eventID = ?";

    protected ArrayList<Integer> getInvolvedCap(Connection DbConn, int EventId) throws SQLException {
        ArrayList<Integer> InvolvedCap= new ArrayList<>();
        PreparedStatement CS=DbConn.prepareStatement(GETINVOLVEDCAP);
        CS.setInt(1, EventId);
        ResultSet CRS=CS.executeQuery();
        while (CRS.next()) {
            InvolvedCap.add(CRS.getInt("CodiceAvviamentoPostale"));
        }
        CS.close();
        return InvolvedCap;
    }

    protected void executeSeismicQuery(Connection DbConn,String SEISMIC) throws SQLException, MalformedEventException {
        PreparedStatement ps = DbConn.prepareStatement(SEISMIC);
        ResultSet rs = compileThisQuery(ps);
        while (rs.next()) {
            int EvId=rs.getInt("eventID");
            SeismicEvent current = new SeismicEvent(EvId, rs.getInt("danger"), rs.getString("description"),
                    rs.getTimestamp("beginTime"), rs.getTimestamp("endTime"), getInvolvedCap(DbConn, EvId),rs.getFloat("richterMagnitude"),rs.getFloat("mercalliMagnitude"),rs.getInt("epicentreCAP"));
            AddOrderByDanger(current);
        }
    }

    protected void executeWeatherQuery(Connection DbConn, String WEATHER) throws SQLException, MalformedEventException {
        PreparedStatement ps = DbConn.prepareStatement(WEATHER);
        ResultSet rs = compileThisQuery(ps);
        while (rs.next()) {
            int EvId=rs.getInt("eventID");
            WeatherEvent current = new WeatherEvent(EvId, rs.getInt("danger"), rs.getString("description"),
                    rs.getTimestamp("beginTime"), rs.getTimestamp("endTime"), getInvolvedCap(DbConn, EvId), rs.getInt("type"), rs.getFloat("windSpeed"));
            AddOrderByDanger(current);
        }
    }

    protected void executeTerroristQuery(Connection DbConn, String TERRORIST) throws SQLException, MalformedEventException {
        PreparedStatement ps = DbConn.prepareStatement(TERRORIST);
        ResultSet rs = compileThisQuery(ps);
        while (rs.next()) {
            int EvId=rs.getInt("eventID");
            TerroristEvent current = new TerroristEvent(EvId, rs.getInt("danger"), rs.getString("description"),
                    rs.getTimestamp("beginTime"), rs.getTimestamp("endTime"), getInvolvedCap(DbConn, EvId));
            AddOrderByDanger(current);
        }
    }

    public ArrayList<Event> getResult() throws RequestNotDoneException {
        if (Result!=null) return Result;
        else throw new RequestNotDoneException();
    }

    private void AddOrderByDanger(Event ToAdd) {
        int i=0;
        while (i<Result.size() && Result.get(i).getDanger()>ToAdd.getDanger()) {
            i++;
        }
        Result.add(i, ToAdd);
    }

    protected abstract ResultSet compileThisQuery(PreparedStatement ps) throws SQLException;
}


