package com.lll.supportotherdemos.leanback.presenter;

import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;

public class DetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {
    @Override
    protected void onBindDescription(ViewHolder vh, Object item) {
        vh.getTitle().setText(item.toString());
        vh.getSubtitle().setText("2013 - 2014   Drama   TV-14");
        vh.getBody().setText("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do "
                + "eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim "
                + "veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo "
                + "consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse "
                + "cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non "
                + "proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");
    }
}
