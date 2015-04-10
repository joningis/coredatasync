/*
 * Copyright 2015 Azazo
 *
 */
package net.joningi.coredata.sync;

import net.joningi.coredata.sync.dto.Document;
import net.joningi.coredata.sync.dto.Project;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.bangsapabbi.api.CoredataClient;
import com.bangsapabbi.api.nav.Nav;
import com.bangsapabbi.api.nav.NavService;

public class ApiWatcher implements Runnable {
    private boolean run = true;

    private DirectoryManager parent;
    private CoredataClient coredataClient;
    private Project project;


    public ApiWatcher(final DirectoryManager parent, final CoredataClient coredataClient, final Project project) {
        this.parent = parent;
        this.coredataClient = coredataClient;
        this.project = project;
    }

    @Override
    public void run() {
        while (run) {
            final NavService navService = coredataClient.getNavService();
            if (!parent.isTransferInProgress()) {
                Map<String, Document> currentDocuments = new HashMap<>(parent.getUuidDocuments());

                for (Nav nav : navService.getAll(project.getNavString())) {
                    if(currentDocuments.containsKey(nav.getId())) {
                        currentDocuments.remove(nav.getId());
                    }
                    // If we are transferring files we want to finish that before adding new content

                    Document document = parent.getDocumentFromUUID(nav.getId());
                    if (document != null) {
                        if (document.isDownloaded() && !document.getSnapshotID().equals(nav.getSnapshotId())) {
                            // File has changed online
                            updateDocument(nav);

                        }
                    } else {
                        // document is new and does not exist locally
                        createDocument(nav);
                    }
                }

                for (Map.Entry<String, Document> entry : currentDocuments.entrySet()) {
                    this.parent.deleteDocument(entry.getValue());
                }
            }

            try {
                Thread.sleep(TimeUnit.MINUTES.toMillis(1));
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void createDocument(final Nav nav) {
        Document doc = new Document(
                nav.getName(),
                nav.getId(),
                nav.getSnapshotId(),
                project.getFolderPath() + nav.getName(),
                nav.getParentId());

        this.parent.createDocument(doc);
    }

    private void updateDocument(final Nav nav) {
        Document doc = new Document(
                nav.getName(),
                nav.getId(),
                nav.getSnapshotId(),
                project.getFolderPath() + nav.getName(),
                nav.getParentId());

        this.parent.changedDocument(doc);
    }

    public void stop() {
        run = false;
    }
}
