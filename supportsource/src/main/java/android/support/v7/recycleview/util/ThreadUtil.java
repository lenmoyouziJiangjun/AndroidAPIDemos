package android.support.v7.recycleview.util;

public interface ThreadUtil<T> {

    interface MainThreadCallBack<T> {

        void updateItemCount(int generation, int itemCount);

        void addTile(int generation, TileList.Tile<T> tile);

        void removeTile(int generation, int position);
    }

    interface BackgroundCallback<T> {

        void refresh(int generation);

        void updateRange(int rangeStart, int rangeEnd, int extRangeStart, int extRangeEnd,
                         int scrollHint);

        void loadTile(int position, int scrollHint);

        void recycleTile(TileList.Tile<T> tile);
    }

    MainThreadCallBack<T> getMainThreadProxy(MainThreadCallBack<T> callBack);

    BackgroundCallback<T> getBackgroundProxy(BackgroundCallback<T> callback);
}
