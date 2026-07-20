package com.bancopago.backend.domain.person;

import com.bancopago.backend.crosscutting.exception.ErrorCode;

public enum PersonError implements ErrorCode {
    NAME_EMPTY("El nombre de la persona no puede estar vacío"),
    NAME_EXCEEDED("El nombre excede los %d caracteres permitidos"),
    DOCUMENT_EMPTY("El número de documento no puede estar vacío"),
    DOCUMENT_EXCEEDED("El documento excede los %d caracteres permitidos"),
    DOCUMENT_TYPE_REQUIRED("El tipo de documento es requerido"),
    EMAIL_EMPTY("El correo electrónico no puede estar vacío"),
    EMAIL_INVALID("El formato del correo electrónico no es válido"),
    EMAIL_EXCEEDED("El correo electrónico excede los %d caracteres permitidos"),
    TYPE_REQUIRED("El tipo de persona es requerido");

    private final String messageTemplate;

    PersonError(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    @Override
    public String getCode() { return "PERSON_" + name(); }
    @Override
    public String getMessageTemplate() { return messageTemplate; }
}
