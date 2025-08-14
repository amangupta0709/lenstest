package com.db.lenstest.runner;

import com.db.lenstest.listener.TestRunContext;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.tagexpressions.TagExpressionParser;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import io.cucumber.testng.PickleWrapper;
import org.testng.annotations.DataProvider;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CucumberOptions(
        features = "classpath:features",
        glue = {"com.db.lenstest.config","com.db.lenstest.steps","com.db.lenstest.hooks"},
        plugin = {
                "pretty",
                "com.db.lenstest.listener.CustomCucumberListener",
//                "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:"
        }
)
public class TestCucumberRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        Object[][] scenarios = super.scenarios();
        System.out.println("runner: "+Thread.currentThread().getId());
        String dynamicTags = (String) TestRunContext.get("filterTag");   //passed from Pipeline
        //Or add any other sources you want to get the tags from

        if ( dynamicTags != null && !dynamicTags.isEmpty()) return (Arrays.stream(scenarios).filter(scenario -> TagExpressionParser.parse(dynamicTags).evaluate(((PickleWrapper) scenario[0]).getPickle().getTags())).toList()).toArray(new Object[0][0]);
        else return scenarios;
    }
}