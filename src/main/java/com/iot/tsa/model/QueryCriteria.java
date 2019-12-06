package com.iot.tsa.model;

import com.iot.tsa.enums.TimeUnit;
import org.influxdb.dto.BoundParameterQuery;
import org.influxdb.dto.Query;
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
    private final String initialTime;
    private final String finalTime;
    private final String[] selectCriteria;
    private final Long timeGroupByCriteria;
    private final TimeUnit timeGroupByUnit;

    private QueryCriteria(Builder builder) {
        this.table = builder.table;
        this.database = builder.database;
        this.id = builder.id;
        this.tenantId = builder.tenantId;
        this.initialTime = builder.initialTime;
        this.finalTime = builder.finalTime;
        this.selectCriteria = builder.selectCriteria;
        this.timeGroupByCriteria = builder.timeGroupByCriteria;
        this.timeGroupByUnit = builder.timeGroupByUnit;
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



        if (!StringUtils.isEmpty(this.initialTime)) {
            query = query.and(gte("time", this.initialTime));
        }
        if (!StringUtils.isEmpty(this.finalTime)) {
            query = query.and(lte("time", this.finalTime));
        }

        SelectQueryImpl selectQuery = null;
        if (!ObjectUtils.isEmpty(this.timeGroupByCriteria) && !ObjectUtils.isEmpty(this.timeGroupByUnit)) {
            selectQuery = query.groupBy(time(this.timeGroupByCriteria, this.timeGroupByUnit.getUnit()));
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

    public String getInitialTime() {
        return initialTime;
    }

    public String getFinalTime() {
        return finalTime;
    }

    public String[] getSelectCriteria() {
        return selectCriteria;
    }

    public Long getTimeGroupByCriteria() {
        return timeGroupByCriteria;
    }

    public TimeUnit getTimeGroupByUnit() {
        return timeGroupByUnit;
    }

    public static class Builder {
        private String table;
        private String database;
        private String id;
        private String tenantId;
        private String initialTime;
        private String finalTime;
        private String[] selectCriteria;
        private Long timeGroupByCriteria;
        private TimeUnit timeGroupByUnit;

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

        public Builder initialTime(String initialTime) {
            this.initialTime = initialTime;
            return this;
        }

        public Builder finalTime(String finalTime) {
            this.finalTime = finalTime;
            return this;
        }

        public Builder selectCriteria(String[] selectCriteria) {
            this.selectCriteria = selectCriteria;
            return this;
        }

        public Builder timeGroupByCriteria(Long timeGroupByCriteria) {
            this.timeGroupByCriteria = timeGroupByCriteria;
            return this;
        }

        public Builder timeGroupByUnit(TimeUnit timeGroupByUnit) {
            this.timeGroupByUnit = timeGroupByUnit;
            return this;
        }

        public QueryCriteria build() {
            return new QueryCriteria(this);
        }
    }

}
