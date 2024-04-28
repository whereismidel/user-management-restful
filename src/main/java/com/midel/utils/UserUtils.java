package com.midel.utils;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public final class UserUtils {

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    public static boolean isEmailValid(String email) {
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isBirthDateValid(LocalDate birthDate, int allowedAge) {
        LocalDate currentDate = LocalDate.now();
        LocalDate minValidDate = currentDate.minusYears(allowedAge);
        return birthDate.isBefore(minValidDate);
    }
}
