package reproducer.issue1819.hibernate74;

import reproducer.issue1819.AbstractIssue1819Test;
import reproducer.issue1819.TestDatabase;

public class H2Issue1819Test extends AbstractIssue1819Test {

    @Override
    protected TestDatabase.Type databaseType() {
        return TestDatabase.Type.H2;
    }

    @Override
    protected String expectedHibernateVersionPrefix() {
        return "7.4.";
    }
}
