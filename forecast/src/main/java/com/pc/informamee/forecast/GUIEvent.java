package com.pc.informamee.forecast;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.pc.informamee.common.*;
import com.pc.informamee.common.events.*;
import com.pc.informamee.common.events.Event;
import com.pc.informamee.common.requests.ForecastRequest;
import com.pc.informamee.common.requests.RequestNotDoneException;


import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

@SuppressWarnings("Convert2Lambda")
public class GUIEvent extends JDialog {
    private JPanel contentPane;
    private JButton eventSend;
    private JButton buttonCancel;
    private JComboBox eventType;
    private JComboBox eventGravity;
    private JTextArea eventDesc;
    private JComboBox eventLasting;
    private JTextField eventCap;
    private JLabel eventGravityLabel;
    private JLabel eventCapLabel;
    private JLabel eventDescLabel;
    private JComboBox eventWeatherRisk;
    private JTextField eventWeatherWind;
    private JTextField eventSeismicRichter;
    private JTextField eventSeismicMercalli;
    private JTextField eventSeismicEpicentre;
    private JLabel eventWeatherLabel;
    private JLabel eventWeatherRiskLabel;
    private JLabel eventWeatherWindLabel;
    private JLabel eventSeismicLabel;
    private JLabel eventSeismicRichterLabel;
    private JLabel eventSeismicMercalliLabel;
    private JLabel eventSeismicEpicentreLabel;
    private JLabel eventTypeLabel;
    private JComboBox eventActionModify;
    private JLabel eventActionLabel;
    private JComboBox eventActionType;
    private JLabel eventBeginLabel;
    private JLabel eventBeginHourLabel;
    private JTextField eventBeginDate;
    private JTextField eventBeginHour;
    private JLabel eventEnd;
    private JSpinner eventLastingDays;
    private Timestamp start;
    private Timestamp end;

