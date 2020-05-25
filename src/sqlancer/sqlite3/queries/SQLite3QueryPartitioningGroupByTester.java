package sqlancer.sqlite3.queries;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import sqlancer.DatabaseProvider;
import sqlancer.Randomly;
import sqlancer.TestOracle;
import sqlancer.sqlite3.SQLite3Provider.SQLite3GlobalState;
import sqlancer.sqlite3.SQLite3Visitor;
import sqlancer.sqlite3.ast.SQLite3Expression;
import sqlancer.sqlite3.ast.SQLite3Expression.SQLite3ColumnName;

public class SQLite3QueryPartitioningGroupByTester extends SQLite3QueryPartitioningBase {

	public SQLite3QueryPartitioningGroupByTester(SQLite3GlobalState state) {
		super(state);
	}

	@Override
	public void check() throws SQLException {
		super.check();
		select.setGroupByClause(select.getFetchColumns());
		select.setWhereClause(null);
		String originalQueryString = SQLite3Visitor.asString(select);

		List<String> resultSet = DatabaseProvider.getResultSetFirstColumnAsString(originalQueryString, errors,
				state.getConnection(), state);

		select.setWhereClause(predicate);
		String firstQueryString = SQLite3Visitor.asString(select);
		select.setWhereClause(negatedPredicate);
		String secondQueryString = SQLite3Visitor.asString(select);
		select.setWhereClause(isNullPredicate);
		String thirdQueryString = SQLite3Visitor.asString(select);
		List<String> combinedString = new ArrayList<>();
		List<String> secondResultSet = TestOracle.getCombinedResultSetNoDuplicates(firstQueryString, secondQueryString,
				thirdQueryString, combinedString, true, state, errors);
		TestOracle.assumeResultSetsAreEqual(resultSet, secondResultSet, originalQueryString, combinedString, state);
	}

	@Override
	List<SQLite3Expression> generateFetchColumns() {
		List<SQLite3Expression> columns = new ArrayList<>();
		columns = Randomly.nonEmptySubset(targetTables.getColumns()).stream().map(c -> new SQLite3ColumnName(c, null))
				.collect(Collectors.toList());
		return columns;
	}

}