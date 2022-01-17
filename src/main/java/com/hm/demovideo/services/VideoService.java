package com.hm.demovideo.services;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.hm.demovideo.interfaces.IVideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class VideoService implements IVideoService {

    private static final String CONTAINER_NAME = "style-stories-service-videos";
    private final BlobServiceClient blobServiceClient;

    @Autowired
    public VideoService(BlobServiceClient blobServiceClient) {
        this.blobServiceClient = blobServiceClient;
    }

    @Override
    public String upload(MultipartFile mediaResource) throws IOException {
        var blobClient = getBlobClient(mediaResource.getOriginalFilename());
        blobClient.upload(mediaResource.getInputStream(), mediaResource.getSize(), Boolean.TRUE);

        var headers = new BlobHttpHeaders();
        headers.setContentType(mediaResource.getContentType());
        blobClient.setHttpHeaders(headers);

        return blobClient.getBlobUrl();
    }

    @Override
    public String uploadFile(File file, BlobHttpHeaders headers) {
        var blobClient = getBlobClient(file.getName());
        blobClient.uploadFromFile(file.getPath(), true);
        blobClient.setHttpHeaders(headers);

        return blobClient.getBlobUrl();
    }

    private BlobClient getBlobClient(String fileName) {
        var containerClient = blobServiceClient.getBlobContainerClient(CONTAINER_NAME);
        if (!containerClient.exists()) {
            containerClient = blobServiceClient.createBlobContainer(CONTAINER_NAME);
        }

        return containerClient.getBlobClient(fileName);
    }
}
