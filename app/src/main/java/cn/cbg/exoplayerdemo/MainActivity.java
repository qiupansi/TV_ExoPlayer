package cn.cbg.exoplayerdemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v17.leanback.widget.HorizontalGridView;
import android.support.v17.leanback.widget.OnChildViewHolderSelectedListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.LinearLayout;

import com.google.android.exoplayer2.offline.DownloadService;
import com.whatjay.recyclerview.adapter.BaseSmartAdapter;

import java.util.ArrayList;
import java.util.List;

import cn.cbg.exoplayer.BaseFactory;
import cn.cbg.exoplayer.DownloadTracker;
import cn.cbg.exoplayer.PlayerExo;
import cn.cbg.exoplayer.PlayerView;
import cn.cbg.exoplayer.utils.CINDEX;
import cn.cbg.exoplayer.utils.ExoDownloadService;
import cn.cbg.exoplayer.utils.Utils;
import cn.cbg.exoplayerdemo.adapter.TVListAdapter;

//更多TV项目资源(如桌面，直播点播，教育，应用市场，文件管理器，设置，酒店应用等)，添加微信：qiupansi
//If you want more TV project resources,such as TvLauncher,TvLive,TvAppStore,TvSettings,TvFileManager,TvEducation,TvHotel,TvMusic,TvRemote and so on，Add me wechat：qiupansi
public class MainActivity extends AppCompatActivity implements PlayerView.PlayState, BaseSmartAdapter.OnRecyclerViewItemClickListener, DownloadTracker.Listener {
    private PlayerView playerView;
    private LinearLayout ll_tv_list;
    private HorizontalGridView hgv;
    private String mediaName = "电影";
    private int position;
    private List<String> seriesItemList;
    private String type = "";
    private boolean showList = false;
    private TVListAdapter listAdapter;
    private CountDownTimer countDownTimerList;
    private long time = 0;
    private String episodeId = "0";
    private PlayerExo playerExo;
    private long startPosition = CINDEX.TIME_UNSET;
    private int startWindow = CINDEX.INDEX_UNSET;
    private String adUrl = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dlinear&correlator=";
    private Uri[] uris = new Uri[1];
    private DownloadTracker downloadTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        downloadTracker = BaseFactory.getInstance(this).getDownloadTracker();
        startService(new Intent(this, ExoDownloadService.class).setAction(DownloadService.ACTION_INIT));
        playerView = findViewById(R.id.epv);
        ll_tv_list = findViewById(R.id.ll_tv_list);
        hgv = findViewById(R.id.hgv);
        seriesItemList = new ArrayList<>();
        playerExo = new PlayerExo(this, playerView);
        // modify by psqiu
        //uris[0] = Uri.parse("http://218.70.82.218:9568/Data/2019-04-26/file_41f6245b-c53a-45e8-9a5c-8d08f39e30dc/playlist.m3u8");
        uris[0] = Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4");
//        uris[0] = Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/big_buck_bunny.mp3");
        initList();
        initExoPlayer();
    }

    private void initList() {
        if (startPosition > 0) {
            startWindow = 0;
        }
        listAdapter = new TVListAdapter(this, R.layout.item_tvprogram, seriesItemList, type);
        hgv.setAdapter(listAdapter);
        hgv.setNumRows(1);
        hgv.setRowHeight(Utils.dp2px(this, 100));
        listAdapter.setOnItemClickListener(this);
        hgv.setSelectedPosition(position);
        hgv.setGravity(Gravity.CENTER);
        listAdapter.setPosition(position);
        listAdapter.setCurrentFocusPosition(position);
        hgv.setOnChildViewHolderSelectedListener(new OnChildViewHolderSelectedListener() {
            @Override
            public void onChildViewHolderSelected(RecyclerView parent, RecyclerView.ViewHolder child, int position, int subposition) {
                super.onChildViewHolderSelected(parent, child, position, subposition);
                if (time <= 2000) {
                    showTVList();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        downloadTracker.addListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (playerView != null) {
            playerView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (playerView != null) {
            playerView.onPause();
        }
    }

    @Override
    public void onStop() {
        downloadTracker.removeListener(this);
        super.onStop();
        if (playerView != null) {
            playerView.onPause();
        }
        if (playerExo.getPlayer() != null) {
            playerExo.releasePlayer();
            startWindow = CINDEX.INDEX_UNSET;
            startPosition = CINDEX.POSITION_UNSET;
//            this.playerExo.getPlayer().stop(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playerExo.releaseAdsLoader();
        cancelTimer();
    }

    private void cancelTimer() {
        if (countDownTimerList != null) {
            countDownTimerList.cancel();
            countDownTimerList = null;
        }
    }

    private void download() {
        downloadTracker.toggleDownload(this, mediaName, uris[0], "");
    }

    private void initExoPlayer() {
        initTitle();
        adUrl = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";
        playerExo.setStartPosition(startWindow, startPosition);
        //http://event.android-studio.org/images/event/201605/google-io-2016/gizmodo-02-Google-Assistant-1.gif
        //https://source.android.google.cn/devices/tv/images/LiveChannels_Add_channel.png
        playerExo.initExoPlayer(uris, "https://source.android.google.cn/devices/tv/images/LiveChannels_sources.png");
        playerView.setPlayStateListener(this);
        //download();
    }

    private void initTitle() {
        if (seriesItemList.size() > 0) {
            playerExo.setTitle(mediaName + "  " + (position + 1));
        } else {
            playerExo.setTitle(mediaName);
        }
        if (startPosition > 0) {
            startWindow = 0;
        }
    }

    @Override
    public void playComplete() {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (showList) {
                    hideTVList();
                    return true;
                }
                if (Utils.isQuickClick()) {
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                hideTVList();
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                if (!showList) {
                    showTVList();
                }
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                Utils.setVolume(this, true, false);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                Utils.setVolume(this, false, false);
                return true;
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                Utils.setVolume(this, false, true);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void hideTVList() {
        if (!showList) {
            return;
        }
        cancelTimer();
        Animation animation = Utils.SlideToBottom(null);
        ll_tv_list.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ll_tv_list.setVisibility(View.GONE);
                showList = false;
                if (hgv.getSelectedPosition() != position) {
                    listAdapter.setCurrentFocusPosition(hgv.getSelectedPosition());
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void showTVList() {
        if (ll_tv_list.getVisibility() != View.VISIBLE) {
            ll_tv_list.setVisibility(View.VISIBLE);
            Utils.getSlideFromBottom(ll_tv_list, null);
            listAdapter.notifyDataSetChanged();
        }
        showList = true;
        cancelTimer();
        countDownTimerList = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                time = millisUntilFinished;
            }

            @Override
            public void onFinish() {
                Animation animation = Utils.SlideToBottom(null);
                ll_tv_list.startAnimation(animation);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        ll_tv_list.setVisibility(View.GONE);
                        showList = false;
                        if (hgv.getSelectedPosition() != position) {
                            listAdapter.setCurrentFocusPosition(hgv.getSelectedPosition());
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        };
        countDownTimerList.start();
    }

    @Override
    public void onItemClick(View view, int position) {
        this.position = position;
        startWindow = CINDEX.INDEX_UNSET;
        startPosition = CINDEX.POSITION_UNSET;
        listAdapter.setPosition(position);
        listAdapter.setCurrentFocusPosition(position);
        if (Utils.isQuickClick()) {
            return;
        }
        initExoPlayer();
        initList();
    }

    @Override
    public void onDownloadsChanged() {

    }
}
