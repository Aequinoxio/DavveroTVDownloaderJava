package DownloadLogic;

public class VideoUrlAndQuality {
    String videoUrl;
    String quality;

    public VideoUrlAndQuality(String quality, String videoUrl) {
        this.videoUrl = videoUrl;
        this.quality = quality;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getQuality() {
        return quality;
    }

    @Override
    public String toString() {
        return "quality= " + quality ;
    }
}

