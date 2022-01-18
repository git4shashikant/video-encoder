package com.hm.demovideo.tasks;

import com.azure.storage.blob.models.BlobHttpHeaders;
import com.hm.demovideo.interfaces.IVideoService;
import com.hm.demovideo.models.EncodingAttributeType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import ws.schild.jave.process.ProcessLocator;
import ws.schild.jave.process.ffmpeg.DefaultFFMPEGLocator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Future;

@Component
public class ThumbnailExtractorTask {

    private static final String PNG_TARGET_FORMAT = ".png";
    private static final Log LOG = LogFactory.getLog(ThumbnailExtractorTask.class);

    private final ProcessLocator locator;
    private final IVideoService videoService;

    public ThumbnailExtractorTask(IVideoService videoService) {
        this.locator = new DefaultFFMPEGLocator();
        this.videoService = videoService;
    }

    @Async("asyncExecutor")
    public Future<String> render(File source, EncodingAttributeType attributeType, int seconds, File target, int quality) throws IOException {
        var ffmpeg = this.locator.createExecutor();
        ffmpeg.addArgument("-i");
        ffmpeg.addArgument(source.getAbsolutePath());
        ffmpeg.addArgument("-f");
        ffmpeg.addArgument("image2");
        ffmpeg.addArgument("-vframes");
        ffmpeg.addArgument("1");
        ffmpeg.addArgument("-ss");
        ffmpeg.addArgument(String.valueOf(seconds));
        ffmpeg.addArgument("-s");
        ffmpeg.addArgument(String.format("%sx%s", attributeType.getVideoWidth(), attributeType.getVideoHeight()));
        ffmpeg.addArgument("-qscale");
        ffmpeg.addArgument(String.valueOf(quality));
        ffmpeg.addArgument(target.getAbsolutePath());

        ffmpeg.execute();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(ffmpeg.getErrorStream()));
            int lineNR = 0;
            String line;
            while ((line = reader.readLine()) != null)
            {
                lineNR++;
                LOG.debug("Input Line (" + lineNR + "): " + line);
            }
        } finally {
            ffmpeg.destroy();
        }

        var headers = new BlobHttpHeaders();
        headers.setContentType(PNG_TARGET_FORMAT);
        String uploadUrl = videoService.uploadFile(target, headers);

        FileUtils.forceDelete(target);
        return new AsyncResult<>(uploadUrl);
    }

}