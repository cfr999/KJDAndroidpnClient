package org.androidpn.demoapp;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.MediaController;


import org.androidpn.client.NotificationHistory;
import org.androidpn.utils.ImageLoader;
import org.androidpn.view.CustomVideoView;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import app.dinus.com.loadingdrawable.LoadingView;

public class SkimActivity extends Activity implements AbsListView.OnScrollListener{


    //以下变量图片相关
    private List<String> mUrList = new ArrayList<String>();
    ImageLoader mImageLoader;
    private GridView mImageGridView;
    private BaseAdapter mImageAdapter;

    private boolean mIsGridViewIdle = true;
    private int mImageWidth = 0;
    private boolean mIsWifi = false;
    private boolean mCanGetBitmapFromNetWork = false;


    private ImageView mImageView;
    private CustomVideoView mVideoView;
    private List<NotificationHistory> mList = new ArrayList<NotificationHistory>();
    private NotificationHistory mNotificationHistory;
    private LoadingView mLoadingView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skim);
        mImageLoader = ImageLoader.build(this);
        mList = DataSupport.findAll(NotificationHistory.class);
        if (mList.size() !=0){
            mNotificationHistory =  mList.get(0);
        }
        initData();
        initView();

    }

    private void initView() {

        mImageView = (ImageView) findViewById(R.id.image);
        mVideoView = (CustomVideoView) findViewById(R.id.video_view);
        mLoadingView = (LoadingView) findViewById(R.id.guard_view);
//        mLoadingView.setLoadingRenderer(new GuardLoadingRenderer.Builder(this).build());
//        if (mNotificationHistory != null){
            mImageLoader.bindBitmap( "http://pic41.nipic.com/20140518/4135003_102912523000_2.jpg",
                    mImageView, mImageWidth, mImageWidth);
            mVideoView.setMediaController(new MediaController(this));
            mVideoView.setVideoURI(
                    Uri.parse("http://flv2.bn.netease.com/videolib3/1604/28/fVobI0704/SD/fVobI0704-mobile.mp4"));
//        }else {
//            new LovelyStandardDialog(this)
//                    .setTitle(getResources().getString(R.string.dialog_title))
//                    .setMessage(getResources().getString(R.string.dialog_message))
//                    .setCancelable(true)
//                    .show();
//        }

        mVideoView.start();
        mVideoView.requestFocus();
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
               mLoadingView.setVisibility(View.GONE);
            }
        });

    }


    private void initData() {
        String[] imageUrls = {
                "http://b.hiphotos.baidu.com/zhidao/pic/item/a6efce1b9d16fdfafee0cfb5b68f8c5495ee7bd8.jpg",
                "http://pic47.nipic.com/20140830/7487939_180041822000_2.jpg",
                "http://pic41.nipic.com/20140518/4135003_102912523000_2.jpg",
                "http://img2.imgtn.bdimg.com/it/u=1133260524,1171054226&fm=21&gp=0.jpg",
                "http://h.hiphotos.baidu.com/image/pic/item/3b87e950352ac65c0f1f6e9efff2b21192138ac0.jpg",
                "http://pic42.nipic.com/20140618/9448607_210533564001_2.jpg",
                "http://pic10.nipic.com/20101027/3578782_201643041706_2.jpg",
                "http://picview01.baomihua.com/photos/20120805/m_14_634797817549375000_37810757.jpg",
                "http://img2.3lian.com/2014/c7/51/d/26.jpg",
                "http://img3.3lian.com/2013/c1/34/d/93.jpg",
                "http://b.zol-img.com.cn/desk/bizhi/image/3/960x600/1375841395686.jpg",
                "http://picview01.baomihua.com/photos/20120917/m_14_634834710114218750_41852580.jpg",
                "http://cdn.duitang.com/uploads/item/201311/03/20131103171224_rr2aL.jpeg",
                "http://imgrt.pconline.com.cn/images/upload/upc/tx/wallpaper/1210/17/c1/spcgroup/14468225_1350443478079_1680x1050.jpg",
                "http://pic41.nipic.com/20140518/4135003_102025858000_2.jpg",
                "http://www.1tong.com/uploads/wallpaper/landscapes/200-4-730x456.jpg",
                "http://pic.58pic.com/58pic/13/00/22/32M58PICV6U.jpg",
                "http://picview01.baomihua.com/photos/20120629/m_14_634765948339062500_11778706.jpg",
                "http://h.hiphotos.baidu.com/zhidao/wh%3D450%2C600/sign=429e7b1b92ef76c6d087f32fa826d1cc/7acb0a46f21fbe09cc206a2e69600c338744ad8a.jpg",
                "http://pica.nipic.com/2007-12-21/2007122115114908_2.jpg",
                "http://cdn.duitang.com/uploads/item/201405/13/20140513212305_XcKLG.jpeg",
                "http://photo.loveyd.com/uploads/allimg/080618/1110324.jpg",
                "http://img4.duitang.com/uploads/item/201404/17/20140417105820_GuEHe.thumb.700_0.jpeg",
                "http://cdn.duitang.com/uploads/item/201204/21/20120421155228_i52eX.thumb.600_0.jpeg",
                "http://img4.duitang.com/uploads/item/201404/17/20140417105856_LTayu.thumb.700_0.jpeg",
                "http://img04.tooopen.com/images/20130723/tooopen_20530699.jpg",
                "http://www.qjis.com/uploads/allimg/120612/1131352Y2-16.jpg",
                "http://pic.dbw.cn/0/01/33/59/1335968_847719.jpg",
                "http://a.hiphotos.baidu.com/image/pic/item/a8773912b31bb051a862339c337adab44bede0c4.jpg",
                "http://h.hiphotos.baidu.com/image/pic/item/f11f3a292df5e0feeea8a30f5e6034a85edf720f.jpg",
                "http://img0.pconline.com.cn/pconline/bizi/desktop/1412/ER2.jpg",
                "http://pic.58pic.com/58pic/11/25/04/91v58PIC6Xy.jpg",
                "http://img3.3lian.com/2013/c2/32/d/101.jpg",
                "http://pic25.nipic.com/20121210/7447430_172514301000_2.jpg",
                "http://img02.tooopen.com/images/20140320/sy_57121781945.jpg",
                "http://www.renyugang.cn/emlog/content/plugins/kl_album/upload/201004/852706aad6df6cd839f1211c358f2812201004120651068641.jpg"
        };
        for (String url : imageUrls) {
            mUrList.add(url);
        }
    }



    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            mIsGridViewIdle = true;
            mImageAdapter.notifyDataSetChanged();
        } else {
            mIsGridViewIdle = false;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        // ignored

    }

    @Override
    protected void onStop() {
        super.onStop();

    }
}