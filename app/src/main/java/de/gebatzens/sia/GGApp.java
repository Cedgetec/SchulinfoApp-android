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

package de.gebatzens.sia;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.util.HashMap;

import de.gebatzens.sia.data.Exams;
import de.gebatzens.sia.data.Filter;
import de.gebatzens.sia.data.GGPlan;
import de.gebatzens.sia.data.Mensa;
import de.gebatzens.sia.data.News;
import de.gebatzens.sia.fragment.RemoteDataFragment;

public class GGApp extends Application {

    public static final int UPDATE_DISABLE = 0, UPDATE_WLAN = 1, UPDATE_ALL = 2;
    public static GGApp GG_APP;

    public GGPlan.GGPlans plans;
    public News news;
    public Mensa mensa;
    public Exams exams;

    public MainActivity activity;
    public GGRemote remote;
    public School school;

    public SharedPreferences preferences;

    public Filter.FilterList filters = new Filter.FilterList();
    public HashMap<String, String> subjects = new HashMap<String, String>();

    @Override
    public void onCreate() {
        super.onCreate();
        GG_APP = this;
        remote = new GGRemote();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        GGBroadcast.createAlarm(this);
        filters = FilterActivity.loadFilter();
        loadSubjectMap();
        School.loadList();
        loadSavedData();
        school = School.getBySID(preferences.getString("sid", null));

    }

    private void loadSavedData() {
        if(!(plans = new GGPlan.GGPlans()).load())
            plans = null;
        if(!(exams = new Exams()).load())
            exams = null;
        if(!(news = new News()).load())
            news = null;
        if(!(mensa = new Mensa()).load())
            mensa = null;
    }

    public RemoteDataFragment.RemoteData getDataForFragment(FragmentType type) {
        switch(type) {
            case PLAN:
                return plans;
            case NEWS:
                return news;
            case MENSA:
                return mensa;
            case EXAMS:
                return exams;
            default:
                return null;
        }
    }

    private void loadSubjectMap() {
        subjects.clear();
        String[] array = getResources().getStringArray(R.array.subjects);
        for(String s : array) {
            String[] parts = s.split("\\|");
            String value = parts[parts.length - 1];
            for(int i = 0; i < parts.length - 1; i++) {
                subjects.put(parts[i], value);
            }
        }
    }

    public void createNotification(int icon, String title, String message, Intent intent, int id, String... strings) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(icon)
                        .setContentTitle(title)
                        .setContentText(message);
        if (strings.length > 1) {
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            inboxStyle.setBigContentTitle(strings[0]);

            boolean b = true;
            for (String s : strings) {
                if (!b) {
                    inboxStyle.addLine(s);
                }
                b = false;
            }

            mBuilder.setStyle(inboxStyle);
        }
        mBuilder.setColor(GGApp.GG_APP.school.getDarkColor());
        if (nlightEnabled()) {
            mBuilder.setLights(school.getColor(), 1000, 1000);
        }

        String vibration = preferences.getString("vibration", "off");
        if(vibration.equals("short"))
            mBuilder.setVibrate(new long[] {0, 500});
        else if(vibration.equals("default"))
            mBuilder.setVibrate(new long[] {0, 200, 200, 200, 200, 200});
        else if(vibration.equals("long"))
            mBuilder.setVibrate(new long[] {0, 200, 100, 400, 200, 800, 300, 1000, 1200, 200});

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(id, mBuilder.build());
    }

    public int translateUpdateType(String s) {
        if(s.equals("disable"))
            return UPDATE_DISABLE;
        else if(s.equals("wifi"))
            return UPDATE_WLAN;
        else if(s.equals("all"))
            return UPDATE_ALL;
        return UPDATE_DISABLE;
    }

    public int getUpdateType() {
        if(preferences.getBoolean("notifications", true))
            return translateUpdateType(preferences.getString("appupdates", "all"));
        else
            return UPDATE_DISABLE;
    }

    public boolean nlightEnabled() {
        return preferences.getBoolean("notification_led", true);
    }

    public FragmentType getFragmentType() {
        return FragmentType.valueOf(preferences.getString("fragtype", "PLAN"));
    }

    public void setFragmentType(FragmentType type) {
        preferences.edit().putString("fragtype", type.toString()).apply();
    }

    public void setDarkThemeEnabled(boolean e) {
        preferences.edit().putBoolean("darkTheme", e).apply();
    }

    public String getCustomThemeName() {
        return preferences.getString("customTheme", null);
    }

    public void setCustomThemeName(String customTheme) {
        preferences.edit().putString("customTheme", customTheme).apply();
    }

    public boolean isDarkThemeEnabled() {
        return preferences.getBoolean("darkTheme", false);
    }

    public void setSchool(String sid) {
        preferences.edit().putString("sid", sid).apply();
        if(sid == null)
            return;

        School b = school;
        school = School.getBySID(sid);
        if(activity != null && b != null && !b.equals(school)) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.recreate();
                }
            });

        }

    }

    public String getDefaultSID() {
        return preferences.getString("sid", null);
    }

    public boolean appUpdatesEnabled() {
        return preferences.getBoolean("autoappupdates", true);
    }

    public void refreshAsync(final Runnable finished, final boolean updateFragments, final FragmentType type) {
        new Thread() {
            @Override
            public void run() {
                boolean update = false;
                switch(type) {
                    case PLAN:
                        GGPlan.GGPlans oldPlans = plans;
                        plans = remote.getPlans(updateFragments);

                        boolean recreate = false;

                        if(activity != null && (oldPlans == null || plans.size() != oldPlans.size())) {
                            recreate = true;
                        } else if (updateFragments) {
                            for (int i = 0; i < plans.size() && !recreate; i++) {
                                if (!plans.get(i).date.equals(oldPlans.get(i).date) || plans.get(i).entries.size() != oldPlans.get(i).entries.size())
                                    recreate = true;
                            }

                        }

                        if(activity != null && recreate) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    activity.removeAllFragments();
                                    FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
                                    activity.mContent = activity.createFragment();
                                    transaction.replace(R.id.content_fragment, activity.mContent, "gg_content_fragment");
                                    transaction.commit();
                                }
                            });
                            Log.d("ggvp", "RECRATE FRAGMENT");
                        }

                        break;
                    case NEWS:
                        News on = news;
                        news = remote.getNews(updateFragments);
                        update = on == null || !on.equals(news);
                        break;
                    case MENSA:
                        Mensa om = mensa;
                        mensa = remote.getMensa(updateFragments);
                        update = om == null || !om.equals(mensa);
                        break;
                    case EXAMS:
                        Exams oe = exams;
                        exams = remote.getExams(updateFragments);
                        update = oe == null || !oe.equals(exams);
                        break;
                }

                if(activity != null && updateFragments && update)
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            activity.mContent.updateFragment();
                        }
                    });

                if(activity != null && finished != null)
                    activity.runOnUiThread(finished);
            }
        }.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setStatusBarColorTransparent(Window w) {
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        w.setStatusBarColor(ContextCompat.getColor(this, R.color.transparent));
    }

    public enum FragmentType {
        PLAN, NEWS, MENSA, EXAMS
    }


}