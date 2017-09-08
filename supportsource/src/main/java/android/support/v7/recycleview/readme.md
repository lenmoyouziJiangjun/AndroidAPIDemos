#RecyclerView框架学习
####1、基本流程
      setAdapter()->onlayout()->LayoutManager.onLayoutChildren()
      逻辑都放在了LayoutManager的onLayoutChildren中去实现了。包括load ViewHolder, measureView ,addView,layoutView,recycleView;
      不同的LayoutManager的实现方案不一样：我们主要讲解LinearLayoutManager 和StaggeredGridLayoutManager

     * 1、LinearLayoutManager 初始加载:
         onLayoutChildren->fill()->layoutChunk()
     * 2、滑动事件：
         ontouchEvent->scrollByInternal()->scrollBy(里面调用了fill()方法找到需要加载ViewHolder）->itemView.offsetLeftAndRight(dx);//完成滚动
####2、架构设计
####3、View优化，内存优化(Recycler)、滑动效果：
      1、item复用的原理是如何实现的？
      2、item的显示隐藏逻辑是什么？

####4、Layout原理：LayoutManager、OrientationHelper
       1、我们知道怎么布局，是通过onlayout方法来实现的。RecyclerView里面定义LayoutManager管理我们不同Layout相关逻辑。
       2、如何计算onMeasure?如何layoutChild？同时vertical和horizontal 不同的处理？
####5、item的滑动处理：RecyclerView跟我们封装了很好的Item操作(滑动，拖动）主要业务逻辑在ItemTouchHelper工具类中。
       我们知道滑动，拖动的事件开头都是从ontachEvent开始的，然后通过scrollTo或者scrollBy来实现的，
       1、如何解决滑动冲突？如何处理事件传递？在拖动的时候怎么计算item的移动？
       2、滑动阴影的绘制？ViewGroup重写onDraw的优化？

####6、使用tips:
       1、如果RecycleView的大小不睡着adapter变化，设置setHasFiexdSize(true)；这样在measure的时候减少计算。

