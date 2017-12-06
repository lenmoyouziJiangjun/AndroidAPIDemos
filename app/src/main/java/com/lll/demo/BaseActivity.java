package com.lll.demo;

import android.support.v7.app.AppCompatActivity;
/**
 * Version 1.0
 * Created by lll on 17/10/13.
 * Description
 * 1、BaseActivity 的作用？
 *
 * 2、Activity 的生命周期？
 *     1、透明主题的activity，onstop方法不会执行
 *     2、activity的生命周期方法都不能执行耗时操作。
 *     3、资源的相关配置改变(常见的屏幕，输入法等等)，都会导致activity的杀死重建。
 *     4、activity的xml配置configchange后，activity不会重建，会调用activity的onconfigChange方法
 *
 * 3、Activity的启动模式有哪些？分别用在什么情况下？ 引导页应该使用哪种模式
 *    1、standard：标准模式。没启动一次就会创建一个实例。默认采用
 *       注意事项：用非activity类型的context去启动activity，需要设置new_task标记。
 *    2、singleTop:栈顶复用
 *    3、singleTask:栈中复用。移除栈中上面的元素。一般用在应用的homeActivity
 *    4、SingleInstance:
 *
 * copyright generalray4239@gmail.com
 */
public class BaseActivity extends AppCompatActivity {




}
