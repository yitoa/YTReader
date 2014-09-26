package org.coolreader.crengine;

import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.yt.reader.R;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: Victor Soskin
 * Date: 11/3/11
 * Time: 2:51 PM
 */
public class ToastView {
    private static class Toast {
        private View anchor;
        private String msg;
        private int duration;

        private Toast(View anchor, String msg, int duration) {
            this.anchor = anchor;
            this.msg = msg;
            this.duration = duration;
        }
    }


    private static LinkedBlockingQueue<Toast> queue = new LinkedBlockingQueue<Toast>();
    private static AtomicBoolean showing = new AtomicBoolean(false);
    private static Handler mHandler = new Handler();
    private static PopupWindow window = null;

    private static Runnable handleDismiss = new Runnable() {
        @Override
        public void run() {
            if (window != null) {
                window.dismiss();
                show();
            }
        }
    };

    private static void show() {
        if (queue.size() == 0) {
            showing.compareAndSet(true, false);
            return;
        }
        Toast t = queue.poll();
        window = new PopupWindow(t.anchor.getContext());
        window.setWidth(WindowManager.LayoutParams.FILL_PARENT);
        window.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        window.setTouchable(false);
        window.setFocusable(false);
        window.setOutsideTouchable(true);
        window.setBackgroundDrawable(null);
        
        LayoutInflater inflater = (LayoutInflater) t.anchor.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        /*window.setContentView(inflater.inflate(R.layout.custom_toast, null, true));
        TextView tv = (TextView) window.getContentView().findViewById(R.id.toast);
        tv.setTextSize(Integer.valueOf( mReaderView.getSetting(ReaderView.PROP_FONT_SIZE) ) );
        tv.setText(t.msg);*/
        window.showAtLocation(t.anchor, Gravity.NO_GRAVITY, 0, 0);
        mHandler.postDelayed(handleDismiss, t.duration == 0 ? 2000 : 3000);
    }
}
