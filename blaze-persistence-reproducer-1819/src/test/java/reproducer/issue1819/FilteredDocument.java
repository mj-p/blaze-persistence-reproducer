package reproducer.issue1819;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "filtered_doc")
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = Long.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class FilteredDocument {

    @Id
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @ElementCollection
    @CollectionTable(name = "filtered_doc_tag", joinColumns = @JoinColumn(name = "doc_id"))
    private Set<Tag> tags = new LinkedHashSet<>();

    protected FilteredDocument() {
    }

    public FilteredDocument(Long id, Long tenantId, Tag... tags) {
        this.id = id;
        this.tenantId = tenantId;
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
