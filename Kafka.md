# Kafka

## 术语

1. Producer：生产者

2. Consumer：消费者

3. Broker：代理服务节点，kafka服务节点

4. Topic：主题

5. Partition：分区，一个主题有多个分区，他分布在多个Broker中。

6. Replica：副本，分区的copy，容灾措施，分为leader副本和follower副本，leader副本负责消息的接受和消费，follower副本负责同步leader副本做备份。

7. AR：kafka分区中所有副本的合集统称为AR。
   
   ISR：与leader副本保持一定程度同步的副本的集合。如果leader副本失效，则会从ISR中选取新的leader副本。
   
   OSR：与leader副本滞后一定程度的副本的集合。 AR = ISR + OSR
   
   leader副本负责维护ISR集合，如果一个ISR中的副本滞后太多，则leader会将其从ISR中剔除。OSR与之相同。

8. HW：high watermark。他标志着一个Consumer所能消费的最大offset。
   
   LEO：下一条待写入消息的offset
   
   <mark>分区中ISR的每个副本都会维护自己的LEO，所有副本的最小LEO称为该分区的HW</mark>

## 生产者

1. 默认参数
   
   - bootstrap.servers：broker节点host1:port，host2:port
   
   - key.serializer和value.serializer：broker接受的消息必须是字节数组，因此，需要制定对应的序列化方式

2. KafkaProducer是线程安全的。

3. Kafka发送数据有三种模式：
   
   <mark>发后即忘、同步、异步</mark>
   
   - 发后即忘： KafkaProducer调用send(ProducerRecord record)就结束，不做任何的容错机制
   
   - 同步：
     
     send()方法有两种定义
     
     ```java
     public Future<RecordMetadata> send(ProducerRecord record)
     public Future<RecordMetadata> send(ProducerRecord record, Call)
     ```
     
     同步有两种方式，一种直接链式调用get()函数
     
     ```java
     try{
         kafkaProducer.send(record).get();
     }catch (ExecutionException | InterruptedException e){
         e.printStackTrack();
     }
     ```
     
     第二种不直接调用
     
     ```java
     try{
         Future<RecordMetadata> future = kafkaProducer.send(record);
         RecordMetadata metadata = future.get();
         //RecordMetadata 中包含当前消息的元数据，包括主题，分区，偏移量
     }catch (ExecutionException | InterruptedException e){
         e.printStackTrack();
     }
     ```
   
   - 异步发送：
     
     在send函数中指定Callback即可，由callback来执行异步调用的结果。
     
     ```java
     producer.send(record, new Callable(){
         public void onCompletion(RecordMetadata metadata, Exception exception){
             if (exception != null){
     
             }else {
     
             }
         }
     })
     ```

4. 生产者拦截器
   
   生产者拦截器主要用于在消息发送之前对消息进行过滤，或修改消息的内容，还可以用来在消息回调之前做一些统计工作。
   
   ```java
   //自定义拦截器需要继承ProducerInterceptor，里面有三个函数:
   public ProducerRecord<K,V> onSend(ProducerRecord<K,V> record)
   public void onAcknowledgement(RecordMetadata metadata, Exception e)
   public void close()
   ```
   
   KafkaProducer会在消息被应答之前或是消息发送失败时调用`onAcknowledgement`方法，该方法运行在Callback 方法之前。
   
   kafka可通过`interceptor.classed`参数来指定拦截器链。

5. Kafka 发送原理
   
   1. 发送流程图
      
      ![](C:\Users\qiang\AppData\Roaming\marktext\images\2020-07-01-22-52-42-image.png)
      
      消息发送主要分为两个线程： **主线程和Sender线程**
      
      消息经由主线程，经过**拦截器、序列化、分区器**，发送给消息累加器RecordAccumulator。RecordAccumulator中为每个分区维护了一个双端队列。队列中的节点是ProducerBatch对象。
      
      **ProducerBatch**中包含一个或多个ProducerRecord。当一个record到达RecordAccumulator时，会从对应分区的队列中获取尾部ProducerBatch，若大小还能插入，则插入，不能则比较record的大小是否大于batch.size，若小于，则创建一个batch.size大小的ProducerBatch，该大小的batch会再使用完之后交给BufferPool复用，否则创建一个record大小的。
      
      Sender获取到消息之后会建立Request发送给Selector执行发送操作，同时他会将该Requset加入到**InFlightRequests**中，InFlightRequests保存的具体对象是`Map<NodeId, Deque<Request>>`,它表示已经发送，但是没收到响应的请求。
   
   2. acks
      
      - acks = 1， 默认值。生产者消息发送成功之后，只要leader收到了就返回成功响应
      
      - acks = 0，生产者发送消息不需要等待响应
      
      - acks = -1 /acks = all， 生产者发送消息，需要等待所有ISR中的节点全接受到数据才返回成功响应

