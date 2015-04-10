/*
 * Copyright 2015 Azazo
 *
 */
package net.joningi.coredata.sync;

import java.nio.file.Path;

public interface DirectoryObserver {
    public void directoryChange( Path path,  FileEvent fileEvent);
}
