package com.test.elk.userservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

import javax.naming.ServiceUnavailableException;
import java.util.List;

@RestController
public class TestDiscoveryController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @GetMapping("/discovery-client/{serviceName}")
    public ResponseEntity<List<ServiceInstance>> discovery(@PathVariable String serviceName) throws RestClientException, ServiceUnavailableException {
        return ResponseEntity.ok(discoveryClient.getInstances(serviceName));
    }
}
