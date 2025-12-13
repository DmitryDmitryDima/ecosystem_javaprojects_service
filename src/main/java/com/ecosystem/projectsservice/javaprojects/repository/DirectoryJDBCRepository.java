package com.ecosystem.projectsservice.javaprojects.repository;


import com.ecosystem.projectsservice.javaprojects.model.DirectoryReadOnly;
import com.ecosystem.projectsservice.javaprojects.model.FileReadOnly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;


// работа с нативными query, нужными для оптимального чтения структуры
// таким образом, мы имеем 2 параллельных репозитория для directory
@Repository
public class DirectoryJDBCRepository {


    @Autowired
    private JdbcTemplate jdbcTemplate;


    // возвращает плоскую структуру папок со всеми зависимостями
    public List<DirectoryReadOnly> loadAWholeStructureFromRoot(long rootId){

        String query = """
                
                with recursive children as (
                select id, parent_id, name, constructed_path, created_at, hidden, immutable from directories where id = ?
                union
                select d.id, d.parent_id, d.name, d.constructed_path, d.created_at, d.hidden, d.immutable from directories d join children c on d.parent_id = c.id
                )
                select * from children;
                
                """;

        return jdbcTemplate.query(query,
                new BeanPropertyRowMapper<>(DirectoryReadOnly.class), rootId);
    }

    // возвращаем все файлы, ассоциированные с директориями
    public List<FileReadOnly> loadFilesAssosiatedWithDirectories(List<Long> directories){
        String inSql = String.join(",", Collections.nCopies(directories.size(), "?"));
        String query = String
                .format("select parent_id, name,id, constructed_path, created_at,hidden, immutable,extension, status from files where files.parent_id in (%s)",
                        inSql);

        System.out.println(query);

        return jdbcTemplate.query(query,new BeanPropertyRowMapper<>(FileReadOnly.class), directories.toArray());

    }


}