## 消费者

1. kafka支持两种消费模式： **点对点** 和 **订阅/发布**
   
   点对点： 如果所有消费者都属于同一个消费组，同一个消费组对于同一个topic不会重复消费。
   
   订阅/发布：如果消费者隶属于不同的消费组，那么所有消息都会广播到每个消费者。

2. 订阅主题分区
   
   **订阅主题函数**
   
   ```java
   public void subscribe(Collection<String> topics)
   public void subscribe(Collection<String> topics, ConsumerRebalanceListener listener)
   public void subscribe(Pattern pattern, ConsumerRebalanceListener listener)
   public void subscribe(Pattern pattern)
   ```
   
   使用正则的方式指定订阅的主题，在之后新增的主题如果满足该正则，则也会被订阅。
   
   ConsumerRebalanceList 是配置再均衡监听器，后续会介绍。
   
   **指定主题分区**
   
   ```java
   public void assign(Collection<TopicPartition> partitions)
   ```
   
   //TopicPartition中包含topic 及 partition信息
   
   **查询主题元数据**
   
   ```
      public List<PartitionInfo> partitionFor(String topic)
   ```
   
      Class PartitionInfo{
   
          private String topic;
          private int partition;
          private final Node leader;
          private final Node[] replicas;
          private final Node[] inSyncReplicas;
          private final Node[] outSyncReplicas;
   
      }

```
   **拉取数据**

   ```java
   public ConsumerRecords<K,V> poll(final Duration timeout)
```

   ConsumerRecords表示一次从kafka中拉取操作获得的ConsumerRecord的集合。ConsumerRecords中提供了一个方法获取消息集中指定的分区消息。可以按照分区来消费

```java
   public List<ConsumerRecord<K,V>> records(TopicPartition partition)
```

   我们也可以按照topic来消费

```java
   public Iterable<ConsumerRecord<K,V>> records(String topic)
