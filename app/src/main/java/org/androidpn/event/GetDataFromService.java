package org.androidpn.event;

/**
 * Created by zhuzhaolin on 2016/9/24.
 */

public class GetDataFromService {

    private final String getData = "get data";
    private String imageUrl;
    private String videoUrl;

    public GetDataFromService(String imageUrl , String videoUrl) {
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
    }

    public String getData() {
        return getData;
    }
}
