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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import de.gebatzens.sia.data.Filter;

public class FilterActivity extends AppCompatActivity {

    Toolbar mToolBar;
    FilterListAdapter adapter;
    ListView listView;

    TextView mainFilterCategory;
    TextView mainFilterContent;
    int selectedMode;
    int mainModePosition;
    boolean changed = false;
    FloatingActionButton mAddFilterButton;
    ScrollView sv;

    @Override
    public void onCreate(Bundle bundle) {
        setTheme(GGApp.GG_APP.school.getTheme());
        super.onCreate(bundle);
        setContentView(R.layout.activity_filter);

        if(GGApp.GG_APP.preferences.getBoolean("first_use_filter", true)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(FilterActivity.this);
            builder.setTitle(getApplication().getString(R.string.explanation));
            builder.setMessage(getApplication().getString(R.string.filter_help));
            builder.setPositiveButton(getApplication().getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }

        sv = (ScrollView) findViewById(R.id.scrollView);

        GGApp.GG_APP.preferences.edit().putBoolean("first_use_filter", false).apply();

        final String[] main_filterStrings = new String[] { getApplication().getString(R.string.school_class), getApplication().getString(R.string.teacher)};

        listView = (ListView) findViewById(R.id.filter_list);
        adapter = new FilterListAdapter(this, GGApp.GG_APP.filters);
        listView.setAdapter(adapter);
        listView.setDrawSelectorOnTop(true);
        setListViewHeightBasedOnChildren(listView);

        if(bundle != null)
            changed = bundle.getBoolean("changed", false);

        TextView tv = (TextView) findViewById(R.id.filter_sep_1);
        tv.setTextColor(GGApp.GG_APP.school.getAccentColor());
        TextView tv2 = (TextView) findViewById(R.id.filter_sep_2);
        tv2.setTextColor(GGApp.GG_APP.school.getAccentColor());

        Filter.FilterList list = GGApp.GG_APP.filters;
        mainFilterCategory = (TextView) findViewById(R.id.filter_main_category);
        mainFilterCategory.setText(list.mainFilter.type == Filter.FilterType.CLASS ? getApplication().getString(R.string.school_class) : getApplication().getString(R.string.teacher));
        mainFilterContent = (TextView) findViewById(R.id.filter_main_content);
        mainFilterContent.setText(list.mainFilter.filter);

        LinearLayout l_mode = (LinearLayout) findViewById(R.id.mainfilter_mode_layout);
        l_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                Filter.FilterList list = GGApp.GG_APP.filters;
                selectedMode = list.mainFilter.type == Filter.FilterType.CLASS ? 0 : list.mainFilter.type == Filter.FilterType.TEACHER ? 1 : 2;
                AlertDialog.Builder builder = new AlertDialog.Builder(FilterActivity.this);
                builder.setTitle(getApplication().getString(R.string.set_main_filter_mode))
                        .setSingleChoiceItems(main_filterStrings, selectedMode, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                changed = true;
                                mainModePosition = which == 0 ? 0 : 1;
                                Filter.FilterList list = GGApp.GG_APP.filters;
                                list.mainFilter.type = Filter.FilterType.values()[mainModePosition];
                                mainFilterCategory.setText(list.mainFilter.type == Filter.FilterType.CLASS ? getApplication().getString(R.string.school_class) : getApplication().getString(R.string.teacher));
                                FilterActivity.saveFilter(GGApp.GG_APP.filters);
                                dialog.dismiss();
                            }
                        });
                builder.setNegativeButton(getApplication().getString(R.string.abort), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog d = builder.create();
                d.show();
            }
        });

        LinearLayout l_content = (LinearLayout) findViewById(R.id.mainfilter_content_layout);
        l_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                AlertDialog.Builder builder = new AlertDialog.Builder(FilterActivity.this);
                builder.setTitle(getApplication().getString(R.string.set_main_filter));
                builder.setView(View.inflate(FilterActivity.this, R.layout.filter_dialog, null));
                builder.setPositiveButton(getApplication().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        changed = true;
                        EditText ed = (EditText) ((Dialog) dialog).findViewById(R.id.filter_text);
                        String filtertext = ed.getText().toString().trim();
                        if (filtertext.isEmpty())
                            Snackbar.make(getWindow().getDecorView().findViewById(R.id.coordinator_layout), getString(R.string.invalid_filter), Snackbar.LENGTH_LONG).show();
                        else {
                            Filter.FilterList list = GGApp.GG_APP.filters;
                            list.mainFilter.filter = filtertext;
                            mainFilterContent.setText(list.mainFilter.filter);
                            FilterActivity.saveFilter(GGApp.GG_APP.filters);
                        }
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(getApplication().getString(R.string.abort), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog d = builder.create();
                d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                d.show();
                Filter.FilterList list = GGApp.GG_APP.filters;
                EditText ed = (EditText) d.findViewById(R.id.filter_text);
                ed.setHint(list.mainFilter.type == Filter.FilterType.CLASS ? getApplication().getString(R.string.school_class_name) : getApplication().getString(R.string.teacher_shortcut));
                ed.setText(list.mainFilter.filter);
                ed.setSelectAllOnFocus(true);

                d.findViewById(R.id.checkbox_contains).setVisibility(View.GONE);
            }
        });

        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        mToolBar.setTitleTextColor(Color.WHITE);
        mToolBar.setBackgroundColor(GGApp.GG_APP.school.getColor());
        mToolBar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mToolBar.inflateMenu(R.menu.filter_menu);
        mToolBar.setTitle(getTitle());

        mToolBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_help) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(FilterActivity.this);
                    builder.setTitle(getApplication().getString(R.string.help));
                    builder.setMessage(getApplication().getString(R.string.filter_help));
                    builder.setPositiveButton(getApplication().getString(R.string.close), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                }
                return false;
            }
        });

        mAddFilterButton = (FloatingActionButton) findViewById(R.id.addfilter_button);
        mAddFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                AlertDialog.Builder builder = new AlertDialog.Builder(FilterActivity.this);
                builder.setTitle(getApplication().getString(R.string.hide_subject));
                builder.setView(View.inflate(FilterActivity.this, R.layout.filter_dialog, null));
                builder.setPositiveButton(getApplication().getString(R.string.hide), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        changed = true;
                        EditText ed = (EditText) ((Dialog) dialog).findViewById(R.id.filter_text);
                        CheckBox cb = (CheckBox) ((Dialog) dialog).findViewById(R.id.checkbox_contains);
                        Filter f = new Filter();
                        f.type = Filter.FilterType.SUBJECT;
                        f.filter = ed.getText().toString().trim();
                        f.contains = cb.isChecked();
                        if (f.filter.isEmpty())
                            Snackbar.make(getWindow().getDecorView().findViewById(R.id.coordinator_layout), getString(R.string.invalid_filter), Snackbar.LENGTH_LONG).show();
                        else {
                            GGApp.GG_APP.filters.add(f);
                            adapter.notifyDataSetChanged();
                            FilterActivity.saveFilter(GGApp.GG_APP.filters);
                            setListViewHeightBasedOnChildren(listView);
                        }
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton(getApplication().getString(R.string.abort), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog d = builder.create();
                d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                d.show();
                final EditText ed = (EditText) d.findViewById(R.id.filter_text);
                ed.setHint(getApplication().getString(R.string.subject_course_name));

                final CheckBox cb = (CheckBox) d.findViewById(R.id.checkbox_contains);
                cb.setEnabled(false);
                cb.setText(getString(R.string.all_subjects_including_disabled));

                ed.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String str = s.toString().trim();
                        if(str.length() == 0) {
                            cb.setEnabled(false);
                            cb.setText(getString(R.string.all_subjects_including_disabled));
                        } else {
                            cb.setEnabled(true);
                            cb.setText(getString(R.string.all_subjects_including, str));
                        }
                    }
                });
            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public static Filter.FilterList loadFilter() {
        Filter.FilterList list = new Filter.FilterList();
        list.mainFilter = null;

        try {
            InputStream in = GGApp.GG_APP.openFileInput("ggfilter");
            JsonReader reader = new JsonReader(new InputStreamReader(in));
            reader.beginArray();
            while(reader.hasNext()) {
                reader.beginObject();
                Filter f = new Filter();
                if(list.mainFilter == null)
                    list.mainFilter = f;
                else
                    list.add(f);
                while(reader.hasNext()) {
                    String name = reader.nextName();
                    if(name.equals("type"))
                        f.type = Filter.FilterType.valueOf(reader.nextString());
                    else if(name.equals("filter"))
                        f.filter = reader.nextString();
                    else if(name.equals("contains"))
                        f.contains = reader.nextBoolean();
                    else
                        reader.skipValue();

                }
                reader.endObject();
            }
            reader.endArray();
            reader.close();
        } catch(Exception e) {
            list.clear();
        }

        if(list.mainFilter == null) {
            Filter f = new Filter();
            list.mainFilter = f;
            f.type = Filter.FilterType.CLASS;
            f.filter = "";
        }

        return list;
    }

    @Override
    public void finish() {
        setResult(changed ? RESULT_OK : RESULT_CANCELED);
        saveFilter(GGApp.GG_APP.filters);
        super.finish();
    }

    public static void saveFilter(Filter.FilterList list) {
        try {
            OutputStream out = GGApp.GG_APP.openFileOutput("ggfilter", Context.MODE_PRIVATE);
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(out));
            writer.setIndent("  ");
            writer.beginArray();
            writer.beginObject();
            writer.name("type").value(list.mainFilter.type.toString());
            writer.name("filter").value(list.mainFilter.filter);
            writer.endObject();
            for(Filter f : list) {
                writer.beginObject();
                writer.name("type").value(f.type.toString());
                writer.name("filter").value(f.filter);
                writer.name("contains").value(f.contains);
                writer.endObject();
            }
            writer.endArray();
            writer.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    protected void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
        b.putBoolean("changed", changed);
        b.putIntArray("ARTICLE_SCROLL_POSITION", new int[]{sv.getScrollX(), sv.getScrollY()});
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        final int[] position = savedInstanceState.getIntArray("ARTICLE_SCROLL_POSITION");
        if(position != null)
            sv.post(new Runnable() {
                public void run() {
                    sv.scrollTo(position[0], position[1]);
                }
            });
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}
