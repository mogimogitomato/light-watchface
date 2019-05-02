package io.github.mogimogitomato.light;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

//import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

//import java.util.concurrent.TimeUnit;

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 * <p>
 * Important Note: Because watch face apps do not have a default Activity in
 * their project, you will need to set your Configurations to
 * "Do not launch Activity" for both the Wear and/or Application modules. If you
 * are unsure how to do this, please review the "Run Starter project" section
 * in the Google Watch Face Code Lab:
 * https://codelabs.developers.google.com/codelabs/watchface/index.html#0
 */
public class LightWatchFace extends CanvasWatchFaceService {

    /**
     * Update rate in milliseconds for interactive mode. Defaults to one second
     * because the watch face needs to update seconds in interactive mode.
     */
//    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
//    private static final int MSG_UPDATE_TIME = 0;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

//    private static class EngineHandler extends Handler {
//        private final WeakReference<LightWatchFace.Engine> mWeakReference;
//
//        public EngineHandler(LightWatchFace.Engine reference) {
//            mWeakReference = new WeakReference<>(reference);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            LightWatchFace.Engine engine = mWeakReference.get();
//            if (engine != null) {
//                switch (msg.what) {
//                    case MSG_UPDATE_TIME:
//                        engine.handleUpdateTimeMessage();
//                        break;
//                }
//            }
//        }
//    }

    /**
     * 描画処理クラス
     */
    private class Engine extends CanvasWatchFaceService.Engine {

//        // インタラクティブモード時に秒表示更新を行うためのHandler.
//        private final Handler mUpdateTimeHandler = new EngineHandler(this);
        // 時間を取得するためのCalendar.
        private Calendar mCalendar;
        // Timezone変更通知を受けるReceiver.
        // 通知を受けた際CalendarにTimezoneを設定し際描画する.
        private final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };
        // TimeZoneReceiverのレシーバ登録有無.
        private boolean mIsRegisteredTimeZoneReceiver = false;
        /* 画面描画関連 */
        private View mView;
        private TextView mDateText, mTimeText;
        private final Point mDisplaySize = new Point();
        private int mSpecWidth, mSpecHeight;
        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        private boolean mLowBitAmbient;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(LightWatchFace.this)
                    .setAcceptsTapEvents(true)
                    .build());

            mCalendar = Calendar.getInstance();
            mView = View.inflate(getBaseContext(), R.layout.watch_layout, null);
            mDateText = mView.findViewById(R.id.date);
            mTimeText = mView.findViewById(R.id.time);
            Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay();
            display.getSize(mDisplaySize);
            mSpecWidth = View.MeasureSpec.makeMeasureSpec(mDisplaySize.x,
                    View.MeasureSpec.EXACTLY);
            mSpecHeight = View.MeasureSpec.makeMeasureSpec(mDisplaySize.y,
                    View.MeasureSpec.EXACTLY);
            mDateText.getPaint().setAntiAlias(true);
            mDateText.getPaint().setAntiAlias(true);

        }

        @Override
        public void onDestroy() {
//            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
//            updateTimer();
        }

        private void registerReceiver() {
            if (mIsRegisteredTimeZoneReceiver) {
                return;
            }
            mIsRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            LightWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mIsRegisteredTimeZoneReceiver) {
                return;
            }
            mIsRegisteredTimeZoneReceiver = false;
            LightWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);

            if (mLowBitAmbient) {
                mDateText.getPaint().setAntiAlias(!inAmbientMode);
                mTimeText.getPaint().setAntiAlias(!inAmbientMode);
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
//            updateTimer();
        }

        /**
         * Captures tap event (and tap type) and toggles the background color if the user finishes
         * a tap.
         */
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            switch (tapType) {
                case TAP_TYPE_TOUCH:
                    // The user has started touching the screen.
                    break;
                case TAP_TYPE_TOUCH_CANCEL:
                    // The user has started a different gesture or otherwise cancelled the tap.
                    break;
                case TAP_TYPE_TAP:
                    // The user has completed the tap gesture.
                    break;
                default:
                    break;
            }
            invalidate();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {

            // Draw HH:MM in ambient mode or HH:MM:SS in interactive mode.
            long now = System.currentTimeMillis();
            mCalendar.setTimeInMillis(now);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN);
            String dateText = sdf.format(mCalendar.getTime()) + String.format("(%s)", getDayString());
            String timeText = String.format("%02d:%02d", mCalendar.get(Calendar.HOUR_OF_DAY),
                    mCalendar.get(Calendar.MINUTE));
            mDateText.setText(dateText);
            mTimeText.setText(timeText);
            mView.measure(mSpecWidth, mSpecHeight);
            mView.layout(0, 0, mView.getMeasuredWidth(), mView.getMeasuredHeight());
            mView.draw(canvas);
        }

        /**
         * Return day name.
         * @return day name
         */
        private String getDayString() {
            String result = "";
            switch (mCalendar.get(Calendar.DAY_OF_WEEK)) {
                case Calendar.MONDAY:
                    result = "Mon";
                    break;
                case Calendar.TUESDAY:
                    result = "Tue";
                    break;
                case Calendar.WEDNESDAY:
                    result = "Wed";
                    break;
                case Calendar.THURSDAY:
                    result = "Thu";
                    break;
                case Calendar.FRIDAY:
                    result = "Fri";
                    break;
                case Calendar.SATURDAY:
                    result = "Sat";
                    break;
                case Calendar.SUNDAY:
                    result = "Sun";
                    break;
            }
            return result;
        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
//        private void updateTimer() {
//            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
//            if (shouldTimerBeRunning()) {
//                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
//            }
//        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
//        private boolean shouldTimerBeRunning() {
//            return isVisible() && !isInAmbientMode();
//        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
//        private void handleUpdateTimeMessage() {
//            invalidate();
//            if (shouldTimerBeRunning()) {
//                long timeMs = System.currentTimeMillis();
//                long delayMs = INTERACTIVE_UPDATE_RATE_MS
//                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
//                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
//            }
//        }
    }
}
