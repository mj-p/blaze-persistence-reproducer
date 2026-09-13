package reproducer.issue1819.hibernate66;

import reproducer.issue1819.AbstractIssue1819Test;
import reproducer.issue1819.TestDatabase;

public class PostgreSQLIssue1819Test extends AbstractIssue1819Test {

    @Override
    protected TestDatabase.Type databaseType() {
        return TestDatabase.Type.POSTGRESQL;
    }

    @Override
    protected String expectedHibernateVersionPrefix() {
        return "6.6.";
    }
}
