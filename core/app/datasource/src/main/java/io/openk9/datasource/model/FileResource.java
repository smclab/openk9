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
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "file_resource", uniqueConstraints = {
    @UniqueConstraint(name = "uc_fileresource_resource_id", columnNames = {
        "resource_id"}),
    @UniqueConstraint(name = "uc_fileresource_fileid_datasource_id", columnNames = {
        "file_id", "datasource_id"})
})
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Cacheable
public class FileResource extends K9Entity {

    @Column(name = "resource_id", nullable = false)
    private String resourceId;

    @Column(name = "file_id", nullable = false)
    private String fileId;

    @Column(name = "datasource_id", nullable = false)
    private String datasourceId;

}
