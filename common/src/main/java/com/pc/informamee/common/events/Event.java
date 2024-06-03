package com.pc.informamee.common.events;

import com.pc.informamee.common.requests.RequestNotDoneException;

import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;

//IMPORTANT!

public abstract class Event implements Serializable, EventInterface {
    private Integer eventID; //potrebbe essere utile in fase di controllo delle nuove allerte
    private Integer danger;
    private String description;
    //https://stackoverflow.com/questions/6777810/a-datetime-equivalent-in-java-sql-is-there-a-java-sql-datetime
    private Timestamp beginTime;
    private Timestamp endTime;
    private ArrayList<Integer> InvolvedCap;
    //protected keyword used to make it accessible to any subclass of Event. Reference:
    //https://stackoverflow.com/questions/215497/what-is-the-difference-between-public-protected-package-private-and-private-in#215505

    private static final String INSERT_EVENT_QUERY =
            "INSERT INTO event (danger, description, beginTime, endTime, forecastID) VALUES (?, ?, ?, ?, ?)";
    private static final String RETRIEVE_ID_QUERY = "SELECT eventID FROM event WHERE eventID = LAST_INSERT_ID()";
    private static final String ADD_HAPPENING =
            "INSERT INTO happeningplace(eventID, CodiceAvviamentoPostale) VALUES(?, ?)";

    private static final String CAPCHECK =
            "SELECT CodiceAvviamentoPostale FROM cap WHERE CodiceAvviamentoPostale = ?";

    protected Event(int eventID, int danger, String description, Timestamp beginTime, Timestamp endTime, ArrayList<Integer> InvolvedCap) throws MalformedEventException {
        setEventID(eventID);
        setDanger(danger);
        setDescription(description);
        setTime(beginTime, endTime);
        setInvolvedCap(InvolvedCap);
    }

    //Constructor used during events sending
    protected Event(int danger, String description, Timestamp beginTime, Timestamp endTime, ArrayList<Integer> InvolvedCap) throws MalformedEventException {
        setDanger(danger);
        setDescription(description);
        setTime(beginTime, endTime);
        setInvolvedCap(InvolvedCap);
    }


    private void setTime(Timestamp beginTime, Timestamp endTime) throws MalformedEventException {
        if (beginTime==null || endTime==null || beginTime.getTime() > endTime.getTime()) throw new MalformedEventException();
        this.beginTime=beginTime;
        this.endTime=endTime;
    }

    private void setEventID(Integer eventID) throws MalformedEventException {
        if (eventID<0) throw new MalformedEventException("Invalid eventID (negative)");
        this.eventID = eventID;
    }

    private void setDanger(Integer danger) throws MalformedEventException {
        if (danger<1 || danger>4) throw new MalformedEventException("Invalid danger, must be between 1 and 4");
        this.danger = danger;
    }

    private void setDescription(String description) throws MalformedEventException {
        if (description==null || description.trim().isEmpty()) throw new MalformedEventException("Empty description");
        this.description = description;
    }

    private void setInvolvedCap(ArrayList<Integer> InvolvedCap) throws MalformedEventException {
        if (InvolvedCap==null || InvolvedCap.size()==0) throw new MalformedEventException("At least one CAP must be specified");
        this.InvolvedCap = InvolvedCap;
    }


    public Timestamp getBeginTime() {
        return this.beginTime;
    }

    public String getDescription() {
        return this.description;
    }

    public Integer getEventID() throws RequestNotDoneException {
        if (eventID==null) throw new RequestNotDoneException();

        return this.eventID;
    }

    public Integer getDanger() {
        return this.danger;
    }

    public Timestamp getEndTime() {
        return this.endTime;
    }

    @Override
    public ArrayList<Integer> getInvolvedCap() {
        return this.InvolvedCap;
    }

    private boolean CheckCap(Connection c) throws SQLException {
        boolean areValid=true;
        ArrayList<Integer> InvalidCAPs=null;
        for (Integer current: InvolvedCap) {
            PreparedStatement ps=c.prepareStatement(CAPCHECK);
            ps.setInt(1, current);
            ResultSet rs=ps.executeQuery();
            if(!rs.next()){
                if (areValid) {
                    InvalidCAPs= new ArrayList<>();
                    areValid=false;
                }
                InvalidCAPs.add(current);
            }
        }
        if (!areValid)
            InvolvedCap=InvalidCAPs;
        return areValid;
    }

    @Override
    public boolean ExecuteQuery(Connection c, int WhoAmI) throws SQLException, RequestNotDoneException {
        if (!CheckCap(c)) {
            return false;
        }
                PreparedStatement ps = c.prepareStatement(INSERT_EVENT_QUERY);
                ps.setInt(1, getDanger());
                ps.setString(2, description);
                ps.setTimestamp(3, beginTime);
                ps.setTimestamp(4, endTime);
                ps.setInt(5, WhoAmI);
                if (ps.executeUpdate()<=0)
                    throw new SQLException("Couldn't retreive the eventID");

        PreparedStatement ps2 = c.prepareStatement(RETRIEVE_ID_QUERY);
                ResultSet rs2 = ps2.executeQuery();
                if (rs2.next()) {
                    eventID=rs2.getInt(1); //1 is the index of the unique column the Query returns
                } else
                    throw new SQLException("Couldn't retreive the eventID");
                PreparedStatement ps3;
                for (Integer current : InvolvedCap) {
                    ps3 = c.prepareStatement(ADD_HAPPENING);
                    ps3.setInt(1, getEventID());
                    ps3.setInt(2, current);
                    ps3.executeUpdate();
                }
            return true;
    }
}