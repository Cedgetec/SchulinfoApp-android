/*
 * Copyright 2015 - 2016 Hauke Oldsen
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

package de.gebatzens.sia.data;

import java.util.ArrayList;

import de.gebatzens.sia.FilterActivity;
import de.gebatzens.sia.SIAApp;
import de.gebatzens.sia.R;

public abstract class Filter {

    private FilterType type = FilterType.CLASS;
    private String filter = "";
    private String filterAN = "";

    /**
     * if true, matches "lat1" for "lat"
     */
    public boolean contains = false;


    public Filter() {

    }

    public Filter(FilterType type, String filter) {
        setType(type);
        setFilter(filter);
    }

    public void setType(FilterType t) {
        type = t;
    }

    public void setFilter(String filter) {
        this.filter = filter;
        if(filter != null && !filter.isEmpty())
            this.filterAN = SIAApp.deleteNonAlphanumeric(filter);
        else
            this.filterAN = filter;
    }

    public FilterType getType() {
        return type;
    }

    public String getFilter() {
        return filter;
    }


    public boolean matches(Filterable filterable) {
        if(filterable instanceof Subst.Entry)
            return matches((Subst.Entry) filterable);
        else if(filterable instanceof Exams.ExamItem)
            return matches((Exams.ExamItem) filterable);
        else
            return false;
    }

    private boolean matches(Subst.Entry e) {
        if(filter.isEmpty())
            return false;

        switch(type) {
            case CLASS:
                return e.getClassAN().equals(filterAN);
            case TEACHER:
                return e.getTeacherAN().equals(filterAN) || e.getCommentAN().endsWith(filterAN);
            case SUBJECT:
                if (contains)
                    return e.getSubjectAN().contains(filterAN);
                else
                    return e.getSubjectAN().equals(filterAN);
            case LESSON:
                return e.getLessonAN().equals(filterAN);
        }
        return false;
    }

    private boolean matches(Exams.ExamItem item) {
        if(filter.isEmpty())
            return false;

        switch(type) {
            case CLASS:
                String[] classes = item.clazz.split(",");
                for (String s : classes) {
                    if (filterAN.equals(SIAApp.deleteNonAlphanumeric(s)))
                        return true;
                }
                return false;
            case TEACHER:
                return SIAApp.deleteNonAlphanumeric(item.teacher).equals(filterAN);
            case SUBJECT:
                if (contains)
                    return SIAApp.deleteNonAlphanumeric(item.subject).contains(filterAN);
                else
                    return SIAApp.deleteNonAlphanumeric(item.subject).equals(filterAN);
            case LESSON:
                return SIAApp.deleteNonAlphanumeric(item.lesson).equals(filterAN);
        }
        return false;
    }

    public static String getTypeString(FilterType type) {
        String s;
        switch(type) {
            case CLASS:
                s = SIAApp.SIA_APP.getString(R.string.school_class);
                break;
            case TEACHER:
                s = SIAApp.SIA_APP.getString(R.string.teacher);
                break;
            case SUBJECT:
                s = SIAApp.SIA_APP.getString(R.string.subject_course_name);
                break;
            case LESSON:
                s = SIAApp.SIA_APP.getString(R.string.lhour);
                break;
            default:
                s = "";
        }
        return s;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    public String toString(boolean st) {
        return (st ? (getTypeString(type) + " ") : "") + filter;
    }

    public enum FilterType {
        CLASS, TEACHER, SUBJECT, LESSON
    }

    public interface Filterable {

    }

    public static class IncludingFilter extends Filter {
        public ArrayList<ExcludingFilter> excluding = new ArrayList<>();

        public IncludingFilter() {

        }

        public IncludingFilter(FilterType type, String f) {
            super(type, f);
        }

        @Override
        public boolean matches(Filterable f) {
            boolean b = super.matches(f);
            if(b) {
                if(f instanceof Subst.Entry && getType() == FilterType.CLASS) {
                    if(!((Subst.Entry) f).clazz.equals(getFilter())) {
                        setFilter(((Subst.Entry) f).clazz);
                        FilterActivity.saveFilter(SIAApp.SIA_APP.filters);
                    }
                }

                for(Filter filter : excluding) {
                    if(filter.matches(f))
                        b = false;
                }
            }

            return b;
        }
    }

    public static class ExcludingFilter extends Filter {
        private IncludingFilter parent;

        public ExcludingFilter() {

        }

        public ExcludingFilter(FilterType type, String f, IncludingFilter in) {
            super(type, f);
            this.parent = in;
        }

        public IncludingFilter getParentFilter() {
            return parent;
        }
    }

    public static class FilterList {
        public ArrayList<IncludingFilter> including = new ArrayList<>();

        public void clear() {
            including.clear();
        }

        public boolean matches(Filterable entry) {
            boolean b = false;
            for(Filter f : including) {
                if(f.matches(entry))
                    return true;
            }

            return false;
        }

        public String getSummary(boolean pre) {
            String text = "";

            if(including.size() == 0) {
                text = SIAApp.SIA_APP.getString(R.string.no_filter_active);
            } else if(including.size() == 1) {
                Filter f = including.get(0);
                text = f.getType() == Filter.FilterType.CLASS ? SIAApp.SIA_APP.getString(R.string.school_class) + " " + f.getFilter() :
                        SIAApp.SIA_APP.getString(R.string.teacher) + " " + f.getFilter();
            } else if(pre) {
                text = SIAApp.SIA_APP.getString(R.string.filters_active, including.size());
            } else {
                for(Filter f : including) {
                    text += f.getFilter() + ", ";
                }

                text = text.substring(0, text.length() - 2);

                if(text.length() > 20) {
                    text = text.substring(0, 19) + "...";
                }
            }

            return text;
        }
    }
}