# Mysql









## 索引



## 性能优化



## InnoDB的锁机制



## 主从复制





## Mysql中遇到的坑

### order by， limit 排序分页数据重复问题

在`Mysql5.6以及5.7`版本（再往上的版本未测试）中，当我们进行排序并且分页的时候，或出现数据重复或丢失的问题。

原因：

> 在MySQL 5.6的以上的版本中，优化器在遇到order by limit语句的时候，做了一个优化，即使用了priority queue。使用 priority queue 的目的，就是在不能使用索引有序性的时候，如果要排序，并且使用了limit n，那么只需要在排序的过程中，保留n条记录即可，这样虽然不能解决所有记录都需要排序的开销，但是只需要 sort buffer 少量的内存就可以完成排序。之所以这些版本出现了第二页数据重复的问题，是因为 priority queue 使用了堆排序的排序方法，而堆排序是一个不稳定的排序方法，也就是相同的值可能排序出来的结果和读出来的数据顺序不一致。5.5 没有这个优化，所以也就不会出现这个问题。



---



解决办法：

>1、 加上索引排序
>​         `select * from table order by xx,id（任意有索引的字段） limit 0,10`
>
>2、给xx字段加上索引
>​         作为验证，您可以在这个字段上加索引 
>
>​	 `alter table tea_course_sort add index(course_sort_order)`
>
>​	然后由于这个表数目太小，以防加索引都未必能用得上，语句修改为
>​       `select * from tea_course_sort  force index(course_sort_order) order by tea_course_sort.course_sort_order desc  limit 0,10;`


​         