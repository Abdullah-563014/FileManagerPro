package filemanager.fileexplorer.pro.model;

public class AdsModel {
    private int adsIntervalInHours;
    private boolean willShowAds;


    public AdsModel() {
    }


    public AdsModel(int adsIntervalInHours, boolean willShowAds) {
        this.adsIntervalInHours = adsIntervalInHours;
        this.willShowAds = willShowAds;
    }

    public int getAdsIntervalInHours() {
        return adsIntervalInHours;
    }

    public void setAdsIntervalInHours(int adsIntervalInHours) {
        this.adsIntervalInHours = adsIntervalInHours;
    }

    public boolean getWillShowAds() {
        return willShowAds;
    }

    public void setWillShowAds(boolean willShowAds) {
        this.willShowAds = willShowAds;
    }


}
