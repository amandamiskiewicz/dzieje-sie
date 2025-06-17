package com.example.event_backend.dto;

import org.hibernate.validator.constraints.URL;

import com.example.event_backend.model.Organizer;
import com.example.event_backend.model.Participant;

import jakarta.validation.constraints.*;

public class RegisterDto {

    @NotBlank(message = "Imię jest wymagane", groups = {Participant.class})
    private String firstName;

    @NotBlank(message = "Nazwisko jest wymagane", groups = {Participant.class})
    private String lastName;

    @NotBlank(message = "Adres e-mail jest wymagany")
    @Email(message = "Niepoprawny adres e-mail")
    private String email;

    @NotBlank(message = "Numer telefonu jest wymagany")
    @Pattern(regexp = "^[0-9\\s\\-+()]{7,20}$", message = "Niepoprawny numer telefonu")
    private String phone;

    @NotBlank(message = "Hasło jest wymagane")
    @Size(min = 6, message = "Hasło musi mieć co najmniej 6 znaków")
    private String password;

    @NotBlank(message = "Proszę powtórzyć hasło")
    private String confirmPassword;

    @NotBlank(message = "Nazwa firmy jest wymagana", groups = {Organizer.class})
    private String companyName;

    @NotBlank(message = "NIP jest wymagany", groups = {Organizer.class})
    @Pattern(regexp = "^[0-9]{10}$", message = "NIP musi mieć dokładnie 10 cyfr", groups = {Organizer.class})
    private String nip;

    @URL(message = "Niepoprawny adres strony internetowej")
    private String website;


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getNip() {
        return nip;
    }

    public void setNip(String nip) {
        this.nip = nip;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

}
