package com.db.lenstest.repository;

import com.db.lenstest.model.TestDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TestDocumentRepository extends MongoRepository<TestDocument, String> {
    // Custom queries can be added here
}
