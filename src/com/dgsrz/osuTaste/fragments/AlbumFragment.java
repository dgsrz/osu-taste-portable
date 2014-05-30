package com.dgsrz.osuTaste.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.dgsrz.osuTaste.R;
import com.dgsrz.osuTaste.loaders.BeatmapLoader;
import com.dgsrz.osuTaste.model.Beatmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dgsrz (dgsrz@vip.qq.com)
 */
public final class AlbumFragment extends Fragment implements LoaderCallbacks<List<Beatmap>> {

    private final static String MUSIC_SERVICE = "com.dgsrz.osuTaste.services.BassService";

    private final static String OPERATE_PLAY = "play";

    private static final int LOADER = 0;

    public static AlbumFragment getInstance() {
        return new AlbumFragment();
    }

    SimpleAdapter mAdapter;

    ListView listView;

    List<Beatmap> mBeatmapList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.list_view, container, false);
        listView = (ListView)v.findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Beatmap beatmap = mBeatmapList.get(position);
                Intent intent = new Intent();
                intent.setAction(MUSIC_SERVICE);
                intent.putExtra(OPERATE_PLAY, beatmap.mDirectory + "/" + beatmap.mFileName);
                getActivity().sendBroadcast(intent);
            }
        });
        return v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER, null, this);
    }

    @Override
    public Loader<List<Beatmap>> onCreateLoader(final int id, final Bundle arg) {
        return new BeatmapLoader(getActivity());
    }

    @Override
    public void onLoadFinished(final Loader<List<Beatmap>> loader, final List<Beatmap> data) {
        if (data.isEmpty()) {
            return;
        }
        mBeatmapList = data;
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (final Beatmap beatmap : data) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("name", beatmap.mFileName);
            map.put("album", R.drawable.music);
            map.put("info", beatmap.mArtist);
            list.add(map);
        }
        mAdapter = new SimpleAdapter(getActivity(),
                list,
                R.layout.list_item_normal,
                new String[] { "album", "name", "info" },
                new int[] { R.id.imageView, R.id.name, R.id.info });
        mAdapter.notifyDataSetChanged();
        listView.setAdapter(mAdapter);
    }

    @Override
    public void onLoaderReset(Loader<List<Beatmap>> listLoader) {
        mAdapter.notifyDataSetChanged();
    }
}
