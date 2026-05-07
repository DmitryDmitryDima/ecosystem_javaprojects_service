package com.ecosystem.projectsservice.javaprojects.repository;


import com.ecosystem.projectsservice.javaprojects.model.read_only.DirectoryReadOnly;
import com.ecosystem.projectsservice.javaprojects.model.read_only.FileReadOnly;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


// работа с нативными query, нужными для оптимального чтения структуры
// таким образом, мы имеем 2 параллельных репозитория для directory
@Repository

public class DirectoryJDBCRepository {


    @Autowired
    private JdbcTemplate jdbcTemplate;


    public List<DirectoryReadOnly> loadAWholeStructureBelowRootWithLevel(UUID rootId, long level){
        String query = """
                
                with recursive children as (
                select id, parent_id, name, constructed_path, created_at, hidden, immutable, status, version, 0 as depth from directories where id = ?
                union
                select d.id, d.parent_id, d.name, d.constructed_path, d.created_at, d.hidden, d.immutable, d.status, d.version, c.depth+1
                 from directories d join children c on d.parent_id = c.id and c.depth<?
                )
                select * from children;
                
                """;

        return jdbcTemplate.query(query,
                new BeanPropertyRowMapper<>(DirectoryReadOnly.class), rootId, level);


    }

    public List<DirectoryReadOnly> loadAWholeStructureAboveRootWithLevel(UUID rootId, long level){
        String query = """
                
                with recursive children as (
                select id, parent_id, name, constructed_path, created_at, hidden, immutable, status, version, 0 as depth from directories where id = ?
                union
                select d.id, d.parent_id, d.name, d.constructed_path, d.created_at, d.hidden, d.immutable, d.status, d.version, c.depth+1
                 from directories d join children c on d.id = c.parent_id and c.depth<?
                )
                select * from children;
                
                """;

        return jdbcTemplate.query(query,
                new BeanPropertyRowMapper<>(DirectoryReadOnly.class), rootId, level);


    }

    // возвращает плоскую структуру папок со всеми зависимостями, начиная с root папки, включая root
    public List<DirectoryReadOnly> loadAWholeStructureBelowRoot(UUID rootId){

        String query = """
                
                with recursive children as (
                select id, parent_id, name, constructed_path, created_at, hidden, immutable, status, version, 0 as depth from directories where id = ?
                union
                select d.id, d.parent_id, d.name, d.constructed_path, d.created_at, d.hidden, d.immutable, d.status, d.version, c.depth+1 from directories d join children c on d.parent_id = c.id
                )
                select * from children;
                
                """;

        return jdbcTemplate.query(query,
                new BeanPropertyRowMapper<>(DirectoryReadOnly.class), rootId);
    }

    // плоская структура папок, при этом возвращается только то, что является предками по отношению к root. включая root
    public List<DirectoryReadOnly> loadAWholeStructureAboveRoot(UUID rootId){

        String query = """
                
                with recursive children as (
                select id, parent_id, name, constructed_path, created_at, hidden, immutable, status, version, 0 as depth from directories where id = ?
                union
                select d.id, d.parent_id, d.name, d.constructed_path, d.created_at, d.hidden, d.immutable, d.status, d.version, c.depth+1 from directories d join children c on d.id = c.parent_id
                )
                select * from children;
                
                """;

        return jdbcTemplate.query(query,
                new BeanPropertyRowMapper<>(DirectoryReadOnly.class), rootId);



    }

    // возвращаем все файлы, ассоциированные с директориями
    public List<FileReadOnly> loadFilesAssosiatedWithDirectories(List<UUID> directories){
        String inSql = String.join(",", Collections.nCopies(directories.size(), "?"));
        String query = String
                .format("select parent_id, name,id, constructed_path, created_at, updated_at, hidden, immutable,extension, status, version from files where files.parent_id in (%s)",
                        inSql);



        return jdbcTemplate.query(query,new BeanPropertyRowMapper<>(FileReadOnly.class),
                directories.toArray());

    }

    // загружаем все файлы ниже root

    public List<FileReadOnly> loadFilesBelowRoot(@NotNull UUID id){
        String query = """
                
                
                select * from files where files.parent_id in ( with recursive children as (
                                select id, parent_id, name, constructed_path, created_at, hidden, immutable, status, version, 0 as depth
                                 from directories where id = ?
                                union
                                select d.id, d.parent_id, d.name, d.constructed_path, d.created_at, d.hidden, d.immutable, d.status, d.version, c.depth+1 
                                from directories d join children c
                				on d.parent_id = c.id
                                )
                                select id from children)
                
                
                """;




        return jdbcTemplate.query(query,
                new BeanPropertyRowMapper<>(FileReadOnly.class), id);

    }

    // загружаем конкретный файл в иерархии root
    public Optional<FileReadOnly> loadFileBelowRoot(UUID rootId, UUID fileId){
        String query = """
                
                
                select * from files where files.parent_id in ( with recursive children as (
                                select id, parent_id, name, constructed_path, created_at, hidden, immutable, status, version, 0 as depth
                                 from directories where id = ?
                                union
                                select d.id, d.parent_id, d.name, d.constructed_path, d.created_at, d.hidden, d.immutable, d.status, d.version, c.depth+1 
                                from directories d join children c
                				on d.parent_id = c.id
                                )
                                select id from children) AND files.id = ?
                
                
                """;

        return jdbcTemplate
                .query(query,new BeanPropertyRowMapper<>(FileReadOnly.class), rootId, fileId)
                .stream().findFirst();
    }




}
