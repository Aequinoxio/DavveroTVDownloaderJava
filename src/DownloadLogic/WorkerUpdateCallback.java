package DownloadLogic;


public interface WorkerUpdateCallback {
    public void update (UpdateEvent updateEvent,String message);
  //  public void update (UpdateInfo updateInfo);

    class UpdateInfo{
        private UpdateEvent updateEvent;
        private String message;

        public UpdateEvent getUpdateEvent() {
            return updateEvent;
        }

        public String getMessage() {
            return message;
        }

        public UpdateInfo(UpdateEvent updateEvent, String message) {
            this.updateEvent = updateEvent;
            this.message = message;
        }
    }
}
