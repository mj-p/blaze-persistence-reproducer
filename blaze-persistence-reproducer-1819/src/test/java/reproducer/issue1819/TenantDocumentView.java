package reproducer.issue1819;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.UpdatableMapping;

import java.util.Set;

@EntityView(TenantDocument.class)
@UpdatableEntityView
public interface TenantDocumentView {

    @IdMapping
    Long getId();

    @UpdatableMapping
    Set<TagView> getTags();
}
