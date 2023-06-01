package com.bitespeed.task.service;

import com.bitespeed.task.dto.ContactDTO;
import com.bitespeed.task.dto.IdentifyRequestDto;
import com.bitespeed.task.dto.IdentifyResponseDto;
import com.bitespeed.task.exceptions.InvalidInputException;
import com.bitespeed.task.models.Contact;
import com.bitespeed.task.enums.LinkPrecedenceType;
import com.bitespeed.task.repository.ContactRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ContactService {

    private final ContactRepository contactRepository;

    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }


    public IdentifyResponseDto identifyContacts(IdentifyRequestDto identifyRequestDto) {

//       basic validation
        if(identifyRequestDto.getPhoneNumber() == null&&(identifyRequestDto.getEmail()==null||identifyRequestDto.getEmail().isEmpty()))
        {
            throw  new InvalidInputException("Both Email or Phone No provided are null or blank kindly send atleast one valid value");
        }

        String email = identifyRequestDto.getEmail();
        String phoneNumber = (identifyRequestDto.getPhoneNumber() == null) ? null : Long.toString(identifyRequestDto.getPhoneNumber());

        List<Contact> matchingContacts = new ArrayList<>();
        HashSet<Long> primaryIdSet = new HashSet<>();

        if (email != null) {
            List<Contact> contacts = contactRepository.findByEmail(email);
            processContacts(contacts, primaryIdSet);
        }
        if (phoneNumber != null) {
            List<Contact> contacts = contactRepository.findByPhoneNumber(phoneNumber);
            processContacts(contacts, primaryIdSet);
        }

        for (Long id : primaryIdSet) {
//            adding the primary contact
            matchingContacts.add(contactRepository.findById(id).orElse(null));

//            adding the secondary contacts
            matchingContacts.addAll(contactRepository.findAllByLinkedId(id));
        }

        //create new entry in db as there are no related contacts present in db
        if (matchingContacts.isEmpty()) {
            Contact newContact = createNewContract(phoneNumber, email, LinkPrecedenceType.primary, null);
            matchingContacts.add(newContact);
            return createResponse(matchingContacts.get(0), matchingContacts);
        } else {
            matchingContacts.sort(Comparator.comparing(Contact::getCreatedAt));
            Contact primaryContact = matchingContacts.get(0);

            // as we have got some results so if any one of email or phonenumber is null means we got the result form other half
            //so no need to create new entry just return the response
            if (email == null || phoneNumber == null) {
                return createResponse(primaryContact, matchingContacts);
            }
            boolean email_flag = false;
            boolean phone_flag = false;
            for (Contact matchingContact : matchingContacts) {
                if (email.equals(matchingContact.getEmail())) {
                    email_flag = true;
                }
                if (phoneNumber.equals(matchingContact.getPhoneNumber())) {
                    phone_flag = true;
                }

                // we got a exact match so no need to create new entry
                if (email.equals(matchingContact.getEmail()) &&
                        phoneNumber.equals(matchingContact.getPhoneNumber())) {
                    return createResponse(primaryContact, matchingContacts);
                }
            }

            // we are here that means  we have  encountered both email or phone i.e phone no and email belongs to
            // two diffrent primary contact otherwish we would have got and exact match so no need to create new entry just modify the relationship
            if (email_flag && phone_flag) {
                for (Contact contact : matchingContacts) {
                    if (!Objects.equals(contact.getId(), primaryContact.getId())) {
                        contact.setLinkedId(primaryContact.getId());
                        contact.setLinkPrecedence(LinkPrecedenceType.secondary);
                        contactRepository.updateContactById(contact.getId(), primaryContact.getId(), LinkPrecedenceType.secondary);
                    }
                }
                return createResponse(primaryContact, matchingContacts);
            } else {
                // we are here means we have encounter either email or phone no not the both so create a new entry and modify the relations
                //                create new entry
                //                for whichever is not encountered
                Contact newContact = createNewContract(phoneNumber, email, LinkPrecedenceType.secondary, primaryContact.getId());
                matchingContacts.add(newContact);
                return createResponse(primaryContact, matchingContacts);
            }
        }
    }

//    getting all the primary ids
    private void processContacts(List<Contact> contacts, HashSet<Long> primaryIdSet) {
        for (Contact contact : contacts) {
            if (contact.getLinkPrecedence().equals(LinkPrecedenceType.secondary)) {
                primaryIdSet.add(contact.getLinkedId());
            } else {
                primaryIdSet.add(contact.getId());
            }
        }
    }

//    create the resonse
    private IdentifyResponseDto createResponse(Contact primaryContact, List<Contact> matchingContacts) {
        List<String> emailsList = new ArrayList<>();
        List<String> phoneNumbersList = new ArrayList<>();
        List<Long> secondaryContactIds = new ArrayList<>();

        emailsList.add(primaryContact.getEmail());
        phoneNumbersList.add(primaryContact.getPhoneNumber());
        HashSet<String> emailSet = new HashSet<>();
        HashSet<String> phoneSet = new HashSet<>();
        emailSet.add(primaryContact.getEmail());
        phoneSet.add(primaryContact.getPhoneNumber());
        for (Contact contact : matchingContacts) {
            if (!emailSet.contains(contact.getEmail())) {
                emailSet.add(contact.getEmail());
                emailsList.add(contact.getEmail());
            }
            if (!phoneSet.contains(contact.getPhoneNumber())) {
                phoneSet.add(contact.getPhoneNumber());
                phoneNumbersList.add(contact.getPhoneNumber());
            }
            if (contact.getLinkPrecedence().equals(LinkPrecedenceType.secondary)) {
                secondaryContactIds.add(contact.getId());
            }
        }
        return responseBuilder(primaryContact, emailsList, phoneNumbersList, secondaryContactIds);
    }

    private Contact createNewContract(String phoneNumber, String email, LinkPrecedenceType linkPrecedenceType, Long linkedId) {
        Contact newContact = Contact.builder()
                .phoneNumber(phoneNumber)
                .email(email)
                .linkedId(linkedId)
                .linkPrecedence(linkPrecedenceType)
                .build();
        return contactRepository.save(newContact);

    }

    private IdentifyResponseDto responseBuilder(Contact primaryContact,
                                                List<String> emailsList,
                                                List<String> phoneNumbersList,
                                                List<Long> secondaryContactIds) {
        ContactDTO contactDTO = ContactDTO.builder()
                .primaryContactId(primaryContact.getId())
                .emails(emailsList)
                .phoneNumbers(phoneNumbersList)
                .secondaryContactIds(secondaryContactIds)
                .build();
        return IdentifyResponseDto.builder()
                .contact(contactDTO)
                .build();
    }

}