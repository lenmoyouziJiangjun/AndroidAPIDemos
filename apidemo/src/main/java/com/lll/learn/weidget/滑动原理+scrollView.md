###滑动介绍
   滑动只要区分为两种：
  **一种是View自身的滑动(位移动画);**
    1、View自身的滑动，通过动画更改View的x,y坐标。或者通过layoutParams更改View的基础参数实现。

  **一种是View内容(content)的滑动.**
    1、view内容的滚动，通过View自带的两个方法：scrollTo() 和 scrollBy()
    2、通过Scroller实现滑动


###ScrollView的原理：
   1、自定义一个ViewGroup，三个基本方法：onMeasure,onLayout,onDraw和事件处理方法：onInterceptTouchEvent 和 touchEvent;






###HorizontalScrollView的原理




###ViewDragHelper原理介绍：


