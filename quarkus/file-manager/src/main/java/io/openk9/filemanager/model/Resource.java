package io.openk9.filemanager.model;

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
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

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
