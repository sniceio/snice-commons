package io.snice.functional;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MapsTest {

    @Test
    public void testFlatten() {
        final Map<String, Map<String, Object>> map = new HashMap<>();
        final Map<String, Object> nested1 = new HashMap<>();
        final Map<String, Object> nested2 = new HashMap<>();

        nested1.put("one", "aaa");
        nested1.put("two", "bbb");

        nested2.put("foo", "woo");
        nested2.put("boo", "zoo");

        map.put("hello", nested1);
        map.put("whatever", nested2);

        final List<Map<String, Object>> flatten = Maps.flatten("name", map);
        assertThat(flatten.size(), is(2));

        final Map<String, Object> one = flatten.get(0);
        assertThat(one.get("name"), is("hello"));
        assertThat(one.get("one"), is("aaa"));
        assertThat(one.get("two"), is("bbb"));

        final Map<String, Object> two = flatten.get(1);
        assertThat(two.get("name"), is("whatever"));
        assertThat(two.get("foo"), is("woo"));
        assertThat(two.get("boo"), is("zoo"));
    }

}