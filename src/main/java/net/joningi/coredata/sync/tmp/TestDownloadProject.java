/*
 * Copyright 2015 Azazo
 *
 */
package net.joningi.coredata.sync.tmp;

import java.io.IOException;

import com.bangsapabbi.api.ClientBuilder;
import com.bangsapabbi.api.CoredataClient;
import com.bangsapabbi.api.file.File;
import com.bangsapabbi.api.file.FileService;
import com.bangsapabbi.api.project.Project;
import com.bangsapabbi.api.project.ProjectService;

public class TestDownloadProject {
    public static void main(String[] args) throws IOException {
        String username = "Administrator";
        String password = "Administrator";
        final CoredataClient client = ClientBuilder.newClient(
                "http://localhost:8100", username, password);

        final ProjectService projectService = client.getProjectService();
        final FileService fileService = client.getFileService();

        Project project = new Project();
        project.setUUID("2ead8d2a-d48e-11e4-8568-6003088b5c52");
        project.setTitle("Verkefni");
        for (File file : projectService.getFilesForProject(project)) {
            System.out.println(file.getFilename());
            fileService.download(file, new java.io.File("/tmp/coredata/" + project.getTitle() + "/" + file.getFilename()), false);
        }
    }
}
