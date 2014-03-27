package org.omnirom.music.providers;

import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

import org.omnirom.music.model.Album;
import org.omnirom.music.model.Artist;
import org.omnirom.music.model.Playlist;
import org.omnirom.music.model.Song;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ProviderAggregator extends IProviderCallback.Stub {

    private static final String TAG = "ProviderAggregator";

    private List<ILocalCallback> mUpdateCallbacks;
    final private List<ProviderConnection> mProviders;
    private ProviderCache mCache;
    private Handler mHandler;

    private Runnable mUpdatePlaylistsRunnable = new Runnable() {
        @Override
        public void run() {
            // We make a copy to avoid synchronization issues and needless locks
            ArrayList<ProviderConnection> providers;
            synchronized (mProviders) {
                providers = new ArrayList<ProviderConnection>(mProviders);
            }

            // Then we query the providers
            for (ProviderConnection conn : providers) {
                try {
                    if (conn.getBinder().isSetup() && conn.getBinder().isAuthenticated()) {
                        conn.getBinder().getPlaylists();
                    } else {
                        Log.i(TAG, "Skipping a provider because it is not setup or authenticated");
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Unable to get playlists from a provider", e);
                    unregisterProvider(conn);
                }
            }
        }
    };

    // Singleton
    private final static ProviderAggregator INSTANCE = new ProviderAggregator();
    public final static ProviderAggregator getDefault() {
        return INSTANCE;
    }

    /**
     * Default constructor
     */
    private ProviderAggregator() {
        mUpdateCallbacks = new ArrayList<ILocalCallback>();
        mProviders = new ArrayList<ProviderConnection>();
        mCache = new ProviderCache();
        mHandler = new Handler();
    }

    /**
     * Posts the provided runnable to this class' Handler, and make sure
     * @param r
     */
    private void postOnce(Runnable r) {
        mHandler.removeCallbacks(r);
        mHandler.post(r);
    }

    /**
     * @return The data cache
     */
    public ProviderCache getCache() {
        return mCache;
    }

    /**
     * Registers a LocalCallback class, which will be called when various events happen from
     * any of the registered providers.
     * @param cb The callback to add
     */
    public void addUpdateCallback(ILocalCallback cb) {
        mUpdateCallbacks.add(cb);
    }

    /**
     * Unregisters a local update callback
     * @param cb The callback to remove
     */
    public void removeUpdateCallback(ILocalCallback cb) {
        mUpdateCallbacks.remove(cb);
    }

    /**
     * Registers a new provider service that has been bound, and add the aggregator as a callback
     * @param provider The provider that connected
     */
    public void registerProvider(ProviderConnection provider) {
        boolean added = false;
        synchronized (mProviders) {
            if (!mProviders.contains(provider)) {
                mProviders.add(provider);
                added = true;
            }
        }

        if (added) {
            try {
                provider.getBinder().registerCallback(this);

                for (ILocalCallback cb : mUpdateCallbacks) {
                    cb.onProviderConnected(provider.getBinder());
                }
            } catch (RemoteException e) {
                // Maybe the service died already?
                Log.e(TAG, "Unable to register as a callback");
            }
        }
    }

    /**
     * Removes the connection to a provider. This may be called either if the connection to a
     * service has been lost (e.g. in case of a DeadObjectException if the service crashed), or
     * simply if the app closes and a service is not needed anymore.
     * @param provider The provider to remove
     */
    public void unregisterProvider(final ProviderConnection provider) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (mProviders) {
                    mProviders.remove(provider);
                    try {
                        provider.getBinder().unregisterCallback(ProviderAggregator.this);
                    } catch (RemoteException e) { }
                    provider.unbindService();
                }
            }
        });
    }

    /**
     * Perform a search query for the provided terms on all providers. The results are pushed
     * progressively as they are given by the providers to the callback.
     * @param query The search terms
     * @param callback The callback to call with results
     */
    public void search(final String query, final ISearchCallback callback) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Returns the list of all cached playlists. At the same time, providers will be called for
     * updates and/or fetching playlists, and LocalCallbacks will be called when providers notify
     * this class of eventual new entries.
     *
     * @return A list of playlists
     */
    public List<Playlist> getAllPlaylists() {
        // We return the playlists from the cache, and query for new playlists. They will be updated
        // in the callback if needed.
        List<Playlist> playlists = mCache.getAllPlaylists();

        postOnce(mUpdatePlaylistsRunnable);

        return playlists;
    }

    /**
     * Called by the provider when a feedback is available about a login request
     *
     * @param success Whether or not the login succeeded
     */
    @Override
    public void onLoggedIn(final IMusicProvider provider, boolean success) throws RemoteException {
        // Request playlists if we logged in
        Log.d(TAG, "onLoggedIn(" + success + ")");
        if (success) {
            postOnce(mUpdatePlaylistsRunnable);
        } else {
            // TODO: Show a message
        }
    }

    /**
     * Called by the provider when the user login has expired, or has been kicked.
     */
    @Override
    public void onLoggedOut(IMusicProvider provider) throws RemoteException {

    }

    /**
     * Called by the provider when a Playlist has been added or updated. The app's provider
     * syndicator will automatically update the local cache of playlists based on the playlist
     * name.
     */
    @Override
    public void onPlaylistAddedOrUpdated(IMusicProvider provider, Playlist p) throws RemoteException {
        // We compare the provided copy with the one we have in cache. We only notify the callbacks
        // if it indeed changed.
        Playlist cached = mCache.getPlaylist(p.getRef());

        boolean notify;
        if (cached == null) {
            mCache.putPlaylist(p);
            notify = true;
        } else {
            notify = !cached.isIdentical(p);
        }

        if (notify) {
            for (ILocalCallback cb : mUpdateCallbacks) {
                cb.onPlaylistUpdate(p);
            }
        }
    }

    /**
     * Called by the provider when the details of a song have been updated.
     */
    @Override
    public void onSongUpdate(IMusicProvider provider, Song s) throws RemoteException {

    }

    /**
     * Called by the provider when the details of an album have been updated.
     */
    @Override
    public void onAlbumUpdate(IMusicProvider provider, Album a) throws RemoteException {

    }

    /**
     * Called by the provider when the details of an artist have been updated.
     */
    @Override
    public void onArtistUpdate(IMusicProvider provider, Artist a) throws RemoteException {

    }


}