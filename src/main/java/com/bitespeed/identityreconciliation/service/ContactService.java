package com.bitespeed.identityreconciliation.service;

import com.bitespeed.identityreconciliation.dto.IdentifyRequest;
import com.bitespeed.identityreconciliation.dto.IdentifyResponse;
import com.bitespeed.identityreconciliation.entity.Contact;
import com.bitespeed.identityreconciliation.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ContactService {

    private final ContactRepository repository;

    public IdentifyResponse identify(IdentifyRequest request) {

        String email = request.getEmail();
        String phone = request.getPhoneNumber();

        if (email == null && phone == null) {
            throw new RuntimeException("At least one field is required");
        }

        // 🔥 Step 1: Find all matching contacts (email OR phone)
        List<Contact> matches =
                repository.findByEmailOrPhoneNumber(email, phone);

        // ✅ CASE 1: No match → create primary
        if (matches.isEmpty()) {

            Contact newContact = Contact.builder()
                    .email(email)
                    .phoneNumber(phone)
                    .linkPrecedence("primary")
                    .linkedId(null)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            repository.save(newContact);

            return buildResponse(newContact);
        }

        // 🔥 Step 2: Collect full cluster
        Set<Contact> allContacts = new HashSet<>(matches);

        for (Contact contact : matches) {

            if ("secondary".equals(contact.getLinkPrecedence())) {
                Contact primary =
                        repository.findById(contact.getLinkedId()).orElse(null);
                if (primary != null) {
                    allContacts.add(primary);
                }
            }

            if ("primary".equals(contact.getLinkPrecedence())) {
                List<Contact> secondaries =
                        repository.findByLinkedId(contact.getId());
                allContacts.addAll(secondaries);
            }
        }

        // 🔥 Step 3: Find oldest primary
        Contact primary = allContacts.stream()
                .filter(c -> "primary".equals(c.getLinkPrecedence()))
                .min(Comparator.comparing(Contact::getCreatedAt))
                .orElseThrow();

        // 🔥 Step 4: Merge multiple primaries if needed
        for (Contact contact : allContacts) {
            if ("primary".equals(contact.getLinkPrecedence())
                    && !contact.getId().equals(primary.getId())) {

                contact.setLinkPrecedence("secondary");
                contact.setLinkedId(primary.getId());
                contact.setUpdatedAt(LocalDateTime.now());

                repository.save(contact);
            }
        }

        // 🔥 Step 5: Check if exact combination exists
        boolean alreadyExists = allContacts.stream().anyMatch(c ->
                Objects.equals(c.getEmail(), email) &&
                        Objects.equals(c.getPhoneNumber(), phone)
        );

        // 🔥 Step 6: Create secondary if new info
        if (!alreadyExists) {

            Contact secondary = Contact.builder()
                    .email(email)
                    .phoneNumber(phone)
                    .linkPrecedence("secondary")
                    .linkedId(primary.getId())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            repository.save(secondary);
        }

        return buildResponse(primary);
    }

    private IdentifyResponse buildResponse(Contact primary) {

        List<Contact> related =
                repository.findByLinkedId(primary.getId());

        related.add(primary);

        List<String> emails = related.stream()
                .map(Contact::getEmail)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<String> phones = related.stream()
                .map(Contact::getPhoneNumber)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<Long> secondaryIds = related.stream()
                .filter(c -> "secondary".equals(c.getLinkPrecedence()))
                .map(Contact::getId)
                .collect(Collectors.toList());

        return IdentifyResponse.builder()
                .contact(
                        IdentifyResponse.ContactResponse.builder()
                                .primaryContactId(primary.getId())
                                .emails(emails)
                                .phoneNumbers(phones)
                                .secondaryContactIds(secondaryIds)
                                .build()
                )
                .build();
    }
}