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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import de.gebatzens.ggvertretungsplan.data.Filter;

public class FilterActivity extends Activity {

    Toolbar mToolBar;
    FilterListAdapter adapter;
    ListView listView;

    TextView mainFilterCategory;
    TextView mainFilterContent;
    ImageButton mAddFilterButton;
    int selected_mode;
    int main_mode_position;

    @Override
    public void onCreate(Bundle bundle) {
        setTheme(GGApp.GG_APP.school.getTheme());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            GGApp.GG_APP.setStatusBarColor(getWindow(), GGApp.GG_APP.school.getDarkColor());
        }
        super.onCreate(bundle);
        setContentView(R.layout.activity_filter);

        final String[] main_filterStrings = new String[] { getApplication().getString(R.string.school_class), getApplication().getString(R.string.teacher)};

        listView = (ListView) findViewById(R.id.filter_list);
        adapter = new FilterListAdapter(this, GGApp.GG_APP.filters);
        listView.setAdapter(adapter);
        listView.setDrawSelectorOnTop(true);
        setListViewHeightBasedOnChildren(listView);

        TextView tv = (TextView) findViewById(R.id.filter_sep_1);
        tv.setTextColor(GGApp.GG_APP.school.getColor());
        TextView tv2 = (TextView) findViewById(R.id.filter_sep_2);
        tv2.setTextColor(GGApp.GG_APP.school.getColor());

        Filter.FilterList list = GGApp.GG_APP.filters;
        mainFilterCategory = (TextView) findViewById(R.id.filter_main_category);
        mainFilterCategory.setText(list.mainFilter.filter.isEmpty() ? getString(R.string.not_selected) : list.mainFilter.type == Filter.FilterType.CLASS ? getApplication().getString(R.string.school_class) : getApplication().getString(R.string.teacher));
        mainFilterContent = (TextView) findViewById(R.id.filter_main_content);
        mainFilterContent.setText(list.mainFilter.filter);

        LinearLayout l_mode = (LinearLayout) findViewById(R.id.mainfilter_mode_layout);
        l_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                Filter.FilterList list = GGApp.GG_APP.filters;
                selected_mode = list.mainFilter.type == Filter.FilterType.CLASS ? 0 : list.mainFilter.type == Filter.FilterType.TEACHER ? 1 : 2;
                AlertDialog.Builder builder = new AlertDialog.Builder(FilterActivity.this);
                builder.setTitle(getApplication().getString(R.string.set_main_filter_mode))
                        .setSingleChoiceItems(main_filterStrings, selected_mode, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                main_mode_position = which == 0 ? 0 : 1;
                                Filter.FilterList list = GGApp.GG_APP.filters;
                                list.mainFilter.type = Filter.FilterType.values()[main_mode_position];
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
                builder.setView(getLayoutInflater().inflate(R.layout.filter_dialog, null));
                builder.setPositiveButton(getApplication().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText text = (EditText) ((Dialog) dialog).findViewById(R.id.filter_text);
                        text.setHint(getApplication().getString(R.string.school_class));
                        String text2 = text.getText().toString().trim();
                        if (text2.isEmpty())
                            Toast.makeText(((Dialog) dialog).getContext(), getApplication().getString(R.string.invalid_filter), Toast.LENGTH_SHORT).show();
                        else {
                            Filter.FilterList list = GGApp.GG_APP.filters;
                            list.mainFilter.filter = text2;
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
                EditText mainEdit = (EditText) d.findViewById(R.id.filter_text);
                mainEdit.setHint(list.mainFilter.type == Filter.FilterType.CLASS ? getApplication().getString(R.string.school_class) : getApplication().getString(R.string.teacher_shortcut));
                mainEdit.setText(list.mainFilter.filter);
            }
        });

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
        mToolBar.inflateMenu(R.menu.filter_menu);
        mToolBar.setTitle(getTitle());

        mToolBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getItemId() == R.id.action_help) {
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

        mAddFilterButton = (ImageButton) findViewById(R.id.addfilter_button);
        mAddFilterButton.setBackgroundResource(R.drawable.floating_action_circle);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            StateListDrawable drawable = (StateListDrawable) mAddFilterButton.getBackground();
            drawable.setColorFilter(GGApp.GG_APP.school.getColor(), PorterDuff.Mode.SRC_ATOP);
        }
        mAddFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                AlertDialog.Builder builder = new AlertDialog.Builder(FilterActivity.this);
                builder.setTitle(getApplication().getString(R.string.add_filter));
                builder.setView(getLayoutInflater().inflate(R.layout.filter_dialog, null));
                builder.setPositiveButton(getApplication().getString(R.string.add), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText text = (EditText) ((Dialog) dialog).findViewById(R.id.filter_text);
                        text.setHint(getApplication().getString(R.string.subject_course));
                        Filter f = new Filter();
                        f.type = Filter.FilterType.SUBJECT;
                        f.filter = text.getText().toString().trim();
                        if (f.filter.isEmpty())
                            Toast.makeText(((Dialog) dialog).getContext(), getApplication().getString(R.string.invalid_filter), Toast.LENGTH_SHORT).show();
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
                });//
                AlertDialog d = builder.create();
                d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                d.show();
                EditText mainEdit = (EditText) d.findViewById(R.id.filter_text);
                mainEdit.setHint(getApplication().getString(R.string.subject_course));
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
        setResult(RESULT_OK);
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
                writer.endObject();
            }
            writer.endArray();
            writer.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
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
