package com.ecosystem.projectsservice.javaprojects.repository;

import com.ecosystem.projectsservice.javaprojects.model.Directory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DirectoryRepository extends JpaRepository<Directory, Long> {


    @NativeQuery
            (
                    "with recursive children as (" +
                            "select id, parent_id, name, constructed_path, created_at, hidden, immutable from directories where id=?1" +
                            " union" +
                            " select d.id, d.parent_id, d.name, d.constructed_path, d.created_at, d.hidden, d.immutable from directories d join children c on d.parent_id = c.id" +
                            ")" +
                            " select * from children;"
            )

    public List<Directory> readTreeStructure(Long id);

}
