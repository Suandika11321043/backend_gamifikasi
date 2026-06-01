package com.example.gamifikasi.controller;

import com.example.gamifikasi.dto.StudentDto;
import com.example.gamifikasi.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StudentDto> createStudent(
            @RequestParam("name") String name,
            @RequestParam("group") String group,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile) {
        try {
            StudentDto studentDto = new StudentDto();
            studentDto.setName(name);
            studentDto.setGroup(group);
            StudentDto created = studentService.createStudent(studentDto, avatarFile);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), e);
        }
    }

    // Get all Students
    @GetMapping
    public ResponseEntity<List<StudentDto>> getAllStudents() {
        try {
            List<StudentDto> students = studentService.getAllStudents();
            if (students.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return new ResponseEntity<>(students, HttpStatus.OK);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), e);
        }
    }

    // Get Student by ID
    @GetMapping("/{id}")
    public ResponseEntity<StudentDto> getStudentById(@PathVariable("id") Long id) {
        try {
            Optional<StudentDto> student = studentService.getStudentById(id);
            if (student.isPresent()) {
                return new ResponseEntity<>(student.get(), HttpStatus.OK);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), e);
        }
    }

    // Get Students by Group
    @GetMapping("/group/{group}")
    public ResponseEntity<List<StudentDto>> getStudentsByGroup(@PathVariable("group") String group) {
        try {
            List<StudentDto> students = studentService.getStudentsByGroup(group);
            if (students.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return new ResponseEntity<>(students, HttpStatus.OK);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), e);
        }
    }

    // Update Student (with optional new avatar)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StudentDto> updateStudent(
            @PathVariable("id") Long id,
            @RequestParam("name") String name,
            @RequestParam("group") String group,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile) {
        try {
            StudentDto studentDto = new StudentDto();
            studentDto.setName(name);
            studentDto.setGroup(group);
            Optional<StudentDto> updated = studentService.updateStudent(id, studentDto, avatarFile);
            if (updated.isPresent()) {
                return new ResponseEntity<>(updated.get(), HttpStatus.OK);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), e);
        }
    }

    // Delete Student
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable("id") Long id) {
        try {
            boolean deleted = studentService.deleteStudent(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), e);
        }
    }
}
