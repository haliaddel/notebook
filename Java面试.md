# Java 基础知识

1. static 用处
   
   1. 修饰变量及方法，被修饰的变量及方法属于类，不属于实例对象，存储在内存中的方法区，方法区是线程共享的空间，里面存储了类信息，常量，静态变量等。
   
   2. 修饰类，只能用于内部类，静态内部类的创建不依赖于外部类的创建， 静态内部类中不能调用外部类的非静态方法和属性
   
   3. 修饰代码块，在类加载进内存的时候会被调用，静态代码块在构造函数之前调用，顺序 ： 静态代码块 ——> 非静态代码块 ——> 构造函数， 静态代码块只运行一次，非静态代码块不是
   
   4. 导包

2. 深拷贝 vs 浅拷贝
   
   > 浅拷贝：针对基本类型进行值传递，对于引用类型数据进行引用传递
   > 深拷贝：针对基本类型进行值传递，对于引用类型，新建一个对象，将属性复制

3. BigDecimal 的用处
   
   《阿里巴巴Java开发手册》中提到：浮点数之间的等值判断，基本数据类型不能用==来比较，包装数据类型不能用 equals 来判断。
   
   使用使用 BigDecimal 来定义浮点数的值，再进行浮点数的运算操作。
   
   在使用BigDecimal时，推荐使用Bigcemial(String) 来构建对象。

4. fail-fast vs fail-safe
   
   **fail-fast**: 在集合迭代的过程中，如果我们对集合进行插入或者删除操作，会抛出异常，这是因为在集合类的源码中，迭代器在初始状态下会将`modcount`变量的值赋值给`expectedModCount`，增删操作会修改modCount，在迭代的过程中会比较`modCount`与`expectedModCount`是否相等，如果不相等，则会抛出异常。
   
   **fail-safe**: 在concurrent包下的集合类中，他们在迭代的时候会copy一个集合，迭代的过程就是这个copy集合的迭代，因此不会产生fail-fast异常。

5. Arrays.asList
   
   > 转换过后，实际上内部维护了一个Array，如果使用list对应的add/remove/clear 会报UnsupportedOperationException
   
   **那么如何正确的将数组转换成list呢**
   
   1. 最简单的方式
      
      ```java
      List<Integer> nums = new ArrayList<>(Arrays.asList(array))
      ```
   
   2. jdk 1.8
      
      ```java
      List<Integer> nums = Arrays.stream(arrays).collect(Collectors.toList())
      ```

6. HashMap 源码解析
   
   HashMap 在1.8之前使用数组+链表的形式来存储键值对， 1.8之后对链表形式进行了改进，在链表长度大于8时，会将链表转换成红黑树。HashMap 初始容量是16。
   
   HashMap链表节点大于8时，会先判断数组容量是否小于64，若不小于，则会选择扩容，不转换红黑树。
   
   **为啥扩容是2的幂次？**
   
   length-1 正好能够生成低位掩码，hash & (length -1)可以确定所在数组的位置
   
   **扩容rehash过程（链表，红黑树原理一致）**
   
   由于扩容后，数组大小为2的幂次，即数组大小<< 1，因此原先链表中的数据存在两种情况，假设原先数组大小为16， 扩容后数组大小为32：
   
   1. 该值的hash从低位起低5位为1， 计算数组index下标为16+oldIndex(原先的桶下标)
   
   2. 该值的hash从低位起低5位为0，计算之后的桶下标保持不变
   
   **扰动函数的作用**
   
   > 1.散列值再松散，只取最后几位存在非常大可能性的碰撞，在java中，int 占32位，将hash的高位与低位做异或运算，一次来增加低位的随机性
   > 2.在我们重写hashCode的时候，如果hashCode分布不均匀，可能存在大量的冲突，扰动函数可以增加hash的随机性
   
   **key 的hash 规则（扰动函数）**
   
   ```java
   static final int hash(Object key){
       int h;
       return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
   }
   ```

7. ConCurrentHashMap
   
   > jdk1.7 ConCurrentHashMap 与hashMap的数组+链表结构类似， 由一段段Segment + HashEntry组成，一个Segment对应一个HashEntry, 在执行更新操作时，需要先获取Segment锁
   > 
   > jdk1.8 取消了Segment，在用CAS跟synchronized来控制并发，synchronized 只锁链表或红黑树的首节点

8. ConCurrentHashMap 与 HashTable的区别
   
   - 数据结构：ConCurrentHashMap在jdk1.8之前，用分段Segment+链表的形式，1.8之后用数组+链表/红黑树， HashTable内部一直采用的是数组+链表
   
   - 加锁方式：ConCurrentHashMap在1.8之前，在Segment上加锁，1.8之后在红黑树根节点加锁，他只会锁相应的桶，而HashTable是全表锁，也就是put的同时不能进行其他的操作。

9. 集合
   
   禁止在foreach中去删除/增加元素，在迭代器调用hasNext()/next()时，会比对modCount是否为expectedModCount，若不是，会直接抛出异常。如果我们在遍历期间插入、删除的话，就会改变modCount。正确的修改方式是通过Iterator来执行遍历和remove操作。
   
   ```java
   while (iterator.hasNext()){
       String item = iterator.next();
       if (item.equals("1")){
           iterator.remove();
       }
   }
   ```

10. HashMap put流程
    
    <img title="" src="http://47.114.144.122/images/2020-10-13-11-34-25-image.png" alt="" data-align="inline">

