package mondrian.rolap;

import java.util.List;

import org.eclipse.daanse.rolap.mapping.api.model.SQLExpressionMapping;
import org.eclipse.daanse.rolap.mapping.api.model.SQLMapping;
import org.eclipse.daanse.rolap.mapping.pojo.SQLMappingImpl;

public class Column implements SQLExpressionMapping {

    private String table;
    private String name;


	public Column(String table, String name) {
        this.table = table;
        this.name = name;
    }

    public String getTable() {
	    return table;
    }

    public String getName() {
	    return name;
    }

    @Override
	public List<? extends SQLMapping> getSqls() {
		return List.of(SQLMappingImpl.builder()
				.withStatement( table == null ? name : new StringBuilder(table).append(".").append(name).toString())
				.withDialects(List.of("generic"))
				.build());
	}

	public void setTable(String table) {
		 this.table =  table;
	}

}
