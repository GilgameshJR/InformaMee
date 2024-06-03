package com.pc.informamee.common.requests;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CapCheckRequest implements Serializable, Request {
    private Boolean isValid;
    private Integer CAP;
    private static final String CAP_QUERY =
            "SELECT CodiceAvviamentoPostale FROM cap WHERE CodiceAvviamentoPostale = ?";

    public CapCheckRequest(Integer CAP){
        isValid = null;
        this.CAP = CAP;
    }

    @Override
    public void ExecuteRequest(Connection c) throws SQLException {
        PreparedStatement ps = c.prepareStatement(CAP_QUERY);
        ResultSet rs = executeThisQuery(ps);
        if(rs.next()){
            isValid = rs.getInt(1) == CAP; //it should always be true
        }else{
            isValid = false;
        }
    }

    public Integer getCAP() {
        return CAP;
    }

    public Boolean getResult() throws RequestNotDoneException
    {
        if (isValid==null) throw new RequestNotDoneException();
        else return isValid;
    }

    protected ResultSet executeThisQuery(PreparedStatement ps) throws SQLException {
        ps.setInt(1, CAP);
        return ps.executeQuery();
    }
}
