package com.iot.tsa.model;

import com.iot.tsa.enums.TimeUnit;
import org.influxdb.dto.BoundParameterQuery;
import org.influxdb.dto.Query;
import org.influxdb.querybuilder.Appendable;
import org.influxdb.querybuilder.SelectQueryImpl;
import org.influxdb.querybuilder.SelectionQueryImpl;
import org.influxdb.querybuilder.WhereQueryImpl;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import static org.influxdb.querybuilder.BuiltQuery.QueryBuilder.*;

public class QueryCriteria {

    private final String table;
    private final String database;
    private final String id;
    private final String tenantId;
    private final String from;
    private final String to;
    private final String[] selectCriteria;
    private final Long intervalValue;
    private final TimeUnit intervalUnit;

    private QueryCriteria(Builder builder) {
        this.table = builder.table;
        this.database = builder.database;
        this.id = builder.id;
        this.tenantId = builder.tenantId;
        this.from = builder.from;
        this.to = builder.to;
        this.selectCriteria = builder.selectCriteria;
        this.intervalValue = builder.intervalValue;
        this.intervalUnit = builder.intervalUnit;
    }

    public Query toQuery() {
        SelectionQueryImpl selection = select();

        if (this.selectCriteria != null) {
            for (String criteria : this.selectCriteria) {
                selection.raw(criteria);
            }
        }

        WhereQueryImpl query = selection
                .from(this.database, this.table)
                .where()
                .and(eq("id", id))
                .and(eq("tenantId", tenantId));



        if (!StringUtils.isEmpty(this.from)) {
            if (Now.isRelativeTime(this.from)) {
                query = query.and(gte("time", new Now(this.from)));
            }else {
                query = query.and(gte("time", this.from));
            }
        }
        if (!StringUtils.isEmpty(this.to)) {
            if (Now.isRelativeTime(this.to)) {
                query = query.and(lte("time", new Now(this.to)));
            }else {
                query = query.and(lte("time", this.to));
            }
        }

        SelectQueryImpl selectQuery = null;
        if (!ObjectUtils.isEmpty(this.intervalValue) && !ObjectUtils.isEmpty(this.intervalUnit)) {
            selectQuery = query.groupBy(time(this.intervalValue, this.intervalUnit.getUnit()));
        }

        return BoundParameterQuery.QueryBuilder
                .newQuery((selectQuery != null ? selectQuery : query).buildQueryString().toString())
                .forDatabase(this.database)
                .create();

    }

    public String getId() {
        return id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String[] getSelectCriteria() {
        return selectCriteria;
    }

    public Long getIntervalValue() {
        return intervalValue;
    }

    public TimeUnit getIntervalUnit() {
        return intervalUnit;
    }

    public static class Builder {
        private String table;
        private String database;
        private String id;
        private String tenantId;
        private String from;
        private String to;
        private String[] selectCriteria;
        private Long intervalValue;
        private TimeUnit intervalUnit;

        public Builder table(String table) {
            this.table = table;
            return this;
        }

        public Builder database(String database) {
            this.database = database;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder from(String from) {
            this.from = from;
            return this;
        }

        public Builder to(String to) {
            this.to = to;
            return this;
        }

        public Builder selectCriteria(String[] selectCriteria) {
            this.selectCriteria = selectCriteria;
            return this;
        }

        public Builder intervalValue(Long intervalValue) {
            this.intervalValue = intervalValue;
            return this;
        }

        public Builder intervalUnit(TimeUnit intervalUnit) {
            this.intervalUnit = intervalUnit;
            return this;
        }

        public QueryCriteria build() {
            return new QueryCriteria(this);
        }
    }

}
