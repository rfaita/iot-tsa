package com.iot.tsa.util;

import org.influxdb.InfluxDBMapperException;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;
import org.influxdb.dto.QueryResult;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Main class responsible for mapping a QueryResult to a POJO.
 *
 * @author rfaita
 */
public class CustomInfluxDBResultMapper {

    /**
     * Data structure used to cache classes used as measurements.
     */
    private static final
    ConcurrentMap<String, ConcurrentMap<String, Field>> CLASS_FIELD_CACHE = new ConcurrentHashMap<>();

    private static final int FRACTION_MIN_WIDTH = 0;
    private static final int FRACTION_MAX_WIDTH = 9;
    private static final boolean ADD_DECIMAL_POINT = true;
    private static final String INTERNAL_DEFAULT_FIELD = "___default___";

    /**
     * When a query is executed without {@link TimeUnit}, InfluxDB returns the <tt>time</tt>
     * column as a RFC3339 date.
     */
    private static final DateTimeFormatter RFC3339_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
            .appendFraction(ChronoField.NANO_OF_SECOND, FRACTION_MIN_WIDTH, FRACTION_MAX_WIDTH, ADD_DECIMAL_POINT)
            .appendZoneOrOffsetId()
            .toFormatter();

    /**
     * <p>
     * Process a {@link QueryResult} object returned by the InfluxDB client inspecting the internal
     * data structure and creating the respective object instances based on the Class passed as
     * parameter.
     * </p>
     *
     * @param queryResult the InfluxDB result object
     * @param clazz       the Class that will be used to hold your measurement data
     * @param <T>         the target type
     * @return a {@link List} of objects from the same Class passed as parameter and sorted on the
     * same order as received from InfluxDB.
     * @throws InfluxDBMapperException If {@link QueryResult} parameter contain errors,
     *                                 <tt>clazz</tt> parameter is not annotated with &#64;Measurement or it was not
     *                                 possible to define the values of your POJO (e.g. due to an unsupported field type).
     */
    public <T> List<T> toPOJO(final QueryResult queryResult, final Class<T> clazz) throws InfluxDBMapperException {
        return toPOJO(queryResult, clazz, TimeUnit.MILLISECONDS);
    }

    /**
     * <p>
     * Process a {@link QueryResult} object returned by the InfluxDB client inspecting the internal
     * data structure and creating the respective object instances based on the Class passed as
     * parameter.
     * </p>
     *
     * @param queryResult the InfluxDB result object
     * @param clazz       the Class that will be used to hold your measurement data
     * @param precision   the time precision of results
     * @param <T>         the target type
     * @return a {@link List} of objects from the same Class passed as parameter and sorted on the
     * same order as received from InfluxDB.
     * @throws InfluxDBMapperException If {@link QueryResult} parameter contain errors,
     *                                 <tt>clazz</tt> parameter is not annotated with &#64;Measurement or it was not
     *                                 possible to define the values of your POJO (e.g. due to an unsupported field type).
     */
    public <T> List<T> toPOJO(final QueryResult queryResult, final Class<T> clazz,
                              final TimeUnit precision) throws InfluxDBMapperException {
        throwExceptionIfMissingAnnotation(clazz);
        String measurementName = getMeasurementName(clazz);
        return this.toPOJO(queryResult, clazz, measurementName, precision);
    }

    /**
     * <p>
     * Process a {@link QueryResult} object returned by the InfluxDB client inspecting the internal
     * data structure and creating the respective object instances based on the Class passed as
     * parameter.
     * </p>
     *
     * @param queryResult     the InfluxDB result object
     * @param clazz           the Class that will be used to hold your measurement data
     * @param <T>             the target type
     * @param measurementName name of the Measurement
     * @return a {@link List} of objects from the same Class passed as parameter and sorted on the
     * same order as received from InfluxDB.
     * @throws InfluxDBMapperException If {@link QueryResult} parameter contain errors,
     *                                 <tt>clazz</tt> parameter is not annotated with &#64;Measurement or it was not
     *                                 possible to define the values of your POJO (e.g. due to an unsupported field type).
     */
    public <T> List<T> toPOJO(final QueryResult queryResult, final Class<T> clazz, final String measurementName)
            throws InfluxDBMapperException {
        return toPOJO(queryResult, clazz, measurementName, TimeUnit.MILLISECONDS);
    }

