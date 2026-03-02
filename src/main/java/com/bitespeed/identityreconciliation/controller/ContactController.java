package com.bitespeed.identityreconciliation.controller;

import com.bitespeed.identityreconciliation.dto.IdentifyRequest;
import com.bitespeed.identityreconciliation.dto.IdentifyResponse;
import com.bitespeed.identityreconciliation.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/identify")
public class ContactController {

    private final ContactService service;

    @PostMapping
    public ResponseEntity<IdentifyResponse> identify(
            @RequestBody IdentifyRequest request) {

        return ResponseEntity.ok(service.identify(request));
    }
}
