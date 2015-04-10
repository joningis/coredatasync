/*
 * Copyright 2015 Azazo
 *
 */
package net.joningi.coredata.sync.io;

import net.joningi.coredata.sync.dto.Document;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.bangsapabbi.api.CoredataClient;

public class UploadServiceImpl implements UploadService {

    private final ExecutorService executor;
    private final CoredataClient coredataClient;

    public UploadServiceImpl(CoredataClient coredataClient) {
        this.coredataClient = coredataClient;
        this.executor = Executors.newFixedThreadPool(4);
    }

    @Override
    public void upload(final UploadNotification uploadNotification, final Document document) {
        executor.submit(new UploadThread(coredataClient, uploadNotification, document));
    }
}
