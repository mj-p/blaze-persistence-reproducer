package reproducer.issue1819.hibernate74;

import reproducer.issue1819.AbstractIssue1819Test;
import reproducer.issue1819.TestDatabase;

public class MySQLIssue1819Test extends AbstractIssue1819Test {

    @Override
    protected TestDatabase.Type databaseType() {
        return TestDatabase.Type.MYSQL;
    }

    @Override
    protected String expectedHibernateVersionPrefix() {
        return "7.4.";
    }
}
