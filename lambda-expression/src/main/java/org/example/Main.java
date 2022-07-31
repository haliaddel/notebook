package org.example;

import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class Main {

    static class Apple {
        private String color;
        private Integer weight;

        public void setColor(String color) {
            this.color = color;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        public Apple(String color, int weight){
            this.color = color;
            this.weight = weight;
        }

        public String getColor() {
            return color;
        }

        public Integer getWeight() {
            return weight;
        }
    }

    public static void main(String[] args) {
        Comparator<Apple> comparator = (Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight());
        Apple appleA = new Apple("green", 100);
        Apple appleB = new Apple("green", 150);
        System.out.println(comparator.compare(appleA, appleB));
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("a");
        list.add("a");
        List<String> newlist = list.stream().map(item -> item + " fuck").collect(Collectors.toList());
        newlist.forEach(item -> System.out.println(item));
    }
}