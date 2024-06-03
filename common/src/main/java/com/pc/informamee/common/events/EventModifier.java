package com.pc.informamee.common.events;

import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class EventModifier implements EventInterface, Serializable {
    private static final String UPDATE = "UPDATE event ";
    private static final String UPDATEWEATHER = "UPDATE weatherevent ";
    private static final String UPDATESEISMIC = "UPDATE Seismicevent ";
    private static final String WHERE = " WHERE eventID = ?";
    private static final String EDITDANGER = UPDATE + " SET danger = ? " + WHERE;
    private static final String EDITDESCRIPRION = UPDATE + " SET description = ? " + WHERE;
    private static final String EDITTIME = UPDATE + "SET beginTime = ?, endTime = ? "+ WHERE;
    private static final String EDITRICHTER = UPDATESEISMIC + " SET richterMagnitude = ? " + WHERE;
    private static final String EDITMERCALLI = UPDATESEISMIC + " SET mercalliMagnitude = ? " + WHERE;
    private static final String EDITEPICENTRE = UPDATESEISMIC + " SET epicentreCAP = ? " + WHERE;
    private static final String EDITTYPE = UPDATEWEATHER + " SET type = ? " + WHERE;
    private static final String EDITWINDSPEED = UPDATEWEATHER + " SET windSpeed = ? " + WHERE;
    private static final String EDITCAP1 = "DELETE FROM happeningplace WHERE eventID = ?;";
    private static final String EDITCAP2 ="INSERT INTO happeningplace(eventID, CodiceAvviamentoPostale) VALUES(?, ?)";
    private static final String ALLOWED_QUERY = "SELECT * FROM event WHERE eventID = ? AND forecastID = ? AND ? < event.endTime";
    private static final String CAPCHECK ="SELECT CodiceAvviamentoPostale FROM cap WHERE CodiceAvviamentoPostale = ?";

    private Integer eventID;
    private Integer danger=null;
    private String description=null;
    private Timestamp beginTime=null;
    private Timestamp endTime=null;
    private ArrayList<Integer> InvolvedCap=null;
    private float richterMagnitude=-1;
    private float mercalliMagnitude=-1;
    private Integer epicentreCAP=null;
    private float windSpeed=-1;
    private Integer type=null;

    //METHODS
    public EventModifier(Integer EventId) {
        this.eventID =EventId;
    }

    public void setTime(Timestamp beginTime, Timestamp endTime) throws MalformedEventException {
        if (beginTime==null || endTime==null || beginTime.getTime() > endTime.getTime()) throw new MalformedEventException();
        this.beginTime=beginTime;
        this.endTime=endTime;
    }

    public void setEventID(Integer eventID) throws MalformedEventException {
        if (eventID<0) throw new MalformedEventException("Invalid eventID (negative)");
        this.eventID = eventID;
    }

    public void setDanger(Integer danger) throws MalformedEventException {
        if (danger<1 || danger>4) throw new MalformedEventException("Invalid danger, must be between 1 and 4");
        this.danger = danger;
    }

    public void setDescription(String description) throws MalformedEventException {
        if (description==null || description.trim().isEmpty()) throw new MalformedEventException("Empty description");
        this.description = description;
    }

    public void setInvolvedCap(ArrayList<Integer> InvolvedCap) throws MalformedEventException {
        if (InvolvedCap==null || InvolvedCap.size()==0) throw new MalformedEventException("At least one CAP must be specified");
        this.InvolvedCap = InvolvedCap;
    }

    public  void setRichterMagnitude(float richterMagnitude) throws MalformedEventException {
        //https://www.quora.com/What-could-possibly-be-the-maximum-magnitude-of-earthquake-on-Richter-scale?share=1
        if (richterMagnitude<0) throw new MalformedEventException("Invalid Richter Magnitude");
        this.richterMagnitude = richterMagnitude;
    }

    public void setMercalliMagnitude(float mercalliMagnitude) throws MalformedEventException {
        if (mercalliMagnitude<0) throw new MalformedEventException("Invalid Mercalli Magnitude");
        this.mercalliMagnitude = mercalliMagnitude;
    }

    public void setEpicentreCAP(Integer epicentreCAP) throws MalformedEventException {
        if (epicentreCAP==null) throw new MalformedEventException("Empty epicentreCAP");
        this.epicentreCAP = epicentreCAP;
    }

    public void setType(int type) throws MalformedEventException {
        if (type<1 || type>10) throw new MalformedEventException("Invalid type");
        this.type=type;
    }
    public void setWindSpeed(float windSpeed) throws MalformedEventException {
        if (windSpeed < 0.0d || windSpeed > 400.0d) throw new MalformedEventException("weatherEvent windSpeed out of scale: " + windSpeed + " is greater than 50 or less than 0");
        this.windSpeed = windSpeed;
    }

    @Override
    public ArrayList<Integer> getInvolvedCap() {
        return InvolvedCap;
    }

    public boolean IAmAllowed(Connection DbConn, int ForecastID, Date ReqTime) throws SQLException {
            PreparedStatement PS = DbConn.prepareStatement(ALLOWED_QUERY);
            PS.setInt(1, eventID);
            PS.setInt(2, ForecastID);
            PS.setTimestamp(3, new Timestamp(ReqTime.getTime()));
            ResultSet RS=PS.executeQuery();
        return RS.next();
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
    public boolean ExecuteQuery(Connection c, int WhoAmI) throws SQLException {
        if (IAmAllowed(c, WhoAmI, Calendar.getInstance().getTime())) {
            if (!CheckCap(c))
                return false;
            PreparedStatement ps=null;
            if (danger != null) {
                ps = c.prepareStatement(EDITDANGER);
                ps.setInt(1, danger);
                ps.setInt(2, eventID);
                ps.executeUpdate();
            }
            if (description != null) {
                ps = c.prepareStatement(EDITDESCRIPRION);
                ps.setString(1, description);
                ps.setInt(2, eventID);
                ps.executeUpdate();
            }
            if (beginTime != null) {
                ps = c.prepareStatement(EDITTIME);
                ps.setTimestamp(1, beginTime);
                ps.setTimestamp(2, endTime);
                ps.setInt(3, eventID);
                ps.executeUpdate();
            }
            if(richterMagnitude!= -1){
                ps = c.prepareStatement(EDITRICHTER);
                ps.setFloat(1, richterMagnitude);
                ps.setInt(2, eventID);
                ps.executeUpdate();
            }
            if(mercalliMagnitude!= -1){
                ps = c.prepareStatement(EDITMERCALLI);
                ps.setFloat(1, mercalliMagnitude);
                ps.setInt(2, eventID);
                ps.executeUpdate();
            }
            if(epicentreCAP!= null){
                ps = c.prepareStatement(EDITEPICENTRE);
                ps.setInt(1, epicentreCAP);
                ps.setInt(2, eventID);
                ps.executeUpdate();
            }
            if(windSpeed!= -1){
                ps = c.prepareStatement(EDITWINDSPEED);
                ps.setFloat(1, windSpeed);
                ps.setInt(2, eventID);
                ps.executeUpdate();
            }
            if(type!= null){
                ps = c.prepareStatement(EDITTYPE);
                ps.setInt(1, type);
                ps.setInt(2, eventID);
                ps.executeUpdate();
            }
            if (InvolvedCap != null) {
                ps = c.prepareStatement(EDITCAP1);
                ps.setInt(1, eventID);
                ps.executeUpdate();
                ps = c.prepareStatement(EDITCAP2);
                for(Integer current : InvolvedCap){
                    ps.setInt(1, eventID);
                    ps.setInt(2, current);
                    ps.executeUpdate();
                }
            }
            if (ps!=null)
                ps.close();
        } //else throw exception? in this case throwable in IAmAllowed too; int value to client? we'll see
        return true;
    }
}