```

3. 位移提交
   
   在旧版Kafka中，offset是存在zookeeper中的，新版kafka用__consumer_offsets来记录主题的offset，这里吧位移持久化的动作成为**提交**。
   
   kafka中提交位移分为 **自动提交** 和 **手动提交** 两种方式。手动提交**同步提交**和**异步提交**。分别对应于KafkaConsumer 中的commitSync() 和commitAsync()两个函数。
   
   **同步提交**：
   
   ```java
   while(true){
       ConsumerRecords<String, String> records = kafkaConsumer.poll(1000);
       for(ConsumerRecord<String, String> record : records.records()){
           //do consume logic
       }
       kafkaConsumer.commitSync();
   }
   ```
   
   无参的同步提交函数，它提交位移的频率跟拉取得频率一致，如果要更细粒度的提交方式，可以用下面的函数
   
   ```java
   public void commitSync(final Map<TopicPartition, OffsetMetadata> offsets)
   ```
   
   我们可以结合ConsumerRecord的partitions()方法和records(TopicPartition)来实现按照分区提交偏移量。
   
   ```java
   try{
       while(isRunning.get()){
           ConsumerRecords<String, String> records = consumer.poll(1000);
           for (TopicPartition partition : records.partitions()){
               List<ConsumerRecord<String, String>> partitionRecords = 
                   records.records(partition);
               for(ConsumerRecord<String, String> record : partitionRecords){
                   //do consume
               }
               long lastConsumeOffset = partitionRecords.get(partitionRecords.size()-1).offset();
               consumer.commitSync(Collections.singletonMap(partition, new OffsetMetadata(lastConsumeOffset + 1));
           }
       }
   }finally{
       consumer.close();
   }
   ```
   
   **异步提交**
   
   异步提交的时候线程不会发生阻塞，因此在提交的过程中可能发起了一次新的拉取消费。
   
   ```java
   public void commitAsync()
   public void commitAsync(OffsetCommitCallback callback)
   public void commitAsync(final Map<TopicPartition, OffsetMetadata> offset,OffsetCommitCallback callback)
   ```

4. 指定位移消费
   
   在kafka中，新增的消费组或是消费组中新订阅的topic，他们是没有可以查找的消费位移的，他们通过参数`auto.offset.reset`来决定。
   
   `auto.offset.reset`有三个值，`earliest lastest none`分别表示从最早的，最晚的和什么都不选，如果设为none却没有指定消费位移，则会报NoOffsetForPartitionException异常。
   
   除此之外，KafkaConsumer中还提供了指定位移的方式来进行消费：
   
   ```java
   public void seek(TopicPartition partition, long offset)
   ```
   
   seek 的使用方式如下
   
   ```java
   KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
   consumer.subscribe(Arrays.asList(topics));
   
   //获取消费线程分配到的分区列表
   Set<TopicPartition> partitions = new HashSet<>();
   while (partitions.size() == 0){
       consumer.poll(Duration.ofMills(100));
       partitions = consumer.assignment();
   }
   for (TopicPartition tp : partitions){
       //设置分区初始消费偏移量为10
       consumer.seek(tp,10);
   }
   while(true){
       //consumer.poll()
   }
   ```
   
   KafkaConsumer中还提供了一种获取指定时间的位移的方法
   
   ```java
   public Map<TopicPartition, OffsetAndTimeStamp> offsetsForTimes(
           Map<TopicPartition,Long>> timestampsToSearch)
   public Map<TopicPartition, OffsetAndTimeStamp> offsetsForTimes(
           Map<TopicPartition,Long>> timestampsToSearch,
           Duration timeout)
   ```

5. 再均衡监听器
   
   再均衡是在消费组新增或删除消费组的时候，进行分区资源的重新分配。在subscribe方法中可以指定自定义再均衡监听器。
   
   再均衡过程中，可能某些消费者消费的位移不能及时提交，会导致重复消费，我们可以通过再均衡监听器来完成消费位移的保存和提交。
   
   ```java
   //在再均衡发生之前和消费者停止消费数据之后执行，可以用来保存/提交消费位移
   void onPartitionRevoked(Collection<TopicPartition> partitions)
   //在重新分配分区之后，消费者消费消息之前调用
   void onPartitionAssigned(Collection<TopicPartition> partitions)
   ```
   
   用法：
   
   ```java
   final KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);
   final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<TopicPartition, OffsetAndMetadata>();
           consumer.subscribe(Arrays.asList(topics), new ConsumerRebalanceListener() {
               public void onPartitionsRevoked(Collection<TopicPartition> collection) {
                   //应用一，直接在类中缓存各分区的消费位移，在发生再均衡时，直接从缓存中获取位移提交。
                   consumer.commitSync(currentOffsets);
                   currentOffsets.clear();
   
                   //应用二：将偏移量存在DB中，从DB获取偏移量设置给重新分配分区的消费者
                   //store in db
               }
   
               public void onPartitionsAssigned(Collection<TopicPartition> collection) {
                   //do nothing
   
                   //从DB获取偏移量设置给重新分配分区的消费者
                   for (TopicPartition tp : collection){
                       consumer.seek(tp, getOffsetFromDB(tp));
                   }
               }
           });
   
           try {
               while (true){
                   ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                   for (ConsumerRecord<String, String> record : records){
                       //do consume
   
                       currentOffsets.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset()+1));
                   }
                   consumer.commitAsync(currentOffsets, null);
               }
           } catch (Exception e) {
               e.printStackTrace();
           } finally {
               consumer.close();
           }
   ```

6. 消费者拦截器
   
   实现ConsumerInterceptor接口
   
   ```java
   //在poll()返回之前做一些过滤操作
   public ConsumerRecords<String, String> onConsume(
       ConsumerRecords<String, String> consumerRecords)
   //提交完位移之后调用
   public void onCommit(Map<TopicPartition, OffsetAndMetadata> map)
   ```
   
   下面实现一个在TTL时间内的数据才有效的过滤器
   
   ```java
   public ConsumerRecords<String, String> onConsume(ConsumerRecords<String, String> consumerRecords) {
           long now = System.currentTimeMillis();
           Map<TopicPartition, List<ConsumerRecord<String, String>>> newRecords = new HashMap<>();
           for (TopicPartition tp : consumerRecords.partitions()){
               List<ConsumerRecord<String, String>> tpRecords = consumerRecords.records(tp);
               List<ConsumerRecord<String, String>> newTpRecords = new ArrayList<>();
               for (ConsumerRecord<String, String> tpRecord: tpRecords){
                   if (now - tpRecord.timestamp() < EXPIRE_INTERVAL){
                       newTpRecords.add(tpRecord);
                   }
               }
               if (!newTpRecords.isEmpty()){
                   newRecords.put(tp, newTpRecords);
               }
           }
           return new ConsumerRecords<>(newRecords);
       }
   
       @Override
       public void onCommit(Map<TopicPartition, OffsetAndMetadata> map) {
   
       }
   
       @Override
       public void close() {
   
       }
   ```

7. KafkaConsumer 多线程
   
   KafkaConsumer是非线程安全的，在该类中所有public方法除了wakeup()之外，都会调用acquire() 及 release() 来保证线程同步，它不像synchronized，他是轻量级的。它通过CAS设置线程ID来实现的。

## 主题与分区

1. 创建主题
   
   使用kafka-topics.sh脚本来创建主题。kafka-topics.sh脚本有5种指令类型：create、list、describe、alter****
   
   ```shell
   ./kafka-topics.sh --zookeeper node1:2181 --create --topic topic-create
    --partitions 4 --replication-factor 2
   ```
   
   kafka会在log.dir或者log.dirs参数所配置的目录下创建响应的分区，默认目录为/tmp/kafka-logs/
   
   ```shell
   //查看kafka记录
   ls -al /tmp/kafka-logs/ | grep topic-create
   ```
   
   //使用zookeeper查看主题分区
   zkCli.sh -server 
   get /brokers/topics/topic-create
   
   //使用kafka.topics.sh查看分区分配
   ./kafka-topics.sh --zookeeper node1:2181 --describe --topic topic-create

```
   使用kafka-topics.sh的指令归纳如下

   ```shell
   kafka-topic.sh --zookeeper <String:host> --create --topic [String:topic]
   --partitions <Integer: partitions> --replication-factor <Integer: factors>
```

   其他参数：

   --replica-assignment： 指定分区分配规则

   --config： 指定参数创建分区

