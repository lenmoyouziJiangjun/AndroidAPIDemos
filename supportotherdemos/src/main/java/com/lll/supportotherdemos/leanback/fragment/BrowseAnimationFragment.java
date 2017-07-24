package com.lll.supportotherdemos.leanback.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.view.View;

import com.lll.supportotherdemos.R;
import com.lll.supportotherdemos.leanback.SearchActivity;
import com.lll.supportotherdemos.leanback.presenter.StringPresenter;

import java.util.Random;

/**
 * Version 1.0
 * Created by lll on 17/7/24.
 * Description
 * copyright generalray4239@gmail.com
 */
public class BrowseAnimationFragment extends android.support.v17.leanback.app.BrowseFragment {

    private static final String TAG = "lll.anim";

    private static final int NUM_ROWS = 10;
    private ArrayObjectAdapter mRowsAdapter;
    private static Random sRand = new Random();

    static class Item {
        final String mText;
        final OnItemViewClickedListener mListener;

        Item(String text, OnItemViewClickedListener listener) {
            this.mText = text;
            mListener = listener;
        }
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {
            ((Item) item).mListener.onItemClicked(itemViewHolder, item, rowViewHolder, row);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBadgeDrawable(getResources().getDrawable(R.drawable.ic_title));
        setTitle("Leanback Sample App");
        setHeadersState(HEADERS_ENABLED);

        setOnSearchClickedListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });

