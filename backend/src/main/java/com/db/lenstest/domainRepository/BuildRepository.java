package com.db.lenstest.domainRepository;

import com.db.lenstest.domain.Build;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface BuildRepository extends ReactiveCrudRepository<Build, Long> {
}
