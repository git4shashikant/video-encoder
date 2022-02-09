package com.hm.demovideo.tasks;

import com.azure.storage.blob.models.BlobHttpHeaders;
import com.hm.demovideo.interfaces.IVideoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import ws.schild.jave.process.ProcessLocator;
import ws.schild.jave.process.ffmpeg.DefaultFFMPEGLocator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.Future;

/******************************************************************************
* ffmpeg -i input.mp4 -map 0 -c copy -f segment -segment_time 1800 -reset_timestamps 1 output_%03d.mp4
* *****************************************************************************/
@Component
public class SegmentationTask {

    private final ProcessLocator locator;
    private final IVideoService videoService;

    private static final String MP4_CONTENT_TYPE = "video/mp4";
    private static final Log LOG = LogFactory.getLog(SegmentationTask.class);

    @Autowired
    public SegmentationTask(IVideoService videoService) {
        this.locator = new DefaultFFMPEGLocator();
        this.videoService = videoService;
    }

    @Async("asyncExecutor")
    public Future<String> segmentVideo(File source, String fileName, String targetFormat) throws IOException {
        var ffmpeg = this.locator.createExecutor();

        ffmpeg.addArgument("-i");
        ffmpeg.addArgument(source.getAbsolutePath());
        ffmpeg.addArgument("-map");
        ffmpeg.addArgument("0");
        ffmpeg.addArgument("-codec:");
        ffmpeg.addArgument("copy");
        ffmpeg.addArgument("-f");
        ffmpeg.addArgument("segment");
        ffmpeg.addArgument("-segment_time");
        ffmpeg.addArgument("10");
        ffmpeg.addArgument("-reset_timestamps");
        ffmpeg.addArgument("1");
        ffmpeg.addArgument(String.format("%s//%s.%s", "temp", fileName + "%03d", targetFormat));

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
        headers.setContentType(MP4_CONTENT_TYPE);
        Arrays.stream(source.getParentFile().listFiles())
                .filter(file -> file.getName().contains(fileName) && file.getName().endsWith(".mp4"))
                .forEach(tsFile -> videoService.uploadFile(tsFile, headers));

        Arrays.stream(source.getParentFile().listFiles())
                .filter(file -> file.getName().contains(fileName) && file.getName().endsWith(".mp4"))
                .forEach(tsFile -> {
                    try {
                        FileUtils.forceDelete(tsFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        return new AsyncResult<>(null);
    }
}
