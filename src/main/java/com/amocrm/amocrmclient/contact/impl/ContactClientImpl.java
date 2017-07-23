package com.amocrm.amocrmclient.contact.impl;


import com.amocrm.amocrmclient.account.AccountClient;
import com.amocrm.amocrmclient.auth.AuthClient;
import com.amocrm.amocrmclient.contact.ContactClient;
import com.amocrm.amocrmclient.contact.entity.links.CLResponseData;
import com.amocrm.amocrmclient.contact.entity.list.LCResponseData;
import com.amocrm.amocrmclient.contact.entity.set.SCAdd;
import com.amocrm.amocrmclient.contact.entity.set.SCParam;
import com.amocrm.amocrmclient.contact.entity.set.SCRequest;
import com.amocrm.amocrmclient.contact.entity.set.SCRequestContacts;
import com.amocrm.amocrmclient.contact.entity.set.SCResponseData;
import com.amocrm.amocrmclient.entity.AuthResponse;
import com.amocrm.amocrmclient.entity.CustomField;
import com.amocrm.amocrmclient.entity.CustomFieldValue;
import com.amocrm.amocrmclient.account.entity.CustomFieldSettings;
import com.amocrm.amocrmclient.account.entity.current.ACData;
import com.amocrm.amocrmclient.iface.IContactAPI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import retrofit2.Call;
import retrofit2.Response;

@AllArgsConstructor
public class ContactClientImpl implements ContactClient {

    private AuthClient authClient;

    private AccountClient accountClient;

    private IContactAPI contactAPI;

    public IContactAPI api() {
        return contactAPI;
    }

    public SCParam createContact(String name) {

        SCParam setContact = new SCParam();
        setContact.request = new SCRequest();
        setContact.request.contacts = new SCRequestContacts();
        setContact.request.contacts.add = new ArrayList<>();
        SCAdd setContactAdd = new SCAdd();
        setContactAdd.name = name;
        setContact.request.contacts.add.add(setContactAdd);

        return setContact;
    }

    public SCParam setContactCustomFields(SCParam setContact, Map<String, String> projectSettings,
                                          Map<String, String> fieldValues, Long linkedLeadId) throws IOException {


        Response<ACData> accountsDataResponse = accountClient.data();

        if (accountsDataResponse.isSuccessful()) {

            List<CustomFieldSettings> customFields =
                    accountsDataResponse.body().response.account.customFields.contacts;

            Map<String, CustomFieldSettings> customFieldsMap = new HashMap<>();

            for (CustomFieldSettings customField : customFields) {
                customFieldsMap.put(customField.name, customField);
            }

            if (linkedLeadId != null) {
                setContact.request.contacts.add.get(0).linkedLeadsId = new ArrayList<>();
                setContact.request.contacts.add.get(0).linkedLeadsId.add(linkedLeadId);
            }

            setContact.request.contacts.add.get(0).customFields = new ArrayList<>();
            for (String fieldName : fieldValues.keySet()) {
                CustomFieldSettings customFieldSettings = customFieldsMap.get(fieldName);
                if ("Y".equals(customFieldSettings.multiple)) {
                    CustomField customField = new CustomField();
                    customField.id = customFieldsMap.get(fieldName).id;
                    customField.values = new ArrayList<>();
                    CustomFieldValue fieldValue = new CustomFieldValue();
                    fieldValue.value = fieldValues.get(fieldName);
                    customField.values.add(fieldValue);
                    if ("Phone".equals(fieldName)) {
                        fieldValue.enumer = "MOB";
                    } else if ("Email".equals(fieldName)) {
                        fieldValue.enumer = "WORK";
                    }
                    setContact.request.contacts.add.get(0).customFields.add(customField);
                } else {

                }


            }
            return setContact;
        }
        return null;
    }

    public Response<SCResponseData> setContact(String name) throws IOException {

        SCParam setContact = createContact(name);

        return setContact(setContact);
    }

    public Response<SCResponseData> setContact(SCParam setContact) throws IOException {

        Call<AuthResponse> authResponse = authClient.auth();

        Response response = authResponse.execute();

        if (response.isSuccessful()) {

            return contactAPI.setContact(setContact).execute();
        }

        return null;
    }

    public Response<LCResponseData> list(String query, int limitRows, int limitOffset, Long id, String responsibleUserId, String type) throws IOException {

        Call<AuthResponse> authResponse = authClient.auth();

        Response response = authResponse.execute();

        if (response.isSuccessful()) {

            if (type != null) {

                if (limitRows >= 0 && limitOffset >= 0) {
                    return contactAPI.listByType(type, limitRows, limitOffset).execute();
                } else if (limitRows >= 0) {
                    return contactAPI.listByType(type, limitRows).execute();
                }

                return contactAPI.listByType(type).execute();

            } else if (id != null) {

                return contactAPI.list(id).execute();

            } else if (responsibleUserId != null) {

                if (limitRows >= 0 && limitOffset >= 0) {
                    return contactAPI.listByResponsibleUserId(responsibleUserId, limitRows, limitOffset).execute();
                } else if (limitRows >= 0) {
                    return contactAPI.listByResponsibleUserId(responsibleUserId, limitRows).execute();
                }

                return contactAPI.listByResponsibleUserId(responsibleUserId).execute();

            } else if (query != null) {

                if (limitRows >= 0 && limitOffset >= 0) {
                    return contactAPI.list(query, limitRows, limitOffset).execute();
                } else if (limitRows >= 0) {
                    return contactAPI.list(query, limitRows).execute();
                }

                return contactAPI.list().execute();

            } else {

                if (limitRows >= 0 && limitOffset >= 0) {
                    return contactAPI.list(limitRows, limitOffset).execute();
                } else if (limitRows >= 0) {
                    return contactAPI.list(limitRows).execute();
                }

                return contactAPI.list().execute();

            }
        }

        return null;
    }

    public Response<LCResponseData> list(String query) throws IOException {

        return list(query, -1, -1, null, null, null);
    }

    public Response<LCResponseData> list() throws IOException {

        return list(null, -1, -1, null, null, null);
    }

    public Response<CLResponseData> links() throws IOException {

        Call<AuthResponse> authResponse = authClient.auth();

        Response response = authResponse.execute();

        if (response.isSuccessful()) {

            return contactAPI.links().execute();
        }

        return null;
    }
}