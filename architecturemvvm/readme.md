###google architecture -mvvm 实现

###mvvm介绍：
  M: model
  V: view
  VM: viewModel: viewModel 除了用来处理常用逻辑，还包含一个dataBind的模块，用来处理数据model和View绑定的逻辑

###J8语法：
  1、基础语法：
    ()->{}
  2、方法引用：
    类/对象::方法；//注意这里只是方法名称，没有参数，没有括号

  3、stream语法：
     简单来讲，stream就是JAVA8提供给我们的对于元素集合统一、快速、并行操作的一种方式。
     它能充分运用多核的优势，以及配合lambda表达式、链式结构对集合等进行许多有用的操作。



###SqlBrite:
   Square 公司推出的一个响应式数据库框架。数据发生变化的时候，UI跟着变化