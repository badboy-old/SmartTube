package com.liskovsoft.smartyoutubetv2.common.app.models.playback.service;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;
import com.liskovsoft.smartyoutubetv2.common.prefs.AppPrefs;
import com.liskovsoft.smartyoutubetv2.common.prefs.AppPrefs.ProfileChangeListener;

import java.util.List;

public class VideoStateService implements ProfileChangeListener {
    @SuppressLint("StaticFieldLeak")
    private static VideoStateService sInstance;
    private static final int MAX_PERSISTENT_STATE_SIZE = 200;
    // Don't store state inside Video object.
    // As one video might correspond to multiple Video objects.
    //private final Map<String, State> mStates = Helpers.createLRUMap(MAX_PERSISTENT_STATE_SIZE);
    private final List<State> mStates = Helpers.createLRUList(MAX_PERSISTENT_STATE_SIZE);
    private final AppPrefs mPrefs;
    private static final String DELIM = "&si;";
    private boolean mIsHistoryBroken;

    private VideoStateService(Context context) {
        mPrefs = AppPrefs.instance(context);
        mPrefs.addListener(this);
        restoreState();
    }

    public static VideoStateService instance(Context context) {
        if (sInstance == null && context != null) {
            sInstance = new VideoStateService(context.getApplicationContext());
        }

        return sInstance;
    }

    public List<State> getStates() {
        return mStates;
    }

    public State getByVideoId(String videoId) {
        for (State state : mStates) {
            if (Helpers.equals(videoId, state.video.videoId)) {
                return state;
            }
        }

        return null;
    }

    public void removeByVideoId(String videoId) {
        Helpers.removeIf(mStates, state -> Helpers.equals(state.video.videoId, videoId));
    }

    public boolean isEmpty() {
        return mStates.isEmpty();
    }

    public void save(State state) {
        mStates.add(state);
    }

    public void clear() {
        mStates.clear();
        persistState();
    }

    public void setHistoryBroken(boolean isBroken) {
        mIsHistoryBroken = isBroken;
    }

    public boolean isHistoryBroken() {
        return mIsHistoryBroken;
    }

    private void restoreState() {
        mStates.clear();
        String data = mPrefs.getStateUpdaterData();

        String[] split = Helpers.splitData(data);

        setStateData(Helpers.parseStr(split, 0));
        mIsHistoryBroken = Helpers.parseBoolean(split, 1);
    }

    public void persistState() {
        mPrefs.setStateUpdaterData(Helpers.mergeData(getStateData(), mIsHistoryBroken));
    }

    public static class State {
        private static final String DELIM = "&sf;";
        public final Video video;
        public final long positionMs;
        public final long durationMs;
        public final float speed;
        public final long timestamp = System.currentTimeMillis();

        public State(Video video, long positionMs) {
            this(video, positionMs, -1);
        }

        public State(Video video, long positionMs, long durationMs) {
            this(video, positionMs, durationMs, 1.0f);
        }

        public State(Video video, long positionMs, long durationMs, float speed) {
            this.video = video;
            this.positionMs = positionMs;
            this.durationMs = durationMs;
            this.speed = speed;
        }

        public static State from(String spec) {
            if (spec == null) {
                return null;
            }

            String[] split = Helpers.split(DELIM, spec);

            if (split.length != 4) {
                return null;
            }

            String videoId = split[0];
            long positionMs = Helpers.parseLong(split[1]);
            long lengthMs = Helpers.parseLong(split[2]);
            float speed = Helpers.parseFloat(split[3]);

            Video video = Video.fromString(videoId);

            // backward compatibility
            if (video == null) {
                video = new Video();
                video.videoId = videoId;
            }

            video.percentWatched = (positionMs * 100f) / lengthMs;

            return new State(video, positionMs, lengthMs, speed);
        }

        @NonNull
        @Override
        public String toString() {
            return Helpers.merge(DELIM, video, positionMs, durationMs, speed);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof State) {
                return Helpers.equals(video, ((State) obj).video);
            }

            return false;
        }
    }

    @Override
    public void onProfileChanged() {
        restoreState();
    }

    private void setStateData(String data) {
        if (data != null) {
            String[] split = Helpers.split(DELIM, data);

            for (String spec : split) {
                State state = State.from(spec);

                if (state != null) {
                    mStates.add(state);
                }
            }
        }
    }

    private String getStateData() {
        StringBuilder sb = new StringBuilder();

        for (State state : mStates) {
            // NOTE: Storage optimization!!!
            //if (state.lengthMs <= MUSIC_VIDEO_LENGTH_MS && !mPlayerData.isRememberSpeedEachEnabled()) {
            //    continue;
            //}

            if (sb.length() != 0) {
                sb.append(DELIM);
            }

            sb.append(state);
        }

        return sb.toString();
    }
}
