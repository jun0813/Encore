/*
 * Copyright (C) 2015 Fastboot Mobile, LLC.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses>.
 */

package com.fastbootmobile.encore.app.tv;

import android.support.v17.leanback.widget.Row;

import com.fastbootmobile.encore.model.Song;

public class SongRow extends Row {
    private final Song mSong;
    private final int mPosition;

    public SongRow(Song song, int position) {
        mSong = song;
        mPosition = position;
    }

    public Song getSong() {
        return mSong;
    }

    public int getPosition() {
        return mPosition;
    }
}
