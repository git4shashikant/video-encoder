package com.hm.demovideo.interfaces;

import com.azure.storage.blob.models.BlobHttpHeaders;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

public interface IVideoService {
    String upload(MultipartFile resource) throws IOException;
    String uploadFile(File file, BlobHttpHeaders headers);
}
