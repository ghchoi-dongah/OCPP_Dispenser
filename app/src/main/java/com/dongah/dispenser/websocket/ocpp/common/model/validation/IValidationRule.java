package com.dongah.dispenser.websocket.ocpp.common.model.validation;

import com.dongah.dispenser.websocket.ocpp.common.PropertyConstraintException;

public interface IValidationRule {
    void validate(String value) throws PropertyConstraintException;
}
