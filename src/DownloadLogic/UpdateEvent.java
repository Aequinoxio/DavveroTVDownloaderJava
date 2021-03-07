package DownloadLogic;

public enum UpdateEvent {
    StartLoadingPage,
    FirstPageLoaded,
    SecondPageLoading,
    SecondPageLoaded,
    VideoDownloadCanStart,
    VideoDownloadStarted,
    VideoDownloadInProgress,
    VideoDownloadEnded,
    Error
}
