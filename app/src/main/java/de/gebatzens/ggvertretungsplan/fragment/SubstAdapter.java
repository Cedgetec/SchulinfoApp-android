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
package de.gebatzens.ggvertretungsplan.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import de.gebatzens.ggvertretungsplan.GGApp;
import de.gebatzens.ggvertretungsplan.R;
import de.gebatzens.ggvertretungsplan.data.GGPlan;

public class SubstAdapter extends FragmentStatePagerAdapter {

    ViewPager viewPager;
    GGPlan.GGPlans plans;
    
    public SubstAdapter(Fragment m, Bundle savedState, ViewPager vp) {
        super(m.getChildFragmentManager());
        this.viewPager = vp;
        plans = GGApp.GG_APP.plans;
        GGApp.GG_APP.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });

    }

    public void update(GGPlan.GGPlans pl) {
        plans = pl;
        GGApp.GG_APP.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });

    }

    public SubstPagerFragment getOverview() {
        return (SubstPagerFragment) instantiateItem(viewPager, 0);
    }

    public SubstPagerFragment getFragment(GGPlan plan) {
        return (SubstPagerFragment) instantiateItem(viewPager, plans.indexOf(plan));
    }

    public void setFragmentsLoading() {
        getOverview().setFragmentLoading();
        for(GGPlan p : plans)
            getFragment(p).setFragmentLoading();
    }

    @Override
    public Fragment getItem(int position) {
        SubstPagerFragment fragment = new SubstPagerFragment();
        if(position == 0)
            fragment.setParams(SubstPagerFragment.INDEX_OVERVIEW);
        else
            fragment.setParams(position - 1);
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int p) {
        switch(p) {
            case 0:
                return GGApp.GG_APP.getResources().getString(R.string.overview);
            default:
                return plans.get(p - 1).getWeekday();
        }
    }

    @Override
    public int getItemPosition(Object o) {
        SubstPagerFragment frag = (SubstPagerFragment) o;
        if(frag.index == SubstPagerFragment.INDEX_OVERVIEW) {
            frag.updateFragment();
            return 0;
        } else if(frag.index == SubstPagerFragment.INDEX_INVALID)
            return POSITION_NONE;
        else {
            int i = plans.indexOf(frag.plan);
            if(i >= 0) {
                frag.updateFragment();
                return i + 1;
            } else
                return POSITION_NONE;
        }

    }

    @Override
    public int getCount() {
        if(plans == null)
            return 1;
        else
            return plans.size() + 1;
    }

}
