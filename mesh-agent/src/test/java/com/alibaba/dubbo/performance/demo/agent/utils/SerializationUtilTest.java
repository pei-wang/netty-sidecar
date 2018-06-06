package com.alibaba.dubbo.performance.demo.agent.utils;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SerializationUtilTest {
    @Test
    public void should_serialization_deserialization_objec_success() {
        Foo foo = new Foo(24, "pei");
        byte[] data = SerializationUtil.serialize(foo);
        Foo deserializedFoo = SerializationUtil.deserialize(data, Foo.class);
        assertThat(deserializedFoo.name, is("pei"));
    }

    class Foo {
        int age;
        String name;

        public Foo(int age, String name) {
            this.age = age;
            this.name = name;
        }
    }
}