2. 查看主题
   
   **list指令**：查看当前可用主题
   
   ```bash
   ./kafka-topics.sh --zookeeper localhost:2181 -list
   ```
   
   describe 指令可以配置三个额外的指令：`topics-with-overrides`(包含覆盖配置的topic)， `under-replication-partitions`（找出所有包含失效副本的分区）和`unavailable-partitions`（查看主题中没有leader副本的分区）

3. 修改主题（过时，推荐使用kafka-config.sh）
   
   alter指令可以对已创建的主题参数进行修改，譬如增加（不支持减少）分区数，主题配置等。

4. 配置管理
   
   kafka-config.sh脚本包含变更配置alter和查看配置describe。不过kafka-config.sh不仅可以更改主题，也可以更改broker配置。
   
   kafka-config.sh通过entity-type来指定操作配置的类型，entity-name参数来指定操作的对象。
   
   entity-type 可以配置四个选项： topics、brokers、clients、users
   
   | entity-type | entity-name |
   | ----------- | ----------- |
   | 主题类型topics  | 主题名称        |
   | brokers     | brokerId    |
   | clients     | clientId    |
   | users       | 指定用户名       |

5. 删除主题
   
   --delete
   
   使用kafka-topics.sh脚本删除主题的行为本质上是在Zookeeper中的/admin/delete_topics路径下创建一个与待删主题一样的节点，以此来标记删除状态。

6. 分区的管理
   
   1. 优先副本的选举
      
      由于某些节点的故障，AR中的follower节点变成leader节点，导致leader节点在broker中的负载不均衡。
      
      优先副本，在AR中，第一个副本节点就是优先副本，该节点一般是leader副本。在上述的情况下，要达到分区负载均衡，我们就需要触发优先副本的选举。
      
      kafka中提供了broker端参数来选择是否自动进行分区平衡，`auto.leader.rebalance.enable`。但在正式环境中不建议开启自动平衡，这是由于分区平衡的时间节点不受控制，可能会发生在业务高负载的情况下。
      
      `kafka-perferred-replica-election.sh`脚本提供了对leader副本的重新平衡功能。
      
      ```bash
      kafka-perferred-replica-election.sh --zookeeper localhost:2181
      ```
      
      `kafka-perferred-replica-election.sh`还提供了`path-to-json-file`参数来小批量的进行分区优先副本的选举操作。`path-to-json-file`指定一个json文件，这个json文件中保存需要进行优先选举的分区。
      
      ```json
      {
          "partitions":[
          {
          "partition":0,
          "topic":"topic-partitions"
          },
          {
          "partition":1,
          "topic":"topic-partitions"
          },
          {
          "partition":2,
          "topic":"topic-partitions"
          }
      ]
      }
      ```
   
   2. 分区重分配
      
      `kafka-reassign-partition.sh`脚本用来执行分区的重新分配工作，他可以在集群扩容，broker节点失效的场景下对分区进行重新分配。
   
   3. 复制限流
      
      分区重分配是新建一个副本，将旧副本中的数据复制到新副本，因此会在数据传输。为了不影响kafka主业务，我们可以针对复制操作进行限流。

