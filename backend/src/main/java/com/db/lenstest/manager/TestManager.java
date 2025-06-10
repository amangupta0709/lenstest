package com.db.lenstest.manager;

import com.db.lenstest.config.ResultPublisher;
import com.db.lenstest.domain.Test;
import com.db.lenstest.domainRepository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class TestManager {
    @Autowired
    private TestRepository testRepository;

    @Autowired
    private ResultPublisher publisher;



//    public Test getTestWithChildren(Test test) {
//        return testRepository.findById(nodeId)
//                .flatMap(node -> buildTree(node, 1));
//    }
//
//    private Mono<Node> buildTree(Node node, int level) {
//        if (level >= 3) {
//            return Mono.just(node); // limit depth
//        }
//
//        return nodeRepository.findByParentId(node.getId())
//                .flatMap(child -> buildTree(child, level + 1))
//                .collectList()
//                .map(children -> {
//                    node.setChildren(children);
//                    return node;
//                });
//    }

}