    /**
     * <p>
     * Process a {@link QueryResult} object returned by the InfluxDB client inspecting the internal
     * data structure and creating the respective object instances based on the Class passed as
     * parameter.
     * </p>
     *
     * @param queryResult     the InfluxDB result object
     * @param clazz           the Class that will be used to hold your measurement data
     * @param <T>             the target type
     * @param measurementName name of the Measurement
     * @param precision       the time precision of results
     * @return a {@link List} of objects from the same Class passed as parameter and sorted on the
     * same order as received from InfluxDB.
     * @throws InfluxDBMapperException If {@link QueryResult} parameter contain errors,
     *                                 <tt>clazz</tt> parameter is not annotated with &#64;Measurement or it was not
     *                                 possible to define the values of your POJO (e.g. due to an unsupported field type).
     */
    public <T> List<T> toPOJO(final QueryResult queryResult, final Class<T> clazz, final String measurementName,
                              final TimeUnit precision)
            throws InfluxDBMapperException {

        Objects.requireNonNull(measurementName, "measurementName");
        Objects.requireNonNull(queryResult, "queryResult");
        Objects.requireNonNull(clazz, "clazz");

        throwExceptionIfResultWithError(queryResult);
        cacheMeasurementClass(clazz);

        List<T> result = new LinkedList<T>();

        queryResult.getResults().stream()
                .filter(internalResult -> Objects.nonNull(internalResult) && Objects.nonNull(internalResult.getSeries()))
                .forEach(internalResult -> {
                    internalResult.getSeries().stream()
                            .filter(series -> series.getName().equals(measurementName))
                            .forEachOrdered(series -> {
                                parseSeriesAs(series, clazz, result, precision);
                            });
                });

        return result;
    }

