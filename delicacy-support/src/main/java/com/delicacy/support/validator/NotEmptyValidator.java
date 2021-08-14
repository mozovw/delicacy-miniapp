package com.delicacy.support.validator;


import com.delicacy.common.utils.ObjectUtils;
import com.delicacy.support.annotation.NotEmpty;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotEmptyValidator implements ConstraintValidator<NotEmpty, Object> {

    private String message;

    @Override
    public void initialize(NotEmpty constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Object list, ConstraintValidatorContext constraintValidatorContext) {
        return !ObjectUtils.isEmpty(list);
    }

}