package org.quuux.headspace.ui;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.text.TextUtils;

import org.quuux.headspace.MainActivity;
import org.quuux.headspace.PlaybackService;
import org.quuux.headspace.R;
import org.quuux.headspace.data.Station;
import org.quuux.headspace.data.StreamMetaData;
import org.quuux.headspace.net.Streamer;

public class PlaybackNotification {

    public static Notification getInstance(final Context context, final Bitmap bitmap, final MediaSession mediaSession) {
        final Notification.Builder builder = new Notification.Builder(context);

        final Streamer stream = Streamer.getInstance();
        final Station station = stream.getStation();
        final StreamMetaData metadata = stream.getLastMetaData();

        String text = metadata != null ? metadata.getTitle() : null;
        if (TextUtils.isEmpty(text))
            text = station.getDescription();

        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setSmallIcon(R.mipmap.ic_play);
        if (bitmap != null)
            builder.setLargeIcon(bitmap);
        builder.setContentTitle(station.getName());
        builder.setContentText(text);

        final int playbackIcon = stream.isPlaying() ? R.mipmap.ic_pause : R.mipmap.ic_play;
        final String playbackText = context.getString(stream.isPlaying() ? R.string.action_pause : R.string.action_play);
        final Intent playbackIntent = new Intent(context, PlaybackService.class);
        playbackIntent.setAction(PlaybackService.ACTION_TOGGLE_PLAYBACK);

        final Intent stopIntent = new Intent(context, PlaybackService.class);
        stopIntent.setAction(PlaybackService.ACTION_STOP_PLAYBACK);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            final PlaybackState.Builder stateBuilder = new PlaybackState.Builder()
                    .setState(PlaybackState.STATE_PLAYING, 0, 1)
                    .setActions((stream.isPlaying() ? PlaybackState.ACTION_PAUSE : PlaybackState.ACTION_PLAY) | PlaybackState.ACTION_STOP);

            mediaSession.setPlaybackState(stateBuilder.build());
            final MediaMetadata mediaMetadata = new MediaMetadata.Builder()

                    .putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, text)
                    .putString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE, station.getName())

                    .putString(MediaMetadata.METADATA_KEY_TITLE, text)
                    .putString(MediaMetadata.METADATA_KEY_ARTIST, station.getName())

                    .putBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON, bitmap)
                    .putBitmap(MediaMetadata.METADATA_KEY_ART, bitmap)
                    .build();
            mediaSession.setMetadata(mediaMetadata);

            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
            final Notification.Action play = new Notification.Action.Builder(playbackIcon, playbackText, PendingIntent.getService(context, 0, playbackIntent, 0)).build();
            builder.addAction(play);

            final Notification.Action stop = new Notification.Action.Builder(R.mipmap.ic_stop, context.getString(R.string.stop), PendingIntent.getService(context, 0, stopIntent, 0)).build();
            builder.addAction(stop);

            builder.setStyle(new Notification.MediaStyle().setShowActionsInCompactView(0, 1).setMediaSession(mediaSession.getSessionToken()));

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            builder.addAction(playbackIcon, playbackText, PendingIntent.getService(context, 0, playbackIntent, 0));
            builder.addAction(R.mipmap.ic_stop, context.getString(R.string.stop), PendingIntent.getService(context, 0, stopIntent, 0));
        }

        final Intent intent = new Intent(context, MainActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, intent, 0));

        return builder.getNotification();
    }
}
