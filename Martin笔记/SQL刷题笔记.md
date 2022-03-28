#### 176.第二高的薪水

Employee表:

| Column Name | Type |
| ----------- | ---- |
| id          | int  |
| salary      | int  |


id 是这个表的主键。
表的每一行包含员工的工资信息。

编写一个 SQL 查询，获取并返回 `Employee` 表中第二高的薪水 。如果不存在第二高的薪水，查询应该返回 `null` 。

```sql
select ifnull((select salary from Employee group by salary order by salar limit 1 offset 1y desc)
, null) as SecondHighestSalary;
```