## 日志存储

1. 文件目录结构
   
   kafka中Log表示的是文件夹，对应命名形式为<topic>-<partition>的文件夹。Log中包含多个LogSegment，每个LogSegment对应于磁盘上的一个日志文件和两个索引文件。
   
   <mark>Log日志是顺序写入的，也就是只有最后一个LogSegment支持写入操作。</mark>
   
   每个LogSegment的日志文件(.log)都有对应的两个索引文件：**偏移量索引文件(.index)** 和**时间戳索引文件(.timeindex)**。<mark>日志文件的文件名就是该LogSegment的baseOffset，即起始偏移量。 </mark>

2. Kafka 日志索引
   
   Kafka日志Segment的两个索引文件是用来提高消息搜索的效率的。
   
   我们可以通过偏移量索引定位日志所在物理位置，同理我们可以用时间戳索引定位日志。
   
   kafka索引文件是以<mark>稀疏矩阵</mark>的形式记录的。每当写入一定量的消息（由broker端参数log.index.interval.bytes）指定。
   
   kafka通过MappedByteBuffer将索引加载进内存，通过二分法查到对应索引或小于该偏移量（时间戳）的最大索引，通过索引中的position来定位日志分段中的物理位置。
   
   Kafka非活跃日志分段都是只读文件，当活跃日志分段文件满足切分条件时，会关闭当前文件并设为只读，新建一个新的日志分段文件。
   
   日志定位的流程：
   
   - 定位到baseOffset
   
   - 计算相对偏移量（relativeOffset = offset - baseOffset）
   
   - 在索引文件中找到不大于相对偏移量的最大索引
   
   - 根据索引的position，继续找对应的Offset
   
   **如何定位到baseOffset呢?**
   
   <mark>利用调表。</mark>Kafka每个日志对象中使用了ConcurrentSkipListMap。每个日志分段的baseOffset作为key。
   
   总结： **为什么Kafka读取这么快？**
   
   - 利用跳表快速定位baseOffset，找到日志分段LogSegment
   
   - 利用MappedByteBuffer加载索引进内存，通过二分法确定不大于目标偏移量的最大索引项。

3. 顺序写：
   
   kafka采用文件追加的方式来写入消息，只能在活跃logsegment中写入，即是顺序写盘。

4. 页缓存：
   
   操作系统的一种磁盘缓存技术。吧磁盘中的数据缓存到内存中，对磁盘的访问转为对内存的访问。
   
   kafka中使用了大量的页缓存技术，消息先写到页缓存，等待操作系统执行刷盘任务。

5. 零拷贝：
   
   数据直接从磁盘拷贝到网卡设备中，不经过中间应用程序。
   
   非零拷贝四次复制：
   
   - 调用read()从磁盘中将数据复制到Read Buffer。
   
   - CPU控制将内核模式数据复制到用户模式
   
   - 调用wirte()时，将用户模式数据复制到Socket Buffer。
   
   - 将Socket Buffer的数据复制到网卡设备中。
   
   零拷贝技术省去了中间操作系统内核的切换，同时也省去了针对内核进行的两次复制。
   
   零拷贝是相对于内核的零次拷贝。
   
   <mark>Kafka写效率高的原因：</mark>
   
   - 顺序写入，顺序写入磁盘的效率比随机写内存的效率更高
   
   - 页缓存，消息先缓存到页缓存，由操作系统执行刷盘操作
   
   - 零拷贝，数据直接从磁盘复制到网卡设备，只执行两次拷贝过程，中间省去了linux内核模式的切换
