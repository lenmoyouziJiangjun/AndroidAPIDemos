package com.lll.commonlibrary.util;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.format.Time;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import edu.wanglai.com.commonlibrary.R;

/**
 * Created by root on 15-8-28.
 * 时间工具类
 */
public class DateUtils {
    /**
     * 获取增加多少月的时间
     *
     * @return addMonth - 增加多少月
     */
    public static Date getAddMonthDate(int addMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, addMonth);
        return calendar.getTime();
    }

    /**
     * 获取增加多少天的时间
     *
     * @return addDay - 增加多少天
     */
    public static Date getAddDayDate(int addDay) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, addDay);
        return calendar.getTime();
    }

    /**
     * 获取增加多少小时的时间
     *
     * @return addDay - 增加多少消失
     */
    public static Date getAddHourDate(int addHour) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, addHour);
        return calendar.getTime();
    }

    /**
     * 显示时间格式为 hh:mm
     *
     * @param when
     * @return String
     */
    @SuppressLint("SimpleDateFormat")
    public static String formatTimeShort(long when, String formatString) {
        SimpleDateFormat sdf = new SimpleDateFormat(formatString, Locale.getDefault());
        String temp = sdf.format(when);
        if (temp != null && temp.length() == 5 && temp.substring(0, 1).equals("0")) {
            temp = temp.substring(1);
        }
        return temp;
    }


    /**
     * 显示时间格式为今天、昨天、yyyy/MM/dd hh:mm
     *
     * @param context
     * @param when
     * @return String
     */
    public static String formatTimeString(Context context, long when) {
        Time then = new Time();
        then.set(when);
        Time now = new Time();
        now.setToNow();

        String formatStr;
        if (then.year != now.year) {
            formatStr = "yyyy/MM/dd";
        } else if (then.yearDay != now.yearDay) {
            // If it is from a different day than today, show only the date.
            formatStr = "MM/dd";
        } else {
            // Otherwise, if the message is from today, show the time.
            formatStr = "HH:MM";
        }

        if (then.year == now.year && then.yearDay == now.yearDay) {
            return context.getString(R.string.str_today);
        } else if ((then.year == now.year) && ((now.yearDay - then.yearDay) == 1)) {
            return context.getString(R.string.str_yesterday);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
            String temp = sdf.format(when);
            if (temp != null && temp.length() == 5 && temp.substring(0, 1).equals("0")) {
                temp = temp.substring(1);
            }
            return temp;
        }
    }

    /**
     * 是否同一天
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isSameDate(long date1, long date2) {
        long days1 = date1 / (1000 * 60 * 60 * 24);
        long days2 = date2 / (1000 * 60 * 60 * 24);
        return days1 == days2;
    }


    /**
     * 将String时间转为long
     *
     * @param dateStr
     * @param formatStr :yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static long changeString2Long(String dateStr, String formatStr) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat(formatStr);
        Date date = format.parse(dateStr);
        return date.getTime();
    }
//
//
//    public static void showTakePictureDialog(Context ctx) {
//        final String[] items = {"相册图片", "拍照"};
//        new AlertDialog.Builder(ctx)
//                .setTitle("设置头像")
//                .setIcon(android.R.drawable.ic_dialog_info)
//                .setItems(items, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        if ("相册图片".equals(items[which])) {
//                            Intent intent = new Intent();
//                            intent.setType("image/*");
//                            intent.setAction(Intent.ACTION_GET_CONTENT);
////                            startActivityForResult(intent, Constant.PHOTO_WITH_DATA);
//                        } else if ("拍照".equals(items[which])) {
//                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                            if (TelephoneUtils.isSdPresent()) {
//                                File temp = new File(Constant.TEMP_FILE_PATH + "tempHeadImage.jpg");
//                                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(temp));
////                                startActivityForResult(intent, Constant.CAMERA_WITH_DATA);
//                            } else {
//                                Toast.makeText(ctx, "无SD卡，无法完成操作！", Toast.LENGTH_LONG).show();
//                            }
//                        }
//                        dialog.dismiss();
//                    }
//                })
//                .show().setCanceledOnTouchOutside(true);
//    }
}
