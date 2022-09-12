package io.openk9.filemanager.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private State state;

    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "version", nullable = false)
    private String version;

    @Column(name = "url", nullable = false)
    private String url;

    public enum State {
        OK, KO, PENDING
    }

}
