/*
 * Copyright (C) 2015 The Dirty Unicorns Project
 * Copyright (C) 2016 Benzo Rom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTileView;
import com.android.internal.logging.MetricsProto.MetricsEvent;

import cyanogenmod.providers.CMSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NavBarTile extends QSTile<QSTile.BooleanState> {
    private boolean mListening;
    private NavBarObserver mObserver;

    public NavBarTile(Host host) {
        super(host);
        mObserver = new NavBarObserver(mHandler);
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    protected void handleClick() {
        toggleState();
        refreshState();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.QUICK_SETTINGS;
    }

    @Override
    public void handleLongClick() {
    }

    protected void toggleState() {
        CMSettings.Global.putInt(mContext.getContentResolver(),
            CMSettings.Global.DEV_FORCE_SHOW_NAVBAR, !isHWKeysDisabled() ? 1 : 0);
    }

    @Override
    public Intent getLongClickIntent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_navbar_title);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        if (isHWKeysDisabled()) {
            state.icon = ResourceIcon.get(R.drawable.ic_qs_navbar);
            state.label = mContext.getString(R.string.quick_settings_navbar);
        } else {
            state.icon = ResourceIcon.get(R.drawable.ic_qs_navbar_off);
            state.label = mContext.getString(R.string.quick_settings_navbar_off);
        }
    }

    private boolean isHWKeysDisabled() {
        return CMSettings.Global.getInt(mContext.getContentResolver(),
            CMSettings.Global.DEV_FORCE_SHOW_NAVBAR, 1) == 1;
    }

    @Override
    public void setListening(boolean listening) {
        if (mListening == listening) return;
        mListening = listening;
        if (listening) {
            mObserver.startObserving();
        } else {
            mObserver.endObserving();
        }
    }

    private class NavBarObserver extends ContentObserver {
        public NavBarObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            refreshState();
        }

        public void startObserving() {
            mContext.getContentResolver().registerContentObserver(
                CMSettings.Global.getUriFor(CMSettings.Global.DEV_FORCE_SHOW_NAVBAR),
                false, this);
        }

        public void endObserving() {
            mContext.getContentResolver().unregisterContentObserver(this);
        }
    }
}
