/*
 * Copyright 2015 Azazo
 *
 */
package net.joningi.coredata.sync.dto;

import java.io.File;
import java.util.UUID;

public class Project {

    private String uuid;

    private String name;

    private String navString;
    private String folderPath;
    private String workspaceID;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getNavString() {
        return navString;
    }

    public void setNavString(final String navString) {
        this.navString = navString;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(final String folderPath) {
        this.folderPath = folderPath;
    }

    public File getInfoFile() {
        return new File(this.folderPath + ".info");
    }

    public String getWorkspaceID() {
        return workspaceID;
    }

    public void setWorkspaceID(final String workspaceID) {
        this.workspaceID = workspaceID;
    }
}
