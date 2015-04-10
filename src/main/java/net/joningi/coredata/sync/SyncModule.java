/*
 * Copyright 2015 Azazo
 *
 */
package net.joningi.coredata.sync;

import net.joningi.coredata.sync.config.ConfigService;
import net.joningi.coredata.sync.config.JsonConfigService;
import net.joningi.coredata.sync.io.DownloadService;
import net.joningi.coredata.sync.io.DownloadServiceImpl;
import net.joningi.coredata.sync.io.UploadService;
import net.joningi.coredata.sync.io.UploadServiceImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class SyncModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ConfigService.class).to(JsonConfigService.class);
        bind(DownloadService.class).to(DownloadServiceImpl.class).in(Singleton.class);
        bind(UploadService.class).to(UploadServiceImpl.class).in(Singleton.class);
    }
}
