package com.blazebit.persistence.examples.spring.data.webmvc.repository;

import com.blazebit.persistence.examples.spring.data.webmvc.view.PersonView;
import com.blazebit.persistence.spring.data.repository.EntityViewRepository;

public interface PersonViewRepository extends EntityViewRepository<PersonView, Long> {
    PersonView findOne(Long id);
}