package io.openk9.filemanager.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.smallrye.mutiny.Uni;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(name = "resource")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Cacheable
public class Resource extends PanacheEntity {

    @Column(name = "resourceId", nullable = false)
    private String resourceId;

    @Column(name = "fileId", nullable = false)
    private String fileId;

    @Column(name = "datasourceId", nullable = false)
    private String datasourceId;

    @Column(name = "version", nullable = false)
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private State state;

    public enum State {
        OK, KO, PENDING
    }

}
