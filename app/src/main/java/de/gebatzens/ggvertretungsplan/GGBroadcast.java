/*
 * Copyright 2015 Hauke Oldsen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gebatzens.ggvertretungsplan;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

import de.gebatzens.ggvertretungsplan.data.GGPlan;
import de.gebatzens.ggvertretungsplan.fragment.SubstFragment;

public class GGBroadcast extends BroadcastReceiver {

    public void checkForUpdates(final GGApp gg, boolean notification) {
        if(!gg.notificationsEnabled() && notification)
            return;
        if(gg.getUpdateType() == GGApp.UPDATE_DISABLE) {
            Log.w("ggvp", "update disabled");
            return;
        }
        boolean w = isWlanConnected(gg);
        if(!w && gg.getUpdateType() == GGApp.UPDATE_WLAN ) {
            Log.w("ggvp", "wlan not conected");
            return;
        }
        GGRemote r = GGApp.GG_APP.remote;

        GGPlan.GGPlans newPlans = r.getPlans(false);
        GGPlan.GGPlans oldPlans = gg.plans;

        if(newPlans.throwable != null || oldPlans == null || oldPlans.throwable != null)
            return;

        List<GGPlan.Entry> newList = null;
        for(int i = 0; i < newPlans.size() && newList == null; i++) {
            GGPlan old = oldPlans.getPlanByDate(newPlans.get(i).date);
            if(old != null) {
                //if(newList == null)
                    newList = newPlans.get(i).filter(gg.filters);
                //else
                 //   newList.addAll(newPlans.get(i).filter(gg.filters));
                List<GGPlan.Entry> oldList = old.filter(gg.filters);
                if(!oldList.equals(newList))
                    newList.removeAll(oldList);
                else
                    newList = null;
            } else
                newList = null;

        }

        gg.plans = newPlans;

        //This will happen very rarely and can probably be ignored since it causes some problems
        /*if(gg.activity != null && gg.getFragmentType() == GGApp.FragmentType.PLAN)
            gg.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    gg.activity.mContent.updateFragment();
                }
            });*/

        if(newList != null) {
            Intent intent = new Intent(gg, MainActivity.class);
            intent.putExtra("fragment", "PLAN");
            if (newList.size() == 1) {
                GGPlan.Entry entry = newList.get(0);
                gg.createNotification(R.drawable.ic_gg_notification, entry.lesson + ". " + gg.getString(R.string.lhour) + ": " + entry.type, entry.subject.replace("&#x2192;", ""),
                        intent, 123, true/*, gg.getString(R.string.affected_lessons) , today.getWeekday() + ": " + stdt,
                        tomo.getWeekday() + ": " + stdtm*/);

            } else {
                gg.createNotification(R.drawable.ic_gg_notification, gg.getString(R.string.schedule_change), newList.size() + " " + gg.getString(R.string.new_entries),
                        intent, 123, true/*, gg.getString(R.string.affected_lessons) , today.getWeekday() + ": " + stdt,
                        tomo.getWeekday() + ": " + stdtm*/);
            }
        }

    }


    public static void createAlarm(Context context) {
        Intent i = new Intent(context, GGBroadcast.class);
        i.setAction("de.gebatzens.ACTION_ALARM");
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 60000, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pi);
    }

    public static boolean isWlanConnected(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w("ggvp", "onReceive " + intent.getAction());
        Intent intent1 = new Intent(context, MQTTService.class);
        context.startService(intent1);
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent i = new Intent(context, GGBroadcast.class);
            i.setAction("de.gebatzens.ACTION_ALARM");
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);

            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, AlarmManager.INTERVAL_HALF_HOUR, pi);

        } else if (intent.getAction().equals("de.gebatzens.ACTION_ALARM")) {
            new AsyncTask<GGApp, Void, Void>() {

                @Override
                protected Void doInBackground(GGApp... params) {
                    checkForUpdates(params[0], true);
                    return null;
                }
            }.execute((GGApp) context.getApplicationContext());

        } else if (intent.getAction().equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
                new AsyncTask<GGApp, Void, Void>() {

                    @Override
                    protected Void doInBackground(final GGApp... params) {
                        int s = 0;
                        while(!isWlanConnected(params[0])) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {

                            }
                            s++;
                            if(s > 100)
                                return null;
                        }
                        if(params[0].activity != null && params[0].getFragmentType() == GGApp.FragmentType.PLAN) {
                            params[0].activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((SubstFragment)params[0].activity.mContent).substAdapter.setFragmentsLoading();
                                }
                            });

                            params[0].refreshAsync(null, true, params[0].getFragmentType());
                        } else {
                            checkForUpdates(params[0], false);
                        }
                        return null;
                    }
                }.execute((GGApp) context.getApplicationContext());

        }
    }

}
