/*
 * Copyright 2015 Lasse Rosenow
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

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;

public class PersonalisationActivity extends Activity {

    Toolbar mToolBar;
    ImageView imgColorCircle;

    @Override
    public void onCreate(Bundle bundle) {
        setTheme(GGApp.GG_APP.school.getTheme());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            GGApp.GG_APP.setStatusBarColor(getWindow(), GGApp.GG_APP.school.getDarkColor());
        }
        super.onCreate(bundle);
        setContentView(R.layout.activity_personalisation);

        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        mToolBar.setBackgroundColor(GGApp.GG_APP.school.getColor());
        mToolBar.setTitleTextColor(Color.WHITE);
        mToolBar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mToolBar.setTitle(getTitle());

        LinearLayout l_toggleDarkTheme = (LinearLayout) findViewById(R.id.personalisation_toggleDarkTheme);
        l_toggleDarkTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                SwitchCompat s_darkThemeSwitchButton = (SwitchCompat) findViewById(R.id.personalisation_darkThemeSwitchButton);
                if (s_darkThemeSwitchButton.isChecked()) {
                    s_darkThemeSwitchButton.setChecked(false);
                } else {
                    s_darkThemeSwitchButton.setChecked(true);
                }
            }
        });

        imgColorCircle = (ImageView) findViewById(R.id.personalisation_themeColorCircle);
        imgColorCircle.setBackgroundResource(R.drawable.colored_circle);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            StateListDrawable drawable = (StateListDrawable) imgColorCircle.getBackground();
            drawable.setColorFilter(GGApp.GG_APP.school.getColor(), PorterDuff.Mode.SRC_ATOP);
        }

        LinearLayout l_changeColor = (LinearLayout) findViewById(R.id.personalisation_changeColor);
        l_changeColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                //Change theme color
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
