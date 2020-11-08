package ru.otus.collection;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        CustomArrayListImplementation<String> collection = new CustomArrayListImplementation<>();
        collection.add("Red");
        collection.add("Green");
        collection.add("Brown");
        System.out.println(Arrays.toString(collection.toArray()));
    }
}
