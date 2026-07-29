package com.bancopago.backend.domain.person;

import com.bancopago.backend.domain.enums.DocumentType;
import com.bancopago.backend.domain.enums.PersonType;
import com.bancopago.backend.domain.person.exceptions.InvalidPersonException;
import com.bancopago.backend.domain.person.vo.DocumentNumber;
import com.bancopago.backend.domain.person.vo.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PersonTest {

    @Nested
    @DisplayName("Client creation")
    class ClientCreation {

        @Test
        @DisplayName("should create a valid client")
        void shouldCreateValidClient() {
            var document = new DocumentNumber(DocumentType.CC, "1234567890");
            var email = new Email("juan@email.com");
            var client = new ClientDomain("Juan Perez", document, email, "3001234567", "CLI-001");

            assertNotNull(client.getId());
            assertEquals("Juan Perez", client.getName());
            assertEquals("1234567890", client.getDocument());
            assertEquals(DocumentType.CC, client.getDocumentType());
            assertEquals("juan@email.com", client.getEmail());
            assertEquals("3001234567", client.getPhone());
            assertEquals(PersonType.CLIENT, client.getPersonType());
            assertEquals("CLI-001", client.getClientNumber());
            assertEquals(LocalDate.now(), client.getMembershipDate());
        }

        @Test
        @DisplayName("should throw exception when name is null")
        void shouldThrowExceptionWhenNameIsNull() {
            var document = new DocumentNumber(DocumentType.CC, "1234567890");
            var email = new Email("juan@email.com");
            assertThrows(InvalidPersonException.class, () ->
                    new ClientDomain(null, document, email, "3001234567", "CLI-001")
            );
        }

        @Test
        @DisplayName("should throw exception when name is empty")
        void shouldThrowExceptionWhenNameIsEmpty() {
            var document = new DocumentNumber(DocumentType.CC, "1234567890");
            var email = new Email("juan@email.com");
            assertThrows(InvalidPersonException.class, () ->
                    new ClientDomain("", document, email, "3001234567", "CLI-001")
            );
        }

        @Test
        @DisplayName("should throw exception when name exceeds max length")
        void shouldThrowExceptionWhenNameExceedsMaxLength() {
            var document = new DocumentNumber(DocumentType.CC, "1234567890");
            var email = new Email("juan@email.com");
            var tooLong = "A".repeat(101);
            assertThrows(InvalidPersonException.class, () ->
                    new ClientDomain(tooLong, document, email, "3001234567", "CLI-001")
            );
        }

        @Test
        @DisplayName("should throw exception when email is invalid")
        void shouldThrowExceptionWhenEmailIsInvalid() {
            assertThrows(InvalidPersonException.class, () ->
                    new Email("invalid-email")
            );
        }

        @Test
        @DisplayName("should throw exception when email is null")
        void shouldThrowExceptionWhenEmailIsNull() {
            assertThrows(InvalidPersonException.class, () ->
                    new Email((String) null)
            );
        }

        @Test
        @DisplayName("should normalize email to lowercase")
        void shouldNormalizeEmailToLowercase() {
            var email = new Email("Juan@Email.Com");
            assertEquals("juan@email.com", email.value());
        }

        @Test
        @DisplayName("should throw exception when document type is null")
        void shouldThrowExceptionWhenDocumentTypeIsNull() {
            assertThrows(InvalidPersonException.class, () ->
                    new DocumentNumber(null, "1234567890")
            );
        }

        @Test
        @DisplayName("should throw exception when document is null")
        void shouldThrowExceptionWhenDocumentIsNull() {
            assertThrows(InvalidPersonException.class, () ->
                    new DocumentNumber(DocumentType.CC, null)
            );
        }

        @Test
        @DisplayName("should throw exception when document is blank")
        void shouldThrowExceptionWhenDocumentIsBlank() {
            assertThrows(InvalidPersonException.class, () ->
                    new DocumentNumber(DocumentType.CC, "")
            );
        }
    }

    @Nested
    @DisplayName("Employee creation")
    class EmployeeCreation {

        @Test
        @DisplayName("should create a valid employee")
        void shouldCreateValidEmployee() {
            var document = new DocumentNumber(DocumentType.CC, "9876543210");
            var email = new Email("maria@bank.com");
            var employee = new EmployeeDomain("Maria Lopez", document, email, "3009876543",
                    "Analyst", "IT", "CC-001", "INDEFINITE");

            assertNotNull(employee.getId());
            assertEquals("Maria Lopez", employee.getName());
            assertEquals(PersonType.EMPLOYEE, employee.getPersonType());
            assertEquals("Analyst", employee.getPosition());
            assertEquals("IT", employee.getArea());
            assertEquals("CC-001", employee.getCostCenter());
            assertEquals("INDEFINITE", employee.getContractType());
        }
    }
}
