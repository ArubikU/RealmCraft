package dev.arubik.realmcraft.Api;

import java.lang.reflect.Field;

import lombok.Getter;

public class RealReflect {

    public static class RealField<T> {

        private Class<T> objectType;
        @Getter
        private String fieldName;
        @Getter
        private Object clazz;
        private Field field;

        public RealField(Class<T> objectType, String fieldName, Object clazz) {
            this.objectType = objectType;
            this.fieldName = fieldName;
            this.clazz = clazz;
            try {
                this.field = clazz.getClass().getDeclaredField(fieldName);
            } catch (NoSuchFieldException | SecurityException e) {
                e.printStackTrace();
            }
        }

        public Class<T> getObjectType() {
            return objectType;
        }

        public void unfreeze() {
            try {
                field.setAccessible(true);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        public void freeze() {
            try {
                field.setAccessible(false);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        public void setValue(T value) {
            try {
                field.set(clazz, value);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        public void setValueStatic(T value) {
            try {
                field.set(null, value);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        public T getValue() {
            try {
                return (T) field.get(clazz);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }

        public T getValueStatic() {
            try {
                return (T) field.get(null);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }

        public static class RealClazz<T> {

            private Class<T> clazz;
            private Object object;

            public RealClazz(T clazz) {
                this.clazz = (Class<T>) clazz.getClass();
                this.object = clazz;
            }

            public static <T> RealClazz<T> fromObject(T Value) {
                return new RealClazz<T>(Value);
            }

            public Class<T> getClazz() {
                return clazz;
            }

            public void setClazz(Class<T> clazz) {
                this.clazz = clazz;
            }

            public <V> RealField<V> getField(String name, Class<V> type) {
                return new RealField<V>(type, name, object);
            }

            public void setValue(String name, Object value) {
                RealField<Object> field = getField(name, Object.class);
                field.unfreeze();
                field.setValue(value);
            }

            public void setValueStatic(String name, Object value) {
                RealField<Object> field = getField(name, Object.class);
                field.unfreeze();
                field.setValueStatic(value);
            }

            public <V> V getValue(String name, Class<V> type) {
                RealField<V> field = getField(name, type);
                field.unfreeze();
                return field.getValue();
            }
        }
    }

}
