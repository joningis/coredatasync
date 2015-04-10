/*
 * Copyright 2015 Azazo
 *
 */
package net.joningi.coredata.sync.io;

import net.joningi.coredata.sync.dto.Document;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.bangsapabbi.api.CoredataClient;

public class DeleteServiceImpl implements DeleteService {

    private final ExecutorService executor;
    private final CoredataClient coredataClient;

    public DeleteServiceImpl(CoredataClient coredataClient) {
        this.coredataClient = coredataClient;
        this.executor = Executors.newFixedThreadPool(4);
    }

    @Override
    public void delete(final DeleteNotification deleteNotification,final Document document) {
        executor.submit(new DeleteRemoteThread(coredataClient, deleteNotification, document));

    }

}
