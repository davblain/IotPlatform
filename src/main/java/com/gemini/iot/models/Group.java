package com.gemini.iot.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_group")
public class Group implements Serializable {
    @Id
    @GeneratedValue
    @Column(name = "uuid")
    private UUID uuid;

    @Column
    private String name;
    @ManyToOne()
    @JoinColumn(name = "admin_id")
    @NotNull
    private User admin;
    @OneToMany(mappedBy = "owner",cascade = CascadeType.ALL,fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Device> devices = new ArrayList<>();
    @ManyToMany(mappedBy = "groups")
    private List<User> members = new ArrayList<>();

    public Group() {
    }

    public Group(User admin,String name) {
        this(name);
        this.admin = admin;
        admin.getGroups().add(this);
    }

    public Group(String name) {
        this.name = name;
    }

    public User getAdmin() {
        return admin;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }

    public void setAdmin(User admin) {
        this.admin = admin;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDevices(List<Device> devices) {
        this.devices = this.devices;
    }

    @JsonManagedReference
    public List<User> getMembers() {
        return members;
    }

    @Override
    public String toString() {
        return  uuid.toString();

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Group group = (Group) o;

        return uuid != null ? uuid.equals(group.uuid) : group.uuid == null;
    }

    @Override
    public int hashCode() {
        return uuid != null ? uuid.hashCode() : 0;
    }
}
