package com.pc.informamee.common;

import java.util.ArrayList;

public class MultiCapParser {

    public final static String CAPNOTNUMBERSSTRING="CAP non valido: ammessi solo numeri e simboli per separare i CAP";
    public static ArrayList<Integer> ParseCap(String CAPs) throws CapParseException {
        if (CAPs==null) throw new CapParseException();
        CAPs=CAPs.trim();
        if (CAPs.length()==0) throw new CapParseException();
        ArrayList<Integer> CAPList= new ArrayList<>();
        boolean resultempty=true;
        int result = 0;
        for (int charit=0; charit<CAPs.length(); charit++) {
            char c=CAPs.charAt(charit);
            if (c==' ') {
                CAPList.add(result);
                result=0;
                resultempty=true;
            }
            else {
                int digit = (int) c - (int) '0';
                if ((digit < 0) || (digit > 9)) throw new CapParseException();
                result *= 10;
                result += digit;
                if (resultempty) resultempty=false;
            }
        } if (!resultempty) CAPList.add(result);
        return CAPList;
    }
}
