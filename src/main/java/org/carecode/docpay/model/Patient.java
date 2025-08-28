package org.carecode.docpay.model;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    private String nic;
    private String phone;
    private LocalDate dob;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    private String address;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public enum Gender { MALE, FEMALE, OTHER }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

