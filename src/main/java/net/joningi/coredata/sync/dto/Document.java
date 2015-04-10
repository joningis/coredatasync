/*
 * Copyright 2015 Azazo
 *
 */
package net.joningi.coredata.sync.dto;

public class Document {

    private String name;

    private String id;

    private String filePath;
    private String snapshotID;
    private boolean downloaded;
    private String parentUUID;

    public Document(final String name,
                    final String id,
                    final String snapshotID,
                    final String filePath,
                    final String parentUUID) {
        this.name = name;
        this.id = id;
        this.snapshotID = snapshotID;
        this.filePath = filePath;
        this.parentUUID = parentUUID;
    }

    public String getUUID() {
        return id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(final String filePath) {
        this.filePath = filePath;
    }

    public String getName() {
        return name;
    }

    public String getSnapshotID() {
        return snapshotID;
    }

    public void setSnapshotID(final String snapshotID) {
        this.snapshotID = snapshotID;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    //TODO(joningi): NEED to rename this, inTransport or something similar
    public void setDownloaded() {
        this.downloaded = true;
    }

    public String getParentUUID() {
        return parentUUID;
    }

    public void setParentUUID(final String parentUUID) {
        this.parentUUID = parentUUID;
    }

    public void setUUID(final String UUID) {
        this.id = UUID;
    }
}
