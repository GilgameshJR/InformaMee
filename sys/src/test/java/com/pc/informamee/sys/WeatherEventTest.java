package com.pc.informamee.sys;

import com.pc.informamee.common.events.Event;
import com.pc.informamee.common.events.EventModifier;
import com.pc.informamee.common.events.MalformedEventException;
import com.pc.informamee.common.events.WeatherEvent;
import com.pc.informamee.common.requests.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class WeatherEventTest {
        private static WeatherEvent ExampleWeather;

        private static int TestDanger;
        private static String TestDescription;
        private static Timestamp TestBeginTime;
        private static Timestamp TestEndTime;
        private static ArrayList<Integer> TestInvolvedCap;
        private static int TestType;
        private static float TestWindSpeed;
        private static Connection DbConn;
        private static int ForecastId;

        @BeforeAll
        public static void setUp() throws MalformedEventException, SQLException, ClassNotFoundException {
                TestDanger=2;
                TestDescription="pippo evento test";
                Date CurrTime= Calendar.getInstance().getTime();
                TestBeginTime=new Timestamp(CurrTime.getTime());
                TestEndTime=new Timestamp(CurrTime.getTime()+1000*60*60);
                TestInvolvedCap= new ArrayList<>();
                TestInvolvedCap.add(23862);
                TestType=5;
                TestWindSpeed=Float.parseFloat("2");
                ForecastId=123;

                DbConn=DatabaseConnection.Connect();

                ExampleWeather=new WeatherEvent(TestDanger, TestDescription, TestBeginTime, TestEndTime, TestInvolvedCap, TestType, TestWindSpeed);

                //check if event is properly built from specifications
                assertSame(TestDanger, ExampleWeather.getDanger());
                assertEquals(TestDescription, ExampleWeather.getDescription());
                assertEquals(TestBeginTime, ExampleWeather.getBeginTime());
                assertEquals(TestEndTime, ExampleWeather.getEndTime());
                assertEquals(TestInvolvedCap, ExampleWeather.getInvolvedCap());
                assertSame(TestType, ExampleWeather.getType());
                assertEquals(TestWindSpeed, ExampleWeather.getWindSpeed());
                System.out.println("SUCCESSFUL WeatherEvent object created and compared to specifications");
        }

        private static boolean isTestEvent(Event ToCompare) {
                if (!(ToCompare instanceof WeatherEvent))
                        return false;
                WeatherEvent ToCompareWeather=(WeatherEvent) ToCompare;
                if (ToCompareWeather.getDanger()==TestDanger && ToCompareWeather.getDescription().equals(TestDescription) && ToCompareWeather.getInvolvedCap().equals(TestInvolvedCap) && ToCompareWeather.getType()==TestType && ((TestBeginTime.getTime())/60000)==((ToCompareWeather.getBeginTime().getTime())/60000) ) { //&& ToCompareWeather.getWindSpeed()==TestWindSpeed
                        BigDecimal BDTestWindSpeed = new BigDecimal(Float.toString(TestWindSpeed));
                        BigDecimal BDToCompareWindSpeed = new BigDecimal(Float.toString(ToCompareWeather.getWindSpeed()));
                        final int Scale=4;
                        BDTestWindSpeed = BDTestWindSpeed.setScale(Scale, RoundingMode.HALF_UP);
                        BDToCompareWindSpeed = BDToCompareWindSpeed.setScale(Scale, RoundingMode.HALF_UP);
                        return BDTestWindSpeed.compareTo(BDToCompareWindSpeed) == 0;
                }
                return false;
        }

        private static boolean isTestEventInList(ArrayList<Event> ResultList) {
                for (Event Ev:ResultList) {
                        if (isTestEvent(Ev)) {
                                return true;
                        }
                }
                return false;
        }

        private static Event FindTestEventInList(ArrayList<Event> ResultList) {
                for (Event Ev:ResultList) {
                        if (isTestEvent(Ev)) {
                                return Ev;
                        }
                }
                return null;
        }

        @Test
        public void InsertRetrieveEditTest() throws SQLException, RequestNotDoneException, MalformedEventException {
                //insert event into DB
                ExampleWeather.ExecuteQuery(DbConn, ForecastId);
                System.out.println("SUCCESSFUL inserted into DB");

                //check if event is properly stored and retrieved by FindRequestCurrent
                FindRequestCurrent FRCInst=new FindRequestCurrent(TestInvolvedCap.get(0));
                FRCInst.ExecuteRequest(DbConn);
                assertTrue(isTestEventInList(FRCInst.getResult()));
                System.out.println("SUCCESSFUL properly stored into DB and retrieved by FindRequestCurrent");

                //check if event is properly stored and retrieved by FindRequestCap
                //with danger
                FindRequestCap FRCapInst=new FindRequestCap(Calendar.getInstance().getTime(), TestInvolvedCap, true, false, false, TestDanger);
                FRCapInst.ExecuteRequest(DbConn);
                assertTrue(isTestEventInList(FRCapInst.getResult()));
                System.out.println("SUCCESSFUL properly stored into DB and retrieved by FindRequestCap (specifying danger)");
                //without danger
                FRCapInst=new FindRequestCap(Calendar.getInstance().getTime(), TestInvolvedCap, true, false, false);
                FRCapInst.ExecuteRequest(DbConn);
                assertTrue(isTestEventInList(FRCapInst.getResult()));
                System.out.println("SUCCESSFUL properly stored into DB and retrieved by FindRequestCap (without specifying danger)");
                //check if event is properly stored and retrieved by FindRequestGlobal
                //with danger
                FindRequestGlobal FRGlobalInst=new FindRequestGlobal(Calendar.getInstance().getTime(), true, false, false, TestDanger);
                FRGlobalInst.ExecuteRequest(DbConn);
                assertTrue(isTestEventInList(FRGlobalInst.getResult()));
                System.out.println("SUCCESSFUL properly stored into DB and retrieved by FindRequestGlobal (specifying danger)");
                //without danger
                FRGlobalInst=new FindRequestGlobal(Calendar.getInstance().getTime(), true, false, false, TestDanger);
                FRGlobalInst.ExecuteRequest(DbConn);
                assertTrue(isTestEventInList(FRGlobalInst.getResult()));
                System.out.println("SUCCESSFUL properly stored into DB and retrieved by FindRequestGlobal (without specifying danger)");

                //check if event is properly stored and retrieved by ForecastRequest ("forecast editable events retriever")
                ForecastRequest FRInst=new ForecastRequest(ForecastId);
                FRInst.ExecuteRequest(DbConn);
                ArrayList<Event> EditableEvents=FRInst.getResult();
                WeatherEvent FoundEvent=(WeatherEvent)FindTestEventInList(EditableEvents);
                assertNotEquals(null, FoundEvent);
                assertEquals(ExampleWeather.getEventID(), FoundEvent.getEventID());
                System.out.println("SUCCESSFUL properly stored into DB and retrieved by ForecastRequest (forecast editable events retriever)");
                //edit event in DB
                EventModifier EMInst = new EventModifier(ExampleWeather.getEventID());
                TestDanger = 4;
                TestDescription = "nuova descrizione modificata";
                TestBeginTime = new Timestamp(TestBeginTime.getTime() + 3600000); //1 hour later
                TestEndTime = new Timestamp(TestEndTime.getTime() + 3600000 * 2); //2 hour later
                TestInvolvedCap.add(23900);
                TestType = 3;
                TestWindSpeed = Float.parseFloat("5");

                EMInst.setDanger(TestDanger);
                EMInst.setDescription(TestDescription);
                EMInst.setTime(TestBeginTime, TestEndTime);
                EMInst.setInvolvedCap(TestInvolvedCap);
                EMInst.setType(3);
                EMInst.setWindSpeed(TestWindSpeed);

                //check event is properly edited using FindRequestCap
                EMInst.ExecuteQuery(DbConn, ForecastId);

                FRCapInst = new FindRequestCap(new Timestamp(TestBeginTime.getTime() + 120000), TestInvolvedCap, true, false, false, TestDanger); //two minutes later begin
                FRCapInst.ExecuteRequest(DbConn);
                assertTrue(isTestEventInList(FRCapInst.getResult()));
                System.out.println("SUCCESSFUL updated ALL possible parameters of event in DB (using EventModifier) and retrieved event using FindRequestCap");
        }

        @AfterAll
        public static void tearDown() throws SQLException, RequestNotDoneException {
                //remove test event from DB
                System.out.println("Time to rollback updates from DB...");

                PreparedStatement DeleteEvent=DbConn.prepareStatement("DELETE FROM event WHERE eventID = ?");
                PreparedStatement DeleteWeather=DbConn.prepareStatement("DELETE FROM weatherevent WHERE eventID= ?");
                PreparedStatement DeleteCap=DbConn.prepareStatement("DELETE FROM HappeningPlace WHERE eventID = ?");

                DeleteEvent.setInt(1, ExampleWeather.getEventID());
                DeleteWeather.setInt(1, ExampleWeather.getEventID());
                DeleteCap.setInt(1, ExampleWeather.getEventID());

                System.out.println("Event delete from 'Event' table row count: "+DeleteEvent.executeUpdate());
                System.out.println("Event delete from 'Weather' table row count: "+DeleteWeather.executeUpdate());
                System.out.println("Event delete from 'HappeningPlace' (aka eventid-cap) table row count: "+DeleteCap.executeUpdate());

                DeleteEvent.close();
                DeleteWeather.close();
                DeleteCap.close();

                DbConn.close();
        }
}