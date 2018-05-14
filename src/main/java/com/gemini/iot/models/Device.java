package com.gemini.iot.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gemini.iot.models.definitions.DeviceDefinition;
import lombok.Data;


import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "devices")
@Data
public class Device {
    @Id
    @GeneratedValue
    @Column(name = "uuid")
    private UUID uuid;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Group owner;

    @ManyToOne
    @JoinColumn(name = "deviceId")
    private DeviceDefinition deviceDefinition;

}
