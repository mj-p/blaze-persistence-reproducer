package reproducer.issue1819;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import org.hibernate.annotations.TenantId;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "tenant_doc")
public class TenantDocument {

    @Id
    private Long id;

    @TenantId
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @ElementCollection
    @CollectionTable(name = "tenant_doc_tag", joinColumns = @JoinColumn(name = "doc_id"))
    private Set<Tag> tags = new LinkedHashSet<>();

    protected TenantDocument() {
    }

    public TenantDocument(Long id, Tag... tags) {
        this.id = id;
        for (Tag tag : tags) {
            this.tags.add(tag);
        }
    }

    public Long getId() {
        return id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Set<Tag> getTags() {
        return tags;
    }
}