        setupRows();
        setOnItemViewClickedListener(new ItemViewClickedListener());
    }

    private void setupRows() {
        ListRowPresenter presenter = new ListRowPresenter();
        mRowsAdapter = new ArrayObjectAdapter(presenter);
        for (int i = 0; i < NUM_ROWS; i++) {
            mRowsAdapter.add(createRandomRow(new HeaderItem(i, "Row " + i)));
        }
        setAdapter(mRowsAdapter);
    }

    ListRow createRandomRow(HeaderItem header) {
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(
                new StringPresenter());
        for (int i = 0; i < 8; i++) {
            listRowAdapter.add(createRandomItem());
        }
        return new ListRow(header, listRowAdapter);
    }

    Item createRandomItem() {
        switch (sRand.nextInt(15)) {
            default:
            case 0:
                return new Item("Remove Item before", new OnItemViewClickedListener() {
                    @Override
                    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                              RowPresenter.ViewHolder rowViewHolder, Row row) {
                        ArrayObjectAdapter adapter = ((ArrayObjectAdapter) ((ListRow) row)
                                .getAdapter());
                        int index = adapter.indexOf(item);
                        if (index >= 0) {
                            if (index > 0)
                                index--;
                            adapter.removeItems(index, 1);
                        }
                    }
                });
            case 1:
                return new Item("Remove Item after", new OnItemViewClickedListener() {
                    @Override
                    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                              RowPresenter.ViewHolder rowViewHolder, Row row) {
                        ArrayObjectAdapter adapter = ((ArrayObjectAdapter) ((ListRow) row)
                                .getAdapter());
                        int index = adapter.indexOf(item);
                        if (index >= 0) {
                            if (index < adapter.size() - 1)
                                index++;
                            adapter.removeItems(index, 1);
                        }
                    }
                });
            case 2:
                return new Item("Remove Item", new OnItemViewClickedListener() {
                    @Override
                    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                              RowPresenter.ViewHolder rowViewHolder, Row row) {
                        ArrayObjectAdapter adapter = ((ArrayObjectAdapter) ((ListRow) row)
                                .getAdapter());
                        int index = adapter.indexOf(item);
                        if (index >= 0) {
                            adapter.removeItems(index, 1);
                        }
                    }
                });
            case 3:
                return new Item("Remove all Items", new OnItemViewClickedListener() {
                    @Override
                    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                              RowPresenter.ViewHolder rowViewHolder, Row row) {
                        ArrayObjectAdapter adapter = ((ArrayObjectAdapter) ((ListRow) row)
                                .getAdapter());
                        adapter.clear();
                    }
                });
            case 4:
                return new Item("add item before", new OnItemViewClickedListener() {
                    @Override
                    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                              RowPresenter.ViewHolder rowViewHolder, Row row) {
                        ArrayObjectAdapter adapter = ((ArrayObjectAdapter) ((ListRow) row)
                                .getAdapter());
                        int index = adapter.indexOf(item);
                        if (index >= 0) {
                            adapter.add(index, createRandomItem());
                        }
                    }
                });
            case 5:
                return new Item("add item after", new OnItemViewClickedListener() {
                    @Override
                    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                              RowPresenter.ViewHolder rowViewHolder, Row row) {
                        ArrayObjectAdapter adapter = ((ArrayObjectAdapter) ((ListRow) row)
                                .getAdapter());
                        int index = adapter.indexOf(item);
                        if (index >= 0) {
                            adapter.add(index + 1, createRandomItem());
                        }
                    }
                });
            case 6:
                return new Item("add random items before",
                        new OnItemViewClickedListener() {
                            @Override
                            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                                      RowPresenter.ViewHolder rowViewHolder, Row row) {
                                ArrayObjectAdapter adapter = ((ArrayObjectAdapter) ((ListRow) row)
                                        .getAdapter());
                                int index = adapter.indexOf(item);
                                if (index >= 0) {
                                    int count = sRand.nextInt(4) + 1;
                                    for (int i = 0; i < count; i++) {
                                        adapter.add(index + i, createRandomItem());
                                    }
                                }
                            }
                        });
            case 7:
                return new Item("add random items after",
                        new OnItemViewClickedListener() {
                            @Override
                            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                                      RowPresenter.ViewHolder rowViewHolder, Row row) {
                                ArrayObjectAdapter adapter = ((ArrayObjectAdapter) ((ListRow) row)
                                        .getAdapter());
                                int index = adapter.indexOf(item);
                                if (index >= 0) {
                                    int count = sRand.nextInt(4) + 1;
                                    for (int i = 0; i < count; i++) {
                                        adapter.add(index + 1 + i,
                                                createRandomItem());
                                    }
                                }
                            }
                        });
            case 8:
                return new Item("add row before", new OnItemViewClickedListener() {
                    @Override
                    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                              RowPresenter.ViewHolder rowViewHolder, Row row) {
                        int index = mRowsAdapter.indexOf(row);
                        if (index >= 0) {
                            int headerId = sRand.nextInt();
                            mRowsAdapter.add(index, createRandomRow(new HeaderItem(
                                    headerId, "Row " + headerId)));
                        }
                    }
                });
            case 9:
                return new Item("add row after", new OnItemViewClickedListener() {
                    @Override
                    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                              RowPresenter.ViewHolder rowViewHolder, Row row) {
                        int index = mRowsAdapter.indexOf(row);
                        if (index >= 0) {
                            int headerId = sRand.nextInt();
                            mRowsAdapter.add(
                                    index + 1, createRandomRow(new HeaderItem(
                                            headerId, "Row " + headerId)));
                        }
                    }
                });
            case 10:
                return new Item("delete row", new OnItemViewClickedListener() {
                    @Override
                    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                              RowPresenter.ViewHolder rowViewHolder, Row row) {
                        mRowsAdapter.remove(row);
                    }
                });
            case 11:
                return new Item("delete row before", new OnItemViewClickedListener() {
                    @Override
                    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                              RowPresenter.ViewHolder rowViewHolder, Row row) {
                        int index = mRowsAdapter.indexOf(row);
                        if (index > 0) {
                            mRowsAdapter.removeItems(index - 1, 1);
                        }
                    }
                });
            case 12:
                return new Item("delete row after", new OnItemViewClickedListener() {
                    @Override
                    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                              RowPresenter.ViewHolder rowViewHolder, Row row) {
                        int index = mRowsAdapter.indexOf(row);
                        if (index < mRowsAdapter.size() - 1) {
                            mRowsAdapter.removeItems(index + 1, 1);
                        }
                    }
                });
            case 13:
                return new Item("Replace Item before", new OnItemViewClickedListener() {
                    @Override
                    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                              RowPresenter.ViewHolder rowViewHolder, Row row) {
                        ArrayObjectAdapter adapter = ((ArrayObjectAdapter) ((ListRow) row)
                                .getAdapter());
                        int index = adapter.indexOf(item);
                        if (index >= 0) {
                            if (index > 0)
                                index--;
                            adapter.replace(index, createRandomItem());
                        }
                    }
                });
            case 14:
                return new Item("Remove all then re-add", new OnItemViewClickedListener() {
                    @Override
                    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                              RowPresenter.ViewHolder rowViewHolder, Row row) {
                        final ArrayObjectAdapter adapter = ((ArrayObjectAdapter) ((ListRow) row)
                                .getAdapter());
                        adapter.clear();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                adapter.add(0, createRandomItem());
                            }
                        }, 1000);
                    }
                });
        }
    }
}
