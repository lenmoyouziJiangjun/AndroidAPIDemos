###Android性能调休
##### 一、资源类性能：（文件I/O，图片资源）
    >>1、核心思想：空间换时间，优化空间，减少主线程I/O(放到子线程)
    >>2、常用优化技巧：
        1、对于配置、常量之类需要多次读取而文件内容又不变的文件。读取一次，保存到内存中。
        2、对于sp文件，将数据存储到一起，最后调用commit。减少调用commit的次数
        3、主线程的IO操作，放到子线程中。
        4、IO操作使用Buffer缓冲区,减少写入次数，例如ByteArrayOutputStream,BufferReader;BufferOutputStream
        5、IO缓冲区Buffer的大小，太小就增加写入次数。太大导致申请buffer的时间太长，反而效率不高。建议使用，2048，4096。java默认大小为8kb
        6、bitmap解码：使用decodeStream代替decodeFile,使用decodeResourceStream代替decodeResource;

