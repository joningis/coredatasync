/*
 * Copyright 2015 Azazo
 *
 */
package net.joningi.coredata.sync.io;

import net.joningi.coredata.sync.DirectoryManager;
import net.joningi.coredata.sync.dto.Document;

public interface UploadService {
    public void upload(UploadNotification uploadNotification, Document document);
}
