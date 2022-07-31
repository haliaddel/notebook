package org.example.lambda_expr;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class MainTest {
    public <T> List<T> filter(List<T> list, Predicate<T> p) {
        List<T> newArrayList = new ArrayList<>();
        for (T t:
             list) {
            if(p.test(t)) {
                newArrayList.add(t);
            }
        }
        return newArrayList;
    }

    public void test() {
        List<Integer> testList = new ArrayList<>();
        for(int i=1; i< 10; i++) {
            testList.add(i);
        }
        List<Integer> newList = filter(testList, a -> a > 6);
    }

    public void checkTypeAndNonCheckType() {
        // 无类型推断
        Comparator<Integer> c = (Integer a1, Integer a2) -> a1.compareTo(a2);
        // 有类型推断
        Comparator<Integer> c2 = (a1, a2) -> a1.compareTo(a2);
    }

    public void useLocalVar() {
        // lambda 表达式中使用的变量应为 final 或有效 final
        int portNumber = 13337;
        Runnable r = () -> System.out.println(portNumber);
        portNumber = 13338;
    }

}
