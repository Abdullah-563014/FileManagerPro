package filemanager.fileexplorer.pro.model;

public class VersionInfoModel {

    private double versionName;
    private int versionNumber;
    private String versionMessage;

    public VersionInfoModel() {
    }

    public VersionInfoModel(double versionName, int versionNumber, String versionMessage) {
        this.versionName = versionName;
        this.versionNumber = versionNumber;
        this.versionMessage = versionMessage;
    }

    public double getVersionName() {
        return versionName;
    }

    public void setVersionName(double versionName) {
        this.versionName = versionName;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getVersionMessage() {
        return versionMessage;
    }

    public void setVersionMessage(String versionMessage) {
        this.versionMessage = versionMessage;
    }
}