    void throwExceptionIfMissingAnnotation(final Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Measurement.class)) {
            throw new IllegalArgumentException(
                    "Class " + clazz.getName() + " is not annotated with @" + Measurement.class.getSimpleName());
        }
    }

    void throwExceptionIfResultWithError(final QueryResult queryResult) {
        if (queryResult.getError() != null) {
            throw new InfluxDBMapperException("InfluxDB returned an error: " + queryResult.getError());
        }

        queryResult.getResults().forEach(seriesResult -> {
            if (seriesResult.getError() != null) {
                throw new InfluxDBMapperException("InfluxDB returned an error with Series: " + seriesResult.getError());
            }
        });
    }

    ConcurrentMap<String, Field> getColNameAndFieldMap(final Class<?> clazz) {
        return CLASS_FIELD_CACHE.get(clazz.getName());
    }

    void cacheMeasurementClass(final Class<?>... classVarAgrs) {
        for (Class<?> clazz : classVarAgrs) {
            if (CLASS_FIELD_CACHE.containsKey(clazz.getName())) {
                continue;
            }
            ConcurrentMap<String, Field> initialMap = new ConcurrentHashMap<>();
            ConcurrentMap<String, Field> influxColumnAndFieldMap = CLASS_FIELD_CACHE.putIfAbsent(clazz.getName(), initialMap);
            if (influxColumnAndFieldMap == null) {
                influxColumnAndFieldMap = initialMap;
            }

            Class<?> c = clazz;
            while (c != null) {
                for (Field field : c.getDeclaredFields()) {
                    Column colAnnotation = field.getAnnotation(Column.class);
                    UnmappedFields unmappedFieldsAnnotation = field.getAnnotation(UnmappedFields.class);
                    if (colAnnotation != null) {
                        influxColumnAndFieldMap.put(colAnnotation.name(), field);
                    }
                    if (unmappedFieldsAnnotation != null) {
                        if (Map.class.isAssignableFrom(field.getType())) {
                            influxColumnAndFieldMap.put(INTERNAL_DEFAULT_FIELD, field);
                        }
                    }
                }
                c = c.getSuperclass();
            }
        }
    }

    String getMeasurementName(final Class<?> clazz) {
        return ((Measurement) clazz.getAnnotation(Measurement.class)).name();
    }

    String getDatabaseName(final Class<?> clazz) {
        return ((Measurement) clazz.getAnnotation(Measurement.class)).database();
    }

    String getRetentionPolicy(final Class<?> clazz) {
        return ((Measurement) clazz.getAnnotation(Measurement.class)).retentionPolicy();
    }

    TimeUnit getTimeUnit(final Class<?> clazz) {
        return ((Measurement) clazz.getAnnotation(Measurement.class)).timeUnit();
    }

    <T> List<T> parseSeriesAs(final QueryResult.Series series, final Class<T> clazz, final List<T> result) {
        return parseSeriesAs(series, clazz, result, TimeUnit.MILLISECONDS);
    }

    <T> List<T> parseSeriesAs(final QueryResult.Series series, final Class<T> clazz, final List<T> result,
                              final TimeUnit precision) {
        int columnSize = series.getColumns().size();
        ConcurrentMap<String, Field> colNameAndFieldMap = CLASS_FIELD_CACHE.get(clazz.getName());
        try {
            T object = null;

            Field defaultField = colNameAndFieldMap.get(INTERNAL_DEFAULT_FIELD);
            for (List<Object> row : series.getValues()) {
                for (int i = 0; i < columnSize; i++) {
                    Field correspondingField = colNameAndFieldMap.get(series.getColumns().get(i)/*InfluxDB columnName*/);
                    if (correspondingField != null) {
                        if (object == null) {
                            object = clazz.newInstance();
                        }
                        setFieldValue(object, correspondingField, row.get(i), precision);
                    } else if (defaultField != null) {
                        setFieldValueToDefaultField(series.getColumns().get(i), defaultField, object, row.get(i));
                    }
                }
                // When the "GROUP BY" clause is used, "tags" are returned as Map<String,String> and
                // accordingly with InfluxDB documentation
                // https://docs.influxdata.com/influxdb/v1.2/concepts/glossary/#tag-value
                // "tag" values are always String.
                if (series.getTags() != null && !series.getTags().isEmpty()) {
                    for (Entry<String, String> entry : series.getTags().entrySet()) {
                        Field correspondingField = colNameAndFieldMap.get(entry.getKey()/*InfluxDB columnName*/);
                        if (correspondingField != null) {
                            // I don't think it is possible to reach here without a valid "object"
                            setFieldValue(object, correspondingField, entry.getValue(), precision);
                        } else if (defaultField != null) {
                            setFieldValueToDefaultField(entry.getKey(), defaultField, object, entry.getValue());
                        }
                    }
                }
                if (object != null) {
                    result.add(object);
                    object = null;
                }
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new InfluxDBMapperException(e);
        }
        return result;
    }

    /**
     * InfluxDB client returns any number as Double.
     * See https://github.com/influxdata/influxdb-java/issues/153#issuecomment-259681987
     * for more information.
     *
     * @param object
     * @param field
     * @param value
     * @param precision
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    <T> void setFieldValue(final T object, final Field field,
                           final Object value, final TimeUnit precision)
            throws IllegalArgumentException, IllegalAccessException {
        if (value == null) {
            return;
        }
        Class<?> fieldType = field.getType();
        try {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            if (fieldValueModified(fieldType, field, object, value, precision)
                    || fieldValueForPrimitivesModified(fieldType, field, object, value)
                    || fieldValueForPrimitiveWrappersModified(fieldType, field, object, value)) {
                return;
            }
            String msg = "Class '%s' field '%s' is from an unsupported type '%s'.";
            throw new InfluxDBMapperException(
                    String.format(msg, object.getClass().getName(), field.getName(), field.getType()));
        } catch (ClassCastException e) {
            String msg = "Class '%s' field '%s' was defined with a different field type and caused a ClassCastException. "
                    + "The correct type is '%s' (current field value: '%s').";
            throw new InfluxDBMapperException(
                    String.format(msg, object.getClass().getName(), field.getName(), value.getClass().getName(), value));
        }
    }

    <T> boolean fieldValueModified(final Class<?> fieldType, final Field field, final T object, final Object value,
                                   final TimeUnit precision)
            throws IllegalArgumentException, IllegalAccessException {
        if (String.class.isAssignableFrom(fieldType)) {
            field.set(object, String.valueOf(value));
            return true;
        }
        if (Instant.class.isAssignableFrom(fieldType)) {
            Instant instant;
            if (value instanceof String) {
                instant = Instant.from(RFC3339_FORMATTER.parse(String.valueOf(value)));
            } else if (value instanceof Long) {
                instant = Instant.ofEpochMilli(toMillis((long) value, precision));
            } else if (value instanceof Double) {
                instant = Instant.ofEpochMilli(toMillis(((Double) value).longValue(), precision));
            } else if (value instanceof Integer) {
                instant = Instant.ofEpochMilli(toMillis(((Integer) value).longValue(), precision));
            } else {
                throw new InfluxDBMapperException("Unsupported type " + field.getClass() + " for field " + field.getName());
            }
            field.set(object, instant);
            return true;
        }
        return false;
    }

    <T> boolean fieldValueForPrimitivesModified(final Class<?> fieldType, final Field field, final T object,
                                                final Object value) throws IllegalArgumentException, IllegalAccessException {
        if (double.class.isAssignableFrom(fieldType)) {
            field.setDouble(object, ((Double) value).doubleValue());
            return true;
        }
        if (long.class.isAssignableFrom(fieldType)) {
            field.setLong(object, ((Double) value).longValue());
            return true;
        }
        if (int.class.isAssignableFrom(fieldType)) {
            field.setInt(object, ((Double) value).intValue());
            return true;
        }
        if (boolean.class.isAssignableFrom(fieldType)) {
            field.setBoolean(object, Boolean.valueOf(String.valueOf(value)).booleanValue());
            return true;
        }
        return false;
    }

    <T> boolean fieldValueForPrimitiveWrappersModified(final Class<?> fieldType, final Field field, final T object,
                                                       final Object value) throws IllegalArgumentException, IllegalAccessException {
        if (Double.class.isAssignableFrom(fieldType)) {
            field.set(object, value);
            return true;
        }
        if (Long.class.isAssignableFrom(fieldType)) {
            field.set(object, Long.valueOf(((Double) value).longValue()));
            return true;
        }
        if (Integer.class.isAssignableFrom(fieldType)) {
            field.set(object, Integer.valueOf(((Double) value).intValue()));
            return true;
        }
        if (Boolean.class.isAssignableFrom(fieldType)) {
            field.set(object, Boolean.valueOf(String.valueOf(value)));
            return true;
        }
        return false;
    }

    <T> boolean setFieldValueToDefaultField(final String fieldName,
                                            final Field defaultField, final T object,
                                            final Object value) throws IllegalArgumentException, IllegalAccessException {
        if (defaultField != null) {
            if (!defaultField.isAccessible()) {
                defaultField.setAccessible(true);
            }
            if (defaultField.get(object) == null) {
                defaultField.set(object, new HashMap<>());
            }
            ((Map) defaultField.get(object)).put(fieldName, value);
            return true;
        }
        return false;
    }

    private Long toMillis(final long value, final TimeUnit precision) {

        return TimeUnit.MILLISECONDS.convert(value, precision);
    }
}
