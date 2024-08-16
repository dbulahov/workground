package mondrian.rolap.util;

import static mondrian.rolap.util.JoinUtil.getLeftAlias;
import static mondrian.rolap.util.JoinUtil.getRightAlias;
import static mondrian.rolap.util.JoinUtil.left;
import static mondrian.rolap.util.JoinUtil.right;

import java.util.List;

import org.eclipse.daanse.rolap.mapping.api.model.AggregationExcludeMapping;
import org.eclipse.daanse.rolap.mapping.api.model.AggregationMeasureMapping;
import org.eclipse.daanse.rolap.mapping.api.model.AggregationNameMapping;
import org.eclipse.daanse.rolap.mapping.api.model.AggregationPatternMapping;
import org.eclipse.daanse.rolap.mapping.api.model.AggregationTableMapping;
import org.eclipse.daanse.rolap.mapping.api.model.InlineTableColumnDefinitionMapping;
import org.eclipse.daanse.rolap.mapping.api.model.InlineTableQueryMapping;
import org.eclipse.daanse.rolap.mapping.api.model.InlineTableRowCellMapping;
import org.eclipse.daanse.rolap.mapping.api.model.InlineTableRowMappingMapping;
import org.eclipse.daanse.rolap.mapping.api.model.JoinQueryMapping;
import org.eclipse.daanse.rolap.mapping.api.model.QueryMapping;
import org.eclipse.daanse.rolap.mapping.api.model.SQLMapping;
import org.eclipse.daanse.rolap.mapping.api.model.TableQueryMapping;
import org.eclipse.daanse.rolap.mapping.api.model.TableQueryOptimizationHintMapping;
import org.eclipse.daanse.rolap.mapping.pojo.AggregationExcludeMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.AggregationMeasureMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.AggregationNameMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.AggregationPatternMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.AggregationTableMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.InlineTableColumnDefinitionMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.InlineTableQueryMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.InlineTableRowCellMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.InlineTableRowMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.JoinQueryMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.JoinedQueryElementMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.QueryMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.SQLMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.TableQueryMappingImpl;
import org.eclipse.daanse.rolap.mapping.pojo.TableQueryOptimizationHintMappingImpl;

import mondrian.olap.Util;

public class PojoUtil {
	public static final String BAD_RELATION_TYPE = "bad relation type ";
	private PojoUtil() {
        // constructor
    }

	public static List<SQLMappingImpl> getSqls(List<? extends SQLMapping> sqls) {
		if (sqls != null) {
			sqls.stream().map(s -> getSql(s)).toList();
		}
		return List.of();
	}

	private static SQLMappingImpl getSql(SQLMapping s) {
		if (s != null) {
			return SQLMappingImpl.builder()
					.withDialects(s.getDialects())
					.withStatement(s.getStatement())
					.build();
		}
		return null;
	}

    /**
     * Copies a {@link QueryMapping}.
     *
     * @param relation A table or a join
     */
    public static QueryMappingImpl copy(
        QueryMapping relation)
    {
        if (relation instanceof TableQueryMapping table) {
        	SQLMappingImpl sqlMappingImpl = getSql(table.getSqlWhereExpression());
        	List<AggregationExcludeMappingImpl> aggregationExcludes = getAggregationExcludes(table.getAggregationExcludes());
            List<TableQueryOptimizationHintMappingImpl> optimizationHints = getOptimizationHints(table.getOptimizationHints());
            List<AggregationTableMappingImpl> aggregationTables = getAggregationTables(table.getAggregationTables());

            return TableQueryMappingImpl.builder()
            		.withAlias(table.getAlias())
            		.withName(table.getName())
            		.withSchema(table.getSchema())
            		.withSqlWhereExpression(sqlMappingImpl)
            		.withAggregationExcludes(aggregationExcludes)
            		.withOptimizationHints(optimizationHints)
            		.withAggregationTables(aggregationTables)
            		.build();

        } else if (relation instanceof InlineTableQueryMapping table) {
            return InlineTableQueryMappingImpl.builder()
            		.withAlias(table.getAlias())
            		.withColumnDefinitions(getColumnDefinitions(table.getColumnDefinitions()))
            		.withRows(getRows(table.getRows()))
            		.build();

        } else if (relation instanceof JoinQueryMapping join) {
            QueryMappingImpl left = copy(left(join));
            QueryMappingImpl right = copy(right(join));
            return JoinQueryMappingImpl.builder()
            		.withLeft(JoinedQueryElementMappingImpl.builder().withAlias(getLeftAlias(join)).withKey(join.getLeft().getKey()).withQuery(left).build())
            		.withRight(JoinedQueryElementMappingImpl.builder().withAlias(getRightAlias(join)).withKey(join.getRight().getKey()).withQuery(right).build())
            		.build();
        } else {
            throw Util.newInternal(BAD_RELATION_TYPE + relation);
        }
    }


