package com.pc.informamee.sys;

import com.pc.informamee.common.events.Event;
import com.pc.informamee.common.events.EventModifier;
import com.pc.informamee.common.events.MalformedEventException;
import com.pc.informamee.common.events.TerroristEvent;
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

public class TerroristEventTest {
        private static TerroristEvent ExampleTerrorist;

        private static int TestDanger;
        private static String TestDescription;
        private static Timestamp TestBeginTime;
        private static Timestamp TestEndTime;
        private static ArrayList<Integer> TestInvolvedCap;
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
                ForecastId=123;

                DbConn=DatabaseConnection.Connect();

                ExampleTerrorist=new TerroristEvent(TestDanger, TestDescription, TestBeginTime, TestEndTime, TestInvolvedCap);

                //check if event is properly built from specifications
                assertSame(TestDanger, ExampleTerrorist.getDanger());
                assertEquals(TestDescription, ExampleTerrorist.getDescription());
                assertEquals(TestBeginTime, ExampleTerrorist.getBeginTime());
                assertEquals(TestEndTime, ExampleTerrorist.getEndTime());
                assertEquals(TestInvolvedCap, ExampleTerrorist.getInvolvedCap());
                System.out.println("SUCCESSFUL WeatherEvent object created and compared to specifications");
        }

        private static boolean isTestEvent(Event ToCompare) {
                if (!(ToCompare instanceof TerroristEvent))
                        return false;
                TerroristEvent ToCompareTerrorist=(TerroristEvent) ToCompare;
                return ToCompareTerrorist.getDanger() == TestDanger && ToCompareTerrorist.getDescription().equals(TestDescription) && ToCompareTerrorist.getInvolvedCap().equals(TestInvolvedCap) && ((TestBeginTime.getTime()) / 60000) == ((ToCompareTerrorist.getBeginTime().getTime()) / 60000);
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
                ExampleTerrorist.ExecuteQuery(DbConn, ForecastId);
                System.out.println("SUCCESSFUL inserted into DB");

                //check if event is properly stored and retrieved by FindRequestCurrent
                FindRequestCurrent FRCInst=new FindRequestCurrent(TestInvolvedCap.get(0));
                FRCInst.ExecuteRequest(DbConn);
                assertTrue(isTestEventInList(FRCInst.getResult()));
                System.out.println("SUCCESSFUL properly stored into DB and retrieved by FindRequestCurrent");

                //check if event is properly stored and retrieved by FindRequestCap
                //with danger
                FindRequestCap FRCapInst=new FindRequestCap(Calendar.getInstance().getTime(), TestInvolvedCap, false, true, false, TestDanger);
                FRCapInst.ExecuteRequest(DbConn);
                assertTrue(isTestEventInList(FRCapInst.getResult()));
                System.out.println("SUCCESSFUL properly stored into DB and retrieved by FindRequestCap (specifying danger)");
                //without danger
                FRCapInst=new FindRequestCap(Calendar.getInstance().getTime(), TestInvolvedCap, false, true, false);
                FRCapInst.ExecuteRequest(DbConn);
                assertTrue(isTestEventInList(FRCapInst.getResult()));
                System.out.println("SUCCESSFUL properly stored into DB and retrieved by FindRequestCap (without specifying danger)");

                //check if event is properly stored and retrieved by FindRequestGlobal
                //with danger
                FindRequestGlobal FRGlobalInst=new FindRequestGlobal(Calendar.getInstance().getTime(), false, true, false, TestDanger);
                FRGlobalInst.ExecuteRequest(DbConn);
                assertTrue(isTestEventInList(FRGlobalInst.getResult()));
                System.out.println("SUCCESSFUL properly stored into DB and retrieved by FindRequestGlobal (specifying danger)");
                //without danger
                FRGlobalInst=new FindRequestGlobal(Calendar.getInstance().getTime(), false, true, false, TestDanger);
                FRGlobalInst.ExecuteRequest(DbConn);
                assertTrue(isTestEventInList(FRGlobalInst.getResult()));
                System.out.println("SUCCESSFUL properly stored into DB and retrieved by FindRequestGlobal (without specifying danger)");

                //check if event is properly stored and retrieved by ForecastRequest ("forecast editable events retriever")
                ForecastRequest FRInst=new ForecastRequest(ForecastId);
                FRInst.ExecuteRequest(DbConn);
                ArrayList<Event> EditableEvents=FRInst.getResult();
                TerroristEvent FoundEvent=(TerroristEvent)FindTestEventInList(EditableEvents);
                assertNotEquals(null, FoundEvent);
                assertEquals(ExampleTerrorist.getEventID(), FoundEvent.getEventID());
                System.out.println("SUCCESSFUL properly stored into DB and retrieved by ForecastRequest (forecast editable events retriever)");

                //edit event in DB
                EventModifier EMInst = new EventModifier(ExampleTerrorist.getEventID());
                TestDanger = 4;
                TestDescription = "nuova descrizione modificata";
                TestBeginTime = new Timestamp(TestBeginTime.getTime() + 3600000); //1 hour later
                TestEndTime = new Timestamp(TestEndTime.getTime() + 3600000 * 2); //2 hour later
                TestInvolvedCap.add(23900);

                EMInst.setDanger(TestDanger);
                EMInst.setDescription(TestDescription);
                EMInst.setTime(TestBeginTime, TestEndTime);
                EMInst.setInvolvedCap(TestInvolvedCap);

                //check event is properly edited using FindRequestCap
                EMInst.ExecuteQuery(DbConn, ForecastId);

                FRCapInst = new FindRequestCap(new Timestamp(TestBeginTime.getTime() + 120000), TestInvolvedCap, false, true, false, TestDanger); //two minutes later begin
                FRCapInst.ExecuteRequest(DbConn);
                assertTrue(isTestEventInList(FRCapInst.getResult()));
                System.out.println("SUCCESSFUL updated ALL possible parameters of event in DB (using EventModifier) and retrieved event using FindRequestCap");
        }

        @AfterAll
        public static void tearDown() throws SQLException, RequestNotDoneException {
                //remove test event from DB
                System.out.println("Time to rollback updates from DB...");

                PreparedStatement DeleteEvent=DbConn.prepareStatement("DELETE FROM event WHERE eventID = ?");
                PreparedStatement DeleteTerrorist=DbConn.prepareStatement("DELETE FROM terroristevent WHERE eventID= ?");
                PreparedStatement DeleteCap=DbConn.prepareStatement("DELETE FROM HappeningPlace WHERE eventID = ?");

                DeleteEvent.setInt(1, ExampleTerrorist.getEventID());
                DeleteTerrorist.setInt(1, ExampleTerrorist.getEventID());
                DeleteCap.setInt(1, ExampleTerrorist.getEventID());

                System.out.println("Event delete from 'Event' table row count: "+DeleteEvent.executeUpdate());
                System.out.println("Event delete from 'Terrorist' table row count: "+DeleteTerrorist.executeUpdate());
                System.out.println("Event delete from 'HappeningPlace' (aka eventid-cap) table row count: "+DeleteCap.executeUpdate());

                DeleteEvent.close();
                DeleteTerrorist.close();
                DeleteCap.close();

                DbConn.close();
        }
}