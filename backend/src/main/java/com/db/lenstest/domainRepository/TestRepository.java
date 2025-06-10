package com.db.lenstest.domainRepository;

import com.db.lenstest.domain.Test;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface TestRepository extends ReactiveCrudRepository<Test, Long> {
}