    public GUIEvent() {
        $$$setupUI$$$();
        setContentPane(contentPane);
        setTitle("Forecast App - inserisci evento");

        //Disabilito tutto tranne previsione meteo
        eventSeismicLabel.setEnabled(false);
        eventSeismicEpicentre.setEnabled(false);
        eventSeismicEpicentreLabel.setEnabled(false);
        eventSeismicMercalli.setEnabled(false);
        eventSeismicMercalliLabel.setEnabled(false);
        eventSeismicRichter.setEnabled(false);
        eventSeismicRichterLabel.setEnabled(false);
        eventEnd.setText("Seleziona inizio e una durata.");

        //nascondo elenco eventi da modificare
        eventActionModify.setVisible(false);

        resetCampi();
        updateEndDate();

        pack();
        setLocationRelativeTo(null);
        setModal(true);
        getRootPane().setDefaultButton(eventSend);

        eventSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                eventSend();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        //call onAlertTypeChange() on value selection in 'tipo di allerta'
        eventType.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                onAlertTypeChange();
            }
        });
        //call onActionTypeChange() on value selection in 'azione' può essere inserisci o modifica
        eventActionType.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onActionTypeChange();
            }
        });
        eventBeginDate.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateEndDate();
            }

            public void removeUpdate(DocumentEvent e) {
                updateEndDate();
            }

            public void insertUpdate(DocumentEvent e) {
                updateEndDate();
            }
        });
        eventBeginHour.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateEndDate();
            }

            public void removeUpdate(DocumentEvent e) {
                updateEndDate();
            }

            public void insertUpdate(DocumentEvent e) {
                updateEndDate();
            }
        });
        eventLasting.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateEndDate();
            }
        });
        //call onActionModifyChange() on value selection in 'eventActionModify' per riempire i campi con i dati dell'evento selezionato
        eventActionModify.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onActionModifyChange();
            }
        });

        eventLastingDays.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateEndDate();
            }
        });
    }

    private void updateEndDate() {
        //provo a calcolare la data finale
        try {
            start = parseStringToTimestamp(eventBeginDate.getText() + " " + eventBeginHour.getText());
            int durataInOre = Integer.parseInt((String) eventLasting.getSelectedItem());
            int durataInGiorni = (Integer) eventLastingDays.getValue();
            if (durataInOre == 0 && durataInGiorni == 0)
                eventEnd.setText("Durata non valida. Minimo 4 ore.");
            else {
                end = new Timestamp(start.getTime() + (durataInOre * 3600000) + (durataInGiorni * 86400000));
                String s = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(end);
                eventEnd.setText(s);
            }
        } catch (ParseException e) {
            eventEnd.setText("Seleziona inizio e una durata.");
        }
    }

    private void onActionTypeChange() {
        String type = (String) eventActionType.getSelectedItem();
        resetCampi();
        eventActionModify.removeAllItems();
        eventActionModify.addItem("Seleziona evento da modificare");
        eventActionModify.setSelectedIndex(0);

        //mostro quello selezionato
        if (type.equals("Aggiungi evento")) {
            eventType.setEnabled(true);
            eventActionModify.setVisible(false);
        } else if (type.equals("Modifica evento")) {
            eventType.setEnabled(false);
            try {
                setModifiableEvents();
            } catch (RequestNotDoneException e) {
                JOptionPane.showMessageDialog(null, "Impossibile ottenere l'ID di un evento da modificare");
            }
            eventActionModify.setVisible(true);
        }
        revalidate();
        repaint();
    }

    private void setModifiableEvents() throws RequestNotDoneException {

        ArrayList<Event> events = getModifiableEvents();
        for (Event i : events) {
            String type = null;
            if (i instanceof WeatherEvent)
                type = "Weather";
            else if (i instanceof SeismicEvent)
                type = "Seismic";
            else if (i instanceof TerroristEvent)
                type = "Terrorist";

            ComboItem element = new ComboItem(i.getEventID(), type, i);
            eventActionModify.addItem(element);
        }

        revalidate();
        repaint();
    }

    private void onActionModifyChange() {
        resetCampi();
        int type = eventActionModify.getSelectedIndex();

        if (type > 0) {
            //Fillo il form con i dati dell'evento
            ComboItem eventContainer = (ComboItem) eventActionModify.getSelectedItem();
            Event event = eventContainer.getEvento();

            eventGravity.setSelectedIndex(event.getDanger() - 1);

            StringBuilder cap = new StringBuilder();
            ArrayList<Integer> CAPList = event.getInvolvedCap();
            for (int a : CAPList)
                cap.append(a).append(" ");
            eventCap.setText(cap.toString());

            eventDesc.setText(event.getDescription());

            Date end = event.getEndTime();
            Date start = event.getBeginTime();
            String formattedDate = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(start);
            String[] splittata = formattedDate.split("\\s+");
            eventBeginDate.setText(splittata[0]);
            eventBeginHour.setText(splittata[1]);

            int durataOre = (int) ((end.getTime() - start.getTime()) / (3600000));
            int durataGiorni = durataOre / 24;
            int OreAvanzate = durataOre - (durataGiorni * 24);
            if (OreAvanzate == 0)
                eventLasting.setSelectedIndex(0);
            else if (OreAvanzate == 4)
                eventLasting.setSelectedIndex(1);
            else if (OreAvanzate == 8)
                eventLasting.setSelectedIndex(2);
            else if (OreAvanzate == 12)
                eventLasting.setSelectedIndex(3);
            else if (OreAvanzate == 16)
                eventLasting.setSelectedIndex(4);
            else if (OreAvanzate == 20)
                eventLasting.setSelectedIndex(5);

            eventLastingDays.setValue(durataGiorni);

            if (event instanceof WeatherEvent) {
                WeatherEvent weatherEvent = (WeatherEvent) event;
                eventType.setSelectedIndex(0);
                eventWeatherWind.setText(Float.toString(weatherEvent.getWindSpeed()));
                eventWeatherRisk.setSelectedIndex(weatherEvent.getType() - 1);

            } else if (event instanceof TerroristEvent) {
                eventType.setSelectedIndex(1);
            } else if (event instanceof SeismicEvent) {
                SeismicEvent seismicEvent = (SeismicEvent) event;
                eventType.setSelectedIndex(2);
                eventSeismicMercalli.setText(Float.toString(seismicEvent.getMercalliMagnitude()));
                eventSeismicEpicentre.setText(seismicEvent.getEpicentreCAP().toString());
                eventSeismicRichter.setText(Float.toString(seismicEvent.getRichterMagnitude()));
            }

        }

        revalidate();
        repaint();
    }

    private ArrayList<Event> getModifiableEvents() {
        ArrayList<Event> events = null;
        ForecastRequest results = new ForecastRequest(Container.getInstance().getCredentials().getID());
        try {
            results = EventUploader.UploadEvent(results);
            events = results.getResult();
        } catch (RequestNotDoneException e) {
            e.printStackTrace();
            System.out.println("Errore di richiesta");
            JOptionPane.showMessageDialog(null, "Errore di richiesta");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Errore di query");
            JOptionPane.showMessageDialog(null, "Errore di query");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Classe non trovata");
            JOptionPane.showMessageDialog(null, "Classe non trovata");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Errore di comunicazione");
            JOptionPane.showMessageDialog(null, "Errore di comunicazione");
        }

        return events;
    }

    private void resetCampi() {
        eventGravity.setSelectedIndex(0);
        eventCap.setText("");
        eventDesc.setText("");

        SimpleDateFormat giorno = new SimpleDateFormat("dd-M-yyyy");
        SimpleDateFormat ora = new SimpleDateFormat("HH:mm");
        Calendar date = Calendar.getInstance();
        long t = date.getTimeInMillis();
        Date afterAddingFiveMins = new Date(t + (3600000));
        eventBeginDate.setText(giorno.format(afterAddingFiveMins));
        eventBeginHour.setText(ora.format(afterAddingFiveMins));
        updateEndDate();

        eventLasting.setSelectedIndex(1);
        eventEnd.setText("Seleziona inizio e una durata.");

        eventSeismicEpicentre.setText("");
        eventSeismicMercalli.setText("0.0");
        eventSeismicRichter.setText("0.0");

        eventType.setSelectedIndex(0);
        eventWeatherRisk.setSelectedIndex(0);
        eventWeatherWind.setText("0.0");

        revalidate();
        repaint();
    }

    private static Timestamp parseStringToTimestamp(String data) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-M-yyyy HH:mm");
        Date parsedDate = dateFormat.parse(data);
        return new Timestamp(parsedDate.getTime());
    }

    private Event createEventToSend() throws MalformedEventException, NumberFormatException, ParseException, CapParseException {
        //attributi comuni
        int danger = eventGravity.getSelectedIndex() + 1;
        String desc = eventDesc.getText();
        ArrayList<Integer> capList = MultiCapParser.ParseCap(eventCap.getText());

        //ottengo la data e ora attuale
        Date now = new Date();
        long nowTime = now.getTime();
        long startTime = start.getTime();
        if (startTime < nowTime || startTime > (nowTime + 86400000)) {
            JOptionPane.showMessageDialog(null, "L'evento non può iniziare prima di now o finire dopo now+24h");
            throw new MalformedEventException("L'evento non può iniziare prima di now o finire dopo now+24h");
        }

        //per debug
        System.out.println("Evento in preparazione da mandare...");
        //end debug

        Event toSend = null;
        if (eventType.getSelectedItem().equals("Meteo")) {
            int type = eventWeatherRisk.getSelectedIndex() + 1;
            String windSpeed = eventWeatherWind.getText();
            float windSpeedParsed = Float.parseFloat(windSpeed);
            toSend = new WeatherEvent(danger, desc, start, end, capList, type, windSpeedParsed);
        } else if (eventType.getSelectedItem().equals("Terroristica")) {
            toSend = new TerroristEvent(danger, desc, start, end, capList);
        } else if (eventType.getSelectedItem().equals("Sismica")) {
            String richter = eventSeismicRichter.getText();
            String mercalli = eventSeismicMercalli.getText();
            String epicentre = eventSeismicEpicentre.getText();
            float richterParsed = Float.parseFloat(richter);
            float mercalliParsed = Float.parseFloat(mercalli);
            int epicentreParsed = Integer.parseInt(epicentre);
            toSend = new SeismicEvent(danger, desc, start, end, capList, richterParsed, mercalliParsed, epicentreParsed);
        }
        return toSend;
    }

    private void eventSend() {
        if ((eventActionType.getSelectedIndex() == 0 || (eventActionType.getSelectedIndex() == 1 && eventActionModify.getSelectedIndex() > 0))) {
            if ((((Integer) eventLastingDays.getValue()) > 0 || eventLasting.getSelectedIndex() > 0)) {
                ArrayList<Integer> InvalidCAPs = null;
                try {
                    Event pippo = createEventToSend();
                    System.out.println("---------------------------------------------------------------------------");
                    System.out.println(pippo);
                    System.out.println("---------------------------------------------------------------------------");

                    if (eventActionType.getSelectedIndex() == 0)
                        InvalidCAPs = EventUploader.UploadEvent(pippo);
                    else if (eventActionType.getSelectedIndex() == 1 && eventActionModify.getSelectedIndex() > 0) {
                        //istanzio un eventodifier con id evento
                        //carico l'evento come se fosse un evento normale (?)
                        ComboItem eventContainer = (ComboItem) eventActionModify.getSelectedItem();
                        Event event = eventContainer.getEvento();
                        EventModifier inModifica = new EventModifier(event.getEventID());

                        inModifica.setInvolvedCap(pippo.getInvolvedCap());
                        inModifica.setDanger(pippo.getDanger());
                        inModifica.setDescription(pippo.getDescription());
                        inModifica.setTime(pippo.getBeginTime(), pippo.getEndTime());

                        if (event instanceof WeatherEvent) {
                            System.out.println("Modifica in corso di evento meterologico");
                            WeatherEvent pippoWeather = (WeatherEvent) pippo;
                            inModifica.setType(pippoWeather.getType());
                            inModifica.setWindSpeed(pippoWeather.getWindSpeed());
                        } else if (event instanceof SeismicEvent) {
                            System.out.println("Modifica in corso di evento Sismico");
                            SeismicEvent pippoSeismic = (SeismicEvent) pippo;

                            inModifica.setEpicentreCAP(pippoSeismic.getEpicentreCAP());
                            inModifica.setMercalliMagnitude(pippoSeismic.getMercalliMagnitude());
                            inModifica.setRichterMagnitude(pippoSeismic.getRichterMagnitude());
                        } else if (event instanceof TerroristEvent) {
                            System.out.println("Modifica in corso di evento terroristico");
                        }
                        InvalidCAPs = EventUploader.UploadEvent(inModifica);
                    }
                    if (InvalidCAPs == null || InvalidCAPs.size() == 0) {
                        onActionTypeChange();
                        resetCampi();
                    } else {
                        StringBuilder InvalidCapStrBuilder = new StringBuilder(100);
                        for (Integer InvCap : InvalidCAPs) {
                            InvalidCapStrBuilder.append(InvCap);
                            InvalidCapStrBuilder.append(' ');
                        }
                        JOptionPane.showMessageDialog(null, "Il CAP " + InvalidCapStrBuilder.toString() + "non è valido");

                    }
                } catch (WrongCredentialsException e) {
                    e.printStackTrace();
                    System.out.println("Credenziali sbagliate");
                    JOptionPane.showMessageDialog(null, "Credenziali sbagliate");
                } catch (IOException e) {
                    System.out.println("Errore di comunicazione");
                    JOptionPane.showMessageDialog(null, "Errore di comunicazione");
                    e.printStackTrace();
                } catch (CapParseException e) {
                    JOptionPane.showMessageDialog(null, MultiCapParser.CAPNOTNUMBERSSTRING);
                } catch (ParseException e) {
                    JOptionPane.showMessageDialog(null, "Correggi il formato della data (dd-M-yyyy HH:mm:ss)");
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Errore: puoi inserire solo numeri nei parametri");
                } catch (MalformedEventException e) {
                    e.printStackTrace();
                } catch (RequestNotDoneException e) {
                    JOptionPane.showMessageDialog(null, "Impossibile ottenere l'ID dell'evento da modificare");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Inserisci una data valida!");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Seleziona l'evento da modificare");
        }
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private void onAlertTypeChange() {
        String type = (String) eventType.getSelectedItem();

        //Disabilito tutto (di default è abilitato solo Meteo)
        eventWeatherLabel.setEnabled(false);
        eventWeatherRiskLabel.setEnabled(false);
        eventWeatherRisk.setEnabled(false);
        eventWeatherWindLabel.setEnabled(false);
        eventWeatherWind.setEnabled(false);

        eventSeismicLabel.setEnabled(false);
        eventSeismicEpicentre.setEnabled(false);
        eventSeismicEpicentreLabel.setEnabled(false);
        eventSeismicMercalli.setEnabled(false);
        eventSeismicMercalliLabel.setEnabled(false);
        eventSeismicRichter.setEnabled(false);
        eventSeismicRichterLabel.setEnabled(false);

        //mostro quello selezionato
        if (type.equals("Meteo")) {
            eventWeatherLabel.setEnabled(true);
            eventWeatherRiskLabel.setEnabled(true);
            eventWeatherRisk.setEnabled(true);
            eventWeatherWindLabel.setEnabled(true);
            eventWeatherWind.setEnabled(true);
        } else if (type.equals("Sismica")) {
            eventSeismicLabel.setEnabled(true);
            eventSeismicEpicentre.setEnabled(true);
            eventSeismicEpicentreLabel.setEnabled(true);
            eventSeismicMercalli.setEnabled(true);
            eventSeismicMercalliLabel.setEnabled(true);
            eventSeismicRichter.setEnabled(true);
            eventSeismicRichterLabel.setEnabled(true);
        }
        revalidate();
        repaint();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        contentPane.setBackground(new Color(-772));
        contentPane.setDoubleBuffered(false);
        contentPane.setPreferredSize(new Dimension(500, 600));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(16, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setBackground(new Color(-772));
        panel1.setForeground(new Color(-16777216));
        contentPane.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        eventGravityLabel = new JLabel();
        eventGravityLabel.setForeground(new Color(-16777216));
        eventGravityLabel.setText("Gravità");
        panel1.add(eventGravityLabel, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        eventType = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("Meteo");
        defaultComboBoxModel1.addElement("Terroristica");
        defaultComboBoxModel1.addElement("Sismica");
        eventType.setModel(defaultComboBoxModel1);
        panel1.add(eventType, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        eventGravity = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel2 = new DefaultComboBoxModel();
        defaultComboBoxModel2.addElement("Verde");
        defaultComboBoxModel2.addElement("Gialla");
        defaultComboBoxModel2.addElement("Arancione");
        defaultComboBoxModel2.addElement("Rossa");
        eventGravity.setModel(defaultComboBoxModel2);
        panel1.add(eventGravity, new GridConstraints(8, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        eventCap = new JTextField();
        panel1.add(eventCap, new GridConstraints(9, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        eventCapLabel = new JLabel();
        eventCapLabel.setForeground(new Color(-16777216));
        eventCapLabel.setText("Cap");
        panel1.add(eventCapLabel, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        eventWeatherRisk = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel3 = new DefaultComboBoxModel();
        defaultComboBoxModel3.addElement("1 - Alluvione");
        defaultComboBoxModel3.addElement("2 - Temporali e fulmini");
        defaultComboBoxModel3.addElement("3 - Frana");
        defaultComboBoxModel3.addElement("4 - Pioggia e grandine");
        defaultComboBoxModel3.addElement("5 - Neve e gelo");
        defaultComboBoxModel3.addElement("6 - Valanga");
        defaultComboBoxModel3.addElement("7 - Nebbia");
        defaultComboBoxModel3.addElement("8 - Venti e mareggiate");
        defaultComboBoxModel3.addElement("9 - Ciclone");
        defaultComboBoxModel3.addElement("10 - Altro");
        eventWeatherRisk.setModel(defaultComboBoxModel3);
        panel1.add(eventWeatherRisk, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        eventWeatherWind = new JTextField();
        panel1.add(eventWeatherWind, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        eventWeatherWindLabel = new JLabel();
        eventWeatherWindLabel.setForeground(new Color(-16777216));
        eventWeatherWindLabel.setText("Velocità vento");
        panel1.add(eventWeatherWindLabel, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        eventWeatherRiskLabel = new JLabel();
        eventWeatherRiskLabel.setForeground(new Color(-16777216));
        eventWeatherRiskLabel.setText("Tipo di rischio");
        panel1.add(eventWeatherRiskLabel, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        eventSeismicRichterLabel = new JLabel();
        eventSeismicRichterLabel.setForeground(new Color(-16777216));
        eventSeismicRichterLabel.setText("Richter");
        panel1.add(eventSeismicRichterLabel, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        eventSeismicMercalliLabel = new JLabel();
        eventSeismicMercalliLabel.setForeground(new Color(-16777216));
        eventSeismicMercalliLabel.setText("Mercalli");
        panel1.add(eventSeismicMercalliLabel, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        eventSeismicRichter = new JTextField();
        panel1.add(eventSeismicRichter, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        eventSeismicMercalli = new JTextField();
        panel1.add(eventSeismicMercalli, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        eventWeatherLabel = new JLabel();
        eventWeatherLabel.setForeground(new Color(-16777216));
        eventWeatherLabel.setText("Meteo-idro");
        panel1.add(eventWeatherLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        eventSeismicLabel = new JLabel();
        eventSeismicLabel.setForeground(new Color(-16777216));
        eventSeismicLabel.setText("Sismico");
        panel1.add(eventSeismicLabel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        eventSeismicEpicentre = new JTextField();
        panel1.add(eventSeismicEpicentre, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        eventSeismicEpicentreLabel = new JLabel();
        eventSeismicEpicentreLabel.setForeground(new Color(-16777216));
        eventSeismicEpicentreLabel.setText("Epicentro");
        panel1.add(eventSeismicEpicentreLabel, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(10, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        eventDesc = new JTextArea();
        eventDesc.setBackground(new Color(-1));
        Font eventDescFont = this.$$$getFont$$$("JetBrains Mono", -1, -1, eventDesc.getFont());
        if (eventDescFont != null) eventDesc.setFont(eventDescFont);
        eventDesc.setForeground(new Color(-16777216));
        eventDesc.setLineWrap(false);
        eventDesc.setRows(5);
        eventDesc.setText("");
        scrollPane1.setViewportView(eventDesc);
        eventDescLabel = new JLabel();
        eventDescLabel.setForeground(new Color(-16777216));
        eventDescLabel.setText("Descrizione");
        panel1.add(eventDescLabel, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(-1, 50), 0, false));
        eventTypeLabel = new JLabel();
        eventTypeLabel.setForeground(new Color(-16777216));
        eventTypeLabel.setText("Tipo evento");
        panel1.add(eventTypeLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        eventActionLabel = new JLabel();
        eventActionLabel.setForeground(new Color(-16777216));
        eventActionLabel.setText("Azione");
        panel1.add(eventActionLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        eventActionType = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel4 = new DefaultComboBoxModel();
        defaultComboBoxModel4.addElement("Aggiungi evento");
        defaultComboBoxModel4.addElement("Modifica evento");
        eventActionType.setModel(defaultComboBoxModel4);
        panel1.add(eventActionType, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        eventActionModify = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel5 = new DefaultComboBoxModel();
        eventActionModify.setModel(defaultComboBoxModel5);
        panel1.add(eventActionModify, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        eventBeginLabel = new JLabel();
        eventBeginLabel.setForeground(new Color(-16777216));
        eventBeginLabel.setText("Data Inizio (dd-MM-yyyy)");
        panel1.add(eventBeginLabel, new GridConstraints(11, 1, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        eventBeginHourLabel = new JLabel();
        eventBeginHourLabel.setForeground(new Color(-16777216));
        eventBeginHourLabel.setText("Ora inizio (HH:mm)");
        panel1.add(eventBeginHourLabel, new GridConstraints(11, 2, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        eventBeginDate = new JTextField();
        panel1.add(eventBeginDate, new GridConstraints(12, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        eventBeginHour = new JTextField();
        panel1.add(eventBeginHour, new GridConstraints(12, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        eventEnd = new JLabel();
        eventEnd.setText("Seleziona inizio e durata.");
        panel1.add(eventEnd, new GridConstraints(15, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setForeground(new Color(-16777216));
        label1.setText("Data di fine:");
        panel1.add(label1, new GridConstraints(15, 1, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        eventLasting = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel6 = new DefaultComboBoxModel();
        defaultComboBoxModel6.addElement("0");
        defaultComboBoxModel6.addElement("4");
        defaultComboBoxModel6.addElement("8");
        defaultComboBoxModel6.addElement("12");
        defaultComboBoxModel6.addElement("16");
        defaultComboBoxModel6.addElement("20");
        eventLasting.setModel(defaultComboBoxModel6);
        panel1.add(eventLasting, new GridConstraints(14, 2, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setForeground(new Color(-16777216));
        label2.setText("Ore");
        panel1.add(label2, new GridConstraints(13, 2, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panel1.add(eventLastingDays, new GridConstraints(14, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setForeground(new Color(-16777216));
        label3.setText("Giorni");
        panel1.add(label3, new GridConstraints(13, 1, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Durata");
        panel1.add(label4, new GridConstraints(13, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 2, new Insets(10, 0, 10, 0), -1, -1, true, false));
        panel3.setBackground(new Color(-1));
        panel2.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        eventSend = new JButton();
        eventSend.setAutoscrolls(false);
        eventSend.setBackground(new Color(-4521981));
        eventSend.setBorderPainted(true);
        eventSend.setContentAreaFilled(true);
        eventSend.setForeground(new Color(-1));
        eventSend.setText("Invia evento");
        panel3.add(eventSend, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setAutoscrolls(false);
        buttonCancel.setBackground(new Color(-4521981));
        buttonCancel.setBorderPainted(true);
        buttonCancel.setContentAreaFilled(true);
        buttonCancel.setForeground(new Color(-1));
        buttonCancel.setText("Cancel");
        panel3.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    private void createUIComponents() {
        eventLastingDays = new JSpinner(new SpinnerNumberModel(0, 0, null, 1));
    }
}
