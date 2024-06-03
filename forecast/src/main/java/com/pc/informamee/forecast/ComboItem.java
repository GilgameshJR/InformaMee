package com.pc.informamee.forecast;
import com.pc.informamee.common.events.Event;

import java.text.SimpleDateFormat;

class ComboItem
{
    private int id;
    private String type;
    private Event evento;

    public Event getEvento() {
        return evento;
    }

    public void setEvento(Event evento) {
        this.evento = evento;
    }

    public ComboItem(int id, String type, Event evento)
    {
        this.id = id;
        this.type = type;
        this.evento = evento;
    }

    @Override
    public String toString()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String dataInizio  = dateFormat.format(this.evento.getBeginTime());

        return id + " - "+ type + " " + dataInizio ;
    }

    public int getId()
    {
        return id;
    }

    public String getType()
    {
        return type;
    }
}