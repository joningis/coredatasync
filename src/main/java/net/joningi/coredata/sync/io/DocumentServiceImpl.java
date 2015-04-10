/*
 * Copyright 2015 Azazo
 *
 */
package net.joningi.coredata.sync.io;

import net.joningi.coredata.sync.dto.Document;

import java.io.File;

public class DocumentServiceImpl implements DocumentService {
    @Override
    public void delete(final Document document) {
        File file = new File(document.getFilePath());
        file.delete();
    }
}
