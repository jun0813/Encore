/*
 * Copyright (C) 2014 Fastboot Mobile, LLC.
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

package org.omnirom.music.app.adapters;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.omnirom.music.app.AlbumActivity;
import org.omnirom.music.app.R;
import org.omnirom.music.app.Utils;
import org.omnirom.music.app.fragments.AlbumsFragment;
import org.omnirom.music.app.fragments.ArtistsListFragment;
import org.omnirom.music.app.fragments.PlaylistListFragment;
import org.omnirom.music.app.fragments.SongsFragment;
import org.omnirom.music.app.ui.AlbumArtImageView;
import org.omnirom.music.app.ui.MaterialTransitionDrawable;
import org.omnirom.music.framework.ListenLogger;
import org.omnirom.music.model.Artist;
import org.omnirom.music.model.Song;
import org.omnirom.music.providers.ProviderAggregator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Adapter to display the playback history
 */
public class HistoryAdapter extends BaseAdapter {
    private static final String TAG = "HistoryAdapter";

    private static SimpleDateFormat sDateFormat = new SimpleDateFormat("MMM dd\nHH:mm");
    private static Comparator<ListenLogger.LogEntry> sTimeSort = new Comparator<ListenLogger.LogEntry>() {
        @Override
        public int compare(ListenLogger.LogEntry lhs, ListenLogger.LogEntry rhs) {
            return rhs.getTimestamp().compareTo(lhs.getTimestamp());
        }
    };

    private ListenLogger mLogger;
    private List<ListenLogger.LogEntry> mEntries;
    private View.OnClickListener mAlbumArtClickListener = new View.OnClickListener() {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onClick(View v) {
            ViewHolder tag = (ViewHolder) v.getTag();
            FragmentActivity activity = (FragmentActivity) v.getContext();

            MaterialTransitionDrawable mtd = (MaterialTransitionDrawable) tag.ivAlbumArt.getDrawable();
            Bitmap bitmap = mtd.getFinalDrawable().getBitmap();

            Intent intent = AlbumActivity.craftIntent(activity, bitmap, tag.song.getAlbum(),
                    tag.song.getProvider(),
                    v.getResources().getColor(R.color.default_album_art_background));
            if (Utils.hasLollipop()) {
                ActivityOptions opts
                        = ActivityOptions.makeSceneTransitionAnimation(activity, tag.ivAlbumArt,
                        "itemImage");
                activity.startActivity(intent, opts.toBundle());
            } else {
                v.getContext().startActivity(intent);
            }
        }
    };
    private View.OnClickListener mOverflowClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ViewHolder tag = (ViewHolder) v.getTag();
            Utils.showSongOverflow((FragmentActivity) v.getContext(), tag.btnOverflow, tag.song);
        }
    };

    public HistoryAdapter(Context ctx) {
        mLogger = new ListenLogger(ctx);
        mEntries = mLogger.getEntries();
        sortByTime(mEntries);
    }

    @Override
    public int getCount() {
        return mEntries.size();
    }

    @Override
    public ListenLogger.LogEntry getItem(int position) {
        return mEntries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ProviderAggregator aggregator = ProviderAggregator.getDefault();
        ViewHolder tag;

        if (convertView == null) {
            Context ctx = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(ctx);

            convertView = inflater.inflate(R.layout.item_history, parent, false);
            tag = new ViewHolder(convertView);

            tag.ivAlbumArt.setOnClickListener(mAlbumArtClickListener);
            tag.btnOverflow.setOnClickListener(mOverflowClickListener);
        } else {
            tag = (ViewHolder) convertView.getTag();
        }

        ListenLogger.LogEntry entry = getItem(position);
        Song song = aggregator.retrieveSong(entry.getReference(), entry.getIdentifier());

        tag.song = song;

        if (song != null) {
            if (song.isLoaded()) {
                tag.ivAlbumArt.loadArtForSong(song);
                tag.tvTitle.setText(song.getTitle());

                Artist artist = aggregator.retrieveArtist(song.getArtist(), song.getProvider());
                if (artist != null && artist.getName() != null && !artist.getName().isEmpty()) {
                    tag.tvArtist.setText(artist.getName());
                } else {
                    tag.tvArtist.setText(R.string.loading);
                }
            } else {
                tag.tvTitle.setText(R.string.loading);
                tag.tvArtist.setText(null);
            }
        }

        tag.tvPlayDate.setText(sDateFormat.format(entry.getTimestamp()));
        return convertView;
    }

    /**
     * Sorts the provided list by time, most recent first
     * @param list The list to sort
     */
    public static void sortByTime(List<ListenLogger.LogEntry> list) {
        Collections.sort(list, sTimeSort);
    }

    private static class ViewHolder {
        public View vRoot;
        public TextView tvArtist;
        public TextView tvTitle;
        public TextView tvPlayDate;
        public AlbumArtImageView ivAlbumArt;
        public ImageView btnOverflow;
        public Song song;

        public ViewHolder(View root) {
            vRoot = root;
            tvArtist = (TextView) root.findViewById(R.id.tvArtist);
            tvTitle = (TextView) root.findViewById(R.id.tvTitle);
            tvPlayDate = (TextView) root.findViewById(R.id.tvPlayDate);
            ivAlbumArt = (AlbumArtImageView) root.findViewById(R.id.ivAlbumArt);
            btnOverflow = (ImageView) root.findViewById(R.id.btnOverflow);

            vRoot.setTag(this);
            tvArtist.setTag(this);
            tvTitle.setTag(this);
            tvPlayDate.setTag(this);
            ivAlbumArt.setTag(this);
            btnOverflow.setTag(this);
        }
    }
}
