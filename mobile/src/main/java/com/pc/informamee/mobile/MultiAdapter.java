package com.pc.informamee.mobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.pc.informamee.R;
import com.pc.informamee.common.StringBuilderFromDate;
import com.pc.informamee.common.events.Event;
import com.pc.informamee.common.events.SeismicEvent;
import com.pc.informamee.common.events.TerroristEvent;
import com.pc.informamee.common.events.WeatherEvent;

import java.util.ArrayList;

public class MultiAdapter extends BaseAdapter {
    Context context;

    private static final int TYPE_WEATHER=0;
    private static final int TYPE_SEISMIC=1;
    private static final int TYPE_TERRORIST =2;
    ArrayList<Event> EventsArr;

    private View ColorByDanger (View ToColor, int Danger) {
        switch (Danger) {
            case 1: {
                ToColor.setBackgroundColor(ContextCompat.getColor(context, R.color.Danger1Color));
                break;
            }
            case 2: {
                ToColor.setBackgroundColor(ContextCompat.getColor(context, R.color.Danger2Color));
                break;
            }
            case 3: {
                ToColor.setBackgroundColor(ContextCompat.getColor(context, R.color.Danger3Color));
                break;
            }
            case 4: {
                ToColor.setBackgroundColor(ContextCompat.getColor(context, R.color.Danger4Color));
                break;
            }
            default: {
                break;
            }
        }
        return ToColor;
    }

    private String DangerToString (int Danger) {
        String ToReturn;
        switch (Danger) {
            case 1: {
                ToReturn=context.getString(R.string.Danger1);
                break;
            }
            case 2: {
                ToReturn = context.getString(R.string.Danger2);
                break;
            }
            case 3: {
                ToReturn=context.getString(R.string.Danger3);
                break;
            }
            case 4: {
                ToReturn=context.getString(R.string.Danger4);
                break;
            }
            default: {
                ToReturn=context.getString(R.string.DangerUndefined);
                break;
            }
        }
        return ToReturn;
    }

    public MultiAdapter(Context context, ArrayList<Event> EventsArray)
    {
        super();
        this.context = context;
        this.EventsArr =EventsArray;
    }
    @Override
    public int getItemViewType(int position) {
        int type = -1;
        if (EventsArr.get(position) instanceof WeatherEvent){
            type = TYPE_WEATHER;
        } else  if (EventsArr.get(position) instanceof SeismicEvent){
            type = TYPE_SEISMIC;
        } else  if (EventsArr.get(position) instanceof TerroristEvent){
            type = TYPE_TERRORIST;
        }
        return type;
    }
    @Override
    public int getViewTypeCount() {
        return 3;
    }
    @Override
    public int getCount() {
        return EventsArr.size();
    }
    @Override
    public Object getItem(int position) {
        return EventsArr.get(position);
    }
    @Override
    public long getItemId(int position) {
        return 0;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater mInflater;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        int type=getItemViewType(position);
        switch (type) {
            case TYPE_WEATHER:
                convertView = mInflater.inflate(R.layout.weather_row, parent, false);

                TextView Tipo = convertView.findViewById(R.id.textViewWeatherType);
                TextView wDescrizione = convertView.findViewById(R.id.textViewWDesc);
                TextView wGravita = convertView.findViewById(R.id.textViewWeatherDanger);
                TextView wInizio = convertView.findViewById(R.id.textViewWeatherBeg);
                TextView wFine = convertView.findViewById(R.id.textViewWeatherEnd);
                TextView wVento = convertView.findViewById(R.id.textViewWeatherSpeed);

                WeatherEvent WeaEv=(WeatherEvent)this.getItem(position);

                switch (WeaEv.getType()) {
                    case 1: Tipo.setText(R.string.WeatherType1);
                        break;
                    case 2: Tipo.setText(R.string.WeatherType2);
                        break;
                    case 3: Tipo.setText(R.string.WeatherType3);
                        break;
                    case 4: Tipo.setText(R.string.WeatherType4);
                        break;
                    case 5: Tipo.setText(R.string.WeatherType5);
                        break;
                    case 6: Tipo.setText(R.string.WeatherType6);
                        break;
                    case 7: Tipo.setText(R.string.WeatherType7);
                        break;
                    case 8: Tipo.setText(R.string.WeatherType8);
                        break;
                    case 9: Tipo.setText(R.string.WeatherType9);
                        break;
                    default:Tipo.setText(R.string.WeatherTypeGeneric);
                        break;
                }
                wDescrizione.setText(WeaEv.getDescription());
                wGravita.setText(DangerToString(WeaEv.getDanger()));
                convertView=ColorByDanger(convertView, WeaEv.getDanger());
                wInizio.setText(StringBuilderFromDate.BuildStringFromDate(WeaEv.getBeginTime()));
                wFine.setText(StringBuilderFromDate.BuildStringFromDate(WeaEv.getEndTime()));

                String WindSpeed= WeaEv.getWindSpeed() + " km/h";
                wVento.setText(WindSpeed);
                break;

            case TYPE_SEISMIC:
                convertView = mInflater.inflate(R.layout.seismic_row, parent, false);

                TextView sGravita = convertView.findViewById(R.id.textViewSeismicDanger);
                TextView sDescrizione = convertView.findViewById(R.id.textViewDescSeismic);
                TextView sInizio = convertView.findViewById(R.id.textViewSeismicBeg);
                TextView sFine = convertView.findViewById(R.id.textViewSeismicEnd);
                TextView sScalaRichter = convertView.findViewById(R.id.textViewRichter);
                TextView sScalaMercalli = convertView.findViewById(R.id.textViewMercalli);
                TextView sEpicentro = convertView.findViewById(R.id.textViewEpic);


                SeismicEvent SeisEv=(SeismicEvent)this.getItem(position);
                sGravita.setText(DangerToString(SeisEv.getDanger()));
                convertView=ColorByDanger(convertView, SeisEv.getDanger());
                sDescrizione.setText(SeisEv.getDescription());
                sInizio.setText(StringBuilderFromDate.BuildStringFromDate(SeisEv.getBeginTime()));
                sFine.setText(StringBuilderFromDate.BuildStringFromDate(SeisEv.getEndTime()));
                sScalaRichter.setText(Float.toString(SeisEv.getRichterMagnitude()));
                sScalaMercalli.setText(Float.toString(SeisEv.getMercalliMagnitude()));
                sEpicentro.setText(Integer.toString(SeisEv.getEpicentreCAP()));
                break;

            case TYPE_TERRORIST:
                convertView = mInflater.inflate(R.layout.terrorist_row, parent, false);

                TextView tGravita = convertView.findViewById(R.id.textViewTerroriticDanger);
                TextView tDescrizione = convertView.findViewById(R.id.textViewDescTerrorism);
                TextView tInizio = convertView.findViewById(R.id.textViewTerroristBeg);
                TextView tFine = convertView.findViewById(R.id.textViewTerroristEnd);

                TerroristEvent TerrEv=(TerroristEvent)this.getItem(position);
                tGravita.setText(DangerToString(TerrEv.getDanger()));
                convertView=ColorByDanger(convertView, TerrEv.getDanger());
                tDescrizione.setText(TerrEv.getDescription());
                tInizio.setText(StringBuilderFromDate.BuildStringFromDate(TerrEv.getBeginTime()));
                tFine.setText(StringBuilderFromDate.BuildStringFromDate(TerrEv.getEndTime()));
                break;
        }
        return convertView;
    }


}