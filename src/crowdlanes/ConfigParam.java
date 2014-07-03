package crowdlanes;

import java.io.IOException;
import java.util.Iterator;
import org.ini4j.Wini;

interface ValueStore<T> extends Iterable {

    void parse(String[] parts);
}

public class ConfigParam<T extends Comparable> implements Iterable<T> {

    private ValueStore valueStore;
    private final Wini config;
    private final Object sectionName;

    private final Class<T> clazz;

    public ConfigParam(Wini config, Object sectionName, Class<T> clazz) throws IOException {
        this.clazz = clazz;
        this.config = config;
        this.sectionName = sectionName;
    }

    public static Object toObject(Class clazz, String value) {
        if (Boolean.class == clazz || Boolean.TYPE == clazz) {
            return Boolean.parseBoolean(value);
        }
        if (Short.class == clazz || Short.TYPE == clazz) {
            return Short.parseShort(value);
        }
        if (Integer.class == clazz || Integer.TYPE == clazz) {
            return Integer.parseInt(value);
        }
        if (Long.class == clazz || Long.TYPE == clazz) {
            return Long.parseLong(value);
        }
        if (Float.class == clazz || Float.TYPE == clazz) {
            return Float.parseFloat(value);
        }
        if (Double.class == clazz || Double.TYPE == clazz) {
            return Double.parseDouble(value);
        }

        if (String.class == clazz) {
            return value;
        }

        throw new IllegalArgumentException("Not supported type: " + clazz.getName());
    }

    private class Ranges implements ValueStore, Iterable {

        private Number start;
        private Number stop;
        private Number step;

        public void parse(String[] parts) {
            if (parts.length < 2) {
                throw new IllegalArgumentException("Ilegal format, expected start:stop:[step]");
            }
            start = (Number) toObject(clazz, parts[0]);
            stop = (Number) toObject(clazz, parts[1]);
            step = (Number) (parts.length == 3 ? toObject(clazz, parts[2]) : 1);
        }

        private class IteratorRangesImpl implements Iterator<T> {

            private Number crrRange;

            public IteratorRangesImpl() {
                crrRange = (Number) start;
            }

            @Override
            public boolean hasNext() {
                return ((Comparable) crrRange).compareTo(stop) < 0;
            }

            @Override
            public T next() {
                Number result = crrRange;
                crrRange = crrRange instanceof Short ? crrRange.shortValue() + step.shortValue()
                        : crrRange instanceof Integer ? crrRange.shortValue() + step.shortValue()
                        : crrRange instanceof Long ? crrRange.longValue() + step.longValue()
                        : crrRange instanceof Float ? crrRange.floatValue() + step.floatValue()
                        : crrRange instanceof Double ? crrRange.doubleValue() + step.doubleValue() : null;
                return clazz.cast(result);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }

        @Override
        public Iterator<T> iterator() {
            return new IteratorRangesImpl();
        }
    }

    private class Values implements ValueStore, Iterable {

        private T crrValues;
        private Object[] vals;

        public void parse(String[] parts) {
            vals = new Object[parts.length];
            for (int i = 0; i < parts.length; i++) {
                String p = parts[i];
                vals[i] = p.equals("null") == true ? null : toObject(clazz, p);
            }
        }

        private class IteratorValuesImpl implements Iterator<T> {

            private int idx;

            public IteratorValuesImpl() {
                idx = 0;
            }

            @Override
            public boolean hasNext() {
                return idx < vals.length;
            }

            @Override
            public T next() {
                return (T) vals[idx++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        }

        @Override
        public Iterator<T> iterator() {
            return new IteratorValuesImpl();
        }

    }

    public void read(Object optionName) {
        String param = config.get(sectionName, optionName, String.class);
        String parts[] = param.split(":");
        if (parts.length == 1) {
            parts = param.split(",");
            valueStore = new Values();
        } else {
            valueStore = new Ranges();
        }

        valueStore.parse(parts);
    }

    public Iterator<T> iterator() {
        return valueStore.iterator();
    }
}
