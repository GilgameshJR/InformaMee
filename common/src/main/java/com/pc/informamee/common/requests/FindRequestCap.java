package com.pc.informamee.common.requests;
import com.pc.informamee.common.events.Event;
import com.pc.informamee.common.events.MalformedEventException;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

public class FindRequestCap extends FindRequest {
    private boolean ReqIsWeather;
    private boolean ReqIsTerrorist;
    private boolean ReqIsSeismic;
    private ArrayList<Integer> ReqCAPList;
    private ArrayList<Integer> InvalidCAPs;

    private static final String CAPTIME = " AND event.eventID IN (SELECT eventID FROM happeningplace WHERE CodiceAvviamentoPostale = ? ";
    private static final String DANGER_Q = " AND event.danger = ?";
    private static final String CAPCHECK = "SELECT CodiceAvviamentoPostale FROM cap WHERE CodiceAvviamentoPostale = ?";
    private static final String MORECAP="OR CodiceAvviamentoPostale = ?";
    private int ReqDanger;


    public FindRequestCap(Date RequestTime, ArrayList<Integer> CAPList, boolean isWeather, boolean isTerrorist, boolean isSeismic)
    {
        super(RequestTime);
        ReqCAPList=CAPList;
        ReqIsSeismic=isSeismic;
        ReqIsTerrorist=isTerrorist;
        ReqIsWeather=isWeather;
        ReqDanger=-1;
    }

    public FindRequestCap(Date RequestTime, ArrayList<Integer> CAPList, boolean isWeather, boolean isTerrorist, boolean isSeismic, int Danger)
    {
        super(RequestTime);
        ReqCAPList=CAPList;
        ReqIsSeismic=isSeismic;
        ReqIsTerrorist=isTerrorist;
        ReqIsWeather=isWeather;
        ReqDanger=Danger;
    }

    private boolean CheckCap(Connection c) throws SQLException {
        boolean areValid=true;
        for (Integer current: ReqCAPList) {
            PreparedStatement ps=c.prepareStatement(CAPCHECK);
            ps.setInt(1, current);
            ResultSet rs=ps.executeQuery();
            if(!rs.next()){
                if (areValid&&InvalidCAPs==null) {
                    InvalidCAPs= new ArrayList<>();
                    areValid=false;
                }
                InvalidCAPs.add(current);
            }
        }
        return areValid;
    }


    @Override
    public void ExecuteRequest(Connection c) throws SQLException, MalformedEventException {
        if (CheckCap(c)) {
            Result = new ArrayList<>();

            //build CAP query
            StringBuilder CapTimeBuilder=new StringBuilder(200);
            CapTimeBuilder.append(CAPTIME);
            for (int i=1; i<ReqCAPList.size(); i++) {
                CapTimeBuilder.append(MORECAP);
            }
            CapTimeBuilder.append(')');
            String CAPTIMEFULL=CapTimeBuilder.toString();

                if (ReqDanger==-1) {
                    if (ReqIsSeismic) {
                        executeSeismicQuery(c, SEISMIC_QUERY + TIME_QUERY + CAPTIMEFULL);
                    }
                    if (ReqIsWeather)
                       executeWeatherQuery(c, WEATHER_QUERY+TIME_QUERY+ CAPTIMEFULL);
                    if (ReqIsTerrorist)
                        executeTerroristQuery(c, TERRORIST_QUERY+TIME_QUERY+ CAPTIMEFULL);
                } else {
                    if (ReqIsSeismic)
                        executeSeismicQuery(c, SEISMIC_QUERY+TIME_QUERY + CAPTIMEFULL + DANGER_Q);
                    if (ReqIsWeather)
                        executeWeatherQuery(c, WEATHER_QUERY+TIME_QUERY + CAPTIMEFULL + DANGER_Q);
                    if (ReqIsTerrorist)
                        executeTerroristQuery(c, TERRORIST_QUERY+TIME_QUERY + CAPTIMEFULL + DANGER_Q);
                }
        }
    }
    public ArrayList<Integer> getInvalidCAPs() {
        return InvalidCAPs;
    }
    @Override
    protected ResultSet compileThisQuery(PreparedStatement ps) throws SQLException {
        ps.setTimestamp(1, new Timestamp(getReqTime().getTime()));
        int i=0;
        while (i<ReqCAPList.size()) {
            ps.setInt((2+i), ReqCAPList.get(i));
            i++;
        }
        if (ReqDanger!=-1)
            ps.setInt(2+i, ReqDanger);
        return ps.executeQuery();
    }
}