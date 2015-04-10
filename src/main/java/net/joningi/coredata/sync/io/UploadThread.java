/*
 * Copyright 2015 Azazo
 *
 */
package net.joningi.coredata.sync.io;

import net.joningi.coredata.sync.dto.Document;
import net.joningi.coredata.sync.utils.FileUtils;

import java.io.IOException;

import com.bangsapabbi.api.CoredataClient;
import com.bangsapabbi.api.file.File;
import com.bangsapabbi.api.file.FileService;

public class UploadThread implements Runnable {

    private final CoredataClient client;
    private final UploadNotification uploadNotification;
    private final Document document;

    public UploadThread(final CoredataClient client,
                        final UploadNotification uploadNotification,
                          final Document document) {

        this.client = client;
        this.uploadNotification = uploadNotification;
        this.document = document;
    }

    @Override
    public void run() {
        final FileService fileService = client.getFileService();

        File filetoUpload = new File();
        if(this.document.getUUID() != null) {
            filetoUpload.setUUID(this.document.getUUID());
        }
        filetoUpload.setLocalPath(this.document.getFilePath());
        filetoUpload.setFilename(document.getName());
        filetoUpload.setTitle(FileUtils.removeFileEnding(document.getName()));
        filetoUpload.setParent(document.getParentUUID());

        try {
            fileService.upload(filetoUpload);
            this.document.setUUID(filetoUpload.getUUID());
            uploadNotification.uploadFinished(document);
        } catch (IOException e) {
            //TODO(joningi): What to do here ?
            e.printStackTrace();
        }
    }


}