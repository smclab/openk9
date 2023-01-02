package io.openk9.datasource.model;

import io.openk9.datasource.model.util.K9Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "file_resource")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor(staticName = "of")
@Cacheable
public class FileResource extends K9Entity {

    @Column(name = "resourceId", nullable = false)
    private String resourceId;

    @Column(name = "fileId", nullable = false)
    private String fileId;

    @Column(name = "datasourceId", nullable = false)
    private String datasourceId;

}
