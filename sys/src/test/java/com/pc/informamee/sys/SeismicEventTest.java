package com.pc.informamee.sys;

import com.pc.informamee.common.events.*;
import com.pc.informamee.common.requests.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class SeismicEventTest {
        private static SeismicEvent ExampleSeismic;

        private static int TestDanger;
        private static String TestDescription;
        private static Timestamp TestBeginTime;
        private static Timestamp TestEndTime;
        private static ArrayList<Integer> TestInvolvedCap;
        private static float TestRichterMagnitude;
        private static float TestMercalliMagnitude;
        private static Integer TestEpicentreCAP;
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

                TestRichterMagnitude =Float.parseFloat("4");
                TestMercalliMagnitude =Float.parseFloat("2");
                TestEpicentreCAP =23862;

                ForecastId=123;

                DbConn=DatabaseConnection.Connect();

                ExampleSeismic =new SeismicEvent(TestDanger, TestDescription, TestBeginTime, TestEndTime, TestInvolvedCap, TestRichterMagnitude, TestMercalliMagnitude, TestEpicentreCAP);

                //check if event is properly built from specifications
                assertSame(TestDanger, ExampleSeismic.getDanger());
                assertEquals(TestDescription, ExampleSeismic.getDescription());
                assertEquals(TestBeginTime, ExampleSeismic.getBeginTime());
                assertEquals(TestEndTime, ExampleSeismic.getEndTime());
                assertEquals(TestInvolvedCap, ExampleSeismic.getInvolvedCap());
                assertEquals(TestEpicentreCAP, ExampleSeismic.getEpicentreCAP());
                assertEquals(TestRichterMagnitude, ExampleSeismic.getRichterMagnitude());
                assertEquals(TestMercalliMagnitude, ExampleSeismic.getMercalliMagnitude());
                System.out.println("SUCCESSFUL WeatherEvent object created and compared to specifications");
        }

        private static boolean isTestEvent(Event ToCompare) {
                if (!(ToCompare instanceof SeismicEvent))
                        return false;
                SeismicEvent ToCompareTerrorist=(SeismicEvent) ToCompare;
                if (ToCompareTerrorist.getDanger()==TestDanger && ToCompareTerrorist.getDescription().equals(TestDescription) && ToCompareTerrorist.getInvolvedCap().equals(TestInvolvedCap) && ToCompareTerrorist.getEpicentreCAP().equals(TestEpicentreCAP) && ((TestBeginTime.getTime())/60000)==((ToCompareTerrorist.getBeginTime().getTime())/60000) ) { //&& ToCompareTerrorist.getMercalliMagnitude()==TestMercalliMagnitude
                        BigDecimal BDTestMercalliMagnitude = new BigDecimal(Float.toString(TestMercalliMagnitude));
                        BigDecimal BDToCompareMercalliMagnitude = new BigDecimal(Float.toString(ToCompareTerrorist.getMercalliMagnitude()));
                        BigDecimal BDTestRichterMagnitude = new BigDecimal(Float.toString(TestRichterMagnitude));
                        BigDecimal BDToCompareRichterMagnitude = new BigDecimal(Float.toString(ToCompareTerrorist.getRichterMagnitude()));
                        final int Scale=4;
                        BDTestMercalliMagnitude = BDTestMercalliMagnitude.setScale(Scale, RoundingMode.HALF_UP);
                        BDToCompareMercalliMagnitude = BDToCompareMercalliMagnitude.setScale(Scale, RoundingMode.HALF_UP);
                        BDTestRichterMagnitude = BDTestRichterMagnitude.setScale(Scale, RoundingMode.HALF_UP);
                        BDToCompareRichterMagnitude = BDToCompareRichterMagnitude.setScale(Scale, RoundingMode.HALF_UP);
                        return BDTestMercalliMagnitude.compareTo(BDToCompareMercalliMagnitude) == 0 && BDTestRichterMagnitude.compareTo(BDToCompareRichterMagnitude) == 0;
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
                ExampleSeismic.ExecuteQuery(DbConn, ForecastId);
                System.out.println("SUCCESSFUL inserted into DB");

                //check if event is properly stored and retrieved by FindRequestCurrent
                FindRequestCurrent FRCInst=new FindRequestCurrent(TestInvolvedCap.get(0));
                FRCInst.ExecuteRequest(DbConn);
                assertTrue(isTestEventInList(FRCInst.getResult()));
                System.out.println("SUCCESSFUL properly stored into DB and retrieved by FindRequestCurrent");

                //check if event is properly stored and retrieved by FindRequestCap
                //with danger
                FindRequestCap FRCapInst=new FindRequestCap(Calendar.getInstance().getTime(), TestInvolvedCap, false, false, true, TestDanger);
                FRCapInst.ExecuteRequest(DbConn);
                assertTrue(isTestEventInList(FRCapInst.getResult()));
                System.out.println("SUCCESSFUL properly stored into DB and retrieved by FindRequestCap (specifying danger)");
                //without danger
                FRCapInst=new FindRequestCap(Calendar.getInstance().getTime(), TestInvolvedCap, false, false, true);
                FRCapInst.ExecuteRequest(DbConn);
                assertTrue(isTestEventInList(FRCapInst.getResult()));
                System.out.println("SUCCESSFUL properly stored into DB and retrieved by FindRequestCap (without specifying danger)");

                //check if event is properly stored and retrieved by FindRequestGlobal
                //with danger
                FindRequestGlobal FRGlobalInst=new FindRequestGlobal(Calendar.getInstance().getTime(), false, false, true, TestDanger);
                FRGlobalInst.ExecuteRequest(DbConn);
                assertTrue(isTestEventInList(FRGlobalInst.getResult()));
                System.out.println("SUCCESSFUL properly stored into DB and retrieved by FindRequestGlobal (specifying danger)");
                //without danger
                FRGlobalInst=new FindRequestGlobal(Calendar.getInstance().getTime(), false, false, true, TestDanger);
                FRGlobalInst.ExecuteRequest(DbConn);
                assertTrue(isTestEventInList(FRGlobalInst.getResult()));
                System.out.println("SUCCESSFUL properly stored into DB and retrieved by FindRequestGlobal (without specifying danger)");
                //check if event is properly stored and retrieved by ForecastRequest ("forecast editable events retriever")
                ForecastRequest FRInst=new ForecastRequest(ForecastId);
                FRInst.ExecuteRequest(DbConn);
                ArrayList<Event> EditableEvents=FRInst.getResult();
                SeismicEvent FoundEvent=(SeismicEvent)FindTestEventInList(EditableEvents);
                assertNotEquals(null, FoundEvent);
                assertEquals(ExampleSeismic.getEventID(), FoundEvent.getEventID());
                System.out.println("SUCCESSFUL properly stored into DB and retrieved by ForecastRequest (forecast editable events retriever)");

                //edit event in DB
                EventModifier EMInst = new EventModifier(ExampleSeismic.getEventID());
                TestDanger = 4;
                TestDescription = "nuova descrizione modificata";
                TestBeginTime = new Timestamp(TestBeginTime.getTime() + 3600000); //1 hour later
                TestEndTime = new Timestamp(TestEndTime.getTime() + 3600000 * 2); //2 hour later
                TestInvolvedCap.add(23900);
                TestEpicentreCAP = 23900;
                TestMercalliMagnitude = Float.parseFloat("5");
                TestRichterMagnitude = Float.parseFloat("5.5");

                EMInst.setDanger(TestDanger);
                EMInst.setDescription(TestDescription);
                EMInst.setTime(TestBeginTime, TestEndTime);
                EMInst.setInvolvedCap(TestInvolvedCap);
                EMInst.setEpicentreCAP(TestEpicentreCAP);
                EMInst.setMercalliMagnitude(TestMercalliMagnitude);
                EMInst.setRichterMagnitude(TestRichterMagnitude);

                //check event is properly edited using FindRequestCap
                EMInst.ExecuteQuery(DbConn, ForecastId);

                FRCapInst = new FindRequestCap(new Timestamp(TestBeginTime.getTime() + 120000), TestInvolvedCap, false, false, true, TestDanger); //two minutes later begin
                FRCapInst.ExecuteRequest(DbConn);
                assertTrue(isTestEventInList(FRCapInst.getResult()));
                System.out.println("SUCCESSFUL updated ALL possible parameters of event in DB (using EventModifier) and retrieved event using FindRequestCap");
        }

        @AfterAll
        public static void tearDown() throws SQLException, RequestNotDoneException {
                //remove test event from DB
                System.out.println("Time to rollback updates from DB...");

                PreparedStatement DeleteEvent=DbConn.prepareStatement("DELETE FROM event WHERE eventID = ?");
                PreparedStatement DeleteTerrorist=DbConn.prepareStatement("DELETE FROM seismicevent WHERE eventID= ?");
                PreparedStatement DeleteCap=DbConn.prepareStatement("DELETE FROM HappeningPlace WHERE eventID = ?");

                DeleteEvent.setInt(1, ExampleSeismic.getEventID());
                DeleteTerrorist.setInt(1, ExampleSeismic.getEventID());
                DeleteCap.setInt(1, ExampleSeismic.getEventID());

                System.out.println("Event delete from 'Event' table row count: "+DeleteEvent.executeUpdate());
                System.out.println("Event delete from 'Terrorist' table row count: "+DeleteTerrorist.executeUpdate());
                System.out.println("Event delete from 'HappeningPlace' (aka eventid-cap) table row count: "+DeleteCap.executeUpdate());

                DeleteEvent.close();
                DeleteTerrorist.close();
                DeleteCap.close();

                DbConn.close();
        }
}