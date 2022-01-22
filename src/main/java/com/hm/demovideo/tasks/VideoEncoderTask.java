package com.hm.demovideo.tasks;

import com.azure.storage.blob.models.BlobHttpHeaders;
import com.hm.demovideo.interfaces.IVideoService;
import com.hm.demovideo.models.EncodingAttributeType;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;
import ws.schild.jave.info.VideoSize;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;

@Component
public class VideoEncoderTask {

    private static final String MP4_CONTENT_TYPE = "video/mp4";
    private final IVideoService videoService;

    @Autowired
    public VideoEncoderTask(IVideoService videoService) {
        this.videoService = videoService;
    }

    @Async("asyncExecutor")
    public Future<String> encode(File source, EncodingAttributeType attributeType, File target, String targetFormat) throws EncoderException, IOException {
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec(attributeType.getAudioCodec());
        audio.setBitRate(attributeType.getAudioBitRate());
        audio.setChannels(attributeType.getAudioChannels());
        audio.setSamplingRate(attributeType.getAudioSamplingRate());

        VideoAttributes video = new VideoAttributes();
        video.setCodec(attributeType.getVideoCodec());
        video.setX264Profile(attributeType.getVideoProfile());
        video.setBitRate(attributeType.getVideoBitRate());
        video.setFrameRate(attributeType.getVideoFrameRate());
        video.setSize(new VideoSize(attributeType.getVideoWidth(), attributeType.getVideoHeight()));

        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setAudioAttributes(audio);
        attrs.setVideoAttributes(video);
        attrs.setOutputFormat(targetFormat);

        Encoder encoder = new Encoder();
        encoder.encode(new MultimediaObject(source), target, attrs);

        var headers = new BlobHttpHeaders();
        headers.setContentType(MP4_CONTENT_TYPE);
        String uploadUrl = videoService.uploadFile(target, headers);

        FileUtils.forceDelete(target);
        return new AsyncResult<>(uploadUrl);
    }
}
