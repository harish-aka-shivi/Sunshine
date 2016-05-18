package app.com.example.android.sunshine;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * Created by root on 18/5/16.
 */
public class SunshineWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        int N = appWidgetIds.length;

        for (int i = 0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

            Intent intent = new Intent(context,MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,0);

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.widget_sunshine);
            remoteViews.setOnClickPendingIntent(R.id.button_widget,pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId,remoteViews);
        }


        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
