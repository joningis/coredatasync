/*
 * Copyright 2015 Azazo
 *
 */
package net.joningi.coredata.sync;

import net.joningi.coredata.sync.config.ConfigException;
import net.joningi.coredata.sync.config.ConfigService;
import net.joningi.coredata.sync.config.JsonConfigService;
import net.joningi.coredata.sync.dto.Project;
import net.joningi.coredata.sync.io.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.*;

import com.bangsapabbi.api.ClientBuilder;
import com.bangsapabbi.api.CoredataClient;
import com.google.common.collect.Maps;

public class Runner {

    static Map<Project, DirectoryManager> monitoredProjects ;

    public static void main(String[] args) throws IOException, ConfigException {

        monitoredProjects = Maps.newHashMap();

        String username = "";
        String password = "";
        final CoredataClient client = ClientBuilder.newClient(
                "http://localhost:8100", username, password);
        DownloadService downloadService = new DownloadServiceImpl(client);
        UploadService uploadService = new UploadServiceImpl(client);
        DeleteService deleteService = new DeleteServiceImpl(client);
        DocumentService documentService = new DocumentServiceImpl();
        ConfigService configService = new JsonConfigService();

        java.util.List objectStoreDatabases = (ArrayList) configService.get("projects");

        for (Object d : objectStoreDatabases) {
            Map pro = (Map) d;
            Project project = new Project();
            project.setName(pro.get("name").toString());
            project.setUuid(pro.get("uuid").toString());
            project.setNavString(pro.get("navString").toString());
            project.setFolderPath(pro.get("folderPath").toString());
            project.setWorkspaceID(client.getProjectService().getWorkspaceUUID(project.getUuid()));
            DirectoryManager manager = new DirectoryManager(client,
                    documentService,
                    downloadService,
                    uploadService,
                    deleteService,
                    project);

            monitoredProjects.put(project, manager);

        }

       /* Project project = new Project();
        project.setName("Verkefni");
        project.setUuid("12175de6-df22-11e4-a242-6003088b5c52");
        project.setNavString("dir/Active%20Projects/Space/Verkefni%20—%202015-1");
        project.setFolderPath("/tmp/coredata/Verkefni/");
*/


        /* Use an appropriate Look and Feel */
        /*
        try {
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        */
        /* Turn off metal's use of bold fonts */
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        //Schedule a job for the event-dispatching thread:
        //adding TrayIcon.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private static void createAndShowGUI() {
        //Check the SystemTray support
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }
        final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon =
                new TrayIcon(createImage("/tmp/azazo.gif"));
        final SystemTray tray = SystemTray.getSystemTray();



        // Create a popup menu components
        MenuItem aboutItem = new MenuItem("About");
        Menu displayMenu = new Menu("Local projects");
        MenuItem exitItem = new MenuItem("Exit");

        //Add components to popup menu
        popup.add(aboutItem);
        popup.add(displayMenu);

        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MenuItem item = (MenuItem) e.getSource();
                //TrayIcon.MessageType type = null;
                System.out.println(item.getLabel());

                //TODO(joningi): Change to hash
                for (Project project : monitoredProjects.keySet()) {
                    if (project.getName().equals(item.getLabel())) {
                        File file = new File(project.getFolderPath());
                        Desktop desktop = Desktop.getDesktop();
                        try {
                            desktop.open(file);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }

                }
            }
        };

        for (Project project : monitoredProjects.keySet()) {
            MenuItem projectMenu = new MenuItem(project.getName());
            displayMenu.add(projectMenu);
            projectMenu.addActionListener(listener);
        }


        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
            return;
        }

        trayIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,
                        "This dialog box is run from System Tray");
            }
        });

        aboutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,
                        "This dialog box is run from the About menu item");
            }
        });




        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tray.remove(trayIcon);
                for (DirectoryManager manager : monitoredProjects.values()) {
                    manager.shutdown();
                }

                System.exit(0);
            }
        });
    }

    protected static Image createImage(String path) {

        String description="";
        String imageURL = path;
        if (imageURL == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }
}
