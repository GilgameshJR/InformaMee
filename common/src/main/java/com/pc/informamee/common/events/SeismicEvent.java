package com.pc.informamee.common.events;

import com.pc.informamee.common.requests.RequestNotDoneException;

import java.sql.*;
import java.util.ArrayList;

public class SeismicEvent extends Event {
    private float richterMagnitude;
    private float mercalliMagnitude;
    private Integer epicentreCAP;
    private static final String INSERT_QUERY ="INSERT INTO seismicevent(eventID, richterMagnitude, mercalliMagnitude, epicentreCAP) VALUES (?, ?, ?, ?)";

    public SeismicEvent(int danger, String description, Timestamp beginTime, Timestamp endTime, ArrayList<Integer> CAPList, float richterMagnitude, float mercalliMagnitude, Integer epicentreCAP) throws MalformedEventException {
        super(danger, description, beginTime, endTime, CAPList);
        setRichterMagnitude(richterMagnitude);
        setMercalliMagnitude(mercalliMagnitude);
        setEpicentreCAP(epicentreCAP);
    }

    public SeismicEvent(int eventID, int danger, String description, Timestamp beginTime, Timestamp endTime, ArrayList<Integer> CAPList, float richterMagnitude, float mercalliMagnitude, Integer epicentreCAP) throws MalformedEventException {
        super(eventID, danger, description, beginTime, endTime, CAPList);
        setRichterMagnitude(richterMagnitude);
        setMercalliMagnitude(mercalliMagnitude);
        setEpicentreCAP(epicentreCAP);
    }

    private  void setRichterMagnitude(float richterMagnitude) throws MalformedEventException {
        //https://www.quora.com/What-could-possibly-be-the-maximum-magnitude-of-earthquake-on-Richter-scale?share=1
        if (richterMagnitude<0) throw new MalformedEventException("Invalid Richter Magnitude");
        this.richterMagnitude = richterMagnitude;
    }

    private void setMercalliMagnitude(float mercalliMagnitude) throws MalformedEventException {
        if (mercalliMagnitude<0) throw new MalformedEventException("Invalid Mercalli Magnitude");
        this.mercalliMagnitude = mercalliMagnitude;
    }

    private void setEpicentreCAP(Integer epicentreCAP) throws MalformedEventException {
        if (epicentreCAP==null) throw new MalformedEventException("Empty epicentreCAP");
        this.epicentreCAP = epicentreCAP;
    }

    public float getRichterMagnitude() {
        return richterMagnitude;
    }
    public float getMercalliMagnitude() {
        return mercalliMagnitude;
    }
    public Integer getEpicentreCAP() {
        return epicentreCAP;
    }

    @Override
    public boolean ExecuteQuery(Connection c, int WhoAmI) throws SQLException, RequestNotDoneException {
            boolean Res=super.ExecuteQuery(c, WhoAmI);
            if (Res) {
                PreparedStatement ps = c.prepareStatement(INSERT_QUERY);
                ps.setInt(1, getEventID());
                if (richterMagnitude != 0)
                    ps.setFloat(2, richterMagnitude);
                else
                    ps.setNull(2, Types.FLOAT);
                if (mercalliMagnitude != 0)
                    ps.setFloat(3, mercalliMagnitude);
                else
                    ps.setNull(3, Types.FLOAT);
                if (epicentreCAP != 0)
                    ps.setFloat(4, epicentreCAP);
                else
                    ps.setNull(4, Types.INTEGER);
                int rs = ps.executeUpdate();
                System.out.println("DB insertion row count: " + rs);
            }
            return Res;
    }
}
/*
    @Override
    public String toString() {
        String toRet = String.format("Seismic %s", super.toString());
        if(mercalliMagnitude > 0) {
            toRet += String.format("Mercalli magnitude %s", Float.toString(mercalliMagnitude));
        }
        if(richterMagnitude > 0){
            toRet += String.format("Richter magnitude %s", Float.toString(richterMagnitude));
        }
        if(epicentreCAP > 0)
            toRet += String.format("Epicentre CAP %s", Float.toString(epicentreCAP));
        return toRet;
    }
 */
