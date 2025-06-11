package com.db.lenstest.config;


import com.db.lenstest.LenstestApplication;
import com.db.lenstest.domain.Build;
import com.db.lenstest.domain.Test;
import com.db.lenstest.domain.TestLevel;
import com.db.lenstest.domainRepository.BuildRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.testng.internal.TestResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class ResultPublisher {

    @Autowired
    private BuildRepository buildRepository;

    private static final Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();

    public static void publishOnScenarioCompletion(Test scenario, Build build) {
        ResultPublisher resultPublisher = SpringContext.getBean(ResultPublisher.class);
        resultPublisher.saveToRepositoryAndPublish(scenario.getParent(), build);
    }

    public static void publishOnBuildCompletion(Build build) {
        ResultPublisher resultPublisher = SpringContext.getBean(ResultPublisher.class);
        resultPublisher.saveToRepositoryAndPublish(build);
    }

    public Flux<String> getTestResultStream() {
        return sink.asFlux();
    }

    public void saveToRepositoryAndPublish(Test feature, Build build) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            build.setStatsSummaryJson(mapper.writeValueAsString(build.getStats()));
            build.setTagSummaryJson(mapper.writeValueAsString(build.getTagStats()));
            feature.setTagsJson(mapper.writeValueAsString(feature.getTags()));
            feature.setChildrenJson(mapper.writeValueAsString(feature.getChildren()));
            for (Test child : feature.getChildren()) {
                child.setChildrenJson(mapper.writeValueAsString(child.getChildren()));
            }
            build.getTests().add(feature);
            build.setTestDetails(mapper.writeValueAsString(feature));

//            reportGenerator(build);

            buildRepository.save(build)
                    .doOnSuccess(b -> sink.tryEmitNext(mapper.valueToTree(b).toPrettyString()))
                    .subscribe();
        } catch (JsonProcessingException ignore) {

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void saveToRepositoryAndPublish(Build build) {
        ObjectMapper mapper = new ObjectMapper();

        buildRepository.save(build)
                .doOnSuccess(b -> sink.tryEmitNext(mapper.valueToTree(b).toPrettyString()))
                .subscribe();
    }

    public static void reportGenerator(Build build) throws Exception {

        // 1. Configure FreeMarker
        //
        // You should do this ONLY ONCE, when your application starts,
        // then reuse the same Configuration object elsewhere.

        Configuration cfg = new Configuration();
        build.getStats().keySet().forEach(l ->build.getStats().get(l).getPassed());

        // Where do we load the templates from:
        cfg.setClassForTemplateLoading(LenstestApplication.class, "templates");

        // Some other recommended settings:
        cfg.setIncompatibleImprovements(new Version(2, 3, 20));
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.US);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        // 2. Proccess template(s)
        //
        // You will do this for several times in typical applications.

        // 2.1. Prepare the template input:

//        Map<String, Object> input = new HashMap<String, Object>();

//        input.put("title", "Vogella example");
//
//        input.put("exampleObject", new ValueExampleObject("Java object", "me"));
//
//        List<ValueExampleObject> systems = new ArrayList<ValueExampleObject>();
//        systems.add(new ValueExampleObject("Android", "Google"));
//        systems.add(new ValueExampleObject("iOS States", "Apple"));
//        systems.add(new ValueExampleObject("Ubuntu", "Canonical"));
//        systems.add(new ValueExampleObject("Windows7", "Microsoft"));
//        input.put("systems", systems);

        // 2.2. Get the template

        Template template = cfg.getTemplate("index.ftl");

        Map<String,Object> model = new HashMap<>();
        model.put("build",build);

        // 2.3. Generate the output

        // Write output to the console
        Writer consoleWriter = new OutputStreamWriter(System.out);
        template.process(model, consoleWriter);

        // For the sake of example, also write output into a file:
        Writer fileWriter = new FileWriter(new File("output.html"));
        try {
            template.process(build, fileWriter);
        } finally {
            fileWriter.close();
        }
    }
}