    public static List<InlineTableColumnDefinitionMappingImpl> getColumnDefinitions(
			List<? extends InlineTableColumnDefinitionMapping> columnDefinitions) {
    	if (columnDefinitions != null) {
    		return columnDefinitions.stream().map(cd -> InlineTableColumnDefinitionMappingImpl.builder()
    				.withName(cd.getName()).withType(cd.getDataType()).build()).toList();
    	}
		return List.of();
	}

	private static List<AggregationTableMappingImpl> getAggregationTables(
			List<? extends AggregationTableMapping> aggregationTables) {
		if (aggregationTables != null) {
			return aggregationTables.stream().map(c -> getAggregationTable(c)).toList();
		}
		return List.of();
	}

	private static AggregationTableMappingImpl getAggregationTable(AggregationTableMapping a) {
		//TODO
		if (a instanceof AggregationNameMapping anm) {
			return AggregationNameMappingImpl.builder()
					.withApproxRowCount(anm.getApproxRowCount())
					.withName(anm.getName())
					.build();
		}
		if (a instanceof AggregationPatternMapping apm) {
			return AggregationPatternMappingImpl.builder()
					.withPattern(apm.getPattern())
					.withAggregationMeasures(getAggregationMeasures(apm.getAggregationMeasures()))
					.build();
		}
		return null;
	}

	private static List<AggregationMeasureMappingImpl> getAggregationMeasures(
			List<? extends AggregationMeasureMapping> aggregationMeasures) {
		if (aggregationMeasures != null) {
			return aggregationMeasures.stream().map(c -> AggregationMeasureMappingImpl.builder()
					.withColumn(c.getColumn())
					.withName(c.getName())
					.withRollupType(c.getRollupType())
					.build()).toList();
		}
		return List.of();
	}

	public static List<TableQueryOptimizationHintMappingImpl> getOptimizationHints(
			List<? extends TableQueryOptimizationHintMapping> optimizationHints) {
		if (optimizationHints != null) {
			return optimizationHints.stream().map(c -> TableQueryOptimizationHintMappingImpl.builder()
					.withValue(c.getValue())
					.withType(c.getType())
					.build()).toList();
		}
		return List.of();
	}


	private static List<AggregationExcludeMappingImpl> getAggregationExcludes(
			List<? extends AggregationExcludeMapping> aggregationExcludes) {
    	if (aggregationExcludes != null) {
    		return aggregationExcludes.stream().map(a ->
    		AggregationExcludeMappingImpl.builder()
    		.withIgnorecase(a.isIgnorecase())
    		.withName(a.getName())
    		.withPattern(a.getPattern())
    		.withId(a.getId())
    		.build()).toList();
    	}
    	return List.of();
	}

	private static List<InlineTableRowCellMappingImpl> getCells(List<? extends InlineTableRowCellMapping> cells) {
		if (cells != null) {
			return cells.stream().map(c -> InlineTableRowCellMappingImpl.builder().withValue(c.getValue()).withColumnName(c.getColumnName()).build()).toList();
		}
		return List.of();
	}

	public static List<InlineTableRowMappingImpl> getRows(List<? extends InlineTableRowMappingMapping> rows) {
    	if (rows != null) {
    		return rows.stream().map(r -> InlineTableRowMappingImpl.builder().withCells(getCells(r.getCells())).build()).toList();
    	}
    	return List.of();
	}

}
