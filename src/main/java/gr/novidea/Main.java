package gr.novidea;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.util.Timer;
import java.util.TimerTask;

public class Main implements HotkeyListener {
    private static final int POPUP_DURATION = 5000; // 5 seconds

    public static void main(String[] args) {
        Main jsonFixerService = new Main();
        jsonFixerService.init();
    }

    private void init() {
        if (!JIntellitype.isJIntellitypeSupported()) {
            System.err.println("Service is not supported on this platform.");
            System.exit(1);
        }

        // Shift + Alt + J
        JIntellitype.getInstance().registerHotKey(1, JIntellitype.MOD_SHIFT + JIntellitype.MOD_ALT, (int) 'J');
        JIntellitype.getInstance().addHotKeyListener(this);
        System.out.println("Service up and running");
    }

    @Override
    public void onHotKey(int key) {
        String selectedText = captureSelectedText();
        String fixedJson = fixJson(selectedText);

        try {
            JsonParser.parseString(fixedJson);
            copyToClipboard(fixedJson);
            try {
                Robot robot = new Robot();
                robot.keyPress(KeyEvent.VK_CONTROL);
                robot.keyPress(KeyEvent.VK_V);

                Thread.sleep(500);

                robot.keyRelease(KeyEvent.VK_V);
                robot.keyRelease(KeyEvent.VK_CONTROL);

                Thread.sleep(500);
            } catch (AWTException | InterruptedException e) {
                showNotification("JSON fix failed");
                return;
            }
            showNotification("JSON fixed successfully");
        } catch (JsonSyntaxException e) {
            showNotification("JSON fix failed");
        }
    }


    private static void copyToClipboard(String content) {
        StringSelection stringSelection = new StringSelection(content);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    private String captureSelectedText() {
        Clipboard awtClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = awtClipboard.getContents(null);

        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                return (String) transferable.getTransferData(DataFlavor.stringFlavor);
            } catch (Exception e) {
                showNotification("No JSON Found");
            }
        }

        return "";
    }

    public String fixJson(String jsonString) {
        String fixedJson = null;
        try {
            StringBuilder sb = new StringBuilder();
            jsonString = jsonString.replace("{", "").replace("}", "");

            String[] lines = jsonString.split("\n");

            for (String line : lines) {
                if (line.contains(":")) {

                    line = line.replaceAll(",", "");

                    String attributeName = line.split(":")[0];

                    attributeName = attributeName.trim();
                    attributeName = attributeName.replaceAll("\"", "");
                    attributeName = "\"" + attributeName + "\"";

                    String attributeValue = line.split(":")[1];
                    attributeValue = attributeValue.stripLeading().stripTrailing();
                    if (attributeValue.startsWith("\"") || attributeValue.endsWith("\"")) {
                        attributeValue = attributeValue.replaceAll("\"","");
                        attributeValue= "\"" + attributeValue +"\"";

                    }else if( attributeValue.startsWith("[") || attributeValue.endsWith("]")){
                        attributeValue = attributeValue.replaceAll("\\s+", ",");
                        attributeValue = attributeValue.replaceAll("[\\[\\]]", "");
                        attributeValue= "[" + attributeValue +"]";
                    }

                    line = "\t"+attributeName + " : " + attributeValue +",";
                    sb.append(line);
                    sb.append("\n");
                }
            }


            fixedJson = sb.substring(0, sb.length() - 2) + "\n";
        } catch (Exception ignored) {
        }
        return "{\n" + fixedJson + "}";
    }


    private void showNotification(String message) {
        JDialog dialog = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE).createDialog("JSON Fixer Notification");

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setLocation(screenSize.width - dialog.getWidth(), screenSize.height - dialog.getHeight());

        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                dialog.dispose();
            }
        }, POPUP_DURATION);

        dialog.setVisible(true);
    }
}