11. HashMap 非线程安全问题
    
    - 多线程下put会导致记录丢失和覆盖
    
    - 多线程resize阶段会出现死循环的情况
      
      ![](http://47.114.144.122/images\2020-10-13-13-47-35-image.png)
      
      thread1 获取到1->A这个节点，之后时间片用完了，然后第二个线程完成了resize阶段，如下
      
      ![](http://47.114.144.122/images\2020-10-13-13-48-45-image.png)
      
      此时thread1重新获取到cpu，继续执行，thread1持有的已经是更新过后的结果，因此处理完1->A之后，处理2->B，2->B的next又是1->A，就会形成循环。

12. ConcurrentHashMap 源码解析
    
    > jdk 1.7:  ReentrantLock + Segment + HashEntry
    > 
    > jdk1.8： synchronized + CAS + HashEntry + 红黑树
    
    在jdk 1.7 中，ConcurrentHashMap是以Segment（分段锁）数组结构 + HashEntry数组结构组成，其中Segment是一种可重入锁，Segment是一个HashEntry数组，HashEntry是以链表的形式存在的。
    
    1.7中，要定位一个数据需要经过两次hash，第一次确定Segment的下表，第二次确定HashEntry的下标，因此，1.7中hash时间要比普通的长。
    
    ```java
    static final class Segment<K,V> extends ReentrantLock implements Serializable {
    
        private static final long serialVersionUID = 2249069246763182397L;
    
        // 和 HashMap 中的 HashEntry 作用一样，真正存放数据的桶
        transient volatile HashEntry<K,V>[] table;
    
        transient int count;
            // 记得快速失败（fail—fast）么？
        transient int modCount;
            // 大小
        transient int threshold;
            // 负载因子
        final float loadFactor;
    
    }
    ```
    
    **1.7中put过程**
    
    - 首先通过自旋的方式来获取锁，如果获取失败次数达到上线，就表示存在锁竞争
    
    - 改为阻塞锁，找到对应的Segment，然后定位到HashEntry
    
    1.8中舍弃了Segment数据结构，基于CAS操作保证数据的获取及使用synchronized关键字对相应的数据段进行加锁。
    
    1.8把之前的HashEntry改成了Node，但是作用不变，把值和next采用了volatile去修饰，保证了可见性，并且也引入了红黑树，在链表大于一定值的时候会转换。
    
    ```java
    static class Node<K,V> implements Map.Entry<K,V> {
           final int hash;
           final K key;
           volatile V val;  //使用了volatile属性
           volatile Node<K,V> next;  //使用了volatile属性
      ...
    }
    ```
    
    **1.8中put过程**
    
    - 计算hashcode
    
    - 判断是否需要初始化
    
    - 获取对应Node，如果node为null，则尝试通过CAS的方式设置Node
    
    - 如果hashcode == MODED == -1，表示正在扩容
    
    - 如果都不满足，则通过synchronized锁的方式进行写入数据
    
    - 判断是否需要树化
    
    ![](http://47.114.144.122/images\2020-10-13-11-34-25-image.png)

13. 为什么HashTable 和 ConcurrentHashMap的key 和value 不能为空
    
    因为HashTable和ConcurrentHashMap都是基于fail-safe机制的，因此查询的时候可能获取的不是最新的数据，如果返回null的话，不知道是key不存在还是说值本来就是null。

14. Error vs Exception
    
    `Error`和`Exception`都是继承自`Throwable`。
    
    `Error`表示程序出现致命不可恢复的异常，他们大部分都是不可捕获和抛出的。常见的Error类型有`OutOfMemoryError`、`NoClassDefFoundError`、`StackOverflowError`等。
    
    `Exception`表示程序设计出现的异常，他是可捕获和抛出的。`Exception`分为两种，一种是运行时异常，一种是编译时异常。编译时异常是需要在程序中明确处理的，如`ClassNotFoundException`，而运行时异常是通过经验来确定异常种类即是否需要处理，如`NullPointerException`。
    
    `NoClassDefFoundError` vs `ClassNotFoundException`
    
    `NoClassDefFoundError`是编译器加载了Class对象，但是在运行时没有找到，抛出的异常。`ClassNotFoundException`是在调用时发现没有找到该Class文件。

# JVM

1. Java 内存的划分
   
   线程共享内存：
   
   1. 堆内存：存储对象
      
      **在 JDK 1.8中移除整个永久代，取而代之的是一个叫元空间（Metaspace）的区域（永久代使用的是JVM的堆内存空间，而元空间使用的是物理内存，直接受到本机的物理内存限制）。**
   
   2. 方法区：类的class信息、常量池、方法数据、方法代码等
      
      <mark>jdk1.7之后，将运行时常量池从方法区中移除，在堆空间中分配一块常量池内存。</mark>
      
      方法区和**永久代**的关系很像Java中接口和类的关系，类实现了接口，而永久代就是HotSpot虚拟机对虚拟机规范中方法区的一种实现方式。
      
      **HotSpot**虚拟机在**1.8**之后已经**取消了永久代**，改为**元空间**，类的元信息被存储在元空间中。元空间没有使用堆内存，而是与堆不相连的本地内存区域。所以，理论上系统可以使用的内存有多大，元空间就有多大，所以不会出现永久代存在时的内存溢出问题。
   
   3. 直接内存： NIO分配的对外内存
      
      在JDK1.4，引入了**NIO**来解决文件读取的瓶颈，他是<mark>一种基于通道（channel）和缓存区（Buffer）的I/O方式</mark>。它直接调用Native来分配一块堆外内存，通过一个存储在堆空间的**DirectByteBuffer**作为这块内存的引用来进行操作。
   
   线程独有内存：
   
   1. 虚拟机栈：用来记录线程的局部变量表（<mark>基本数据类型及引用的指针地址</mark>）、操作数栈、出口信息、动态链接等。
   
   2. 本地方法栈： 用来记录线程调用的本地Native方法中的局部变量、操作数栈等信息
   
   3. 程序计数器： 用来记录线程执行记录，实现代码中循环、选择、异常处理的流程控制。在多线程情况下，记录程序上一次运行的指令行数，以便接着运行。

2. HotSpot为什么在1.8中取消了永久代
   
   > 元空间直接使用系统内存，不与堆空间连接，只要本地内存足够，就不会发生永久代内存溢出。
   > 
   > 类信息的大小难以掌握，永久代的合适大小难以确定。

3. HotSpot虚拟机对象创建分为五个部分：
   
   > 类加载——内存分配——初始化零值——设置对象头——初始化
   
   1. 内存分配：内存大小在类加载过程中已经确定。
      
      内存分配有两种方式： **指针碰撞**（内存规整）和**空闲列表**（内存不规整）。这两种方式选择取决于内存是否规整，这取决于GC算法是“**标记-清楚**”还是“**标记-整理**”，复制算法内存也是规整的。
      
      **内存分配时的多线程安全问题？**
      
      HotSpot在分配内存时，指定了两种方式来保证线程安全问题：
      
      1. CAS+失败重试
      
      2. TLAB： 在Eden区预先分配一块内存，创建对象先判断TLAB内存能否满足，如果内存不足或者对象太大，再使用CAS+失败重试分配内存。
   
   2. 设置对象头：对象头中存储了对象的GC分代信息、对象的hash值
   
   3. 类加载策略：
      
      **双亲委派机制**：当一个类要加载时，首先会访问他的父加载器，让其判断是否能被加载，如果上层加载器不能加载，才会被当前加载器加载。
      
      这样是为了保证核心API中的类都是由上层加载器加载，不会被篡改。

4. GC堆：GC堆内存分为新生代和老年代。新生代中包含Eden空间、From Survivor和To Survivor。

5. 内存分配策略：
   
   - 新生对象内存在Eden空间分配。当Eden空间不足时会触发Minor GC。
   
   - 大对象直接存在老年代。大对象包括字符串、数组等连续内存空间。这样做是为了防止分配担保机制导致的内存复制。
     
     当对象需要的内存大于-XX：PretenureSizeThreshold参数的值时，对象会直接在老年代分配内存。
   
   - 对象年龄超过15岁转入老年代。
     
     **动态年龄判定**：当进行Minor GC之后，若某个年龄的所有存活对象大小大于survivor空间的一半，年龄大于等于该年龄的可以直接进入老年代。
   
   - 分配担保机制：
     
     当进行Minior GC之前，首先查看老年代连续可用空间是否大于新生代占用总空间。如果大于则没有风险之间进行，否则需要判断连续可用空间是否大于以往的平均minor gc的存活大小，若不满足，则执行FULL GC。
     
     Young GC之后如果成功(Young GC后晋升对象能放入老年代)，则代表担保成功，不用再进行Full GC，提高性能；如果失败，则会出现“<mark>promotion failed</mark>”错误，代表担保失败，需要进行Full GC。

6. JIT技术
   
   JIT（JUST IN TIME 即时编译）。JIT会检测方法的执行次数，当方法超过阈值，就认为是热点代码，针对这块热点代码，JIT会将其翻译成相关的机器码，并执行优化，然后缓存起来下次使用。
   
   JIT针对热点代码除了缓存机器码外，他会执行一系列优化，包括逃逸分析、锁消除、锁膨胀、方法内联、控制检查消除、类型检查消除等。

7. 逃逸分析
   
   逃逸分析，是一种可以有效减少Java 程序中同步负载和内存堆分配压力的跨函数全局数据流分析算法。<mark>通过逃逸分析确定一个对象是否要分配在堆上。</mark>
   
   **方法逃逸**：当一个对象在方法里面被定义后，它可能被外部方法所引用，例如作为调用参数传递到其它方法中。
   
   **线程逃逸**：这个对象甚至可能被其它线程访问到，例如赋值给类变量或可以在其它线程中访问的实例变量
   
   **如果对象不存在逃逸，则可以做如下优化**：
   
   - **栈上分配**。如果一个对象确认不存在逃逸，那么他可以直接在栈上分配内存，会随着方法的结束而自动销毁。
   
   - **同步消除**。不存在逃逸的对象不可能被其他线程访问，因此可以消除对象中的同步操作。
   
   - **标量替换**。标量就是不可分割的量，主要为基本类型和Reference类型。如果将一个对象拆分，其属性都可以变成标量，那么就可以直接在栈空间创建这些变量，而不会去创建一个对象。

8. **JVM在实际应用程序中如何设置合适的大小？**
   
   依据的原则是根据Java Performance里面的推荐公式来进行设置。
   
   | 空间      | 参数                             | 大小                  |
   | ------- | ------------------------------ | ------------------- |
   | heap堆内存 | -Xms -Xmx                      | 通常是FullGC之后老年代的3-4倍 |
   | 永久代     | -XX: PermSize -XX: MaxPermSize | 设置为老年代存活对象的1.2-1.5倍 |
   | 年轻代     | -Xmn                           | 老年代存活对象的1-1.5倍      |
   | 老年代     |                                | 老年代存活对象的2-3倍        |
   
   **如何确认老年代存活对象大小？**
   
   JVM参数中添加GC日志，GC日志中会记录每次FullGC之后各代的内存大小，观察老年代GC之后的空间大小。可观察一段时间内（比如2天）的FullGC之后的内存情况，根据多次的FullGC之后的老年代的空间大小数据来预估FullGC之后老年代的存活对象大小（可根据多次FullGC之后的内存大小取平均值）
   
   指令：
   
   > jmap -head <pid> 查看堆内存使用情况

9. 对象是否死亡？
   
   1. 引用计数法：有地方引用就+1，引用失效就-1。
      
      <mark>弊端：存在循环引用导致对象不能释放。</mark>
   
   2. **可达性分析**：
      
      通过一系列GC roots节点乡下搜索，如果该对象到GC roots之间没有引用链，则认为该节点不可达。
      
      引用：强引用、软引用、弱引用、虚引用
      
      GC过程：不可达对象被第一次标记并且筛选，看finalize方法是否被调用或覆盖，第一次标记过后的对象会放到一个队列中进行第二次标记，如果在此过程中仍然不可达，则进行回收。
   
   3. 废弃常量：没有引用
   
   4. 无用类需要满足三个条件才**可能**被释放：
      
      - 所有实例都被回收
      
      - 加载该类的ClassLoader被回收
      
      - 对应的Class对象没有被引用
   
   5. GC roots: 
      
      1. 所有存活线程中的引用对象
      
      2. 所有由系统ClassLoader加载的对象
      
      3. JNI 方法的local对象

10. <mark>垃圾回收算法</mark>
    
    垃圾回收算法主要分为四种算法：标记-清除、复制算法、标记-整理、分代收集
    
    标记-清除：首先标记出需要回收的对象 ，在标识完之后统一回收所有对象。
    
    复制算法：将内存分为两块，每次只使用一块内存，执行算法时，将还存活的对象复制到另一块空白内存中。
    
    标记-整理：标记出需要回收的对象，将存活的对象移到一端，删除界限之后的可回收对象。
    
    <mark>分代收集（当前虚拟机使用的算法）</mark>：根据不同代的特性选择不同的GC方式。
    
    对于新生代来说，每次GC都会有很多对象需要回收，采用复制算法，可以借助少量的空间来完成复制。针对老年代则可以选择标记-清除或是标记-整理的方式来进行收集。

11. 垃圾收集器
    
    并行 vs 并发 在垃圾回收器中的概念：
    
    并行： 多条垃圾回收线程并行工作，用户线程处于暂停状态
    
    并发：垃圾回收和用户线程并发执行。
    
    - Serial New收集器：单线程收集器，在执行GC时会stop the world。
      
      针对新生代的gc，使用的复制算法
    
    - ParNew收集器：多线程收集器，除了用多线程进行垃圾回收之外，与Serial收集器的原理一致。 stop the world
      
      针对新生代的gc，使用的复制算法
    
    - Parallel Scanvenge 收集器
      
      与ParNew一样是多线程的复制算法的收集器，但是它注重与CPU的使用率，提供了很多参数来让用户选择最合适的停顿时间。
      
      - 设置吞吐量大小的 -XX:GCTimeRatio参数。
      
      - 控制最大垃圾收集停顿时间的-XX:MaxGCPauseMillis参数。
      
      - 自适应策略开关UseAdaptiveSizePolicy参数。
      
      新生代选择复制算法
    
    - Serial Old: Serial收集器的老年代版本，实现算法是标记整理。
    
    - Parallel Old: Parallel收集器的老年代版本，实现算法是标记整理。
    
    - <mark>CMS</mark>：第一个真正意义上的并发收集器。收集线程和用户线程可以同时运行。
      
      CMS是基于标记-清除算法来实现的。
      
      CMS执行GC的过程：
      
      - 初始标记(STW initial mark)
        
        1. 通过gc roots，标记直接引用的对象
        
        2. 标记年轻代引用的老年代对象
      
      - 并发标记(Concurrent marking)
        
        从初始标记的对象出发，标记所有存活的对象。当引用关系发生变化，会将其标记为dirty card，留作后续处理
      
      - 并发预清理(Concurrent precleaning)
        
        在并发标记阶段，用户程序可能改变了对象的引用关系。处理并发标记阶段的dirty card，标记更新过后的对象
      
      - <mark>重新标记(STW remark)</mark>
        
        该阶段的任务是完成老年代所有存活对象的标记。
        
        **该阶段会执行全堆扫描，** 他将扫描young gen，将新生代中引用老年代的对象也作为GC roots 的一部分，这一阶段是整个CMS最耗时的阶段。
        
        `-XX:+CMSScavengeBeforeRemark `该参数可以在重新标记阶段之前执行一次ygc来减少young gen的大小，减少扫描的次数。
      
      - 并发清理(Concurrent sweeping)
      
      - 并发重置(Concurrent reset)
      
      优点：**并发收集，低停顿**
      
      缺点：
      
      - 使用标记-清除算法，空间碎片多
      
      - 对CPU资源敏感
      
      - 处理不了浮动垃圾（浮动垃圾，在并发清除阶段可能产生新的垃圾，这些垃圾被称为浮动垃圾，需要等到下一次GC处理）
      
      **XX:+UseCMSCompactAtFullCollection**参数，应用于在FULL GC后再进行一个碎片整理过程。
      
      **XX:CMSFullGCsBeforeCompaction**,多少次不压缩的full gc后来一次带压缩的。
    
    - G1收集器
      
      一款针对服务器的垃圾回收器，主要对于配备多核CPU及大内存的服务器，满足GC停顿要求的同时满足并发量。
      
      - 初始标记：暂停其他所有线程，记录下与root直接关联的对象
      
      - 并发标记：JVM是使用Remembered Set保存了对象引用的调用信息，在可达性分析的时候只需要同时遍历remembered set就好了，不需要从根节点开始挨个遍历。
      
      - 最终标记：由于并发标记阶段，用户线程仍然在工作，会对标记产生一些偏差，这时候需要通过remembered set log来记录这些改变，在这个阶段将改变合并到remembered set中。完成最终标记。
      
      - 并发清除：通过标记整理的算法，根据用户配置的回收时间，和维护的优先级列表，优先收集价值最大的region。收集阶段是基于标记-整理和复制算法实现。

12. CMS收集器
    
    CMS(Concurrent Mark-Sweep)是以牺牲吞吐量为代价来获得最短回收停顿时间的垃圾回收器。对于要求服务器响应速度的应用上，这种垃圾回收器非常适合。在启动JVM参数加上-XX:+UseConcMarkSweepGC ，这个参数表示对于老年代的回收采用CMS。CMS采用的基础算法是：标记—清除。
    
    CMS中使用了<mark>Card Table</mark>的数据结构，里面记录了老年代到新生代的引用，在YGC中，老年代是不回收的，因此需要对老年代进行扫描作为GC roots。用Card Table可以避免扫描整个老年代。
    
    ![CMS 7ä¸ªé¶æ®µ](https://user-gold-cdn.xitu.io/2019/9/25/16d68b1dc3d0b0ba?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)
    
    - GC 过程
      
      1. 初始标记阶段
         
         标记GC roots 及新生代存活对象的直接引用，STW。
         
         这个过程是支持多线程的（1.8之后可以指定`CMSParallelInitialMarkEnabled`调整）
      
      2. 并发标记阶段
         
         从阶段1标记的对象进行可达性分析。通过卡片标记（Card Marking)，提前把老年代空间逻辑划分为相等大小的区域（Card）,如果引用关系发生变化，就将该区域标记为“脏区”。后续只需扫描这些Dirty Card的对象，避免扫描整个老年代。
      
      3. 并发预清理
         
         通过参数`CMSPrecleaningEnabled`选择关闭该阶段，默认启用，主要做两件事情：
         
         - 处理新生代已经发现的引用，譬如在并发阶段，Eden区中分配了一个对象A，对象A引用了一个老年代B，B之前没有被标记，那么这时就会被标记。
         
         - 重新标记那些在并发阶段引用被更新的对象（晋升到老年代、原本就是老年代的对象）
      
      4. 并发可取消的预清理
         
         在最终标记前尽可能多做一些工作，减少STW时间，在该阶段不断换换处理：标记老年代可达对象、扫描Dirty Card区域中的对象。
         
         循环终止条件：1. 达到循环次数 2.达到循环执行时间阈值 3.新生代内存使用率达到阈值
      
      5. 最终标记
         
         第二次STW，完成老年代中所有存活对象的标记。在此阶段 ：1.遍历新生代对象，重新标记 2. 根据GC roots，重新标记  3.遍历老年代的Dirty Card，重新标记。
      
      6. 并发清除
      
      7. 并发重置
         
         重置CMS算法相关的内部数据。
    
    - 常见问题
      
      1. 最终标记停顿时间过长
         
         可能原因：
         
         CMS停顿时间80%在最终标记阶段，该阶段停留时间过长，常见的原因是新生代对老年代的无效引用，在上个阶段并发可取消预处理阶段中，执行阈值时间内未完成循环，来不及触发YGC。
         
         处理方式：
         
         通过添加参数：-XX:+CMSScavengeBeforeRemark。在执行最终操作之前先触发Young GC。
      
      2. <mark>并发模式失败（concurrent mode failure） & 晋升失败(promotion failed)</mark>
         
         原因：
         
         并发模式失败，当CMS执行回收时，新生代发生垃圾回收，老年代又没有足够的空间容纳晋升的对象时，CMS会退化成单线程的FULL GC。
         
         晋升失败，新生代发生垃圾回收，老年代有足够的空间，但是由于空间的碎片化，导致晋升失败，此时会触发单线程且带有压缩动作的FULL GC。
         
         处理方式：
         
         - 降低触发CMS GC的阈值，即参数`-XX:CMSInitiatingOccupancyFraction`的值，让CMS早点执行
         
         - 增加CMS线程数，即`-XX:ConcGCThreads`
         
         - 增大老年代空间
         
         - 让对象尽量在新生代回收
      
      3. 内存碎片问题
         
         CMS采用标记清除算法，存在大量的空间碎片。以下场景会触发内存碎片压缩：
         
         - 新生代YGC出现新生代晋升担保失败
         
         - 程序主动执行System.gc()
         
         通过参数`CMSFullGCsBeforeCompaction`，设置多少次FULL GC之后触发一次压缩。带压缩的算法为单线程Serial Old算法，STW时间非常长。

13. G1收集器
    
    G1收集器的设计原则是“收集尽可能多的垃圾”，目标是减少处理超大堆的停顿时间。g1不会等到内存耗尽（比如Serial 串行收集器，Parallel并行收集器）或者快耗尽的时候（CMS），才开始收集，而是采用了启发式算法，在老年代中找出最高收益的Region进行收集。
    
    1. G1的内存分配。
       
       G1中也有分代的概念，但是他们在物理地址上不是连续的，G1将内存分成了一个个Region。每个Region都可以被分为年轻代或者老年代。每个Region默认按照512Kb划分成多个Card，所以RSet需要记录的东西应该是 xx Region的 xx Card。
       
       如果对象的内存超过了Region的50%，那么他会被分到`Humongous`内存，他是专门用来存储大对象的。
    
    2. 回收过程中用到的数据结构
       
       - Rset(Remember Set)
         
         <mark>Rset中记录了其他分区对于该分区的引用情况。</mark>G1使用Rset有效避免了在回收阶段对于整个内存堆的扫描，只需要加载Rset来查看对象的被引用情况。
         
         这个RSet其实是一个hash table，key是别的region的起始地址，value是一个集合，里面的元素是card table的index。
         
         Rset中主要记录以下信息：
         
         > 老年代对于新生代的引用
         > 
         > 老年代对于老年代的引用
         
         当在并发标记的阶段引用发生了改变，为了维护这些Rset，G1中采用pre-write barrier和concurrent refinement threads实现了Rset的更新。
         
         分代式G1模式下有两种选定CSet的子模式，分别对应young GC与Mixed GC
         
         > young GC:选定所有young gen里的region
         > 
         > mixed GC:选定所有young gen里的region，外加根据global concurrent marking统计得出高收益的old gen region
         
         可以看出young gen region总是在Cset中的，因此以新生代region为出发点的引用更新不会被Rset记录。
         
         <mark>RSet是怎样辅助GC的？</mark>
         
         在YGC中，以young gen region的RSet作为根集，RSet中记录了所有old -> young的引用，不需要进行老年代扫描。
         
         在Mixed GC中，从young gen region中的RSet，找到直接引用的old，然后从old gen中的RSet找到old -> old 的引用，不需要对整个老年代进行扫描。
       
       - CSet
         
         需要被收集的集合，也就是在垃圾收集暂停过程中被回收的目标。GC时在CSet中的所有存活数据（Live Data）都会被转移，分区释放回空闲分区队列。
       
       - IHOP
         
         InitiatingHeapOccupancyPercent，当老年代空间超整个堆内存的指定比例，就会启动一次Mixed GC。
    
    3. G1的垃圾回收过程
       
       从最高层看，G1的回收器主要是两部分内容：
       
       > 全局并发标记（global concurrent marking）
       > 
       > 拷贝存活对象(evacuation)
       
       **全局并发标记**基于SATB形式的并发标记，主要分为几个阶段：
       
       - 初始标记：STW，从根节点扫描，标记所有从根节点可直接达到的对象并将他们的字段压入扫描栈（marking stack）。
       
       - 并发标记：并发阶段，不断从扫描栈取出引用递归扫描整个堆里面的对象图。每扫描一个对象会对其标记并压入扫描栈。也会扫描SATB write barrier所记录下的引用。
       
       - 最终标记：STW。负责将并发阶段每个线程未处理完的SATB write barrier的引用处理完。
         
         （与CMS不同的是，CMS需要重新扫描mod-union table里面的脏区（dirty card）外加整个根集合，此时整个young gen 都会当成根集合的一部分，因此会非常缓慢）
       
       - 清除。STW，清点和重置标记状态，在这个过程中，不会在堆上sweep实际对象，而是在marking bitmap里统计每个region被标记为活的对象有多少。如果发现一个region中没有存活的对象，会将其加到可回收region列表。
       
       **Evacuation**阶段是全暂停的，他负责将一部分region里存活对象拷贝到region里去，然后回收原来的region空间。
       
       **什么是Evacuation Failure？**
       
       当JVM在GC期间复制对象到Survivor区或者提升对象时，对空间耗尽，堆区域升级失败。这种情况下在`-XX:+PrintGCDetails`将会以TO空间溢出（to-space overflow）的形式表示。
    
    4. **SATB**（snapshot-at-the-begining）
       
       在GC一开始的时候对象图形成快照，即开始时存活的对象就被认为是活的。GC中新分配的对象都认为是活的。其他不可达对象都是死的。
       
       **logging write barrier**
       
       Write barrier是对“**对引用类型字段赋值**”这个动作的环切，也就是说赋值的前后都在barrier覆盖的范畴内。在赋值前的部分的write barrier叫做pre-write barrier，在赋值后的则叫做post-write barrier。
       
       以SATB write barrier为例，每个线程都有一个独立的、定长的SATBMarkQueue。**mutator在barrier里只把old_value压入该队列**，如果队列满了，就会将其添加到全局的SATB队列中，然后给对应的Java线程换一个新的队列。
       
       并发标记会定期检查全局SATB队列集合的大小。当全局集合中队列数量超过一定阈值之后，concurrent marker会处理集合里面的所有队列：吧所有记录的对象都标记上，并将其引用字段压到marking stack上等进一步标记。
       
       跟SATB marking queue一样，每个java线程都有一个dirty card queue，也就是Rset log，然后也有个全局的DirtyCardQueueSet。实际上更新RSet的动作交由多个ConcurrentG1RefineThread并发完成，当达到一定的阈值，就会取出若干队列，更新到对应的Rset中去。
    
    5. CMS VS G1
       
       G1需要暂停拷贝对象，而CMS在暂停中只需要扫描对象。
       
       CMS最大的停顿就在remark阶段，如果在并发标记阶段，程序任然在告诉的创建新的对象，那么remark阶段可能就会有较长的停顿，young gen越大，CMS暂停的时间可能越长。相反，如果分配速率比较温和，G1很难超过CMS。

14. 类加载器
    
    **三个重要的ClassLoader**：
    
    - **BootstrapClassLoader**(启动类加载器)：最顶层的加载器，负责加载`%JAVA_HOME\lib`目录下的jar包和类。或者被-Xbootclasspath参数指定路径的所有类。
    
    - **ExtensionClassLoader**(扩展类加载器)：负责加载目录`%JRE_HOME%/lib/ext`目录下的jar包和类，或被`java.ext.dirs`系统变量所指定路径下的包。
    
    - **APPClassLoader**（应用程序类加载器）：面向用户的类加载器，负责加载当前应用classpath下的所有jar包和类。
    
    **双亲委派模型**：
    
    ![](http://47.114.144.122/images\2020-07-17-14-30-52-image.png)
    
    在类加载过程中，系统首先会判断当前类是否被加载，如果被加载，则直接返回。否则尝试加载。加载的时候首先请求父加载器的`loadClass()`处理，因此所有请求都会经过`BootstrapClassLoader`。当父加载器不能加载时，才会尝试自己来处理。

# 并发

1. 线程 vs 进程
   
   进程：
   
   程序的一次执行过程。在Java中，启动一个main函数就相当与启动了一个JVM进程。
   
   线程：
   
   一个进程在执行的过程中会产生许多线程。同类的多个线程共享内存中的堆和方法区资源，每个线程都有自己独有的程序计数器、本地方法栈及虚拟机栈。

2. 线程的生命周期及状态
   
   | 状态           | 说明                                         |
   | ------------ | ------------------------------------------ |
   | NEW          | 线程刚被创建，没有调用start()方法                       |
   | RUNNABLE     | 线程调用start()，处于就绪队列，等待CPU资源，running状态属于就绪队列 |
   | BLOCKED      | 阻塞状态， 等待获取锁                                |
   | WAITING      | 线程处于等待状态，需要其他线程唤醒                          |
   | TIME_WAITING | 线程睡眠，到达一定的时间之后进入RUNNABLE状态                 |
   | TERMINATED   | 线程运行结束                                     |

3. sleep() vs wait()
   
   > sleep不会释放锁资源，而wait会释放
   > 
   > sleep(long timeout) timeout时间过后线程会自动苏醒，直接调用wait()方法不会苏醒，需要其他线程notify()或者notifyAll()，除非也指定timeout
   > 
   > wait 一般用来线程间调度、通信，sleep一般用于暂停线程

4. 为什么需要调用start()，而不是直接调用run()
   
   start()方法会执行线程就绪之前的准备工作，它会让线程进入就绪状态，等待时间片。直接调用run()方法只是在主线程中调用了一个普通方法，并不会开启一个线程。

5. synchronized关键字
   
   1. 修饰实例方法：获取该实例对象的锁
   
   2. 修饰静态方法：获取该类的锁，会作用于所有该类的实例。由于静态方法属于类，因此，在调用synchronized修饰静态方法的同时，也可以调用synchronized的非静态方法。
   
   3. 修饰代码块：需要指定一个对象，获取该指定对象的锁。

6. 双验证单例模式
   
   ```java
   public Class Singleton{
       private volatile static Singleton singleton;
       
       private Singleton(){}
   
       public Singleton getInstance(){
           if (singleton == null){
               synchronized(Singleton.class){
                   if (singleton == null){
                       singleton = new Singleton();
                   }
               }
           }
           return singleton;
       } 
   }
   ```
   
   JVM拥有<mark>指令重排</mark>的特性，singleton = new Singleton()实际上分成了三步：
   
   1. 分配内存空间
   
   2. 初始化对象
   
   3. 将singleton地址指向分配的内存空间
   
   在JVM中，由于指令重排，可能会发生1->3->2的情况，那么在多线程的情况下，线程A执行了1，3，未执行2的时候，线程B判断singleton不为空，就直接返回，但是该对象还未初始化。<mark>volatile关键词可以禁止JVM指令重排</mark>。

7. synchronized 底层原理
   
   1. synchronized修饰代码块时，class字节码文件中标注了monitorenter，monitorexit，前者表示开启锁，将锁计数器+1，后者释放锁，计数器-1。
   
   2. synchronized修饰方法时，会在方法中标记ACC_SYNCHRONIZED，表明他是同步方法。
   
   在同步的过程中需要获取对象的对象锁，所谓对象锁就是对象头中的`Mark Word`。`Mark Word`里默认存放的对象的HashCode，分代年龄和锁标记位。

8. jdk 1.6 对synchronized锁的优化
   
   锁主要有四种状态： 无锁状态、偏向锁状态、轻量级锁状态、重量级锁状态。锁是随着竞争的激烈程度的增加进行升级，不能降级。对应锁状态Mark Word中的存储形式也不一样。
   
   | 锁状态  | 25bit    | 4bit   | 1bit 是否是偏向锁 | 2bit 锁标记位 |
   |:----:| -------- | ------ | ----------- | --------- |
   | 无锁状态 | hashCode | 对象分代年龄 | 0           | 01        |
   
   ![](http://47.114.144.122/images\2020-09-11-13-45-02-image.png)
   
   - **偏向锁**
     
     锁所属偏向于第一个获取锁的线程，如果在逻辑中没有其他线程来抢锁，则为无锁状态。
     
     当一个线程访问同步块，并获取锁时，会在**对象头**和**栈帧中的锁记录**里存储偏向锁的线程ID，以后该线程访问，不需要CAS加锁和解锁，只需要测试当前Mark Word中的线程是否是当前线程，如果不是，则查看锁标记位是否是1，如果不是则CAS加锁，如果设置了，尝试使用CAS将对象头的偏向锁指向当前的线程。
   
   - **轻量级锁**
     
     与偏向锁一致，都是为了在锁竞争比较小的条件下，简化锁带来的开销，与偏向锁不一样的是，在无竞争的情况下它使用CAS来代替互斥量。
     
     JVM会现在当前线程的栈空间创建用于存储锁记录的空间，然后将对象的Mark Word复制到锁空间中，然后线程尝试通过CAS的方式将对象的Mark Word 设置为锁记录指针。
   
   - **自旋锁**
     
     轻量级锁失败之后，为了防止线程切换，引起linux从用户态切换到内核态，花费较长的时间，让线程执行忙等待，这种不停循环获取锁的方式就是自旋。<mark>自旋为了在锁等待时间较短的情况下，减少线程睡眠、唤醒造成的时间浪费。</mark>
     
     自适应自旋锁：自旋锁的重试次数取上一次同类锁自旋等待的次数。

9. synchronized vs ReenTrantLock
   
   - synchronized 与ReenTrantLock 都是可重入锁。
   
   - synchronized是借助JVM来实现优化的，而ReenTrantLock是通过API来实现的。
   
   - ReenTrantLock有一些高级特性：
     
     - 等待可中断
       
       调用lock.lockInterruptibly，线程可以放弃锁等待执行其他的操作
     
     - <mark>可选择性通知</mark>
       
       线程可以注册在Condition中，通过Condition来实现选择性通知。Condition是绑定在ReenTrantLock中的，一个lock可以绑定不同的Condition，condition通过await()/signal()来实现可选择性通知。
       
       例：读写锁
       
       ```java
       final Lock lock = new ReenTrantLock();
       final Condition readCondition = lock.newCondition();
       final Condition writeCondition = lock.newCondition();
       ```
     
     - 可实现公平锁
       
       ReenTrantLock(boolean fair)可以指定公平锁，synchronized只能是非公平锁。公平锁就是先等待的线程可以优先获取锁。

10. Condition
    
    `Condition`与`Lock`配合用于解决线程之间的等待通知机制，一个Condition可以绑定多个线程，调用`await`方法的线程会加到等待队列，调用`signal/signalAll`方法会将该Condition中的等待队列的头部/全部加到同步队列，但是只有拿到了同步锁，才会从await方法返回。
    
    Condition的底层使用了AQS的原理。
    
    Condition的等待通知机制跟Object的wait和notify/notidyAll的区别
    
    - Condition支持不响应中断，而Object不支持
    
    - Condition支持多个等待队列，Object不支持
    
    - Condition支持超时时间的设置

11. 多线程的可见性及Happens-Before原则
    
    为了解决CPU与内存之间计算的指数级差异，引入了高速缓存作为中间件，需要运算的数据从内存复制到缓存区，让CPU可以快速运算，结束后再将数据从缓存区复制到内存。每个CPU都有自己的高速缓存区。
    
    **高速缓存区带来的弊端：**
    
    - 可见性。两个线程A、B运行在不同的CPU上，此时A线程对共享变量发生的修改，如果没有同步到主内存，那么对于线程B来说修改是不可见的。
    
    - 重排序。两个线程A、B运行在不同的CPU上，有两个共享变量X、Y，如果A先对X修改，再对Y修改，此时Y同步到主存，那么对于线程B来说，Y的修改发生在X之前。
    
    **JVM Happens-Before原则**:
    
    对于两个操作A和B，这两个操作可以在不同的线程中执行。如果A Happens-Before B，那么可以保证A执行完之后，A操作的结果对B操作是可见的。
    
    Happens-Before包括：
    
    - 程序顺序规则
      
      在一个线程内部，按照程序代码的书写顺序，写在前面的先执行。
    
    - 锁定规则
      
      对锁M解锁之前的所有操作`Happens-Before`对锁M加锁之后的所有操作。即同一时刻只能有一个线程执行锁中的操作。
    
    - volatile变量规则
      
      对一个volatile变量的写操作及这个写操作之前所有操作`Happens-Before`对这个变量的读操作及这个读操作之后的所有操作。
      
      ```java
      volatile boolean initialized = false;
      
      configText = readConfigFile(fileName);  //1
      processConfigOptions(configText, configOptions); //2
      initialized = true; //3
      ```
      
      这个规则的意思是，在标记3处执行了对于initialized的写操作，以此为界限，在此标记位之前的1、2之间是可能发生重排序的，但是3不能与1、2发生重排序。写操作完成后，voliate变量本身和在他之前那些操作的结果同步到主内存。
    
    - 线程启动规则
      
      Thread对象的start方法及书写在start方法前面的代码`Happens-Before`该线程的每一个动作。start方法和新线程的动作肯定是不在一个线程中的。
      
      **调用start方法时，会将start方法之前所有操作的结果同步到内存，新线程创建好之后，需要从主内存获取数据。**
    
    - 线程结束规则
      
      线程中的任何操作都`Happens-Before`其他线程检测到该线程已经结束。
      
      两个线程s、t，在s中调用`t.join()`方法，s线程挂起，监听t线程的结束，t线程对于共享变量的修改对于s线程来说是可见的。（线程结束后，数据会同步到内存）
    
    - 中断规则
      
      一个线程在另一个线程上调用`interrupt Happens-Before`被中断线程检测到interrupt方法被调用。线程A执行了Operation A，然后调用了线程B的`interrupt`方法，线程B可以见到Op A对于共享变量的修改。
    
    - 终结器规则
      
      一个对象的构造函数的执行`Happens-Before`他的`finalize()`方法的开始。
    
    - 传递性规则
      
      A Happens-Before B 且 B Happens-Before C， 那么A Happens-Before C。

12. volatile 关键字
    
    每个线程都有一个专用内存，他们把程序的变量从主存（共享内存）中读取，放入专用内存，当线程对改变量发生修改时，其他线程是看不到的。
    
    volatile关键字修饰的变量，线程在读取写入时，会跳过专用内存，直接到共享内存中去获取更新，因此，他可以保证所有线程的可见性。
    
    volatile还可以用来禁止JVM指令重排

13. volatile实现原理
    
    生成汇编代码时会在volatile修饰的共享变量进行写操作的时候会多出**Lock前缀**的指令。
    
    Lock指令的效果主要有两方面：
    
    - 将当前处理器缓存中的数据写回内存
    
    - 其他CPU中该内存地址的缓存数据失效
    
    在多处理器下，各个CPU的缓存会嗅探在总线上传播的数据来更新自身的缓存，如果发现自己缓存地址的数据被修改，就会将其设为无效状态。

14. volatile vs synchronized
    
    - volatile 只能保证可见性，不能保证原子性，synchronized两者都可保证
    
    - volatile只能修饰变量，synchronized可以修饰代码块，方法
    
    - volatile不会发生阻塞，synchronized会发生阻塞
    
    - volatile多用于变量在线程间可见性，synchronized用来实现资源在多线程下的同步

15. ThreadLocal
    
    `Thread`类有个`ThreadLocal.ThreadLocalMap`的实例变量`ThreadLocals`。
    
    ThreadLocal用来存储线程独立变量，每个线程都有一个副本，他们之间是不共享的。
    
    ThreadLocal中用来存储变量的是ThreadLocalMap，他是已ThreadLocal作为key，set的键值对作为value。
    
    ThreadLocalMap中的key是弱引用，Value是强引用，key在GC时会被回收，导致value回收不了，不过get(), set()时会把key为null的值清除。
    
    **`ThreadLocalMap`Hash算法**
    
    ```java
    int i = key.threadLocalHashCode & (n-1)
    ```
    
    `i`就是key在散列表中对应的数组下标。
    
    `threadLocalHashCode`利用斐波那契黄金数，产生比较均匀的hashCode。
    
    **Hash冲突**
    
    开放地址法，会线性往后查找，直到找到一个Entry为null的槽位才会停止查找。
    
    **`ThreadLocalMap.set()`原理**
    
    ThreadLocalMap.set有好几种情况：
    
    - 计算hash对应位置Entry为null,直接插入槽位
    
    - 槽位数据不为空，槽位的key与插入的key相同，更新Entry的值
    
    - 槽位数据不为空，往后遍历，找到Entry为空的槽位之前，没有遇到过期的key，将数据插入槽位
    
    - 槽位Entry不为null，在往后的遍历时遇到key过期的Entry：
      
      在遍历过程中遇到key过期的Entry，此时就会执行`replaceStaleEntry()`，替换过期数据的逻辑。以过期key的下标作为起始点开始进行探测式数据清理工作。
      
      - 以当前的staleSlot开始向前迭代，寻找过期的key，更新staleToExpunge，直到遇到`Entry=null`的槽位才停止迭代。
      
      - 从当前的staleSlot向后遍历,直到Entry=null如果找到了相同的key更新，如果没有找到，开始进行过期Entry的清理工作。然后新建一个Entry插入。
    
    **过期key的探测式清理流程**
    
    遍历散列数据，从`staleToExpunge`开始往后探测清理过期数据，将过期数据Entry设为null。沿途中遇到未过期的数据，则将此数据rehash后重新在table定位，如果对应下标有数据，则查找最近的一个Entry=null将数据插入。
    
    例如一开始某个值hash之后下标为4，4有数据，往后遍历找到index=7处插入，插入后index=5的key被回收，那么在下一次插入操作时，就会触发探测式清理，index=7的数据会rehash到index=5上，这样可以靠近原始hash下标，提高查询效率。

16. AtomicInteger原理
    
    CAS + volatile
    
    <mark>CAS ==> compareAndSwap,它是一条CPU并发原语</mark>

17. AQS(AbstractQueueSynchronizer)
    
    <mark>AQS是一个构建锁和同步器的框架。</mark>ReenTrantLock，Semaphore，ReenTrantReadWriteLock，FutureTask等等都是基于AQS来实现的。
    
    **AQS原理是什么？**
    
    共享资源在被线程占用时会上锁，其他线程获取不到，只有等该线程释放锁才能获取，资源释放时需要进行锁分配，阻塞的线程会被封装成一个LCH节点加到等待锁队列LCH，LCH是一个虚拟的双向队列，他们只存在节点的相互关系。
    
    ![](http://47.114.144.122/images\2020-07-01-10-49-10-image.png)
    
    state 是一个volatile int 变量，队列里面的节点通过CAS的方式来获取state资源。
    
    AQS共享方式
    
    - 独占
      
      - 非公平锁
      
      - 公平锁
    
    - 共享
      
      - Semaphore(允许多个线程访问)、CountDownLatch(程序计数器)、CyclicBarrier(屏障，线程阻塞到达一定的数量才会打开屏障)
    
    **同步队列**
    
    LCH是一个同步双向链表，其中每个节点是AQS中的一个静态内部类对象Node.
    
    > voliate int waitStatus; //节点状态
    > 
    > voliate Node prev; //前驱节点
    > 
    > voliate Node next; //后继节点
    > 
    > voliate Thread thread; //加入同步队列的线程引用
    > 
    > Node nextWaiter;  //等待队列中的下一个节点
    
    节点状态如下：
    
    > int CANCELLED = 1 //节点从同步队列中取消
    > 
    > int SIGNAL = -1 //后继节点处于等待状态，如果当前节点释放同步状态会通知后续节点
    > 
    > int CONDITION = -2 //当前节点进入等待队列
    > 
    > int PROPAGATE = -3 //下一次共享式同步状态获取将会无条件传播下去
    
    **独占锁获取同步状态**
    
    <img title="" src="http://47.114.144.122/images/2020-09-10-13-57-50-image.png" alt="" width="691">
    
    **独占锁的释放**
    
    会将当前线程从队列中删除，并通过unpark方法唤醒下一个节点引用的线程，因此，LCH是个FIFO的队列。

18. 乐观锁与悲观锁
    
    乐观锁不会对资源加锁，通过记录读取时候的值，在更新时比较原先读取的值与现在的值是否一致来达到类似锁的功效。它适合<mark>多读少写</mark>的场景。悲观锁就是我们常用的synchronized及ReentrantLock。

19. 乐观锁的两种实现方式：
    
    CAS算法：compare and swap
    
    版本号机制：在数据表中加一个版本号字段

20. 乐观锁问题
    
    1. ABA问题：
       
       CAS算法中，如果更改一个链表，读取时，链首为A，现将A删除，插入B，在插入A，此时原先程序原先读取的链表头还是A，会认为状态没有发生变化，其实已经被更改过了。
    
    2. 循环时间长：自旋CAS如果不成功就一直循环到成功为止，如果长时间不成功会给CPU带来很大负担
    
    3. 只能保证一个共享变量的原子操作。

21. **线程池Thread Pool**
    
    1. 四种常见的线程池
    
    | 线程池名称                  | 核心线程大小 | 最大线程大小  | 阻塞队列                | 适用场景                             | 存在的问题点              |
    | ---------------------- | ------ | ------- | ------------------- | -------------------------------- | ------------------- |
    | newFixedThreadPool     | 固定     | 与核心线程一样 | LinkedBlockingQueue | CPU密集型，任务计算时间比较长，尽可能分配少的线程       | 未限制阻塞队列的大小，容易发生OOM。 |
    | newCachedThreadPool    | 0      | 无限制     | SynchronousQueue    | 过来一个任务就会创建一个线程，频繁的创建线程对CPU的影响非常大 | 为限制线程大小数量           |
    | newSingleThreadPool    | 1      | 1       | LinkedBlockingQueue | 只有一个线程在运行，适合串行执行任务               | 未限制阻塞队列的大小，容易发生OOM。 |
    | newScheduledThreadPool | 指定     | 无限制     | DelayQueue          | 周期性的执行任务，任务执行完会放回延迟队列            | 为限制线程大小数量           |
    
    2. 核心参数：
       
       **corePoolSize**: 核心线程数
       
       **maximunPoolSize**: 最大线程数
       
       **keepAliveTime**： 非核心线程空闲存活时间，默认60s
       
       **unit**：非核心线程空闲时间单位
       
       **workQueue**：阻塞队列
       
       **threadFactory**：创建线程的工厂
       
       **handler**：拒绝策略
    
    3. 任务提交的流程
    
    ![](http://47.114.144.122/images\2020-09-04-09-04-37-image.png)
    
    4. **拒绝策略：**
       
       **AbortPolicy**: 直接抛出异常
       
       **DiscardPolicy**: 直接抛弃
       
       **DiscardOldestPolicy**: 抛弃最早的
       
       **CallerRunsPolicy**: 由调用线程池的线程执行
    
    5. **线程池异常处理**
       
       - 在任务代码中try/catch
       
       - 利用submit获取future对象，通过`future.get()`获取异常
       
       - 自定义factory工厂，为工作线程设置`UncaughtExceptionHandler`。
       
       ```java
       ExecutorService threadPool = Executors.newFixedThreadPool(1, r -> {
            Thread t = new Thread(r);
            t.setUncaughtExceptionHandler(
                 (t1, e) -> {
                      System.out.println(t1.getName() + "线程抛出的异常"+e);
                 });
            return t;
       });
       threadPool.execute(()->{
           Object object = null;
           System.out.print("result## " + object.toString());
       });
       ```
       
       - 重写`ThreadPoolExecutor`的`afterExecute`方法。
         
         ```java
         class CustomThreadPoolExecutor extends ThreadPoolExecutor { 
         
             public CustomThreadPoolExecutor() { 
              super(1,10,60,TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(1000)); 
             } 
         
             protected void afterExecute(Runnable r, Throwable t) { 
             super.afterExecute(r, t); 
             if (t == null && r instanceof Future<?>) { 
              try { 
              Object result = ((Future<?>) r).get(); 
              System.out.println(result); 
              } catch (CancellationException ce) { 
               t = ce; 
              } catch (ExecutionException ee) { 
               t = ee.getCause(); 
              } catch (InterruptedException ie) { 
               Thread.currentThread().interrupt(); // ignore/reset 
              } 
             } 
             if (t != null) 
              t.printStackTrace(); 
             } 
         } 
         public class CustomThreadPoolExecutorDemo{ 
         
               public static void main(String args[]){ 
                System.out.println("creating service"); 
                //ExecutorService service = Executors.newFixedThreadPool(10); 
                CustomThreadPoolExecutor service = new CustomThreadPoolExecutor(); 
                service.submit(new Runnable(){ 
                  public void run(){ 
                   int a=4, b = 0; 
                   System.out.println("a and b="+a+":"+b); 
                   System.out.println("a/b:"+(a/b)); 
                   System.out.println("Thread Name in Runnable after divide by zero:"+Thread.currentThread().getName()); 
                  } 
                }); 
                service.shutdown(); 
               } 
           } 
         ```
         
         ```
         
         ```
    
    6. **线程池都有哪几种工作队列？**
    - ArrayBlockingQueue
      
      用数组实现的有界队列，遵从FIFO原则。
    
    - LinkedBlockingQueue
      
      基于链表形式的阻塞队列，如果不指定队列大小，默认是`Integer.MAX_VALUE`
    
    - DelayQueue
      
      DelayQueue（延迟队列）是一个任务定时周期的延迟执行的队列。根据指定的执行时间从小到大排序，否则根据插入到队列的先后排序。
    
    - PriorityBlockingQueue
      
      根据优先级大小排列，无界阻塞队列。
    
    - SynchronousQueue
      
      一个不存储元素的阻塞队列，每个插入操作必须等到另一个线程调用移除操作，否则插入操作一直处于阻塞状态，吞吐量通常要高于LinkedBlockingQuene，newCachedThreadPool线程池使用了这个队列。
    7. **如何设置合适的自定义线程大小？** 
       线程池的大小主要取决与执行的任务的种类，主要分为两种，一种是**CPU密集型任务**，即需要较长计算时间的任务，第二种是**IO密集型任务**，即存在大量的阻塞时间，需要多线程来提高效率。
       
       - CPU密集型任务 
         该情况需要多核CPU才能发挥效果，线程数要尽量少，减少线程切换带来内核态转换为用户态的开销。<mark>合适的线程数为(CPU核数+1)个线程的线程池。</mark>
       
       - IO密集型任务
          该情况有两种公式：
          线程数 = CPU数 * 2
          线程数 = CPU数 / (1-阻塞系数) （0.8-0.9之间）
         
         **阻塞系数**：花在系统IO上的时间与CPU密集任务所耗的时间比值。
         
         比如：8核 / (1 – 0.9) = 80个线程数

## IO

1. IO特性：同步与异步
   
   当你同步执行某项任务时，你必须等到该任务完成才能继续其他任务。当你执行异步操作时，则不需要等待。

2. IO特性：阻塞和非阻塞
   
   阻塞：当你发起一个请求时，需要等待请求返回结果，当前线程会被挂起，无法进行其他任务操作
   
   非阻塞：当你发起一个请求，无需等待请求结果，可以继续执行其他任务。
   
   同步与异步是从任务执行的方式来描述的，阻塞和非阻塞是从调用结果的状态来描述的。

3. BIO
   
   传统阻塞同步IO，数据读取写入阻塞在一个线程里。
   
   缺点：JVM的线程资源是非常宝贵的，线程的创建和销毁成本很高。当客户端并发量过多会导致线程堆栈溢出，线程创建失败等问题。
   
   伪异步IO
   
   利用线程池来达到复用线程的目的，但底层还是BIO

4. NIO
   
   同步非阻塞IO。他支持面向缓冲的，基于通道的I/O操作方法。NIO提供了与BIO中的`Socket`和`ServerSocket`类似的`SocketChannel`和`ServerSocketChannel`两种套接字通道，他们都支持阻塞和非阻塞两种模式。

5. NIO与IO的区别
   
   - IO流是阻塞的，NIO是非阻塞的
   
   - Buffer（缓存区）
     
     <mark>IO面向流，NIO面向缓存区</mark>
     
     NIO在库中加了Buffer对象，数据直接从Buffer中读取或写入。I/O中虽然也有个Buffer的扩展类，但他只是流的包装类，实际是从流中写入到缓冲区。
   
   - Channel（通道）
     
     通道是双向的，流是单向的。通道直接与Buffer交互，因此通道可以<mark>异步读写</mark>。
   
   - Selector（选择器）
     
     NIO有选择器，IO没有
     
     选择器是用单个线程来处理多个Channel。

6. AIO（Asynchronous I/O）
   
   异步非阻塞IO模型。异步IO是基于事件和回调机制实现的，应用操作后会直接返回，不会阻塞，当事件处理完毕，会通知相应的线程执行后续的操作。

7. <mark>linux的5种IO模型</mark>
   
   - 阻塞式IO
   
   - 非阻塞式IO
     
     通过轮询的方式，不停的去问内核数据有没有准备好。若某一次数据准备好了，就复制到内存空间
   
   - 复用IO
     
     多个进程IO注册到同一个通道，这个通道会统一和内核打交道，数据准备好后，进程再把数据考到用户空间。
   
   - 信号驱动IO
     
     应用进程预先向内核注册一个信号处理函数，然后直接返回，等到数据准备完毕是，通知用户进程，用户进程吧数据拷到用户空间
   
   - 异步IO
     
     用户进程将IO操作提交给内核之后，由内核进行**数据报准备**和**数据复制**，复制完之后通知用户进程。

## Java 8

1. 接口默认方法（虚拟扩展方法）
   
   通过`default`关键字接口添加非抽象方法。
   
   ```java
   interface Formula{
       double calculate(int a);
   
       default double sqrt(int a){
           return Math.sqrt(a);
       }
   }
   ```

2. lambda表达式
   
   ```java
   Collections.sort(names, (String a, String b) -> b.compareTo(a));
   ```
   
       //若只有一行函数，则可以更加简单
   
       names.sort((a,b) -> b.compareTo(a)); 

3. 函数式接口
   
   函数式接口指的是仅仅只包含一个抽象方法，但可以有多个非抽象方法的接口。
   
   示例：
   
   ```java
   @FunctionalInterface
   public interface Converter<F, T>{
       T convert(F from);
   }
   ```
   
   ```java
   Converter<String, Integer> converter = (from) -> Integer.valueOf(from);
   Integer converted = converter.convert("123");
   ```

4. 方法和构造函数引用
   
   java8允许通过`::`关键词传递方法或构造函数的引用。
   
   ```java
   //静态或普通方法
   Converter<String, Integer> converter = Integer::valueOf;
   //构造方法
   PersonFactory<Person> personFactory = Person::new;
   ```

5. Lambda作用域
   
   - 访问局部变量
     
     虽然外部的局部变量可以不用像匿名内部类调用一样声明为final类型，但是其内容不能发生修改，具有隐性final的语义。
   
   - 访问字段和静态变量
     
     lambda表达式可以访问实例字段和静态变量。
   
   - 无法访问`default`修饰的默认接口

6. Stream（流）
   
   - Filter过滤
     
     过滤是通过一个predicate接口来过滤并保留符合条件的元素，他是一个中间操作，后面可以接着Stream的其他操作，譬如foreach。
   
   - Sorted排序
     
     中间操作，如不指定自定义Comparator，则使用默认排序。
   
   - Map映射
     
     中间操作map会将元素根据指定的function转为另外的对象。
     
     ```java
     stringList.stream()
     .map(String::toUpperCase)
     .sorted((a,b) -> b.compareTo(a))
     .foreach(System.out::println);
     ```
   
   - Match匹配
     
     Stream提供了多种匹配操作，所有匹配都是**最终操作**。
     
     ```java
     boolean anyStartsWithA = stringList.stream()
         .anyMatch((s) -> s.startWith("a"));
     boolean allStartsWithA = stringList.stream()
         .allMatch((s) -> s.startWith("a"));
     boolean noneStartsWithZ = stringList.stream()
         .noneMatch((s) -> s.startWith("z"));
     ```
   
   - Count计数
     
     最终操作， 返回Stream中元素个数， 返回值类型是long
   
   - Reduce规约
     
     最终操作，多个元素规约为一个函数。规约后的结果是用过Optional接口表示的。
     
     ```java
     Optional<String> reduced = stringList.stream()
     .sorted()
     .reduced((s1, s2) -> s1+"#"+s2);
     ```
     
     //concat
     String concat = Stream.of("A","B","C","D").reduce("", String::concat);
     //最小值
     double minvalue = Stream.of(-1.5, 1.0, -3.0, -2.0)
     .reduce(Double.MAX_VALUE, Double::min);
     //求和 有起始值
     int sum = Stream.of(1,2,3,4).reduce(0, Integer::sum);
     
     //无起始值
     sum = Stream.of(1,2,3,4).reduce(Integer::sum).get();
     
     ```
     
     ```

7. Parallel Stream 并行流
   
   Stream分为串行和并行两种，串行stream操作在一个线程里，并行在多个线程同时执行。
   
   并行与串行的区别就是将`stream()`改为`parallelStream()`。

### java 9+

1. java 10 var
   
   java 10引入了局部变量折断var用于局部变量声明。
   
   如`var user = new ArrayList<User>()`
   
   原理：在处理`var`时，编译器先查看表达式右边部分，就是**构造器**，将其作为变量类型。

2. java 11 Http client 升级
   
   java 11中，Http Client 的包名由`jdk.incubator.http`改为`java.net.http`，该API通过CompleteableFuture提供非阻塞请求和响应语义。
   
   ```java
   var request = HttpRequest.newBuilder()
   .uri(URL.create("https://javastack.cn"))
   .GET()
   .build();
   var client = HttpClient.newHttpClient();
   //同步
   HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
   System.out.println(response.body());
   //异步
   client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
   .thenApply(HttpResponse::body)
   .thenAccept(System.out::println);
   ```

## 网络层

### 网络协议：

OSI七层协议：

物理层 -> 数据链路层 -> 网络层 -> 运输层 -> 会话层 -> 表示层 -> 应用层

TCP/IP体系：

网络接口层 -> 网际层IP -> 运输层（TCP/UDP） -> 应用层（TELNET,FTP,SMTP）

五层协议：

物理层 -> 数据链路层 -> 网络层 -> 运输层 -> 应用层

1. 应用层
   
   应用层就是进程之间的交互。
   
   应用层协议有很多，比如DNS域名解析服务， Http协议，邮件协议SMTP等等。

2. 运输层
   
   运输层的主要任务就是负责向两台主机进程之间的通信提供数据传输服务。
   
   运输层协议：
   
   TCP：面向链接的，可靠的数据传输服务。
   
   UDP： 面向报文的，提供无连接的，最大可能保证数据传输服务
   
   全双工 vs 半双工
   
   全双工就是在发送数据的同时，可以可接受数据。半双工则不行，同一时间只能执行一个操作。

3. 网络层
   
   数据运输过程中，需要经过不同的通讯子网。网络层的任务就是选择合适的中间路由和交换节点，保证数据及时传输。

4. 数据链路层
   
   两台主机之间的数据传输，总是在一段段的链路上传送的，这就需要专门的链路层协议。**数据链路层将网络层传下来的IP数据报组装成帧。**

5. 物理层
   
   实现相邻计算机节点之间的比特流的透明传送。

### TCP三次握手四次挥手

1. 三次握手
   
   ![](http://47.114.144.122/images\2020-07-20-15-25-29-image.png)
   
   步骤：
   
   1. 客户端发送带有SYN的数据包
   
   2. 服务端收到数据包，发送SYN/ACK的数据包
   
   3. 客户端收到服务端SYN/ACK包，发送ACK标志的数据包
   
   **为什么需要三次握手？**
   
   三次握手服务端，客户端验证了都验证了自身的发送及接受能力都是正常的，表示一条正常的数据链路。
   
   第一次握手：
   
   客户端自己什么都确认不了， 
   
   服务端确认：客户端发送正常，服务端接受正常
   
   第二次握手：
   
   客户端确认：客户端发送正常，接收正常，服务端：发送正常、接受正常
   
   服务端确认：客户端发送正常，服务端接受正常
   
   第三次握手：
   
   客户端、服务端都确认自己及对方的收发能力正常。
   
   **第二次握手传回了ACK，为什么还需要SYN？**
   
   接收端发送ACK是为了告诉客户端，自己接收到你发送的信息了，SYN则是为了建立并确认从服务端到客户端的通信，SYN是让客户端知道服务端收到的消息就是客户端发送的。

2. 四次挥手
   
   ![](http://47.114.144.122/images\2020-07-20-15-43-21-image.png)
   
   步骤：
   
   1. 客户端发送FIN信号，标记断开链接
   
   2. 服务端收到FIN信号，并发送ACK信号，确认序号为收到的序号+1
   
   3. 服务端发送FIN信号
   
   4. 客户端收到FIN信号，并发送ACK信号，确认序号为收到的序号+1
   
   **为什么要四次挥手？**
   
   通信双方在数据传送结束后发出连接释放通知，待对方确认后进入半关闭状态，等到对方数据发送完毕之后，对方发送释放通知，确认之后就完全关闭连接。
   
   就如通话双方，当一方话说晚了不能要求对方也挂电话，等到对方也表示话说完了，才算通话结束。

### TCP VS UDP

| 类型  | 面向连接 | 传输可靠性 | 传输形式  | 传输效率 | 所需资源 | 应用场景           |
| --- | ---- | ----- | ----- | ---- | ---- | -------------- |
| TCP | 是    | 可靠    | 字节流   | 慢    | 多    | 要求通信可靠（文件、邮件等） |
| UDP | 否    | 不可靠   | 数据报文段 | 快    | 少    | 要求通信速度高（域名转换）  |

UDP在数据传输之前不需要建立数据链路。虽然UDP不提供可靠性交付，但是在即时通信领域，比如QQ语音，QQ视频，直播等非常高效。

TCP提供面向连接的可靠性服务。他会有三次握手，四次挥手来建立连接。在数据传输时，有确认、窗口、重传、拥塞控制机制。

### TCP协议的可靠性传输

1. TCP给发送的每一个包进行编号，接收方对数据包进行排序，吧有序数据传给应用层。

2. **校验和**： TCP将保持它首部和数据的检验和。如果接收端检测检验和有差错，则丢弃和不确认收到次报文

3. TCP端会丢弃重复的数据。

4. **流量控制**：TCP发送和接受都有固定大小的缓存，如果接受端数据来不及处理发送端发来的数据，则会通知发送端放慢发送速度，防止包丢失。
   
   TCP使用的流量控制的协议是<mark>滑动窗口协议</mark>。
   
   <mark>滑动窗口协议：</mark>
   
   TCP将数据包分段并标记排序，他利用了一个窗口来标记正在发送的所有报文，在窗口之前是是已经发送并且收到确认的消息，在窗口之后的是不能发送的消息，窗口中的报文分为两种，一种是已发送但没有收到ACK，一种是可发送但还未发送的。当窗口第一个元素收到确认，窗口就会向后滑一格。
   
   **流量控制的具体做法**
   
   当发送方发现接收方的接收窗口为0，则停止发送报文，启动定时器，在定时器时间到了之后去查看接收方的接收窗口是否为零，若是，则重置定时器，若不是，则继续发送。

5. **拥塞控制**：
   
   当网络拥塞时，减少数据的发送。

6. **ARQ协议**：每发完一个分组就停止发送，等待确认，收到确认后再发下一个分组。

7. 超时重传：当一个TCP段发出后，会启动一个定时器，等待确认消息，如果定时器到时了，未收到确认，将会重发这个报文段。

### ARQ协议

自动重传请求是数据链路层和传输层的错误纠正协议之一。他使用**超时**和**重传**两个机制来保证信息的可靠传输。

1. 停止等待ARQ协议
   
   停止等待协议是TCP每发送一个分组（一个报文段）后就停止发送，等到收到该分组的确认之后才继续发送。如果过了超时时间，没有收到ACK确认就会重新发送。
   
   缺点：信道利用率低，等待时间长
   
   **确认丢失**：确认消息在传输过程中丢失。A发送消息M1， B收到消息，发送确认，确认半途丢失，A超时之后重新发送，B收到重复消息会将数据丢弃，并重新发送确认。
   
   **确认迟到**：确认消息在传输过程中迟到。A发送消息M1，B收到发送确认，A在重试时间内未收到确认，重新发送消息，B收到消息，丢弃，重新发送确认，A收到第二次确认并继续。A之后又收到B的第一次确认，确认消息丢弃。

2. 连续ARQ协议
   
   凡位于窗口的分组都可以连续发出，而不需要对方确认。接收方一般采用累计确认，对<mark>按序</mark>(不按序的一个个发送确认)到达的最后一个分组发送确认，表明这个窗口的所有分组都已经正确收到。
   
   Go-back-N
   
   如果发送端发送了5个消息，中间三个分组丢失了，接收端只能对前两个发送确认，发送端需要对后面三个分组进行重传。

### 拥塞控制

若对网络中某一资源的需求超过该资源所能提供的可用部分，网络就会拥塞。拥塞控制就是防止过多的数据注入到网络中，防止过载。

为了进行拥塞控制，TCP发送方要维持一个拥塞窗口的状态变量。发送方让自己的发送窗口取为拥塞窗口和接收窗口中较小的一个。

1. 控制机制
   
   发送发维护一个拥塞窗口cwnd的状态变量，其值取决于网络的拥塞程度，并动态变化
   
   - cwdn维护原则：当没有发生网络拥塞时，拥塞窗口就再增大一些，只要网络出现拥塞，cwdn就减少一些。
   
   - 拥塞判断条件：没有按时收到应该到达的确认报文，即<mark>发生重传</mark>。
   
   发送方将拥塞窗口作为发送窗口，即swdn = cwdn
   
   维护一个慢开始门限ssthresh状态变量：
   
   - 当cwdn < ssthresh时，使用慢开始算法。
   
   - 当cwdn > ssthresh时，使用拥塞避免算法
   
   - 当cwdn = ssthresh时，既可使用满开始算法，也可使用拥塞避免算法。

2. TCP拥塞采取了四种算法：**慢开始、拥塞避免、快重传和快恢复**。
   
   传输轮次：发送方给接收方发送数据报文段后，发送方收到相应的确认，一个传输轮次所使用的时间就是往返时间RRT。
   
   **慢开始**：从小到大的增大拥塞窗口数值，cwnd初始值为1，每经过一个传播轮次就翻倍。
   
   **避免拥塞**：缓慢增加拥塞窗口cwdn， 每经过一个往返时间RTT就把发送方的cwnd+1。
   
   ![](http://47.114.144.122/images\2020-07-21-10-26-17-image.png)
   
   **当发生超时重传时，判断网络可能出现拥塞：**
   
   1. 将ssthresh的值更新为发生拥塞时cwdn的一半
   
   2. 将cwdn重置为1，并重新开始慢开始算法
   
   当某个数据段网络丢失了，TCP就会误认为发生网络阻塞了，cwdn就会被重置为1，被迫执行慢开始算法，这回影响传输效率。
   
   **快重传与快恢复**：
   
   1. 所谓快重传就是不需要进行超时等待再重传，可以提前知道数据丢失
      
      - 接收方不要等待自己发送数据时才进行稍待确认，而是立即发送确认。
      
      - 接收方即使收到失序的报文，也要立即对已收到的报文段发送<mark>重复确认</mark>（譬如收到2号报文段，之后没收到3号，后面收到4，5，6时，需要发送对2号的重复确认）
      
      - 发送方一旦收到三条重复确认，就将相应的报文立即重传。
   
   2. 发送方收到三条重复的确认之后，就知道某个报文被丢弃了，就会执行快恢复算法：
      
      - 发送方将满开始门限ssthresh和cwdn设置为当前窗口的一半，开始执行拥塞避免算法。
      
      - 也有的快恢复实现是吧快恢复开始时的cwdn再增大一些，相当于新的ssthresh+3（三条报文已经存在于接收端的缓存中，而不需要再占用网络资源）

### 从url请求到响应返回

1. DNS域名解析，获取IP

2. 建立TCP链接

3. 发送HTTP请求

4. 服务器响应，返回Http报文

5. 浏览器解析渲染页面

6. 关闭连接

**DNS域名解析服务**

首先会在本地的域名服务器中去查找是否解析过该域名，否则去根域名服务器（.）查找，若不存在，则去com顶级域名服务器查找，类推下去，知道找到域名对应ip缓存到本地。解析流程`.->com->google.com->www.google.com`。

DNS中既使用了TCP，也使用了UDP。TCP用于区域传输，其他时候用UDP。

DNS中有两种DNS，一种主DNS,一种辅助DNS。主DNS从本机获取DNS数据，从DNS需要从区的主DNS中获取数据信息，这就是区域传输。

**Https vs http**

http默认端口80，https默认端口443。http中传输内容是明文，客户端，服务端都无法验证对方的身份。Https是建立在SSL/TLS之上的http协议。所有传输内容采都经过加密，加密使用对称加密，但对称加密的密钥用服务器方的证书进行了非对称加密。

## 操作系统

1. 什么是操作系统
   
   操作系统是管理机器硬件及软件资源的程序，为用户提供与硬件体系交互的操作界面。
   
   操作系统分为内核与外壳。我们可以把外壳理解成围绕内核的应用程序，内核就是能操作硬件的程序。
   
   > 内核负责管理系统的进程、内存、设备驱动程序、文件和网络系统等等，决定着系统的性能和稳定性。是连接应用程序和硬件的桥梁。

2. 什么是系统调用？
   
   首先了解一下用户态和系统态：
   
   **用户态**：以用户态运行的进程能够直接读取用户程序的数据。
   
   **系统态**：系统态运行的进程或程序几乎可以访问计算机的任何资源。
   
   **什么是系统调用？**
   
   我们在用户态跑的程序，凡是与系统态资源有关的操作，都需要通过操作（如文件管理、进程控制、内存管理等），都必须通过系统调用的方式向操作系统发送请求，由操作系统代为完成。
   
   这些系统调用分为以下几类：
   
   - 文件管理。完成文件的读、写、创建及删除等功能。
   
   - 进程控制。完成进程的创建、撤销、阻塞及唤醒等功能。
   
   - 进程通讯。完成进程之间的消息传递、信号传递等功能。
   
   - 内存管理。完成内存分配、回收等功能。

3. 进程间通讯 IPC
   
   每个进程都有不同的用户地址空间，任何一个进程的全局变量在另一个进程中是不可见的。所有数据交换需要经过内核，内核会分配一块缓冲区，A进程将数据拷到内核，B进程从内核中取数据。
   
   **进程间通讯的7种方式：**
   
   1. 管道/匿名管道（pipe）FIFO
      
      管道是单向的，如果需要双向通信则需要建立两个管道。
      
      只能用于父子进程或者兄弟进程之间
      
      向一个缓存队列，一头读取，一头添加
      
      <mark>只存在于内存中的文件。</mark>
   
   2. 有名管道（FIFO）
      
      <mark>已有名管道的文件形式存在于文件系统</mark>，这样即便没有血缘关系，也可以使用管道通信。
   
   3. 信号
      
      信号可以发给某一个进程而不需要知道进程的状态，如果进程未执行，则会在内核中保存，等到进程恢复时再发送。
      
      信号是软件层面上对于中断的一种模拟，是一种异步通讯方式。信号可以在用户进程和内核之间直接交互，内核可以通过信号通知用户进程发生了那些系统事件。
      
      **信号生命周期和处理流程**
      
      - 信号被某个进程产生，并设置需要传递给的进程id，然后交给操作系统
      
      - 操作系统根据目标进程对于信号的设置（阻塞/非阻塞），如阻塞就等到目标进程解除阻塞再传递
      
      - 目标进程收到信号之后，会保存正在执行的上下文，暂停但钱执行的任务，处理完信号之后再回到中断位置。
   
   4. 消息（Message）队列
      
      <mark>存放在内核的消息链表。</mark>只有在内核重启时或者显示地删除消息队列时，才会真正删除。
   
   5. 共享内存
      
      多个进程读写同一块内存空间，是最快的可用IPC形式。
   
   6. 信号量
      
      信号量是一个计数器，用于多进程对共享数据的访问，信号量的意图在于进程间同步。<mark>对信号量的操作，主要分为P操作和V操作</mark>
      
      - P操作：先检查信号量的大小，若值大于0，则将信号量减1，同时进程获得共享资源的访问权限。若发现信号量小于等于0，则进程阻塞
      
      - V操作：将信号量大小+1，若有进程处于阻塞状态，则其中一个将被唤醒。
      
      **互斥量与信号量的区别**
      
      - 互斥量用于线程互斥，信号量用于线程同步。
      
      - 互斥表示一个资源同一时间只允许一个访问者访问，同步指进程之间对于资源的**有序访问**。
      
      - 互斥量只能是0/1，信号量是非负整数。
      
      - 互斥量加锁和解锁必须由同一县城分别使用，信号量可以由一个线程释放，另一个县城得到。
   
   7. 套接字

4. 线程的同步方式
   
   - 互斥量
   
   - 信号量
   
   - 事件。通过wait/nofity

5. 进程调度算法
   
   - 先到先服务调度算法（FCFS）：从就绪队列中选择最先进入队列的进程分配资源。
   
   - 短作业有限的调度算法（SJF）：选择一个估计时间最短的进程分配资源。
   
   - 时间片轮询调度算法：每个进程分配一个时间段，该进程运行一个时间段后退出CPU。
   
   - 多级反馈队列调度算法：既能使高优先级的进程得到运行也能使短作业快速完成。
   
   - 优先级调度：首先执行最高优先级的进程。

6. 内存管理机制
   
   简单分为连续内存分配管理方式和非连续分配管理方式两种。连续内存分配就是块式管理，不连续的分配方式是页式管理、段式管理以及段页式管理。
   
   - 块式管理：将内存分为几个几个块，当程序需要内存时，就将空闲的内存块分配给他，缺点是空间利用率低，存在很多内存碎片。
   
   - 页式管理：将内存划分为一页一页的形式，他的大小比块的内存要小，提高内存利用率。通过页表对应的逻辑地址和物理地址来管理。
   
   - 段式管理：页式管理中的页没有实际的意义，段式管理将内存划分为一段段，每段的大小比页还要小，而且段都定义了一组逻辑信息。如主程序段，子程序段，数据段等。通过短表对应的逻辑地址和物理地址来管理。
   
   - 段页式管理：将主存划分成一个个段，然后将段内存划分成若干页。

7. 

### Spring

1. 重要的Spring模块
   
   - Spring Core：主要提供依赖注入
   
   - Spring Aspects：与AspectJ的集成提供支持
   
   - Spring Aop：面向切面
   
   - Spring JDBC：数据库
   
   - Spring JMS：Java消息服务
   
   - Spring ORM：支持Hibernate等ORM工具
   
   - Spring Web：web应用程序提供支持
   
   - Spring Test：对Junit和TestNG测试的支持

2. `@RestController` vs `@Controller`
   
   `@Controller`返回一个页面
   
   `@RestController`返回JSON或XML形式数据
   
   `@RestController = @Controller + @ReuestBody`

3. Spring IOC & AOP
   
   依赖注入，吧底层类作为参数传递给上层类，实现上层对下层类的控制。
   
   **IOC**：将原来需要在程序中手动创建的对象的控制权，交给Spring框架管理，程序不用去关心对象如何创建。Ioc容器实际上就是个Map(key, value)，map中存放的是各种对象。
   
   **AOP**：面向切面编程。可以将业务模块共同调用（譬如日志，事务处理等）的逻辑封装起来，减少代码的重复度和耦合度。
   
   1. 动态代理与静态代理。
      
      **代理模式主要的两个功能：**
      
      - 控制对基础对象的访问
      
      - 在访问时增加额外的功能
      
      代理模式的行为：
      
      - 代理对象持有基础对象的引用
      
      - 通过代理对象访问基础对象的方法
      
      **静态代理**：
      
      静态代理的代理关系在编译期就已经绑定，一个代理类对应一个基础接口。
      
      ```java
      public interface HttpApi{
          String get(String url);
      }
      public class RealModule implements HttpApi{
          public String get(String url){
              return "result";
          }
      }
      public class Proxy implements HttpApi {
          private HttpApi target;
          Proxy(HttpApi target){
              this.target = target;
          }
       public String get(String url){
          logger.info("静态代理", + url);
          target.get(url);
       }
      }
      ```
      
      静态代理的缺点：
      
      - 假如有一个新的基础接口，就需要重写一个代理类
      
      - 基础接口一旦发生改变，代理类也必须改变
      
      **动态代理：**
      
      #### JDK动态代理
      
      基于接口代理，凡是类的方法非public修饰，或者用了static关键字修饰，那这些方法都不能被Spring AOP增强
      
      #### 原理
      
      在获取到代理类的classLoader及相应的接口之后，可以获取到代理类的Class对象，通过反射获取到构造方法，new一个对象绑定到InvocationHandler上。
      
      ```java
      #Proxy类
      public static Object newProxyInstance(ClassLoader loader,
                  Class<?>[] interfaces, InvocationHandler h)
      ```
      
      > * loder，选用的类加载器。一般都会用加载代理对象的类加载器。
      > * interfaces，被代理的类所实现的接口，这个接口可以是多个。
      > * h，绑定代理类的一个方法。
      
      loader跟interfaces基本决定了这个代理类的所属对象
      
      InvocationHandler在调用代理类的方法时会执行一个绑定的方法，同时会替代原本方法的结果返回。
      
      ```java
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
          //会导致死循环
          method.invoke(proxy, args)
          //object为被代理的对象
          method.invoke(object,args)
      }
      ```
      
      proxy是代理对象，当该对象的方法被调用时，会触发InvocationHandler中的invoke方法。
      
      例子
      
      ```java
      public interface Person {
      
          public void say();
      }
      ```
      
      ```java
      public class Men implements Person {
      
          @Override
          public void say() {
              System.out.println("hello, i am a man");
          }
      }
      ```
      
      ```java
              Men man = new Men();
              Person person = (Person) Proxy.newProxyInstance(
              man.getClass().getClassLoader(), 
              man.getClass().getInterfaces(), 
              new InvocationHandler() {
                  @Override
                  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                      System.out.println("before say method begin");
                      method.invoke(man,args);
                      System.out.println("after say method finish");
                      return null;
                  }
              });
              person.say();
      ```
      
      结果
      
      ```java
      before say method begin
      hello, i am a man
      after say method finish
      ```
      
      #### Cglib动态代理
      
      基于子类代理，凡是类的方法使用了private、static、final修饰，那这些方法都不能被Spring AOP增强
      
      <mark>JDK动态代理，发现其真实对象必须提供接口才可以使用</mark>。<mark>在一些不提供接口的环境中，只能采用一些别的第三方技术，比如CGLIB动态代理。</mark>
      
      ```java
          <dependency>
            <groupId>cglib</groupId>
            <artifactId>cglib</artifactId>
            <version>2.2.2</version>
          </dependency>
      ```
      
      ```java
      public class CglibProxyExample implements MethodInterceptor {
      
          public Object getProxy(Class clz){
              //CGLIB enhancer增强类对象
              Enhancer enhancer = new Enhancer();
              //设置增强类型
              enhancer.setSuperclass(clz);
              //定义代理对象为当前对象，要求当前对象实现MethodInterceptor方法
              enhancer.setCallback(this);
              return enhancer.create();
          }
      
          @Override
          /**
           * @param proxy 代理对象
           * @param method 方法
           * @param args 方法参数
           * @param methodProxy 方法代理
           * @return 代理逻辑返回
           * @throws Throwable 抛出异常
           */
          public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
      
              System.out.println("before say method begin");
              Object retObj = methodProxy.invokeSuper(proxy,args);
              System.out.println("after say method finish");
              return retObj;
          }
      }
      ```
      
      ```java
          CglibProxyExample cglibProxyExample = new CglibProxyExample();
          Men men = (Men)cglibProxyExample.getProxy(Men.class);
          men.say();
      ```

4. Spring AOP和AspectJ AOP有什么区别
   
   <mark>AspectJ 是静态代理， Spring AOP是动态代理。</mark>
   
   AspectJ在编译阶段会生成静态代理类，将AspectJ织入进Java字节码中，运行的时候就会调用增强过后的代理类。
   
   Spring AOP每次运行时在内存中临时创建一个AOP对象，这个AOP对象包含了目标对象的全部方法，并且针对切点方法做了增强处理。
   
   Spring AOP属于运行时增强，而AspectJ是编译时增强。
   
   Spring AOP是基于代理，而AspectJ基于字节码操作。

5. **IOC体系**
   
   1. 六大体系
   - Resource体系：对资源的抽象，每一个实现类都代表了一种资源的访问策略。`UrlResource、ClassPathResource、FileSystemResource`等。
   
   - ResourceLoader体系: 统一资源加载，主要应用于根据给定的资源文件地址，返回对应的 Resource 。
   
   - BeanFactory体系：Ioc bean 容器，其中`BeanDefinition`是他的基础结构，内部维护了一个`BeanDefinition map`，并可以根据`BeanDefinition`的定义进行bean的创建和管理。配置文件中每一个`<bean>`都是一个`BeanDefinition`。
   
   - BeanDefinition体系：描述Bean对象。
   
   - BeanDefinitionReader体系：读取Spring中的配置文件，将其转换为Ioc容器内部的数据结构`BeanDefinition`。
   
   - ApplicationContext体系：应用上下文，继承自BeanFactory，但是他拥有更多、更强大的功能。
     
     <mark>与BeanFactory的区别：</mark>
     
     > 集成MessageSource接口，提供国际化的表针访问策略
     > 
     > 集成ApplicationEventPublisher接口，提供强大的事件机制
     > 
     > 扩展ResourceLoader，可以用来加载多种Resource
     > 
     > 对Web应用的支持
   2. Ioc容器的使用过程
      
      > 资源定位  -> 装载  -> 注册
      
      资源定位就是通过ResourceLoader定位获取Resource资源对象
      
      装载就是通过BeanDefinitionReader读取、解析Resource资源，转换成Ioc内部数据结构BeanDefinition
      
      注册，将第二步解析好的BeanDefinition，通过BeanDefinitionRegistry接口来实现，其实就是将其注入到一个HashMap容器中。**注意：此时并没有完成Bean的创建（依赖注入），只有到getBean()时，才会进行创建。**
   
   3. BeanDefinition解析、注册过程
      
      ![](http://47.114.144.122/images\2020-09-15-11-41-12-image.png)
   
   4. 

6. Spring bean
   
   1. Spring bean的作用域
      
      - singleton： 唯一实例，Spring中默认都是单例的。
      
      - prototype：每次调用都会创建一个新的实例
      
      - request：每次Http请求都会产生一个新的bean，该bean仅在该Http request中生效
      
      - session：每一次Http请求都会产生一个新的bean，该bean仅在该Http session中生效
      
      - global-session：全局session作用域
   
   2. Spring bean的线程安全问题（解决方法）
      
      - 避免在bean中定义可变的成员变量
      
      - 利用ThreadLocal对象中存储的与线程ID关联的特性，定义一个ThreadLocal的成员变量
   
   3. @Companent 和 @Bean的区别
      
      - 作用域不同：`@Companent`作用于类上，而`@Bean`作用于方法上
      
      - `@Companent`的作用是用来标记类，使其能够被`@CompanentScan`扫描，自动装配到Spring的bean容器。`@Bean`就是标记方法的返回对象作为一个bean实例，它能够加载起三方包的类的实例作为bean实例。
   
   4. <mark>Spring Bean的生命周期</mark>
      
      - 接口方法：
        
        1. bean自身方法：配置文件中<bean>的init-method和destory-method。除了xml配置的方式，Spring也支持用`@PostConstruct`和 `@PreDestroy`注解来指定init和destroy方法。
        
        2. bean级生命周期方法：BeanNameAware、BeanFactoryAware、InitializingBean和DisposableBean。
           
           - ApplicationContextAware: 获得ApplicationContext对象,可以用来获取所有Bean definition的名字。
           - BeanFactoryAware:获得BeanFactory对象，可以用来检测Bean的作用域。
           - BeanNameAware:获得Bean在配置文件中定义的名字。
           - ResourceLoaderAware:获得ResourceLoader对象，可以获得classpath中某个文件。
           - ServletContextAware:在一个MVC应用中可以获取ServletContext对象，可以读取context中的参数。
           - ServletConfigAware在一个MVC应用中可以获取ServletConfig对象，可以读取config中的参数。
        
        3. 容器级生命周期接口方法：InstantiationAwareBeanPostProcessor和BeanPostProcessor这两个接口。
        
        4. 工厂后处理接口方法：AspectJWeavingEnabler，ConfigurationClassPostProcessor，CustomAutowireConfigurer等工厂后处理器方法。
      
      - bean的生命周期
        
        - 找到Bean容器中bean的定义。
        
        - Bean 容器利用`Java Reflection API`创建一个Bean的实例。
        
        - 如果涉及一些属性值，利用set方法设置一些属性值
        
        - 如果实现了`BeanNameAware`接口，调用setBeanName设置bean名字
        
        - 如果实现了`BeanClassLoaderAware`接口，调用`setBeanClassLoader`方法传入ClassLoader对象的实例。
        
        - 如果有其他的`*Aware`接口的实现，就调用相应的方法。
        
        - 如果有相关的`BeanPostProcessor`对象，则调用`postProcessBeforeInitialization`方法。
        
        - 如果bean实现了`InitializingBean`接口，执行`afterPropertiesSet`方法。
        
        - 如果有和加载这个 Bean 相关的 `BeanPostProcessor` 对象，执行`postProcessAfterInitialization()` 方法
        
        - 当要销毁 Bean 的时候，如果 Bean 实现了 `DisposableBean` 接口，执行 `destroy()` 方法。
        
        - 当要销毁 Bean 的时候，如果 Bean 在配置文件中的定义包含 destroy-method 属性，执行指定的方法。
        
        ![](http://47.114.144.122/images\2020-07-27-19-58-00-image.png)
   
   5. Spring MVC
      
      Spring MVC把后端逻辑分为Service层（业务处理）、DAO层（数据库操作）、entity层（实体类）、Controller层（请求处理与数据返回）
      
      **Spring MVC流程说明**
      
      ![](http://47.114.144.122/images\2020-07-27-20-27-53-image.png)
      
      1. 用户请求到达前端处理器DispatcherServlet.
      
      2. DispatcherServelet调用HandlerMapping获取Handler。
      
      3. 获取到Handler后，交给HandlerAdapter处理
      
      4. HandlerAdapter调用真正的处理器Controller来处理逻辑
      
      5. 处理完毕后返回ModelAndView返回给DispatcherServlet.
      
      6. ViewResolver会根据ModelAndView里面的逻辑View来解析真正的View
      
      7. DispatcherServlet将Model交给View，渲染之后将View返回给客户端。
   
   6. Spring中的设计模式
      
      - 工厂模式：`BeanFactory、ApplicationContext`创建bean对象
      
      - 代理模式：AOP
      
      - 单例模式：bean
      
      - 模板方法模式：`jdbcTemplate、redisTemplate`
      
      - 适配器模式：Controller处理方式HandlerAdapter。
   
   7. Spring事务
      
      - 隔离级别
        
        1. **TransactionDefinition.ISOLATION_DEFAULT**：数据源默认的隔离级别
        
        2. **TransactionDefinition.ISOLATION_READ_UNCOMMITTED**：读未提交。可能会造成脏读，幻读或不可重复读。
        
        3. **TransactionDefinition.ISOLATION_READ_COMMITTED**：读已提交。允许读取并发事务提交的数据。可能出现幻读和不可重复读。
        
        4. **TransactionDefinition.ISOLATION_REPEATABLE_READ**：可重复读。幻读仍可能发生。
        
        5. **TransactionDefinition.ISOLATION_SERIALIZABLE**：串行化。所有事务依次逐个运行。
      
      - 不同隔离级别导致的问题
        
        1. 脏读：事务A读取到事务B未提交的修改，事务B因为错误回滚。比如事务A从某个账户扣钱，事务B打钱，如果先扣钱还未提交，就加钱，事务A支付失败就会导致账户金额不对
        
        2. 不可重复读：事务A在事务中前后两次数据读取由于事务B的提交而不一样。
        
        3. 幻读：在事务A的操作过程，前后两次读取的记录条数不一样。读取到事物B新插入的数据。<mark>这里后者的读是当前读</mark>，主要用在update，delete上。
      
      - 事务传播机制
        
        1. 支持当前事务，加入该事务如当前无事务，有三种传播机制
           
           - 新创建一个
           
           - 以非事务运行
           
           - 抛出异常
        
        2. 不支持当前事务，如当前存在事务，则有三种情况
           
           - 新创建一个，挂起原来的
           
           - 挂起旧的，以非事务运行
           
           - 抛出异常
        
        3. 存在事务，新建一个当嵌套事务，不存在，就新建一个
      
      - `Transactional(rollbackFor=Exception.class)`
        
        1. 当注解使用在类上，则该类的所有public方法都具有事务属性。当注解在方法上，方法抛出异常就会回滚
        
        2. 如果不配置`rollbackFor`，则遇到`RuntimeException`就回滚，加上`rollbackFor`可以在遇到非运行时异常也可以回滚。
   
   8. JPA（Java 持久化API）
      
      ```java
      Entity(name="USER")
      public class User {
      
          @Id
          @GeneratedValue(strategy = GenerationType.AUTO)
          @Column(name = "ID")
          private Long id;
      
          @Column(name="USER_NAME")
          private String userName;
      
          @Column(name="PASSWORD")
          private String password;
      
          private String secrect;
      
      }
      ```
      
      如果某个字段不想被持久化，则可以使用`transient`修饰。
   
   9. Spring Boot配置文件读取
      
      1. 通过`@Value`读取比较简单的配置
         
         **`@value`这种方式是不被推荐的，Spring 比较建议的是下面几种读取配置信息的方式。**
         
         ```java
         @Value("${wuhan2020}")
         String wuhan2020;
         ```
      
      2. 通过`@ConfigurationProperties`读取与绑定bean，还可以校验
         
         ```java
         @Getter
         @Setter
         @ToString
         @ConfigurationProperties("my-profile")
         @Validated
         public class ProfileProperties {
            @NotEmpty
            private String name;
         
            @Email
            @NotEmpty
            private String email;
         
            //配置文件中没有读取到的话就用默认值
            private Boolean handsome = Boolean.TRUE;
         
         }
         ```
      
      3. `@PropertySource`读取指定properties文件
         
         ```java
         @Component
         @PropertySource("classpath:website.properties")
         @Getter
         @Setter
         class WebSite {
             @Value("${url}")
             private String url;
         }
         ```
   
   10. 数据验证
       
       - 对Controller验证参数，如果参数是@RequestBody形式，则直接在参数前加`@Valid`，如果是` (Path Variables 和 Request Parameters)`，在Controller类上加`@Validated`，告诉Spring去校验方法参数。
       
       - 对Service验证参数，在类上加上`@Validated`注解，在方法参数加上`@Valid`注解。
         
         ```java
         @Service
         @Validated
         public class Person{
             public void doSomething(@Valid Person person){
         
         }
         }
         ```
       
       - 自定义注解
         
         1. 创建注解
            
            ```java
            @Target({FIELD})
            @Retention(RUNTIME)
            @Constarint(validatedBy = RegionValidator.class)
            @Documented
            public @interface Region{
                String message() default "Region 值不在可选范围内";
                Class<?>[] groups() default {};
                Class<? extends Payload>[] payload() default {};
            }
            ```
         
         2. 实现`ConstraintValidator`接口
            
            ```java
            public class RegionValidator implements ConstraintValidator<Region, String> {
            
                @Override
                public boolean isValid(String value, ConstraintValidatorContext context) {
                    HashSet<Object> regions = new HashSet<>();
                    regions.add("China");
                    regions.add("China-Taiwan");
                    regions.add("China-HongKong");
                    return regions.contains(value);
                }
            }
            ```
       
       - ## `@NotNull` vs `@Column(nullable = false)`
         
         - `@NotNull`是对bean的验证与数据库无关
         
         - `@Column(nullable = false)`是JPA用来在创建数据库表时加上列非空约束。
   
   11. <mark>Spring 中IOC容器分为两种</mark>，一种是`BeanFactory`，一种是`ApplicationContext`，前者是低级容器，后者是高级容器，前者之负责bean的创建工作。，后者扩展了`BeanFactory`，提供了事件发布、生命周期管理，自定义初始化，多资源加载等功能。
   
   12. 常见的`ApplicationContext`容器
       
       - ClassPathXmlApplicationContext: 从classpath的xml配置文件读取上下文。
       
       - FileSystemXmlApplicationContext: 由文件系统中的XML配置文件读取上下文。
       
       - XmlWebApplicationContext: 由Web应用的xml文件读取上下文。
       
       - ConfigServletServerApplicationContext: Spring Boot的ApplicationContext容器。
   
   13. <mark>循环依赖</mark>
       
       循环依赖就是循环引用，就是两个或两个以上的bean互相引用对方。
       
       Spring在循环依赖的场景有两种：
       
       - 构造器的循环依赖（无法解决，会抛出异常）
       
       - field属性的循环依赖
       
       Spring只解决scope为singleton的循环依赖，对于prototype的bean，直接抛出异常。
       
       <mark>Spring容器中的三级缓存：</mark>
       
       - singletonObjects：已经初始化完成的bean实例缓存。
       
       - earlySingletonObjects：存放的是早期bean实例，用来解决循环依赖问题。早期bean指的是未初始化完成的bean
       
       - singletonFactories：存放的是各个工厂bean。
       
       循环依赖的解决过程：
       
       - A在完成初始化第一步之后，将早期bean提前暴露，加到singletonFactories中。
       
       - A在后续的初始化过程中发现自己依赖于B，因此去尝试get(B)，这是B还未创建
       
       - B走创建流程，B发现自己依赖于A，然后从singletonFactories中获取ObjectFactory调用getObject()方法拿到A对象，并将A对象加到earlySingletonObjects中，然后B完成初始化，将自己加到singletonObjects缓存。
       
       - A可以拿到B的都一项，A就可以顺利完成初始化。

### Spring Boot

1. 什么是Spring Boot?
   
   Spring Boot是一个能够用很少的配置文件就能创建一个独立应用的构建项目的方式。Spring Boot在构建项目时就可以选择第三方工具，进行项目的快速搭建。

2. Spring Boot 启动流程
   
   - `@SpringBootConfiguration`中`@EnableAutoConfigurationImportSelector`
   
   - springApplication.run方法。该方法会初始化springApplication对象，通过SpringFactoriesLoader加载自动化配置类。
   
   - 获取`EnventPublishingRunListener`对象，该对象在Springboot启动过程的不同阶段用来发射内置的生命周期事件。
   
   - 创建并准备`Environment`
   
   - 创建并初始化`ApplicationContext`。例如设置`Environment`，加载配置等
   
   - `refresh`方法刷新容器。调用bean factory的后置处理器BeanPostProcessor，创建内嵌的tomcat服务器等。
   
   - 调用`ApplicationRunner`和`CommandLineRunner`的run 方法，执行我们的一些数据加载工作。

3. Spring boot 的特点（优点）
   
   - 构建项目只需要很少的配置文件，<mark>Spring Boot遵循“固执己见的默认配置”</mark>
   
   - Spring boot 内部提供嵌入式HTTP服务器，如tomcat和jetty，可以轻松的进行部署。
   
   - Spring boot可以更容易的与Spring生态系统集成，如Spring JDBC，Spring ORM, Spring Security等
   
   - Spring boot 支持各种REST API的实现方式
   
   - Spring Boot提供了多种插件，可以使用内置工具(如Maven和Gradle)开发和测试Spring Boot应用程序。

4. Spring Boot Starters
   
   Spring Boot Starters是一系列依赖关系的集合。它帮我们整合一个功能所需要的所有包的依赖关系。
   
   如我们开发REST和WEB服务时，我们需要Spring MVC, Tomcat 和Jackson这样的库，一个`spring-boot-starter-boot`就可以搞定了。

5. `@SpringBootApplication`注解
   
   ```java
   package org.springframework.boot.autoconfigure;
   @Target(ElementType.TYPE)
   @Retention(RetentionPolicy.RUNTIME)
   @Documented
   @Inherited
   @SpringBootConfiguration
   @EnableAutoConfiguration
   @ComponentScan(excludeFilters = {
           @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
           @Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
   public @interface SpringBootApplication {
   
   }
   ```
   
   ```java
   package org.springframework.boot;
   @Target(ElementType.TYPE)
   @Retention(RetentionPolicy.RUNTIME)
   @Documented
   @Configuration
   public @interface SpringBootConfiguration {
   
   }
   ```
   
   从上面代码可以看出，`@SpringBootApplication`其实由`@ComponentScan`、`@SpringBootConfiguration`以及`@EnableAutoConfiguration`三个部分组成。

6. Spring Boot的自动配置是如何实现的？
   
   `@SpringBootApplication`中`@EnableAutoConfiguration`就是启动自动配置的。我们看一下`@EnableAutoConfiguration`的定义。
   
   ```java
   @Target({ElementType.TYPE})
   @Retention(RetentionPolicy.RUNTIME)
   @Documented
   @Inherited
   @AutoConfigurationPackage
   @Import({AutoConfigurationImportSelector.class})
   public @interface EnableAutoConfiguration {
       String ENABLED_OVERRIDE_PROPERTY = "spring.boot.enableautoconfiguration";
   
       Class<?>[] exclude() default {};
   
       String[] excludeName() default {};
   }
   ```
   
   `@EnableAutoConfiguration`中有个`@Import`注解，它的作用是将配置类或者bean注入到当前类中。`AutoConfigurationImportSelector`中`getCandidateConfigurations`方法会将所有自动配置类用list形式返回，这些list中的配置类会被Spring容器当作bean处理。`getCandidateConfigurations`会调用`SpringFactoriesLoader`中的`loadFactoryNames`方法。`SpringFactoriesLoader`会搜寻各个包下面的`spring.factories`。`loadFactoryNames`方法返回类名集合，然后再通过反射构建对象实例。
   
   ![](https://user-gold-cdn.xitu.io/2018/8/6/1650e0c6e28a0407?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)
   
   **自动配置流程**
   
   ![](http://47.114.144.122/images\2020-09-14-10-55-29-image.png)

### 微服务 Spring Cloud

1. Eureka 服务发现和注册
   
   - 注册中心如何保证高并发的？<mark>多级缓存</mark>
     
     Eureka Client 每30秒会往Eureka Server 发送心跳。同时也会拉取最新的服务列表。
     
     Server端维护了一个名为`register`的`ConcurrentHashMap`，它是直接维护在纯内存中，有很高的性能。其次，`Server`端运用了多层缓存技术，`Client`获取最新的服务列表时，会先到`ReadOnlyCacheMap`中获取，如果没有，就会到`ReadWriteCacheMap`中获取，如果还不存在，就直接从内存中读取`EurekaServer`中的注册列表数据。
     
     **服务注册列表发生变化时**，`Server`端在更新内存注册表的同时会失效`ReadWriteCacheMap`，等到30s之后，`ReadOnlyCacheMap`同步`ReadWriteCacheMap`时，发现读写缓存为空，就会直接到`Eureka Server`中获取数据，并更新缓存。
     
     **优势**： 
     
     1. 直接操作内存，性能非常高。
     
     2. 多级缓存，减少读写冲突，也能保证最终一致性。
   
   - 读写锁`ReentrantReadWriteLock`读写锁保证共享服务注册列表的线程安全。
     
     读写锁简介：
     
     - 当加写锁时，只能有一个线程进行写操作，其他线程阻塞。
     
     - 当加读锁时，写线程阻塞，读线程不阻塞。
     
     Eureka中使用了名为`register`的`ConcurrentHashMap`，在服务注册和读取服务过期时会分别加上写锁和读锁来保证注册列表的线程安全。

### Mybatis

1. `#{}`和`${}`的区别是什么？
   
   - `${}`拼接符sql拼接，直接将其替换成变量的值， `#{}`占位符sql预编译，会调用PreparedStatement的set方法来赋值
   
   - `#{}`变量替换之后，其对应的变量会加上单引号''，`${}`不会
   
   - `#{}`能防止sql注入，`${}`不能
   
   建议：
   
   - 多用#{}，不用或少用${}
   
   - 表名作为参数，必须使用`${}`
   
   - `order by`时，必须使用`${}`
   
   - 使用`${}`时，要注意何时加引号

2. Xml映射文件中除了select|update|delete|insert还有什么？
   
   还有`<resultMap> <resultType> <selectKey> <include>`，动态sql中的`trim|where|if|set|foreach|when|otherwise|bind`等。通过`<include>`标签引入 sql 片段，`<selectKey>`为不支持自增的主键生成策略标签。

3. Dao接口，Xml映射文件的工作原理
   
   每个Dao接口，一般来说都有一个映射文件xml，接口的全限名就是映射文件中的`namespace`的值，每个接口中的方法名，对应的就是映射文件中`MappedStatement`的id值，通过全限名加方法名，可以唯一定位一个`MappedStatement`。
   
   由于此映射关系，Dao接口中的方法是不能重载的。
   
   Dao接口的工作原理是基于<mark>JDK动态代理</mark>，Mybatis运行时会使用JDK动态代理为Dao接口生成代理proxy对象，代理对象会拦截接口方法，转而执行MappedStatement所代表的sql语句。

4. Mybatis分页
   
   内存分页 vs 物理分页
   
   内存分页是将所有满足条件的记录全部取出，舍弃前面offset条数据，获取limit条数据，这种方式性能较差
   
   逻辑分页是在sql中指定offset和limit，让mysql自己计算。
   
   Mybatis中，我们可以使用分页插件来完成物理分页。例：PageHelper
   
   PageHelper的原理：pagehelper我们在调用startpage时，她会将offset和limit计算好放到**ThreadLocal对象**中，然后他实现了Mybatis的插件接口，通过拦截器，重新拼装sql。

5. Mybatis的插件运行原理
   
   Mybatis仅支持对`ParameterHandler`、`ResultSetHandler`、`StatementHandler`、`Executor`这四种接口的插件。Mybatis使用JDK动态代理，为需要拦截的接口生成代理对象以实现接口的增强。

6. `association`和`collection`
   
   在`<resultMap>`中我们可以通过`<association>`来给结果中关联对象赋值，通过`<collection>`来给对象中的list类型进行赋值。
   
   ```java
       <resultMap type="PersonResult" id="PersonMap">
           <id property="id" column="id" />
           <result property="name" column="name" />
   
           <!-- 一对一关联：单向。方式二：使用内联方式直接列出。 -->
           <association property="idCard" column="idcard_id" javaType="IdCard">
               <id column="cid" property="id" />
               <result column="number" property="number" />
               <result column="expired_time" property="expiredTime" />
           </association>
       </resultMap>
   ```

7. 延迟加载
   
   Mybatis仅支持`assocation`关联对象和`collection`关联集合对象的延迟加载。通过配置文件中，设置`lazyLoadingEnabled=true|false`。
   
   他的原理就是使用CGLIB创建代理对象，在调用对象中的关联属性（对象、集合）的属性时，如`a.getB().getName()`，拦截器检测到`a.getB()`为空时，就会发送事先指定好的sql执行查询，吧关联对象B赋值给a。

8. Mybatis 执行器 `Executor`
   
   Mybatis有三种基本的Executor执行器，`SimpleExecutor`、`ReuseExecutor`、`BatchExecutor`。
   
   `SimpleExectuor`：每一次执行update或select，就开启一个Statement对象，用完立刻关闭。
   
   `ReuseExectuor`：执行update 或select，以sql作为key查询Statement，不存在就创建一个，用完之后存在Map<String, Statement>内，以供下一次使用。
   
   `BatchExectuor`：执行update，将所有sql都添加到批处理中，等待统一执行。

### 分布式

1. 分布式锁
   
   - ZK实现
     
     ZK中有中临时节点，临时节点有某个客户端创建，当客户端与ZK集群断开，则该节点自动被删除。
     
     ZK实现分布式锁的步骤
     
     - 客户端调用create()方法创建名为`/dlm-locks/lockname/lock-`的临时顺序节点。
     
     - 客户端调用getChildren("lockname")方法来获取所有已经创建的子节点。
     
     - 客户端获取到所有子节点path之后，如果发现自己创建的子节点的序号是最小的，则获取到分布式锁
     
     - 如果不是最小的，那么监视比自己节点序号小的最大的节点进入等待，知道下次监视的子节点变更时，在进行子节点的获取。
   
   - Redis实现
     
     Redis有`SETNX（set if not exists）`和`GETSET(先写新值，返回旧值，原子性操作，可以用于分辨是不是首次操作)`。
     
     - 线程A发送SETNX 尝试获取锁，如果锁不存在，则set获取锁。
     
     - 如果存在锁，则判断值（时间戳）是否过期，如果没超时，则等一会再重试
     
     - 如果锁过期，则通过`GETSET`来尝试获取锁，如果拿到的时间戳仍是过期的，则获取到锁。
     
     - 如果不是过期，则表示可能另一个线程获取到锁。

2. 秒杀
   
   1. 分表分库
      
      将秒杀业务、订单业务、支付业务和库存业务拆分开，按照此维度进行垂直拆分。
      
      **垂直拆分 vs 水平拆分**
      
      垂直拆分： 
      
      垂直分库：就是根据业务解耦，将不同业务所需的表拆分到不同的数据库中，提高整体的QPS。
      
      垂直分表：一般被拆分的表字段比较多，将不常用的字段和过长的字段从表中抽离，防止数据跨页的发生。
      
      水平拆分：
      
      水平分库：水平分库是将库中表的数据按照一定的规则分配到不同节点相同数据结构的数据库中。
      
      水平分表：水平分表是在同一个数据库中将表的数据水平拆分，按照一定的规则分到多个表中。
      
      分表ID的解决办法： UUID或者用一张表来存放生成的 Sequence。UUID生成的数据占用空间大。Sequence表节省了空间，但是依赖于单表的稳定性，容易导致整体系统的崩溃。
      
      UUID的方式：
      
      - 全局表：单表数据并发瓶颈，稳定性影响大
      
      - redis缓存
      
      - Twitter的snowflake：
        
        本地生成一个Long型数据作为UUID，前41位作为时间戳，10位作为机器的ID（5位作为数据中心id， 5位作为机器id），12位作为毫秒内的流水号，最后以为置零。
   
   2. 分布式事务
      
      **CAP**原理：
      
      - C：Consistency。一致性，分布式系统中的数据，同一时刻要有同样的值。
      
      - A：Available。可用性。集群中某个节点下线，其他节点可以保持服务。
      
      - P：Partition torlerance。分区容错性。由于网络抖动等原因，两个数据节点之间的存在不同步的情况。此时需要在C和A中进行选择。
      
      **两阶段提交**：
      
      事务协调器，也叫事务管理器。他是用来协调事务的，什么时候事务提交是由他来控制。
      
      参与者，也叫资源管理器。他是负责具体事务的执行，包括支付事务，订单提交事务等。
      
      - 第一阶段（准备阶段）：事务协调者向参与者发送Prepare消息，用来询问各个节点是否准备就绪，各个参与者需要去查看自身的条件，譬如库存是否足够。
      
      - 第二阶段（提交阶段）：如果协调者收到参与者失败或者超时的情况，就会给参与者发送rollback指令；否则执行commit。
      
      **TCC 事务机制**
      
      TCC：Try 尝试  Confirm 确认 Cancel 取消
      
      Try阶段：尝试资源是否能够满足事务所需，如果能够满足，则锁定资源。但不会执行具体的操作。需要建立中间表或者添加字段来记录锁定信息。其核心思想是：针对每个操作，都要注册一个与其对应的确认和补偿（撤销）操作。
      
      Confirm阶段：执行具体的事务操作
      
      Cancel阶段：执行Try阶段操作对应的回滚操作。
      
      **Seata 阿里开源分布式事务框架**
      
      **Transaction Coordinator (TC)**： 事务协调器，它是独立的中间件，需要独立部署运行，它维护全局事务的运行状态，接收TM指令发起全局事务的提交与回滚，负责与RM通信协调各各分支事务的提交或回滚。  
      **Transaction Manager (TM)**： 事务管理器，TM需要嵌入应用程序中工作，它负责开启一个全局事务，并最终向TC发起全局提交或全局回滚的指令。  
      **Resource Manager (RM)**： 控制分支事务，负责分支注册、状态汇报，并接收事务协调器TC的指令，驱动分支（本地）事务的提交和回滚

### 延迟队列

1. 定义
   
   延时队列相比于普通队列最大的区别就体现在其延时的属性上，普通队列的元素是先进先出，按入队顺序进行处理，而延时队列中的元素在入队时会指定一个**延迟时间**，表示其希望能够**在经过该指定时间后处理**。

2. 场景
   
   - 支付订单支付超时取消
   
   - 会议预定系统提前30分钟提醒
   
   - 外卖超时提醒
     
     ......

3. 实现方案
   
   - Redis ZSet
     
     Redis中有序集合ZSet，ZSet中每个元素都有一个对应Score， ZSet中所有元素都是按照其Score进行排序的。
     
     **入队操作**
     
     `ZADD KEY timestamp task`，我们将需要处理的任务，按其需要延迟处理时间作为Score加入到ZSet中。
     
     **定时查看**
     
     起一个进程定时（比如每隔一秒）通过`ZRANGEBYSCORE`方法查询ZSet中Score最小的元素，具体操作为：`ZRANGEBYSCORE key -inf +inf limit 0 1 withscores`。查询结果分两种：
     
     a. 查询出的分数小于等于当前时间戳，该任务应该需要执行，异步去处理该任务
     
     b. 查询出来的分数大于当前时间戳，则跳过。
   
   - RabbitMQ
     
     RabbitMQ 本身并不直接提供对延迟队列的支持，我们依靠 RabbitMQ 的**TTL**以及**死信队列**功能，来实现延迟队列的效果。
     
     **死信队列**
     
     死信队列是一种RabbitMQ的消息处理机制，当RabbitMQ在生产和消费消息的时候，消息遇到如下情况就会变成**死信**：
     
     1. 消息被拒绝`basic.reject/ basic.nack`并且不再重新投递`requeue=false`
     
     2. 消息超时未消费，也就是TTL过期了
     
     3. 消息队列达到最大长度
     
     消息一旦变成一条死信，便会重新投递到死信交换机（Dead-Letter-Exchange），然后死信交换机根据绑定规则转发到对应的死信队列上，监听该队列就可以让消息被重新消费
     
     **消息生存时间TTL**
     
     有两种不同的方式可以设置消息的TTL属性：
     
     a. 直接在创建队列的时候设置整个队列的TTL过期时间，所有进入队列的消息都在固定的时间后过期。
     
     ```java
     Map<String, Object> args = new HashMap<>();
     args.put("x-message-ttl",6000);
     channel.queueDeclare(queueName, druable, exclusive, autoDelete, args); 
     ```
     
     b. 针对单条消息进行设置
     
     ```java
     AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
     builder.eexpiration("6000");
     AMQP.BasicProperties properties = builder.build();
     channel.basicPublish(exchangeName, routingKey, mandatory, properties, "msg content".getBytes());
     ```
     
     **实现思路**
     
     ![](http://47.114.144.122/images\2020-10-31-14-58-53-image.png)
   
   - TimeWheel 时间轮
     
     ![](http://47.114.144.122/images\2020-10-31-16-53-38-image.png)
     
     时间轮是一个存储延迟消息的环形队列，其**底层采用数组实现**，可以高效循环遍历。这个环形队列中的**每个元素对应一个延迟任务，这个列表是个双向环形链表**。
     
     时间轮上会有表盘指针，表示时间轮当前所指时间，随着时间推移，改制真会不断前进，并处理对应位置上的延迟任务列表。
     
     **多层时间轮**
     
     单层时间轮数组长度是固定的，针对延迟时间比较长的任务就需要维护一个很大的时间轮，这样就会导致底层数组的寻址效率变低。
     
     kafka引入多层时间轮的概念。多层时间轮的概念跟时钟里面的时针、分针、秒针的概念非常类似，将一层时间轮不能表示的任务，通过多层时间轮来表示。
     
     ![](http://47.114.144.122/images\2020-10-31-17-19-44-image.png)
     
     Kafka中时间轮算法添加延迟任务以及推动时间轮滚动的核心流程如下，其中Bucket即时间轮的延迟任务队列，并且Kafka引入的DelayQueue解决了多数Bucket为空导致的时间轮滚动效率低下的问题：
