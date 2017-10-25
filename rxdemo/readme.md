###Rx系列demo
  >1、RxAndroid:https://github.com/ReactiveX/RxAndroid
  >2、RxBinding:https://github.com/JakeWharton/RxBinding
  >3、Rx系列：https://github.com/ReactiveX
  >4、RxLifecycle:https://github.com/trello/RxLifecycle
  >5、RxPermissions:https://github.com/tbruyelle/RxPermissions

###参考文档：
   1、Rx源码解析：https://mp.weixin.qq.com/s?__biz=MzI1MTA1MzM2Nw==&mid=2649796857&idx=1&sn=ed8325aeddac7fd2bd81a0717c010e98&mpshare=1&scene=1&srcid=0802K6sXdtwFTGZHCWzl7Zef&key=98a117c12818f031d9162a0bd1b944768879062cc303cc57921e5759e6ba2f923a4e04ad2239b7124ce45052fc49a4115cb7d7496c920545d6826dd7ab17388b9893f152c0289af0001805b4b2e1503d&ascene=0&uin=MTQ3NDUzMzg0Mg%3D%3D&devicetype=iMac+MacBookPro12%2C1+OSX+OSX+10.11.6+build(15G1510)&version=12020810&nettype=WIFI&fontScale=100&pass_ticket=wRifpJK5mrGqYY1WezHUtvKTkM8yktPRfDC7oM2KyYaN5WpHw32mBPIfBN1LiMKJ

###Rx1.0介绍：
  RxJava最核心的两个东西是Observables（被观察者，事件源）和Subscribers（观察者）。Observables发出一系列事件，Subscribers处理这些事件。这里的事件可以是任何你感兴趣的东西（触摸事件，web接口调用返回的数据。。。）
  一个Observable可以发出零个或者多个事件，知道结束或者出错。每发出一个事件，就会调用它的Subscriber的onNext方法，最后调用Subscriber.onNext()或者Subscriber.onError()结束。
  Rxjava的看起来很想设计模式中的观察者模式，但是有一点明显不同，那就是如果一个Observerble没有任何的的Subscriber，那么这个Observable是不会发出任何事件的。



