package com.blazebit.persistence.examples.spring.data.webmvc.view;

import com.blazebit.persistence.examples.spring.data.webmvc.model.Person;
import com.blazebit.persistence.view.*;

import java.util.List;

@EntityView(Person.class)
public interface PersonView {
    @IdMapping
    Long getId();

    String getName();

    @Limit(limit = "4", order =  {"age DESC", "id DESC"})
    @Mapping(fetch = FetchStrategy.SUBSELECT)
    List<CatSimpleView> getKittens();
}
