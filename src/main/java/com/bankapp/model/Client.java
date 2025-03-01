package com.bankapp.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Client {
    private Long id;
    private String fullName;
    private String phoneNumber;
    private String inn;
    private String address;
    private byte[] passportScanCopy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Client() {
    }

    public Client(Long id, String fullName, String phoneNumber, String inn, String address, byte[] passportScanCopy) {
        this.id = id;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.inn = inn;
        this.address = address;
        this.passportScanCopy = passportScanCopy;
    }

    public Long  getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public byte[] getPassportScanCopy() {
        return passportScanCopy;
    }

    public void setPassportScanCopy(byte[] passportScanCopy) {
        this.passportScanCopy = passportScanCopy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return id == client.id &&
                Objects.equals(fullName, client.fullName) &&
                Objects.equals(phoneNumber, client.phoneNumber) &&
                Objects.equals(inn, client.inn) &&
                Objects.equals(address, client.address) &&
                Objects.equals(passportScanCopy, client.passportScanCopy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fullName, phoneNumber, inn, address, passportScanCopy);
    }

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", inn='" + inn + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}