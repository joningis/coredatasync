/*
 * Copyright 2015 Azazo
 *
 */
package net.joningi.coredata.sync.io;

import net.joningi.coredata.sync.dto.Document;

public interface DeleteService {

    public void delete( DeleteNotification deleteNotification, Document document);
}
