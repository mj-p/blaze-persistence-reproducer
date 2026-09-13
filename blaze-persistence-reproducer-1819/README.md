# Blaze-Persistence issue #1819 reproducer

This Maven project reproduces collection-update failures caused by Hibernate filters in Blaze-Persistence's updatable
entity-view save flow.

Each database runs two tests:

1. A parameterized Hibernate `@Filter` excludes another owner, then the selected owner's element collection is updated.
2. Hibernate discriminator multitenancy through `@TenantId` performs the same lookup and update.

Both tests first prove that the other owner cannot be fetched. They then modify a fetched view and call
`EntityViewManager.save()`, which produces the failing collection DELETE and the following INSERT. Direct
`deleteCollection()` calls and references created without a filtered lookup are outside this reproducer's contract.

## Matrix

There are explicit test classes for two Hibernate versions and three distinct delete strategies:

| Module        | Hibernate    | Test classes                                                       |
|---------------|--------------|--------------------------------------------------------------------|
| `hibernate66` | 6.6.54.Final | `H2Issue1819Test`, `PostgreSQLIssue1819Test`, `MySQLIssue1819Test` |
| `hibernate74` | 7.4.5.Final  | `H2Issue1819Test`, `PostgreSQLIssue1819Test`, `MySQLIssue1819Test` |

H2 exercises the owner-removal rewrite and fails on an owner alias that is no longer in scope. PostgreSQL exercises
`DELETE ... USING`, while MySQL exercises its joined DELETE; both retain the owner join but fail because Hibernate
supplies three binders for SQL containing two placeholders.

## Run

The default Blaze-Persistence version is `1.6.20`.

```bash
mvn --fail-at-end test
```

Run one Hibernate version or one database explicitly:

```bash
mvn -pl hibernate66 test
mvn -pl hibernate74 test
mvn -pl hibernate74 -Dtest=PostgreSQLIssue1819Test test
```

Docker is required for PostgreSQL and MySQL. H2 is embedded.

Against the untouched `1.6.20`, all 12 test cases fail:

- H2: invalid owner alias in the collection DELETE.
- PostgreSQL and MySQL: extra filter binder in the collection DELETE.
