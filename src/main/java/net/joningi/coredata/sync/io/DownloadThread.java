/*
 * Copyright 2015 Azazo
 *
 */
package net.joningi.coredata.sync.io;

import net.joningi.coredata.sync.dto.Document;

import java.io.IOException;

import com.bangsapabbi.api.CoredataClient;
import com.bangsapabbi.api.file.File;
import com.bangsapabbi.api.file.FileService;

public class DownloadThread implements Runnable {

    private final CoredataClient client;
    private final DownloadNotification downloadNotification;
    private final Document document;

    public DownloadThread(final CoredataClient client,
                          final DownloadNotification downloadNotification,
                          final Document document) {

        this.client = client;
        this.downloadNotification = downloadNotification;
        this.document = document;
    }

    @Override
    public void run() {
        final FileService fileService = client.getFileService();

        File filetoDownload = new File();
        filetoDownload.setUUID(this.document.getUUID());

        final java.io.File outputFile = new java.io.File(this.document.getFilePath());
        try {
            fileService.download(filetoDownload, outputFile, true);
            downloadNotification.downloadFinished(document);
        } catch (IOException e) {
            //TODO(joningi): What to do here ?
            e.printStackTrace();
        }
    }
}
