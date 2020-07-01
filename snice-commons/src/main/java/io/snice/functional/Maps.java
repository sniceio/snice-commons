package io.snice.functional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Maps {

    /**
     * Quite specialized helper function whose sole purpose is to deal with maps that has the following
     * structure (and perhaps was created using Lightbends Configuration objects):
     *
     * <pre>
     * {
     *     "whatever" = {
     *         "hello" = "world"
     *         "one" = "two"
     *     }
     *     "something" = {
     *         "yes" = "no"
     *     }
     * }
     * </pre>
     *
     * And what you want to do is to take the "whatever", which is the name for the subsequent map (the
     * ones that contain the keys "hello" and "one) and flatten it down into the same map like so:
     * <pre>
     *     {
     *         "name" = "whatever
     *         "hello" = "world"
     *         "one" = "two"
     *     }
     * </pre>
     *
     * Read https://github.com/lightbend/config and HOCON and it probably will make more sense.
     *
     * @param keyName the name of the new key we will insert into the flattened map
     * @param map the map of maps to flatten.
     * @return a list of flattened maps where the original outer map and it's single key has been merged
     * as an entry into the original sub-map under the key name of <keyName>
     */
    public static List<Map<String, Object>> flatten(final String keyName, final Map<String, Map<String, Object>> map) {
        return map.entrySet().stream().map(e -> {
            final Map<String, Object> flattened = e.getValue();
            flattened.put(keyName, e.getKey());
            return flattened;
        }).collect(Collectors.toList());
    }

    public static List<Map<String, Object>> flatten(final String keyName, final Object map) {
        try {
            return flatten(keyName, (Map<String, Map<String, Object>>) map);
        } catch (final ClassCastException e) {
            throw new IllegalArgumentException("The given Object must be of type Map<String, Map<String, Object>>");
        }
    }
}
