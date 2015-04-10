/*
 * Copyright 2015 Azazo
 *
 */
package net.joningi.coredata.sync;

import net.joningi.coredata.sync.dto.Document;
import net.joningi.coredata.sync.dto.Project;
import net.joningi.coredata.sync.io.DeleteNotification;
import net.joningi.coredata.sync.io.DeleteService;
import net.joningi.coredata.sync.io.DocumentService;
import net.joningi.coredata.sync.io.DownloadNotification;
import net.joningi.coredata.sync.io.DownloadService;
import net.joningi.coredata.sync.io.UploadNotification;
import net.joningi.coredata.sync.io.UploadService;
import net.joningi.coredata.sync.utils.FileUtils;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bangsapabbi.api.CoredataClient;
import com.bangsapabbi.api.nav.Nav;
import com.bangsapabbi.api.nav.NavService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;


public class DirectoryManager implements DownloadNotification, UploadNotification, DirectoryObserver, DeleteNotification {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryManager.class);

    private final CoredataClient coredataClient;
    private final UploadService uploadService;
    private final DeleteService deleteService;
    private final Project project;

    // UUID to document
    private final Map<String, Document> uuidDocuments;


    private final Map<String, Document> pathToDocument;

    private final Set<String> pathsInDownload;

    private DocumentService documentService;
    private DownloadService downloadService;

    /**
     * Set of files being downloaded at startup, will trigger events that we want to ignore.
     */
    private Set<String> initDownloads;


    public DirectoryManager(CoredataClient coredataClient,
                            DocumentService documentService,
                            DownloadService downloadService,
                            UploadService uploadService,
                            DeleteService deleteService,
                            Project project) {
        this.coredataClient = coredataClient;
        this.documentService = documentService;
        this.downloadService = downloadService;
        this.uploadService = uploadService;
        this.deleteService = deleteService;
        this.project = project;
        this.uuidDocuments = Maps.newHashMap();
        this.pathToDocument = Maps.newHashMap();
        this.pathsInDownload = Sets.newHashSet();
        this.initDownloads = Sets.newHashSet();

        getDocumentsInfo(coredataClient);
        loadKnownDocuments();

        ApiWatcher apiWatcher = new ApiWatcher(this, coredataClient, project);
        new Thread(apiWatcher).start();

        DirectoryWatcher watcher = new DirectoryWatcher(this, project.getFolderPath());
        new Thread(watcher).start();
    }

    @Override
    public void downloadFinished(Document document) {
        document.setDownloaded();
        this.pathsInDownload.remove(document.getFilePath());
        LOGGER.info("Finished downloading document " + document.getName());
        updateModifiedDate(document);
    }

    @Override
    public synchronized void uploadFinished(final Document document) {

        this.pathsInDownload.remove(document.getFilePath());
        LOGGER.info("Finished uploading document " + document.getName());

        if (!this.uuidDocuments.containsKey(document.getUUID())) {
            // This is a new document that was added locally and uploaded to server.
            final NavService navService = coredataClient.getNavService();

            this.uuidDocuments.put(document.getUUID(), document);
            this.pathToDocument.put(document.getFilePath(), document);

            String navString = project.getNavString().substring(4) + "/" + document.getName();
            Nav nav = navService.get(navString);
            document.setSnapshotID(nav.getSnapshotId());


            LOGGER.info("Updated info for new document " + document.getName());
        }
        document.setDownloaded();
        updateModifiedDate(document);
    }

    @Override
    public void deleteFinished(final Document document) {
        this.pathsInDownload.remove(document.getFilePath());

        if (this.uuidDocuments.containsKey(document.getUUID())) {
            this.uuidDocuments.remove(document.getUUID());
            this.pathToDocument.remove(document.getFilePath());
        }
        document.setDownloaded();
        LOGGER.info("Finished deleting document " + document.getName());
        updateModifiedDate(document);
    }

    private void updateModifiedDate(final Document document) {
        File file = new File(document.getFilePath());
        document.setLastModifiedLocally(file.lastModified());
    }

    /**
     * Save .info file
     * Wait for uploads in progress (what if not connected to internet?)
     */
    public void shutdown() {

        Gson gson = new Gson();
        PrintWriter writer;
        try {
            writer = new PrintWriter(this.project.getInfoFile(), "UTF-8");

            for (Document document : uuidDocuments.values()) {
                writer.println(gson.toJson(document));
            }

            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }


    private void loadKnownDocuments() {
        try {
            Map<String, Document> currentDocuments = new HashMap<>(this.uuidDocuments);
            List<String> lines;
            if (this.project.getInfoFile().exists()) {
                lines = Files.readAllLines(this.project.getInfoFile().toPath());
            } else {
                lines = Lists.newArrayList();
            }
            Gson gson = new Gson();
            for (String line : lines) {
                Document document = gson.fromJson(line, Document.class);
                if (currentDocuments.containsKey(document.getUUID())) {
                    File localFile = new File(document.getFilePath());

                    // The current version first checks if we have updated version online, then checks if our local version is more
                    // resent then the one online. So server has higher priority then local
                    // TODO(joningi): Change this to avoid overwriting
                    if (!currentDocuments.get(document.getUUID()).getSnapshotID().equals(document.getSnapshotID())) {
                        // We have updated version of the file online
                        initDownloadDocument(document);

                        LOGGER.info("File has been changed " + document.getName() + "(" + document.getUUID() + ")");
                    } else if(localFile.lastModified() > document.getLastModifiedLocally()) {
                        // The file has local changes that have not been sent to the server
                        LOGGER.info("Local file has been changed " + document.getName() + "(" + document.getUUID() + ")");
                        this.pathsInDownload.add(document.getFilePath());
                        this.uploadService.upload(this, document);
                    }
                    else {
                        LOGGER.info("File is already the most recent version " + document.getName() + "(" + document.getUUID() + ")");
                    }
                    currentDocuments.remove(document.getUUID());
                } else {
                    // File was not found in coredata (has been deleted)
                    documentService.delete(document);
                    if (this.uuidDocuments.containsKey(document.getUUID())) {
                        this.uuidDocuments.remove(document.getUUID());
                        this.pathToDocument.remove(document.getFilePath());
                    }

                    //TODO(joningi): Setja callback á þetta til að láta vita að búið sé að eyða
                    // Þurfum að halda utanum skrár sem er verið að eyða og koma delete eventar um.


                    LOGGER.info("File has been deleted " + document.getName() + "(" + document.getUUID() + ")");
                }
            }

            // Check if there are any new files online that we need to download
            for (Map.Entry<String, Document> entry : currentDocuments.entrySet()) {
                initDownloadDocument(entry.getValue());
                LOGGER.info("File was not on local machine " + entry.getValue().getName() + "(" + entry.getValue().getUUID() + ")");
            }

        } catch (IOException e) {
            LOGGER.error("Unable to read info file for project\n" + ExceptionUtils.getStackTrace(e));
        }
    }

    private void initDownloadDocument(final Document document) {
        this.initDownloads.add(document.getFilePath());
        this.downloadDocument(document);
    }

    private void downloadDocument(final Document document) {
        this.pathsInDownload.add(document.getFilePath());
        downloadService.download(this, document);
    }


    /**
     * Used on startup to get the most current version of files in the folder
     *
     * @param coredataClient
     */
    private void getDocumentsInfo(final CoredataClient coredataClient) {
        final NavService navService = coredataClient.getNavService();

        for (Nav nav : navService.getAll(project.getNavString())) {
            Document doc = new Document(
                    nav.getName(),
                    nav.getId(),
                    nav.getSnapshotId(),
                    project.getFolderPath() + nav.getName(),
                    nav.getParentId());

            this.uuidDocuments.put(nav.getId(), doc);
            this.pathToDocument.put(project.getFolderPath() + nav.getName(), doc);
        }
    }

    @Override
    public synchronized void directoryChange(final Path path, final FileEvent fileEvent) {

        // If we are downloading the file we do not want to upload it at the same time
        // This also protects from the events that happen when we are initializing the folder
        // after startup, create, modify, delete.

        if(FileUtils.isTempFile(path.getFileName().toString())) {
            LOGGER.info("Tmp file, do nothing " + path.toString());
        } else {
            LOGGER.info("Not tmp file");
            final Document document = pathToDocument.get(path.toString());
            if (document == null) {
                // new document
                Document newDocument = new Document(nameFromPath(path), null, null, path.toString(),
                        this.project.getWorkspaceID());
                this.initDownloads.add(path.toString());
                this.pathsInDownload.add(path.toString());
                this.uploadService.upload(this, newDocument);
            } else {
                if (!pathsInDownload.contains(document.getFilePath())) {
                    if (initDownloads.contains(document.getFilePath())) {
                        this.initDownloads.remove(document.getFilePath());
                    } else {
                        if (fileEvent.equals(FileEvent.CREATE) || fileEvent.equals(FileEvent.MODIFY)) {

                            this.pathsInDownload.add(document.getFilePath());
                            this.uploadService.upload(this, document);
                        } else if (fileEvent.equals(FileEvent.DELETE)) {

                            this.pathsInDownload.add(document.getFilePath());
                            this.deleteService.delete(this, document);
                        }
                    }
                } else {
                    LOGGER.info("Got event for path that is in download/delete state");
                }
            }
        }
        System.out.println(path);
        System.out.println(fileEvent);
    }

    private String nameFromPath(final Path path) {
        return path.getFileName().toString();
    }

    public synchronized Document getDocumentFromUUID(final String uuid) {
        return this.uuidDocuments.get(uuid);
    }


    public synchronized void changedDocument(final Document document) {
        initDownloadDocument(document);
        this.uuidDocuments.put(document.getUUID(), document);
        this.pathToDocument.put(project.getFolderPath() + document.getName(), document);
        LOGGER.info("Document has been changed on server " + document.getName());

    }

    public synchronized void createDocument(final Document document) {
        initDownloadDocument(document);
        this.uuidDocuments.put(document.getUUID(), document);
        this.pathToDocument.put(project.getFolderPath() + document.getName(), document);
        LOGGER.info("New document on server " + document.getName());
    }

    public boolean isTransferInProgress() {
        return this.pathsInDownload.size() > 0;
    }


    public Map<String, Document> getUuidDocuments() {
        return uuidDocuments;
    }

    public void deleteDocument(final Document document) {
        this.documentService.delete(document);
        LOGGER.info("Local file deleted " + document.getName());

        if (this.uuidDocuments.containsKey(document.getUUID())) {
            this.uuidDocuments.remove(document.getUUID());
            this.pathToDocument.remove(document.getFilePath());
        }
    }
}
