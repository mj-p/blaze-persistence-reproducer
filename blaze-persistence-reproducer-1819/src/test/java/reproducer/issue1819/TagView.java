package reproducer.issue1819;

import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;

@EntityView(Tag.class)
@CreatableEntityView
@UpdatableEntityView
public interface TagView {

    String getKey();

    void setKey(String key);

    String getValue();

    void setValue(String value);
}
