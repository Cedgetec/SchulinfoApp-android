/*
 * Copyright 2015 Fabian Schultis
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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FirstUseActivity extends Activity {
    Button nextStep;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        if(BuildConfig.DEBUG) {
            startActivity(new Intent(FirstUseActivity.this, SetupActivity.class));
            return;
        }

        setContentView(R.layout.activity_firstuse);

        nextStep = (Button) findViewById(R.id.nextStep);
        nextStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                //save and switch setup page

                //startActivity(new Intent(FirstUseActivity.this, SetupActivity.class));
            }
        });

    }

}