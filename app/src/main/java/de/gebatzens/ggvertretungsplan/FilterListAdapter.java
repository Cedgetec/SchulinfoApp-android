/*
 * Copyright (C) 2015 Hauke Oldsen
 *
 * This file is part of GGVertretungsplan.
 *
 * GGVertretungsplan is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GGVertretungsplan is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GGVertretungsplan.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.gebatzens.ggvertretungsplan;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class FilterListAdapter extends BaseAdapter {

    FilterActivity.FilterList list;
    FilterActivity c;

    public FilterListAdapter(FilterActivity c, FilterActivity.FilterList filters) {
        this.c = c;
        list = filters;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        FilterActivity.Filter filter = list.get(position);
        ViewGroup vg = (ViewGroup) ((LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.filter_item, parent, false);
        ((TextView) vg.findViewById(R.id.filter_main_text)).setText(filter.toString());
        ImageButton edit = (ImageButton) vg.findViewById(R.id.filter_edit);
        edit.setTag(position);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(c);
                builder.setTitle(c.getString(R.string.edit_filter));
                builder.setView(c.getLayoutInflater().inflate(R.layout.filter_dialog, null));
                builder.setPositiveButton(c.getString(R.string.add), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Spinner spinner = (Spinner) ((Dialog) dialog).findViewById(R.id.filter_spinner);
                        EditText text = (EditText) ((Dialog) dialog).findViewById(R.id.filter_text);
                        FilterActivity.Filter f = new FilterActivity.Filter();
                        f.type = FilterActivity.Filter.getTypeFromString((String) spinner.getSelectedItem());
                        f.filter = text.getText().toString().trim();
                        if (f.filter.isEmpty())
                            Toast.makeText(((Dialog) dialog).getContext(), c.getString(R.string.invalid_filter), Toast.LENGTH_SHORT).show();
                        else {
                            GGApp.GG_APP.filters.add(f);
                            notifyDataSetChanged();
                        }
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(c.getString(R.string.abort), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog d = builder.create();
                d.show();
                Spinner s = (Spinner) d.findViewById(R.id.filter_spinner);
                ArrayAdapter<String> a = new ArrayAdapter<String>(c,
                        android.R.layout.simple_spinner_item, c.filterStrings);
                a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                s.setAdapter(a);
                FilterActivity.FilterType type = list.get((int) v.getTag()).type;
                s.setSelection(type == FilterActivity.FilterType.CLASS ? 0 : type == FilterActivity.FilterType.TEACHER ? 1 : 2);
                EditText ed = (EditText) d.findViewById(R.id.filter_text);
                ed.setText(list.get((int) v.getTag()).filter);
            }
        });
        ((ImageButton)vg.findViewById(R.id.filter_delete)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.remove(getItem(position));
                notifyDataSetChanged();
            }
        });
        return vg;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


}
