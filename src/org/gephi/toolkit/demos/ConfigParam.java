package org.gephi.toolkit.demos;

import java.io.IOException;
import java.util.Iterator;
import org.ini4j.Wini;

public class ConfigParam<T extends Comparable> implements Iterable<T> {

    private final Wini config;
    private final Object sectionName;
    private Number start;
    private Number stop;
    private Number step;
    private Number crrRange;
    private T crrValues;
    private Object[] vals;
    private boolean areValues;
    private boolean areRanges;
    private final Class<T> clazz;

    public ConfigParam(Wini config, Object sectionName, Class<T> clazz) throws IOException {
        this.clazz = clazz;
        this.config = config;
        this.sectionName = sectionName;
    }

    private void parseRanges(String[] parts) {
        if (parts.length < 2) {
            throw new IllegalArgumentException("Ilegal format, expected start:stop:[step]");
        }

        if (clazz.equals(Double.class) || clazz.equals(double.class)) {
            start = Double.parseDouble(parts[0]);
            stop = Double.parseDouble(parts[1]);
            step = parts.length == 3 ? Double.parseDouble(parts[2]) : 1.0d;
        } else if (clazz.equals(Float.class) || clazz.equals(float.class)) {
            start = Float.parseFloat(parts[0]);
            stop = Float.parseFloat(parts[1]);
            step = parts.length == 3 ? Float.parseFloat(parts[2]) : 1.0f;
        } else if (clazz.equals(Integer.class) || clazz.equals(int.class)) {
            start = Integer.parseInt(parts[0]);
            stop = Integer.parseInt(parts[1]);
            step = parts.length == 3 ? Integer.parseInt(parts[2]) : 1;
        } else {
            throw new IllegalArgumentException("Not supported type: " + clazz.getName());
        }
    }

    private void parseValues(String[] parts) {
        vals = new Object[parts.length];
        int i = 0;
        for (String t : parts) {
            if (clazz.equals(Double.class) || clazz.equals(double.class)) {
                vals[i++] = Double.parseDouble(t);
            } else if (clazz.equals(Float.class) || clazz.equals(float.class)) {
                vals[i++] = Float.parseFloat(t);
            } else if (clazz.equals(Integer.class) || clazz.equals(int.class)) {
                vals[i++] = Integer.parseInt(t);
            } else if (clazz.equals(Boolean.class) || clazz.equals(int.class)) {
                vals[i++] = Boolean.parseBoolean(t);
            } else {
                vals[i++] = clazz.cast(t);
            }
        }
    }

    public void read(Object optionName) {
        String param = config.get(sectionName, optionName, String.class);
        String parts[] = param.split(":");
        if (parts.length == 1) {
            areValues = true;
            parts = param.split(",");
            parseValues(parts);
        } else {
            areRanges = true;
            parseRanges(parts);
        }
    }

    public Iterator<T> iterator() {
        return new IteratorImpl();
    }

    private class IteratorImpl implements Iterator<T> {

        private int i;

        public IteratorImpl() {
            crrRange = (Number) start;
            i = 0;
        }

        public boolean hasNext() {
            if (areRanges) {
                return ((Comparable) crrRange).compareTo(stop) < 0;
            } else {
                return i < vals.length;
            }
        }

        public T next() {
            if (areValues) {
                crrValues = (T) vals[i++];
                return crrValues;
            }

            Number result = crrRange;
            if (crrRange instanceof Integer) {
                crrRange = crrRange.intValue() + step.intValue();
            } else if (crrRange instanceof Float) {
                crrRange = crrRange.floatValue() + step.floatValue();
            } else if (crrRange instanceof Double) {
                crrRange = crrRange.doubleValue() + step.doubleValue();
            }

            return (T) result;
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
}
