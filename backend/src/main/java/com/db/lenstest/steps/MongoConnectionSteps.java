package com.db.lenstest.steps;

import com.db.lenstest.model.TestDocument;
import com.db.lenstest.repository.TestDocumentRepository;
import io.cucumber.java.en.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.dao.DataAccessResourceFailureException;
import static org.junit.jupiter.api.Assertions.*;

public class MongoConnectionSteps {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private TestDocumentRepository repository;

    private boolean connectionSuccess;
    private Exception connectionError;
    private String testCollection = "test_connection";


    @When("I connect to MongoDB")
    public void connectToMongoDB() {
        try {
            // Actual connection is handled by Spring's MongoTemplate
            mongoTemplate.getDb().listCollectionNames().first();
            connectionSuccess = true;
        } catch (DataAccessResourceFailureException e) {
            connectionError = e;
            connectionSuccess = false;
        }
    }


    @And("I insert a test document with id {string}")
    public void iInsertATestDocumentWithId(String id) {
        repository.save(new TestDocument(id,"lalalalalala"));
    }

    @Then("I should find {int} document with id {string}")
    public void iShouldFindDocumentWithId(int expectedCount, String id) throws InterruptedException {
        long count = repository.findById(id).stream().count();
//        if(id.equals("test-1")){
//            Thread.sleep(15000);
//        } else if(id.equals("test-2")){
//            Thread.sleep(45000);
//        }
//        throw new RuntimeException("aaa");
        assertEquals(expectedCount,count);
    }

    @And("I should be able to delete the document with id {string}")
    public void iShouldBeAbleToDeleteTheDocumentWithId(String id) {
        repository.deleteById(id);
    }

    @Given("id is {string}")
    public void idIs(String id) {
    }
}