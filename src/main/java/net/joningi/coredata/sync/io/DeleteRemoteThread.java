/*
 * Copyright 2015 Azazo
 *
 */
package net.joningi.coredata.sync.io;

import net.joningi.coredata.sync.dto.Document;

import com.bangsapabbi.api.CoredataClient;
import com.bangsapabbi.api.file.File;
import com.bangsapabbi.api.file.FileService;

public class DeleteRemoteThread implements Runnable {
    private final CoredataClient client;
    private final DeleteNotification deleteNotification;
    private final Document document;

    public DeleteRemoteThread(final CoredataClient coredataClient,
                              final DeleteNotification deleteNotification, final Document document) {
        this.client = coredataClient;
        this.deleteNotification = deleteNotification;
        this.document = document;
    }

    @Override
    public void run() {
        final FileService fileService = client.getFileService();

        if (this.document.getUUID() != null) {
            fileService.delete(document.getUUID());
            deleteNotification.deleteFinished(document);
        }
    }
}
