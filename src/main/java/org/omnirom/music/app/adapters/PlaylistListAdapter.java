package org.omnirom.music.app.adapters;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.omnirom.music.app.R;
import org.omnirom.music.app.ui.AlbumArtImageView;
import org.omnirom.music.model.Playlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 */
public class PlaylistListAdapter extends BaseAdapter {

    private static final int DEFERRED_DELAY = 20;

    private List<Playlist> mPlaylists;
    private Handler mHandler;

    private static class ViewHolder {
        public AlbumArtImageView ivCover;
        public TextView tvTitle;
        public TextView tvSubTitle;
        public Playlist playlist;
    }

    public PlaylistListAdapter() {
        mPlaylists = new ArrayList<Playlist>();
        mHandler = new Handler();
    }

    private void sortList() {
        Collections.sort(mPlaylists, new Comparator<Playlist>() {
            @Override
            public int compare(Playlist playlist, Playlist playlist2) {
                if (playlist != null && playlist2 != null)
                    return playlist.getName().compareTo(playlist2.getName());
                else
                    return 0;
            }
        });
    }

    public void addItem(Playlist p) {
        mPlaylists.add(p);
        sortList();
    }

    public void addItemUnique(Playlist p) {
        if (!mPlaylists.contains(p)) {
            mPlaylists.add(p);
            sortList();
        }
    }

    public void addAll(List<Playlist> ps) {
        mPlaylists.addAll(ps);
        sortList();
        notifyDataSetChanged();
    }

    public void addAllUnique(List<Playlist> ps) {
        boolean didChange = false;
        for (Playlist p : ps) {
            if (p != null && !mPlaylists.contains(p)) {
                mPlaylists.add(p);
                didChange = true;
            }
        }

        if (didChange) {
            sortList();
            notifyDataSetChanged();
        }
    }

    public boolean contains(Playlist p) {
        return mPlaylists.contains(p);
    }

    @Override
    public int getCount() {
        return mPlaylists.size();
    }

    @Override
    public Playlist getItem(int position) {
        return mPlaylists.get(position);
    }

    @Override
    public long getItemId(int position) {
        if (position >= 0 && position < mPlaylists.size()) {
            return mPlaylists.get(position).getRef().hashCode();
        } else {
            return -1;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context ctx = parent.getContext();
        assert ctx != null;

        View root = convertView;
        if (convertView == null) {
            // Recycle the existing view
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            root = inflater.inflate(R.layout.medium_card_two_lines, parent, false);
            assert root != null;

            ViewHolder holder = new ViewHolder();
            holder.ivCover = (AlbumArtImageView) root.findViewById(R.id.ivCover);
            holder.tvTitle = (TextView) root.findViewById(R.id.tvTitle);
            holder.tvSubTitle = (TextView) root.findViewById(R.id.tvSubTitle);

            root.setTag(holder);
        }

        // Fill in the fields
        final Playlist playlist = getItem(position);
        final ViewHolder tag = (ViewHolder) root.getTag();

        tag.playlist = playlist;

        if (playlist.isLoaded()) {
            tag.tvTitle.setText(playlist.getName());
            tag.tvSubTitle.setText(ctx.getResources().getQuantityString(R.plurals.songs_count, playlist.getSongsCount(), playlist.getSongsCount()));
            tag.ivCover.loadArtForPlaylist(playlist);
        } else {
            tag.tvTitle.setText(R.string.loading);
            tag.tvSubTitle.setText(R.string.loading);
            tag.ivCover.setDefaultArt();
        }

        return root;
    }

}
