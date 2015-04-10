/*
 * Copyright 2015 Azazo
 *
 */
package net.joningi.coredata.sync;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public class DirectoryWatcher implements Runnable {

    private String folder;

    private boolean running;

    private DirectoryManager manager;

    public DirectoryWatcher(final DirectoryManager manager, final String folder) {
        this.manager = manager;
        this.folder = folder;
    }

    @Override
    public void run() {
        this.running = true;

        try {
            WatchService watcher = FileSystems.getDefault().newWatchService();


            Path dir = Paths.get(folder);
            registerEvents(watcher, dir);

            while (running) {

                // wait for key to be signaled
                WatchKey key;
                try {
                    key = watcher.take();
                } catch (InterruptedException x) {
                    return;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    // This key is registered only
                    // for ENTRY_CREATE events,
                    // but an OVERFLOW event can
                    // occur regardless if events
                    // are lost or discarded.
                    if (kind == OVERFLOW) {
                        continue;
                    }

                    // The filename is the
                    // context of the event.
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();


                    // Resolve the filename against the directory.
                    // If the filename is "test" and the directory is "foo",
                    // the resolved name is "test/foo".
                    Path child = dir.resolve(filename);
                    manager.directoryChange(child, mapWatchEventToFileEvent(kind));
                }

                // Reset the key -- this step is critical if you want to
                // receive further watch events.  If the key is no longer valid,
                // the directory is inaccessible so exit the loop.
                boolean valid = key.reset();
                if (!valid) {
                    break;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private FileEvent mapWatchEventToFileEvent(final WatchEvent.Kind<?> kind) {
        if (kind == ENTRY_CREATE) {
            return FileEvent.CREATE;
        } else if (kind == ENTRY_MODIFY) {
            return FileEvent.MODIFY;
        } else if (kind == ENTRY_DELETE) {
            return FileEvent.DELETE;
        } else {
            return FileEvent.UNKNOWN;
        }
    }

    private void registerEvents(final WatchService watcher, final Path dir) {
        try {
            WatchKey key   = dir.register(watcher,
                    ENTRY_CREATE,
                    ENTRY_DELETE,
                    ENTRY_MODIFY);
        } catch (IOException x) {
            System.err.println(x);
        }
    }
}
