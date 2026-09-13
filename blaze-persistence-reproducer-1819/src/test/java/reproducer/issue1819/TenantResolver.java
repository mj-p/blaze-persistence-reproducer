package reproducer.issue1819;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

public class TenantResolver implements CurrentTenantIdentifierResolver<Long> {

    private static final ThreadLocal<Long> CURRENT = ThreadLocal.withInitial(() -> 1L);

    public static void use(long tenantId) {
        CURRENT.set(tenantId);
    }

    @Override
    public Long resolveCurrentTenantIdentifier() {
        return CURRENT.get();
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }
}
