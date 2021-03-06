package weatherwear.weatherwear.alarm;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;

/**
 * Created by emilylin27 on 2/28/16.
 * Allows us to stop and start alarm sounds
 */
public class AlarmAlertManager {
    private static MediaPlayer mMp;
    private static Vibrator mVibrator;
    private static boolean mIsPlaying;

    // Empty constructor, allows for access to static mMp and mVibrator without creating new ones
    public AlarmAlertManager() {}

    // Create media player and vibrator
    public AlarmAlertManager(Context context) {
        Uri alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        mMp = MediaPlayer.create(context.getApplicationContext(), alarm);
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    // Start sounds and vibration
    public void startAlerts() {
        mVibrator.vibrate(new long[]{500, 500}, 0);
        mMp.start();
        mIsPlaying = true;
    }

    // Stop sounds and vibrations
    public void stopAlerts() {
        try { // Because it crashed once on Emma's phone for no reasons
            mIsPlaying = false;
            mMp.stop();
            mVibrator.cancel();
            mMp.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Returns whether or not sounds are playing
    public Boolean isPlaying(){
        return mIsPlaying;
    }
}
